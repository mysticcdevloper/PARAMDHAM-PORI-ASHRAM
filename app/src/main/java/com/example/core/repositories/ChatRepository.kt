package com.example.core.repositories

import com.example.core.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
  // Realtime Active Flows
  val conversations: StateFlow<List<ChatConversation>>
  val activeStatuses: StateFlow<List<MemberStatusUpdate>>
  val activeBroadcasts: StateFlow<List<BroadcastCampaign>>
  val downloadQueue: StateFlow<List<ChatDownloadTask>>
  val favoritedMessages: StateFlow<List<ChatMessage>>

  // Online & Typing Realtime Statuses
  fun getOnlineStatusFlow(memberId: String): Flow<OnlineStatus>
  fun getTypingStatusFlow(conversationId: String, memberId: String): Flow<Boolean>

  // Messages operations
  fun observeMessages(conversationId: String): Flow<List<ChatMessage>>
  suspend fun sendMessage(
    conversationId: String,
    content: String,
    type: MessageType = MessageType.TEXT,
    repliedToId: String? = null,
    attachment: ChatAttachment? = null,
    poll: PollData? = null,
    scheduledTime: Long? = null
  ): Result<ChatMessage>

  suspend fun editMessage(messageId: String, newContent: String): Result<Unit>
  suspend fun deleteMessage(messageId: String, forEveryone: Boolean): Result<Unit>
  suspend fun addReaction(messageId: String, emoji: String, senderId: String, senderName: String): Result<Unit>
  suspend fun removeReaction(messageId: String, senderId: String): Result<Unit>
  suspend fun pinMessage(conversationId: String, messageId: String, pin: Boolean): Result<Unit>
  suspend fun starMessage(messageId: String, star: Boolean): Result<Unit>
  suspend fun forwardMessage(messageId: String, targetConversationId: String): Result<Unit>

  // Group Management
  suspend fun createGroup(
    name: String,
    description: String?,
    creatorId: String,
    members: List<String>
  ): Result<ChatConversation>
  suspend fun updateGroupMetadata(conversationId: String, name: String, description: String?, avatarUrl: String?): Result<Unit>
  suspend fun approveMemberJoin(conversationId: String, memberId: String): Result<Unit>
  suspend fun removeGroupMember(conversationId: String, memberId: String): Result<Unit>
  suspend fun muteGroupMember(conversationId: String, memberId: String, mute: Boolean): Result<Unit>

  // Poll operations
  suspend fun voteOnPoll(messageId: String, optionId: String, memberId: String): Result<Unit>

  // Status operations
  suspend fun uploadStatus(memberId: String, memberName: String, type: StatusType, content: String, mediaUrl: String?): Result<MemberStatusUpdate>
  suspend fun viewStatus(statusId: String, viewerId: String, viewerName: String): Result<Unit>
  suspend fun reactToStatus(statusId: String, emoji: String): Result<Unit>

  // Broadcast operations
  suspend fun sendBroadcast(title: String, content: String, targetRoles: List<String>): Result<BroadcastCampaign>

  // Downloads Manager
  suspend fun addToDownloadQueue(fileName: String, size: Long, url: String): Result<String>
  suspend fun pauseDownload(taskId: String): Result<Unit>
  suspend fun resumeDownload(taskId: String): Result<Unit>
  suspend fun cancelDownload(taskId: String): Result<Unit>
}
