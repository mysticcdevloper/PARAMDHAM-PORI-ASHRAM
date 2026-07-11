package com.example.core.repositories

import android.util.Log
import com.example.core.config.SupabaseConfig
import com.example.core.api.SupabaseApi
import com.example.core.models.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID

class ChatRepositoryImpl : ChatRepository {

  private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

  // Internal reactive States
  private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
  override val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

  private val _activeStatuses = MutableStateFlow<List<MemberStatusUpdate>>(emptyList())
  override val activeStatuses: StateFlow<List<MemberStatusUpdate>> = _activeStatuses.asStateFlow()

  private val _activeBroadcasts = MutableStateFlow<List<BroadcastCampaign>>(emptyList())
  override val activeBroadcasts: StateFlow<List<BroadcastCampaign>> = _activeBroadcasts.asStateFlow()

  private val _downloadQueue = MutableStateFlow<List<ChatDownloadTask>>(emptyList())
  override val downloadQueue: StateFlow<List<ChatDownloadTask>> = _downloadQueue.asStateFlow()

  private val _favoritedMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
  override val favoritedMessages: StateFlow<List<ChatMessage>> = _favoritedMessages.asStateFlow()

  // In-Memory message store: Map<ConversationId, List<ChatMessage>>
  private val _messageStore = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())

  // Presence states
  private val _memberPresence = MutableStateFlow<Map<String, OnlineStatus>>(emptyMap())
  private val _typingStatus = MutableStateFlow<Map<String, Set<String>>>(emptyMap()) // Map<ConversationId, Set<MemberId>>

  init {
    seedInitialData()
    if (SupabaseConfig.isConfigured()) {
      Log.d("ChatRepoSync", "Chat Repository Initialized in LIVE SUPABASE MODE.")
      startSupabaseSync()
    } else {
      Log.d("ChatRepoSync", "Chat Repository Initialized in SANDBOX/DEMO FALLBACK MODE (No keys configured).")
      startRealtimeSimulation()
    }
  }

  private fun startSupabaseSync() {
    repositoryScope.launch {
      while (isActive) {
        try {
          Log.d("ChatRepoSync", "Syncing active channels, statuses, and campaigns from Supabase...")
          val api = SupabaseApi.get()
          
          // 1. Fetch Conversations
          val convResponse = api.getConversations()
          if (convResponse.isSuccessful && convResponse.body() != null) {
            val dbConvs = convResponse.body()!!
            val mappedConvs = dbConvs.map { db ->
              ChatConversation(
                id = db.id ?: "conv_${UUID.randomUUID().toString().take(6)}",
                name = db.name,
                description = db.description,
                isGroup = db.is_group,
                avatarUrl = db.avatar_url,
                inviteLink = db.invite_link,
                qrCodeText = db.qr_code_text,
                createdBy = db.created_by
              )
            }
            _conversations.value = mappedConvs
          }

          // 2. Fetch Status Updates
          val statusResponse = api.getStatuses()
          if (statusResponse.isSuccessful && statusResponse.body() != null) {
            val dbStatuses = statusResponse.body()!!
            val mappedStatuses = dbStatuses.map { db ->
              val statusType = try {
                StatusType.valueOf(db.type)
              } catch (e: Exception) {
                StatusType.TEXT
              }
              MemberStatusUpdate(
                id = db.id ?: "status_${UUID.randomUUID().toString().take(6)}",
                memberId = db.member_id,
                memberName = db.member_name,
                type = statusType,
                content = db.content ?: "",
                mediaUrl = db.media_url,
                timestamp = System.currentTimeMillis()
              )
            }
            _activeStatuses.value = mappedStatuses
          }

          // 3. Fetch Broadcast Campaigns
          val broadResponse = api.getBroadcasts()
          if (broadResponse.isSuccessful && broadResponse.body() != null) {
            val dbBroads = broadResponse.body()!!
            val mappedBroads = dbBroads.map { db ->
              BroadcastCampaign(
                id = db.id ?: "broad_${UUID.randomUUID().toString().take(6)}",
                title = db.title,
                content = db.content,
                targetRoles = db.target_roles,
                sentAt = System.currentTimeMillis()
              )
            }
            _activeBroadcasts.value = mappedBroads
          }

        } catch (e: Exception) {
          Log.e("ChatRepoSync", "Supabase DB sync loop encountered error: ${e.message}")
        }
        delay(6000) // Poll sync targets every 6 seconds
      }
    }
  }

  override fun getOnlineStatusFlow(memberId: String): Flow<OnlineStatus> {
    return _memberPresence.map { it[memberId] ?: OnlineStatus.OFFLINE }
  }

  override fun getTypingStatusFlow(conversationId: String, memberId: String): Flow<Boolean> {
    return _typingStatus.map { it[conversationId]?.contains(memberId) == true }
  }

  override fun observeMessages(conversationId: String): Flow<List<ChatMessage>> {
    if (SupabaseConfig.isConfigured()) {
      return flow {
        while (true) {
          try {
            val api = SupabaseApi.get()
            val response = api.getMessages("eq.$conversationId")
            if (response.isSuccessful && response.body() != null) {
              val mapped = response.body()!!.map { db ->
                val mType = try {
                  MessageType.valueOf(db.type)
                } catch (e: Exception) {
                  MessageType.TEXT
                }
                val mStatus = try {
                  MessageStatus.valueOf(db.status)
                } catch (e: Exception) {
                  MessageStatus.READ
                }
                ChatMessage(
                  id = db.id ?: "msg_${System.currentTimeMillis()}",
                  conversationId = db.conversation_id,
                  senderId = db.sender_id,
                  senderName = db.sender_name,
                  senderRoleIcon = db.sender_role_icon,
                  content = db.content,
                  type = mType,
                  status = mStatus,
                  timestamp = System.currentTimeMillis()
                )
              }
              emit(mapped)
            }
          } catch (e: Exception) {
            Log.e("ChatRepoSync", "Error polling live chat stream: ${e.message}")
          }
          delay(4000) // Poll conversation message history every 4 seconds
        }
      }
    }
    return _messageStore.map { it[conversationId] ?: emptyList() }
  }

  override suspend fun sendMessage(
    conversationId: String,
    content: String,
    type: MessageType,
    repliedToId: String?,
    attachment: ChatAttachment?,
    poll: PollData?,
    scheduledTime: Long?
  ): Result<ChatMessage> {
    if (SupabaseConfig.isConfigured()) {
      return try {
        Log.d("ChatRepoSync", "Publishing message to live Supabase channel: $content")
        val api = SupabaseApi.get()
        val dbMsg = com.example.core.api.DbMessage(
          conversation_id = conversationId,
          sender_id = "curr_user",
          sender_name = "You",
          sender_role_icon = "👑",
          content = content,
          type = type.name,
          status = "READ"
        )
        val response = api.insertMessage(dbMsg)
        if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
          val added = response.body()!!.first()
          val finalMsg = ChatMessage(
            id = added.id ?: "",
            conversationId = added.conversation_id,
            senderId = added.sender_id,
            senderName = added.sender_name,
            senderRoleIcon = added.sender_role_icon,
            content = added.content,
            type = MessageType.valueOf(added.type),
            status = MessageStatus.valueOf(added.status)
          )
          Result.success(finalMsg)
        } else {
          Result.failure(Exception("Message insertion failed: ${response.errorBody()?.string()}"))
        }
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

    val messageId = "msg_${UUID.randomUUID().toString().take(8)}"
    
    // Find replied details if present
    var rText: String? = null
    var rSender: String? = null
    if (repliedToId != null) {
      val original = _messageStore.value[conversationId]?.firstOrNull { it.id == repliedToId }
      rText = original?.content
      rSender = original?.senderName
    }

    val newMessage = ChatMessage(
      id = messageId,
      conversationId = conversationId,
      senderId = "curr_user",
      senderName = "You",
      senderRoleIcon = "👑",
      content = content,
      type = type,
      status = if (type == MessageType.POLL) MessageStatus.READ else MessageStatus.SENDING,
      repliedToId = repliedToId,
      repliedToText = rText,
      repliedToSender = rSender,
      attachment = attachment,
      poll = poll,
      scheduledTime = scheduledTime,
      timestamp = System.currentTimeMillis()
    )

    // Update messages in store
    updateMessagesList(conversationId) { currentList ->
      currentList + newMessage
    }

    // If it's a scheduled message, we don't dispatch it to the conversation instantly
    if (scheduledTime != null) {
      return Result.success(newMessage)
    }

    // Simulate sending progress, then delivered, then read receipts
    repositoryScope.launch {
      delay(500)
      updateMessageStatus(conversationId, messageId, MessageStatus.DELIVERED)
      delay(800)
      updateMessageStatus(conversationId, messageId, MessageStatus.READ)
      
      // Auto reply from ashram guides if direct messaging
      simulateAutoResponse(conversationId, content)
    }

    // Update conversation last message & unread count
    updateConversationLastMessage(conversationId, newMessage)

    return Result.success(newMessage)
  }

  override suspend fun editMessage(messageId: String, newContent: String): Result<Unit> {
    _messageStore.value.forEach { (convId, messages) ->
      val index = messages.indexOfFirst { it.id == messageId }
      if (index != -1) {
        val updatedMessage = messages[index].copy(
          content = "$newContent (edited)",
          editHistory = messages[index].editHistory + messages[index].content
        )
        updateMessagesList(convId) { current ->
          current.map { if (it.id == messageId) updatedMessage else it }
        }
        return Result.success(Unit)
      }
    }
    return Result.failure(Exception("Message not found"))
  }

  override suspend fun deleteMessage(messageId: String, forEveryone: Boolean): Result<Unit> {
    _messageStore.value.forEach { (convId, messages) ->
      val index = messages.indexOfFirst { it.id == messageId }
      if (index != -1) {
        updateMessagesList(convId) { current ->
          if (forEveryone) {
            current.map {
              if (it.id == messageId) {
                it.copy(content = "🚫 This message was deleted.", type = MessageType.TEXT)
              } else it
            }
          } else {
            current.filterNot { it.id == messageId }
          }
        }
        return Result.success(Unit)
      }
    }
    return Result.failure(Exception("Message not found"))
  }

  override suspend fun addReaction(
    messageId: String,
    emoji: String,
    senderId: String,
    senderName: String
  ): Result<Unit> {
    _messageStore.value.forEach { (convId, messages) ->
      val index = messages.indexOfFirst { it.id == messageId }
      if (index != -1) {
        val original = messages[index]
        val reactions = original.reactions.toMutableList()
        reactions.removeAll { it.senderId == senderId }
        reactions.add(MessageReaction(emoji, senderId, senderName))
        val updated = original.copy(reactions = reactions)

        updateMessagesList(convId) { current ->
          current.map { if (it.id == messageId) updated else it }
        }
        return Result.success(Unit)
      }
    }
    return Result.failure(Exception("Message not found"))
  }

  override suspend fun removeReaction(messageId: String, senderId: String): Result<Unit> {
    _messageStore.value.forEach { (convId, messages) ->
      val index = messages.indexOfFirst { it.id == messageId }
      if (index != -1) {
        val original = messages[index]
        val updated = original.copy(reactions = original.reactions.filterNot { it.senderId == senderId })

        updateMessagesList(convId) { current ->
          current.map { if (it.id == messageId) updated else it }
        }
        return Result.success(Unit)
      }
    }
    return Result.failure(Exception("Message not found"))
  }

  override suspend fun pinMessage(conversationId: String, messageId: String, pin: Boolean): Result<Unit> {
    _conversations.value = _conversations.value.map { conv ->
      if (conv.id == conversationId) {
        val newPins = if (pin) {
          (conv.pinnedMessages + messageId).distinct()
        } else {
          conv.pinnedMessages.filterNot { it == messageId }
        }
        conv.copy(pinnedMessages = newPins)
      } else conv
    }

    _messageStore.value[conversationId]?.let { list ->
      updateMessagesList(conversationId) { current ->
        current.map { if (it.id == messageId) it.copy(isPinned = pin) else it }
      }
    }
    return Result.success(Unit)
  }

  override suspend fun starMessage(messageId: String, star: Boolean): Result<Unit> {
    _messageStore.value.forEach { (convId, messages) ->
      val index = messages.indexOfFirst { it.id == messageId }
      if (index != -1) {
        val updated = messages[index].copy(isStarred = star)
        updateMessagesList(convId) { current ->
          current.map { if (it.id == messageId) updated else it }
        }

        val favs = _favoritedMessages.value.toMutableList()
        favs.removeAll { it.id == messageId }
        if (star) {
          favs.add(updated)
        }
        _favoritedMessages.value = favs
        return Result.success(Unit)
      }
    }
    return Result.failure(Exception("Message not found"))
  }

  override suspend fun forwardMessage(messageId: String, targetConversationId: String): Result<Unit> {
    var foundMessage: ChatMessage? = null
    _messageStore.value.values.forEach { list ->
      val msg = list.firstOrNull { it.id == messageId }
      if (msg != null) {
        foundMessage = msg
      }
    }

    foundMessage?.let { original ->
      val forwardMsg = original.copy(
        id = "msg_${UUID.randomUUID().toString().take(8)}",
        conversationId = targetConversationId,
        senderId = "curr_user",
        senderName = "You",
        senderRoleIcon = "👑",
        isForwarded = true,
        timestamp = System.currentTimeMillis(),
        reactions = emptyList(),
        isPinned = false,
        isStarred = false
      )
      updateMessagesList(targetConversationId) { current -> current + forwardMsg }
      updateConversationLastMessage(targetConversationId, forwardMsg)
      return Result.success(Unit)
    }

    return Result.failure(Exception("Original message not found"))
  }

  override suspend fun createGroup(
    name: String,
    description: String?,
    creatorId: String,
    members: List<String>
  ): Result<ChatConversation> {
    if (SupabaseConfig.isConfigured()) {
      return try {
        val api = SupabaseApi.get()
        val dbConv = com.example.core.api.DbConversation(
          name = name,
          description = description,
          is_group = true,
          created_by = creatorId
        )
        val res = api.createConversation(dbConv)
        if (res.isSuccessful && res.body()?.isNotEmpty() == true) {
          val db = res.body()!!.first()
          val newGroup = ChatConversation(
            id = db.id ?: "",
            name = db.name,
            description = db.description,
            isGroup = db.is_group,
            groupMembers = (members + "curr_user").distinct(),
            groupAdmins = listOf("curr_user"),
            createdBy = creatorId,
            inviteLink = db.invite_link,
            qrCodeText = db.qr_code_text
          )
          Result.success(newGroup)
        } else {
          Result.failure(Exception("Could not create group: ${res.errorBody()?.string()}"))
        }
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

    val id = "conv_${UUID.randomUUID().toString().take(8)}"
    val newGroup = ChatConversation(
      id = id,
      name = name,
      description = description,
      isGroup = true,
      groupMembers = (members + "curr_user").distinct(),
      groupAdmins = listOf("curr_user"),
      createdBy = creatorId,
      inviteLink = "https://podhiashram.org/join/group/$id",
      qrCodeText = "ashram_group_join_$id"
    )

    _conversations.value = _conversations.value + newGroup
    _messageStore.value = _messageStore.value + (id to listOf(
      ChatMessage(
        id = "msg_init",
        conversationId = id,
        senderId = "system",
        senderName = "Ashram Portal",
        content = "🍁 Group Created: \"$name\" under spiritual administration.",
        type = MessageType.TEXT
      )
    ))

    return Result.success(newGroup)
  }

  override suspend fun updateGroupMetadata(
    conversationId: String,
    name: String,
    description: String?,
    avatarUrl: String?
  ): Result<Unit> {
    _conversations.value = _conversations.value.map {
      if (it.id == conversationId) {
        it.copy(name = name, description = description, avatarUrl = avatarUrl)
      } else it
    }
    return Result.success(Unit)
  }

  override suspend fun approveMemberJoin(conversationId: String, memberId: String): Result<Unit> {
    _conversations.value = _conversations.value.map {
      if (it.id == conversationId) {
        it.copy(groupMembers = (it.groupMembers + memberId).distinct())
      } else it
    }
    return Result.success(Unit)
  }

  override suspend fun removeGroupMember(conversationId: String, memberId: String): Result<Unit> {
    _conversations.value = _conversations.value.map {
      if (it.id == conversationId) {
        it.copy(groupMembers = it.groupMembers.filterNot { id -> id == memberId })
      } else it
    }
    return Result.success(Unit)
  }

  override suspend fun muteGroupMember(conversationId: String, memberId: String, mute: Boolean): Result<Unit> {
    _conversations.value = _conversations.value.map {
      if (it.id == conversationId) {
        val newMutes = if (mute) {
          (it.mutedMembers + memberId).distinct()
        } else {
          it.mutedMembers.filterNot { id -> id == memberId }
        }
        it.copy(mutedMembers = newMutes)
      } else it
    }
    return Result.success(Unit)
  }

  override suspend fun voteOnPoll(messageId: String, optionId: String, memberId: String): Result<Unit> {
    _messageStore.value.forEach { (convId, messages) ->
      val index = messages.indexOfFirst { it.id == messageId }
      if (index != -1) {
        val msg = messages[index]
        val poll = msg.poll ?: return Result.failure(Exception("Not a poll message"))
        
        // Update options
        val updatedOptions = poll.options.map { option ->
          if (poll.isMultipleChoice) {
            if (option.id == optionId) {
              val alreadyVoted = option.votes.contains(memberId)
              option.copy(votes = if (alreadyVoted) option.votes - memberId else option.votes + memberId)
            } else option
          } else {
            // Single choice: remove memberId from other option votes first, toggle on selected option
            if (option.id == optionId) {
              val alreadyVoted = option.votes.contains(memberId)
              option.copy(votes = if (alreadyVoted) option.votes - memberId else option.votes + memberId)
            } else {
              option.copy(votes = option.votes - memberId)
            }
          }
        }

        val updatedMsg = msg.copy(poll = poll.copy(options = updatedOptions))
        updateMessagesList(convId) { current ->
          current.map { if (it.id == messageId) updatedMsg else it }
        }
        return Result.success(Unit)
      }
    }
    return Result.failure(Exception("Message not found"))
  }

  override suspend fun uploadStatus(
    memberId: String,
    memberName: String,
    type: StatusType,
    content: String,
    mediaUrl: String?
  ): Result<MemberStatusUpdate> {
    if (SupabaseConfig.isConfigured()) {
      return try {
        val api = SupabaseApi.get()
        api.insertStatus(
          com.example.core.api.DbStatus(
            member_id = memberId,
            member_name = memberName,
            type = type.name,
            content = content,
            media_url = mediaUrl
          )
        )
        Result.success(
          MemberStatusUpdate(
            id = "st_${UUID.randomUUID().toString().take(6)}",
            memberId = memberId,
            memberName = memberName,
            type = type,
            content = content,
            mediaUrl = mediaUrl
          )
        )
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

    val newStatus = MemberStatusUpdate(
      id = "status_${UUID.randomUUID().toString().take(6)}",
      memberId = memberId,
      memberName = memberName,
      type = type,
      content = content,
      mediaUrl = mediaUrl,
      timestamp = System.currentTimeMillis()
    )
    _activeStatuses.value = listOf(newStatus) + _activeStatuses.value
    return Result.success(newStatus)
  }

  override suspend fun viewStatus(statusId: String, viewerId: String, viewerName: String): Result<Unit> {
    _activeStatuses.value = _activeStatuses.value.map { status ->
      if (status.id == statusId) {
        if (status.views.any { it.viewerId == viewerId }) {
          status
        } else {
          status.copy(views = status.views + StatusView(viewerId, viewerName))
        }
      } else status
    }
    return Result.success(Unit)
  }

  override suspend fun reactToStatus(statusId: String, emoji: String): Result<Unit> {
    _activeStatuses.value = _activeStatuses.value.map { status ->
      if (status.id == statusId) {
        val count = status.reactions[emoji] ?: 0
        val updatedReactions = status.reactions.toMutableMap()
        updatedReactions[emoji] = count + 1
        status.copy(reactions = updatedReactions)
      } else status
    }
    return Result.success(Unit)
  }

  override suspend fun sendBroadcast(
    title: String,
    content: String,
    targetRoles: List<String>
  ): Result<BroadcastCampaign> {
    if (SupabaseConfig.isConfigured()) {
      return try {
        val api = SupabaseApi.get()
        api.insertBroadcast(
          com.example.core.api.DbBroadcast(
            title = title,
            content = content,
            target_roles = targetRoles
          )
        )
        Result.success(
          BroadcastCampaign(
            id = "broad_${UUID.randomUUID().toString().take(6)}",
            title = title,
            content = content,
            targetRoles = targetRoles,
            sentAt = System.currentTimeMillis()
          )
        )
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

    val campaign = BroadcastCampaign(
      id = "broad_${UUID.randomUUID().toString().take(6)}",
      title = title,
      content = content,
      targetRoles = targetRoles,
      sentAt = System.currentTimeMillis()
    )
    _activeBroadcasts.value = listOf(campaign) + _activeBroadcasts.value

    // Mirror to some conversations
    _conversations.value.forEach { conv ->
      val systemMsg = ChatMessage(
        id = "msg_broad_${UUID.randomUUID().toString().take(6)}",
        conversationId = conv.id,
        senderId = "admin_broadcast",
        senderName = "🚨 Ashram Announcement",
        senderRoleIcon = "🏛️",
        content = "📢 *$title*\n$content"
      )
      updateMessagesList(conv.id) { current -> current + systemMsg }
      updateConversationLastMessage(conv.id, systemMsg)
    }

    return Result.success(campaign)
  }

  override suspend fun addToDownloadQueue(fileName: String, size: Long, url: String): Result<String> {
    val taskId = "dl_${UUID.randomUUID().toString().take(6)}"
    val task = ChatDownloadTask(
      id = taskId,
      fileName = fileName,
      size = size,
      progress = 0f,
      status = DownloadStatus.QUEUED,
      url = url
    )
    _downloadQueue.value = _downloadQueue.value + task

    // Simulate downloading progress
    repositoryScope.launch {
      delay(1000)
      updateDownloadTask(taskId, DownloadStatus.DOWNLOADING, 0.1f)
      delay(1000)
      updateDownloadTask(taskId, DownloadStatus.DOWNLOADING, 0.45f)
      delay(800)
      updateDownloadTask(taskId, DownloadStatus.DOWNLOADING, 0.85f)
      delay(600)
      updateDownloadTask(taskId, DownloadStatus.COMPLETED, 1f)

      // Also update messages to show that this attachment is fully offline and downloaded!
      _messageStore.value.forEach { (convId, messages) ->
        val updated = messages.map { msg ->
          if (msg.attachment?.url == url) {
            msg.copy(attachment = msg.attachment.copy(isDownloaded = true, isDownloading = false, progress = 1f))
          } else msg
        }
        _messageStore.value = _messageStore.value + (convId to updated)
      }
    }

    return Result.success(taskId)
  }

  override suspend fun pauseDownload(taskId: String): Result<Unit> {
    updateDownloadTask(taskId, DownloadStatus.PAUSED, null)
    return Result.success(Unit)
  }

  override suspend fun resumeDownload(taskId: String): Result<Unit> {
    val task = _downloadQueue.value.firstOrNull { it.id == taskId } ?: return Result.failure(Exception("Task not found"))
    updateDownloadTask(taskId, DownloadStatus.DOWNLOADING, task.progress)
    repositoryScope.launch {
      delay(1000)
      updateDownloadTask(taskId, DownloadStatus.DOWNLOADING, 0.90f)
      delay(500)
      updateDownloadTask(taskId, DownloadStatus.COMPLETED, 1.0f)
    }
    return Result.success(Unit)
  }

  override suspend fun cancelDownload(taskId: String): Result<Unit> {
    _downloadQueue.value = _downloadQueue.value.filterNot { it.id == taskId }
    return Result.success(Unit)
  }

  // Helper State Mutators
  private fun updateMessagesList(conversationId: String, mutator: (List<ChatMessage>) -> List<ChatMessage>) {
    val currentMap = _messageStore.value.toMutableMap()
    val list = currentMap[conversationId] ?: emptyList()
    currentMap[conversationId] = mutator(list)
    _messageStore.value = currentMap
  }

  private fun updateMessageStatus(conversationId: String, messageId: String, status: MessageStatus) {
    updateMessagesList(conversationId) { list ->
      list.map { if (it.id == messageId) it.copy(status = status) else it }
    }
  }

  private fun updateConversationLastMessage(conversationId: String, lastMsg: ChatMessage) {
    _conversations.value = _conversations.value.map { conv ->
      if (conv.id == conversationId) {
        val unread = if (lastMsg.senderId != "curr_user") conv.unreadCount + 1 else conv.unreadCount
        conv.copy(
          unreadCount = unread
        )
      } else conv
    }
  }

  private fun updateDownloadTask(id: String, status: DownloadStatus, progress: Float?) {
    _downloadQueue.value = _downloadQueue.value.map { task ->
      if (task.id == id) {
        task.copy(status = status, progress = progress ?: task.progress)
      } else task
    }
  }

  // --- Seed initial simulated ashram data ---
  private fun seedInitialData() {
    val conv1Id = "conv_1"
    val conv2Id = "conv_2"
    val conv3Id = "conv_3"
    val conv4Id = "conv_4"

    _conversations.value = listOf(
      ChatConversation(
        id = conv1Id,
        name = "Daily Sunderkand Reciters ✨",
        description = "Sadhana chanting schedule under Shastri Ji guidance. Standard decorum rules apply.",
        isGroup = true,
        inviteLink = "https://podhiashram.org/join/group/sunderkand",
        qrCodeText = "ashram_group_sunderkand",
        groupMembers = listOf("curr_user", "member_1", "member_4"),
        groupAdmins = listOf("member_1")
      ),
      ChatConversation(
        id = conv2Id,
        name = "Siddhant Vani Scholar Circle",
        description = "Deep spiritual verse dissection and Tartam Sagar study.",
        isGroup = true,
        inviteLink = "https://podhiashram.org/join/group/siddhant_vani",
        qrCodeText = "ashram_group_siddhant_vani",
        groupMembers = listOf("curr_user", "member_3", "member_1"),
        groupAdmins = listOf("member_3")
      ),
      ChatConversation(
        id = conv3Id,
        name = "Mahadev Pranami",
        description = "Senior Sanchalak & Pravachan Speaker.",
        isGroup = false,
        groupMembers = listOf("curr_user", "member_1")
      ),
      ChatConversation(
        id = conv4Id,
        name = "Ashram Seva Volunteers",
        description = "Seva arrangements, medical drives, and prasad coordination.",
        isGroup = true,
        inviteLink = "https://podhiashram.org/join/group/seva_vols",
        qrCodeText = "ashram_group_seva_vols",
        groupMembers = listOf("curr_user", "member_4", "member_2"),
        groupAdmins = listOf("member_4")
      )
    )

    // Seed Messages
    _messageStore.value = mapOf(
      conv1Id to listOf(
        ChatMessage(
          id = "m1_1",
          conversationId = conv1Id,
          senderId = "member_1",
          senderName = "Mahadev Pranami",
          senderRoleIcon = "🛕",
          content = "Hari Om Seva Ji, hope everyone completed their evening Jaap rounds.",
          timestamp = System.currentTimeMillis() - 3600000 * 2
        ),
        ChatMessage(
          id = "m1_2",
          conversationId = conv1Id,
          senderId = "member_4",
          senderName = "Kabir Patel",
          senderRoleIcon = "🔥",
          content = "Yes Mahadev Ji! Completed 5 malas of prarthana chanting. Feeling highly serene.",
          timestamp = System.currentTimeMillis() - 3600000,
          repliedToId = "m1_1",
          repliedToSender = "Mahadev Pranami",
          repliedToText = "Hari Om Seva Ji, hope everyone completed their evening Jaap rounds."
        ),
        ChatMessage(
          id = "m1_poll",
          conversationId = conv1Id,
          senderId = "member_1",
          senderName = "Mahadev Pranami",
          senderRoleIcon = "🛕",
          content = "Cast your availability for tomorrow's live Sunderkand Sabha:",
          type = MessageType.POLL,
          poll = PollData(
            id = "poll_1",
            question = "Cast your availability for tomorrow's live Sunderkand Sabha:",
            options = listOf(
              PollOption("opt_1", "Yes, 05:00 AM sharp", listOf("member_4")),
              PollOption("opt_2", "Will watch recorded session later", emptyList()),
              PollOption("opt_3", "Cannot attend due to Seva duty", emptyList())
            ),
            isAnonymous = false,
            isMultipleChoice = false
          ),
          timestamp = System.currentTimeMillis() - 1800000
        )
      ),
      conv2Id to listOf(
        ChatMessage(
          id = "m2_1",
          conversationId = conv2Id,
          senderId = "member_3",
          senderName = "Gaurav Shastri",
          senderRoleIcon = "📖",
          content = "Welcome all seeker souls. Let us dissect verse 12 of Shri Siddhant Sagar today.",
          timestamp = System.currentTimeMillis() - 7200000
        ),
        ChatMessage(
          id = "m2_2",
          conversationId = conv2Id,
          senderId = "member_1",
          senderName = "Mahadev Pranami",
          senderRoleIcon = "🛕",
          content = "Pranam Shastri Ji, I have shared the PDF compilation of verses with commentary below.",
          timestamp = System.currentTimeMillis() - 3600000
        ),
        ChatMessage(
          id = "m2_doc",
          conversationId = conv2Id,
          senderId = "member_1",
          senderName = "Mahadev Pranami",
          senderRoleIcon = "🛕",
          content = "Siddhant_Vani_Verses_12_20_Commentary.pdf",
          type = MessageType.DOCUMENT,
          attachment = ChatAttachment(
            id = "att_doc",
            name = "Siddhant_Vani_Commentary.pdf",
            type = "document",
            url = "https://podhiashram.org/assets/books/siddhant_vani_verses.pdf",
            size = 4580000L,
            isDownloaded = false
          ),
          timestamp = System.currentTimeMillis() - 3400000
        )
      ),
      conv3Id to listOf(
        ChatMessage(
          id = "m3_1",
          conversationId = conv3Id,
          senderId = "member_1",
          senderName = "Mahadev Pranami",
          senderRoleIcon = "🛕",
          content = "Hari Om disciples! Please make sure to update your Sadhana logs regularly on the dashboard so we can track ashram participation index.",
          timestamp = System.currentTimeMillis() - 86400000
        )
      ),
      conv4Id to listOf(
        ChatMessage(
          id = "m4_1",
          conversationId = conv4Id,
          senderId = "member_4",
          senderName = "Kabir Patel",
          senderRoleIcon = "🔥",
          content = "Guru Purnima event photography drive planning starts now.",
          timestamp = System.currentTimeMillis() - 86400000 * 2
        )
      )
    )

    // Presence seeding
    _memberPresence.value = mapOf(
      "member_1" to OnlineStatus.ONLINE,
      "member_2" to OnlineStatus.ONLINE,
      "member_3" to OnlineStatus.OFFLINE,
      "member_4" to OnlineStatus.ONLINE
    )

    // Statuses seeding
    _activeStatuses.value = listOf(
      MemberStatusUpdate(
        id = "st_1",
        memberId = "member_1",
        memberName = "Mahadev Pranami",
        type = StatusType.VANI,
        content = "🌷 True peace lies in surrendering your attachments to the Lotus feet of Raj Shyama Ji. Hari Om! 🙏",
        timestamp = System.currentTimeMillis() - 10800000,
        reactions = mapOf("🌸" to 5, "🙏" to 12)
      ),
      MemberStatusUpdate(
        id = "st_2",
        memberId = "member_4",
        memberName = "Kabir Patel",
        type = StatusType.PHOTO,
        content = "Glimpse of our beautiful newly painted main prayer hall dome at Paramdham Podhi Ashram. Golden Aura looking exquisite! ✨🛕",
        mediaUrl = "https://images.unsplash.com/photo-1609137144814-7221370df594", // temple dome illustration
        timestamp = System.currentTimeMillis() - 21600000,
        reactions = mapOf("❤️" to 8, "🪔" to 14)
      )
    )
  }

  // --- Simulated Live Realtime Interaction Engine ---
  private fun startRealtimeSimulation() {
    repositoryScope.launch {
      while (isActive) {
        delay(35000) // Trigger simulated activity every 35 seconds to keep it dynamic but not intrusive

        val randomConversation = _conversations.value.randomOrNull() ?: continue
        if (randomConversation.isMuted) continue

        val membersInConv = randomConversation.groupMembers.filterNot { it == "curr_user" }
        if (membersInConv.isEmpty()) continue

        val actorId = membersInConv.random()
        val actorName = when (actorId) {
          "member_1" -> "Mahadev Pranami"
          "member_2" -> "Aditi Vyas"
          "member_3" -> "Gaurav Shastri"
          "member_4" -> "Kabir Patel"
          else -> "Devotee Soul"
        }
        val actorRoleIcon = when (actorId) {
          "member_1" -> "🛕"
          "member_2" -> "🌸"
          "member_3" -> "📖"
          "member_4" -> "🔥"
          else -> "🙏"
        }

        // 1. Show actor starts Typing
        val currentTyping = _typingStatus.value.toMutableMap()
        val currentSet = (currentTyping[randomConversation.id] ?: emptySet()).toMutableSet()
        currentSet.add(actorId)
        currentTyping[randomConversation.id] = currentSet
        _typingStatus.value = currentTyping

        delay(3000) // type for 3 seconds

        // 2. Hide Typing
        val stopTyping = _typingStatus.value.toMutableMap()
        val stopSet = (stopTyping[randomConversation.id] ?: emptySet()).toMutableSet()
        stopSet.remove(actorId)
        stopTyping[randomConversation.id] = stopSet
        _typingStatus.value = stopTyping

        // 3. Dispatch incoming message
        val spiritualContents = listOf(
          "Hari Om Ji! Let us all recite Tartam Mantra 108 times today. 📿✨",
          "Just completed Seva duties in prasad kitchen. Truly fulfilling! 🌸🥘",
          "Beautiful Pravachan recorded from today's live stream. Blessed!",
          "Has anyone checked out the new library Holy books shelf?",
          "Pranam Shastri Ji, the verses of Sunderkand bring boundless joy. 🙏🛕",
          "Ashramites, tomorrow's evening Aarti is dedicated to Guru Vandana.",
          "Let us raise our spiritual vibrations together. Hari Om!"
        )

        val text = spiritualContents.random()
        val incomingMsg = ChatMessage(
          id = "msg_sim_${System.currentTimeMillis().toString().takeLast(6)}",
          conversationId = randomConversation.id,
          senderId = actorId,
          senderName = actorName,
          senderRoleIcon = actorRoleIcon,
          content = text,
          timestamp = System.currentTimeMillis()
        )

        updateMessagesList(randomConversation.id) { list -> list + incomingMsg }

        // Increment unread count in conversations
        _conversations.value = _conversations.value.map { conv ->
          if (conv.id == randomConversation.id) {
            conv.copy(unreadCount = conv.unreadCount + 1)
          } else conv
        }
      }
    }

    // Secondary simulation: poll voting activity
    repositoryScope.launch {
      while (isActive) {
        delay(42000)
        val convId = "conv_1"
        val messages = _messageStore.value[convId] ?: continue
        val pollMsg = messages.firstOrNull { it.type == MessageType.POLL } ?: continue
        val poll = pollMsg.poll ?: continue

        val randomOption = poll.options.random()
        val voter = "member_2" // Aditi Vyas votes
        
        if (!randomOption.votes.contains(voter)) {
          voteOnPoll(pollMsg.id, randomOption.id, voter)
        }
      }
    }
  }

  private suspend fun simulateAutoResponse(conversationId: String, text: String) {
    val lowerText = text.lowercase()
    val conv = _conversations.value.firstOrNull { it.id == conversationId } ?: return
    if (conv.isGroup) return // don't auto-reply on groups

    // Typing delay
    delay(2000)

    val responseContent = when {
      lowerText.contains("hari om") || lowerText.contains("pranam") || lowerText.contains("hello") -> {
        "Hari Om Seva Ji! May Raj Shyam Ji shower divine blessings upon you. How can I assist your spiritual journey today?"
      }
      lowerText.contains("seva") || lowerText.contains("volunteer") -> {
        "Pranam. Our next volunteer drive is the Guru Purnima Prasad distribution. Please join the 'Ashram Seva Volunteers' group to stay fully coordinated! 🙏🥘"
      }
      lowerText.contains("sundarkand") || lowerText.contains("satsang") -> {
        "We host daily virtual Sunderkand chanting at 05:00 AM. Access the Live Sabha tab from the navigation panel to participate synchronously!"
      }
      lowerText.contains("book") || lowerText.contains("read") || lowerText.contains("pdf") -> {
        "You can browse premium digital scriptures in our Ashram Library. We recommend starting with 'Siddhant Vani' compiled by Shastri Ji. 📖"
      }
      else -> {
        "Hari Om. Received your message in spiritual silence. Our Ashram Seva Desk will review and revert back soon. Have a serene day ahead! 📿🌸"
      }
    }

    val systemReply = ChatMessage(
      id = "msg_auto_${UUID.randomUUID().toString().take(6)}",
      conversationId = conversationId,
      senderId = "member_1",
      senderName = "Mahadev Pranami",
      senderRoleIcon = "🛕",
      content = responseContent,
      timestamp = System.currentTimeMillis()
    )

    updateMessagesList(conversationId) { list -> list + systemReply }
    _conversations.value = _conversations.value.map { c ->
      if (c.id == conversationId) {
        c.copy(unreadCount = c.unreadCount + 1)
      } else c
    }
  }
}
