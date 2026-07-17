package com.example.features.chat

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.models.*
import com.example.ui.components.GlassmorphicCard
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatRoomView(
  viewModel: AppViewModel,
  conversation: ChatConversation,
  onBack: () -> Unit
) {
  val scope = rememberCoroutineScope()
  val listState = rememberLazyListState()
  
  // States
  var textInput by remember { mutableStateOf("") }
  val messages by viewModel.chatRepository.observeMessages(conversation.id).collectAsState(initial = emptyList())
  val wallpaper by viewModel.chatWallpaper.collectAsState()
  
  var activeSearchQuery by remember { mutableStateOf("") }
  var isSearchActive by remember { mutableStateOf(false) }

  // Drawers & Dialogs
  var isAttachmentOpen by remember { mutableStateOf(false) }
  var selectedMessageForMenu by remember { mutableStateOf<ChatMessage?>(null) }
  var replyToMessage by remember { mutableStateOf<ChatMessage?>(null) }
  
  // Custom attachment builders
  var showPollDialog by remember { mutableStateOf(false) }
  var showScheduleDialog by remember { mutableStateOf(false) }
  
  // Voice Recording Simulator States
  var isRecording by remember { mutableStateOf(false) }
  var isNoiseReduction by remember { mutableStateOf(false) }
  var recordingSeconds by remember { mutableStateOf(0) }
  var recordedAudioBytes by remember { mutableStateOf<ByteArray?>(null) }
  
  // Scroll to bottom on load or new message
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  // Timer for voice note
  LaunchedEffect(isRecording) {
    if (isRecording) {
      recordingSeconds = 0
      while (isRecording) {
        delay(1000)
        recordingSeconds++
      }
    }
  }

  // Define wallpaper background
  val wallpaperBrush = remember(wallpaper) {
    when (wallpaper) {
      ChatWallpaper.GOLDEN_GLOW -> Brush.radialGradient(
        colors = listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3), Color(0xFFFFD54F).copy(alpha = 0.3f))
      )
      ChatWallpaper.HOLY_TEMPLE -> Brush.verticalGradient(
        colors = listOf(Color(0xFFFFE0B2), Color(0xFFFFF3E0), Color(0xFFE65100).copy(alpha = 0.1f))
      )
      ChatWallpaper.LOTUS_TEMPLE -> Brush.verticalGradient(
        colors = listOf(Color(0xFFFCE4EC), Color(0xFFF8BBD0), Color(0xFFC2185B).copy(alpha = 0.1f))
      )
      ChatWallpaper.DEVOTIONAL_FESTIVAL -> Brush.sweepGradient(
        colors = listOf(Color(0xFFFFCC80), Color(0xFFFFAB91), Color(0xFFFFF9C4))
      )
      ChatWallpaper.DARK_COSMIC -> Brush.verticalGradient(
        colors = listOf(Color(0xFF1A1B2F), Color(0xFF161622), Color(0xFF3F3D56).copy(alpha = 0.4f))
      )
      ChatWallpaper.DEFAULT_SLATE -> Brush.verticalGradient(
        colors = listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC))
      )
    }
  }

  val filteredMessages = remember(messages, activeSearchQuery) {
    if (activeSearchQuery.isEmpty()) messages
    else messages.filter { it.content.contains(activeSearchQuery, ignoreCase = true) }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
          }
        },
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (conversation.isGroup) "🛕" else "🙏",
                fontSize = 18.sp
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = conversation.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
              )
              Text(
                text = if (conversation.isGroup) "${conversation.groupMembers.size} members online" else "Active presence",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        },
        actions = {
          IconButton(onClick = { isSearchActive = !isSearchActive; if(!isSearchActive) activeSearchQuery = "" }) {
            Icon(Icons.Default.Search, contentDescription = "Search Messages")
          }
          IconButton(onClick = { showScheduleDialog = true }) {
            Icon(Icons.Default.CalendarToday, contentDescription = "Schedule Message")
          }
          // Wallpaper Quick Option
          var showWallpaperMenu by remember { mutableStateOf(false) }
          IconButton(onClick = { showWallpaperMenu = true }) {
            Icon(Icons.Default.Wallpaper, contentDescription = "Theme Wallpapers")
          }
          DropdownMenu(expanded = showWallpaperMenu, onDismissRequest = { showWallpaperMenu = false }) {
            ChatWallpaper.values().forEach { wall ->
              DropdownMenuItem(
                text = { Text(wall.name.replace("_", " ")) },
                onClick = {
                  viewModel.updateChatWallpaper(wall)
                  showWallpaperMenu = false
                }
              )
            }
          }
        }
      )
    }
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(wallpaperBrush)
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Search Panel
        if (isSearchActive) {
          OutlinedTextField(
            value = activeSearchQuery,
            onValueChange = { activeSearchQuery = it },
            placeholder = { Text("Filter messages within chat...", fontSize = 12.sp) },
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp)
              .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp)),
            trailingIcon = {
              IconButton(onClick = { activeSearchQuery = ""; isSearchActive = false }) {
                Icon(Icons.Default.Close, contentDescription = null)
              }
            }
          )
        }

        // Messages list
        LazyColumn(
          state = listState,
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
        ) {
          items(filteredMessages, key = { it.id }) { msg ->
            val isMe = msg.senderId == "curr_user"
            
            // Animation slide & fade
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .combinedClickable(
                  onLongClick = { selectedMessageForMenu = msg },
                  onClick = {
                    if (msg.type == MessageType.POLL) {
                      // Click option 1 by default for simulated feedback
                      msg.poll?.options?.firstOrNull()?.let { opt ->
                        scope.launch {
                          viewModel.chatRepository.voteOnPoll(msg.id, opt.id, "curr_user")
                        }
                      }
                    }
                  }
                ),
              contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
            ) {
              MessageBubble(
                message = msg,
                isMe = isMe,
                onStar = { scope.launch { viewModel.chatRepository.starMessage(msg.id, !msg.isStarred) } },
                onReply = { replyToMessage = msg }
              )
            }
          }
        }

        // Reply bar preview
        if (replyToMessage != null) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
              .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Reply, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text("Replying to ${replyToMessage!!.senderName}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
              Text(replyToMessage!!.content, fontSize = 11.sp, maxLines = 1, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            }
            IconButton(onClick = { replyToMessage = null }, modifier = Modifier.size(24.dp)) {
              Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
            }
          }
        }

        // Input bottom panel
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Add Attachment Button
          IconButton(
            onClick = { isAttachmentOpen = !isAttachmentOpen },
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
          ) {
            Icon(
              imageVector = if (isAttachmentOpen) Icons.Default.Close else Icons.Default.Add,
              contentDescription = "Attach Files",
              tint = MaterialTheme.colorScheme.primary
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Text Field / Audio Recording State
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(24.dp))
              .background(Color.White.copy(alpha = 0.9f))
              .padding(horizontal = 12.dp, vertical = 2.dp)
          ) {
            if (isRecording) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(44.dp)
              ) {
                // Recording indicator flashing red
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Recording Voice Note... ${recordingSeconds}s",
                  fontSize = 12.sp,
                  color = Color.Red,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.weight(1f)
                )
                // Noise reduction toggle
                IconButton(
                  onClick = { isNoiseReduction = !isNoiseReduction },
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Hearing,
                    contentDescription = null,
                    tint = if (isNoiseReduction) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Slide to cancel ➔",
                  fontSize = 10.sp,
                  color = Color.Gray,
                  modifier = Modifier.clickable {
                    isRecording = false
                    recordedAudioBytes = null
                  }
                )
              }
            } else {
              Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                  value = textInput,
                  onValueChange = { textInput = it },
                  placeholder = { Text("Recite divine verses...", fontSize = 13.sp, color = Color.Gray) },
                  colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                  ),
                  modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 0.dp)
                )
                
                // Holy stickers quick drawer shortcut
                IconButton(onClick = {
                  // Simulate sticker sending
                  scope.launch {
                    val stickerAttachment = ChatAttachment(
                      id = "st_${System.currentTimeMillis()}",
                      name = "Golden Lotus Sticker",
                      type = "sticker",
                      url = "https://podhiashram.org/assets/stickers/lotus.png",
                      size = 120000L
                    )
                    viewModel.chatRepository.sendMessage(
                      conversationId = conversation.id,
                      content = "🌸 Golden Lotus",
                      type = MessageType.STICKER,
                      attachment = stickerAttachment
                    )
                    viewModel.showToast("Sticker sent with Saffron blessings!")
                  }
                }, modifier = Modifier.size(28.dp)) {
                  Text("🌸", fontSize = 18.sp)
                }
              }
            }
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Send / Voice Record Button
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary)
              .combinedClickable(
                onClick = {
                  if (isRecording) {
                    // Send voice note
                    isRecording = false
                    scope.launch {
                      val voiceAttach = ChatAttachment(
                        id = "voice_${System.currentTimeMillis()}",
                        name = "Voice_Note_${System.currentTimeMillis() % 10000}.mp3",
                        type = "voice",
                        url = "https://podhiashram.org/assets/voice/note.mp3",
                        size = 350000L,
                        durationSec = recordingSeconds
                      )
                      viewModel.chatRepository.sendMessage(
                        conversationId = conversation.id,
                        content = "🎤 Voice message (${recordingSeconds}s)",
                        type = MessageType.VOICE,
                        attachment = voiceAttach
                      )
                    }
                  } else if (textInput.trim().isNotEmpty()) {
                    val content = textInput
                    textInput = ""
                    val replyId = replyToMessage?.id
                    replyToMessage = null
                    scope.launch {
                      viewModel.chatRepository.sendMessage(
                        conversationId = conversation.id,
                        content = content,
                        repliedToId = replyId
                      )
                    }
                  } else {
                    viewModel.showToast("Hold button to record Holy Voice Note")
                  }
                },
                onLongClick = {
                  if (!isRecording) {
                    isRecording = true
                    viewModel.showToast("Recording started! Slide left to cancel.")
                  }
                }
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (textInput.isNotEmpty() || isRecording) Icons.Default.Send else Icons.Default.Mic,
              contentDescription = "Send",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Expanded Drawer Attachment Panel
        AnimatedVisibility(
          visible = isAttachmentOpen,
          enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
          exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
          AttachmentDrawer(
            onImageSelect = {
              scope.launch {
                val imgAttach = ChatAttachment("img_${System.currentTimeMillis()}", "temple_aura.jpg", "image", "https://images.unsplash.com/photo-1545128485-c400e7702796", 980000L)
                viewModel.chatRepository.sendMessage(conversation.id, "Image: Temple Aura Shared ✨", MessageType.IMAGE, attachment = imgAttach)
              }
              isAttachmentOpen = false
            },
            onVideoSelect = {
              scope.launch {
                val videoAttach = ChatAttachment("vid_${System.currentTimeMillis()}", "Divine_Satsang_Clip.mp4", "video", "https://assets.mixkit.co/videos/preview/mixkit-stars-in-space-background-1611-large.mp4", 12400000L, 45)
                viewModel.chatRepository.sendMessage(conversation.id, "Video: Divine Satsang Clip Shared 🎥", MessageType.VIDEO, attachment = videoAttach)
              }
              isAttachmentOpen = false
            },
            onDocSelect = {
              scope.launch {
                val docAttach = ChatAttachment("doc_${System.currentTimeMillis()}", "Siddhant_Vani_Holy_Scripture.pdf", "document", "https://podhiashram.org/holy_verses.pdf", 4500000L)
                viewModel.chatRepository.sendMessage(conversation.id, "Siddhant_Vani_Holy_Scripture.pdf", MessageType.DOCUMENT, attachment = docAttach)
              }
              isAttachmentOpen = false
            },
            onStickerSelect = { name, emoji ->
              scope.launch {
                val stickerAttach = ChatAttachment("stick_${System.currentTimeMillis()}", "$name.png", "sticker", "https://podhiashram.org/assets/stickers/$name.png", 50000L)
                viewModel.chatRepository.sendMessage(conversation.id, "$emoji $name", MessageType.STICKER, attachment = stickerAttach)
              }
              isAttachmentOpen = false
            },
            onCreatePoll = { showPollDialog = true; isAttachmentOpen = false },
            onSchedule = { showScheduleDialog = true; isAttachmentOpen = false }
          )
        }
      }
    }
  }

  // Poll Dialog Setup
  if (showPollDialog) {
    var question by remember { mutableStateOf("") }
    var opt1 by remember { mutableStateOf("") }
    var opt2 by remember { mutableStateOf("") }
    var isAnon by remember { mutableStateOf(false) }

    AlertDialog(
      onDismissRequest = { showPollDialog = false },
      title = { Text("Create Spiritual Poll", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(value = question, onValueChange = { question = it }, label = { Text("Question or Topic") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = opt1, onValueChange = { opt1 = it }, label = { Text("Option 1") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = opt2, onValueChange = { opt2 = it }, label = { Text("Option 2") }, modifier = Modifier.fillMaxWidth())
          Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isAnon, onCheckedChange = { isAnon = it })
            Text("Anonymous Poll results")
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (question.isNotEmpty() && opt1.isNotEmpty() && opt2.isNotEmpty()) {
              val pollData = PollData(
                id = "poll_${UUID.randomUUID().toString().take(6)}",
                question = question,
                options = listOf(PollOption("o1", opt1), PollOption("o2", opt2)),
                isAnonymous = isAnon,
                isMultipleChoice = false
              )
              scope.launch {
                viewModel.chatRepository.sendMessage(
                  conversationId = conversation.id,
                  content = question,
                  type = MessageType.POLL,
                  poll = pollData
                )
              }
              showPollDialog = false
            }
          }
        ) {
          Text("Launch Poll")
        }
      }
    )
  }

  // Schedule Message Dialog Setup
  if (showScheduleDialog) {
    var text by remember { mutableStateOf("") }
    var delayMinutes by remember { mutableStateOf(1) }

    AlertDialog(
      onDismissRequest = { showScheduleDialog = false },
      title = { Text("Schedule Divine Message", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Schedule festival wishes, daily vani, or reminders.", fontSize = 11.sp, color = Color.Gray)
          OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Message Content") }, modifier = Modifier.fillMaxWidth())
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Send in:")
            Slider(
              value = delayMinutes.toFloat(),
              onValueChange = { delayMinutes = it.toInt() },
              valueRange = 1f..10f,
              modifier = Modifier.weight(1f)
            )
            Text("$delayMinutes min")
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (text.isNotEmpty()) {
              scope.launch {
                val triggerAt = System.currentTimeMillis() + (delayMinutes * 60 * 1000)
                viewModel.chatRepository.sendMessage(
                  conversationId = conversation.id,
                  content = text,
                  scheduledTime = triggerAt
                )
                viewModel.showToast("Message successfully scheduled for festival greeting!")
              }
              showScheduleDialog = false
            }
          }
        ) {
          Text("Confirm Schedule")
        }
      }
    )
  }

  // Long-press Actions Menu Overlay
  if (selectedMessageForMenu != null) {
    val msg = selectedMessageForMenu!!
    AlertDialog(
      onDismissRequest = { selectedMessageForMenu = null },
      title = {
        Column {
          Text("Message Action Panel", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Text("Sent by ${msg.senderName}", fontSize = 11.sp, color = Color.Gray)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          // Reactions Bar
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
              .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val quickReactions = listOf("❤️", "🙏", "🌸", "🪔", "📿", "👍", "👏", "😊", "🔥", "✨")
            quickReactions.forEach { emoji ->
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                  .clickable {
                    scope.launch {
                      viewModel.chatRepository.addReaction(msg.id, emoji, "curr_user", "You")
                      viewModel.showToast("Reaction $emoji sent!")
                    }
                    selectedMessageForMenu = null
                  },
                contentAlignment = Alignment.Center
              ) {
                Text(emoji, fontSize = 18.sp)
              }
            }
          }

          Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))

          // Menu items
          RowMenuItem(icon = Icons.Default.Reply, text = "Reply to this Message") {
            replyToMessage = msg
            selectedMessageForMenu = null
          }
          RowMenuItem(icon = Icons.Default.Star, text = if (msg.isStarred) "Unstar Message" else "Star Message") {
            scope.launch { viewModel.chatRepository.starMessage(msg.id, !msg.isStarred) }
            selectedMessageForMenu = null
          }
          RowMenuItem(icon = Icons.Default.PushPin, text = if (msg.isPinned) "Unpin from Group" else "Pin Message") {
            scope.launch { viewModel.chatRepository.pinMessage(conversation.id, msg.id, !msg.isPinned) }
            selectedMessageForMenu = null
          }
          RowMenuItem(icon = Icons.Default.ArrowForward, text = "Forward Message") {
            // Forward to direct message conversation 3 by default
            scope.launch { viewModel.chatRepository.forwardMessage(msg.id, "conv_3") }
            viewModel.showToast("Forwarded to Mahadev Pranami!")
            selectedMessageForMenu = null
          }
          if (msg.senderId == "curr_user") {
            RowMenuItem(icon = Icons.Default.Edit, text = "Edit Message Content") {
              scope.launch { viewModel.chatRepository.editMessage(msg.id, msg.content + " (revised)") }
              selectedMessageForMenu = null
            }
            RowMenuItem(icon = Icons.Default.Delete, text = "Delete for Everyone", color = Color.Red) {
              scope.launch { viewModel.chatRepository.deleteMessage(msg.id, true) }
              selectedMessageForMenu = null
            }
          } else {
            RowMenuItem(icon = Icons.Default.Report, text = "Report Spam / Policy Violation", color = Color.Red) {
              viewModel.showToast("Message flagged for administration review.")
              selectedMessageForMenu = null
            }
            RowMenuItem(icon = Icons.Default.Block, text = "Block Member", color = Color.Red) {
              viewModel.showToast("You blocked ${msg.senderName}.")
              selectedMessageForMenu = null
            }
          }
        }
      },
      confirmButton = {}
    )
  }
}

@Composable
fun RowMenuItem(
  icon: ImageVector,
  text: String,
  color: Color = Color.Unspecified,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 10.dp, horizontal = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(icon, contentDescription = null, tint = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
    Spacer(modifier = Modifier.width(12.dp))
    Text(text, fontSize = 13.sp, color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onBackground)
  }
}

@Composable
fun MessageBubble(
  message: ChatMessage,
  isMe: Boolean,
  onStar: () -> Unit,
  onReply: () -> Unit
) {
  // Setup colors matching Temple Holy theme
  val bubbleBg = if (isMe) {
    MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
  } else {
    Color.White.copy(alpha = 0.9f)
  }
  val textColor = if (isMe) Color.White else MaterialTheme.colorScheme.onBackground
  val statusIcon = when (message.status) {
    MessageStatus.SENDING -> "⏳"
    MessageStatus.DELIVERED -> "✓"
    MessageStatus.READ -> "✓✓"
  }

  Column(
    modifier = Modifier
      .widthIn(max = 280.dp)
      .clip(
        RoundedCornerShape(
          topStart = 16.dp,
          topEnd = 16.dp,
          bottomStart = if (isMe) 16.dp else 4.dp,
          bottomEnd = if (isMe) 4.dp else 16.dp
        )
      )
      .background(bubbleBg)
      .border(
        width = 1.dp,
        color = if (isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        shape = RoundedCornerShape(
          topStart = 16.dp,
          topEnd = 16.dp,
          bottomStart = if (isMe) 16.dp else 4.dp,
          bottomEnd = if (isMe) 4.dp else 16.dp
        )
      )
      .padding(10.dp)
  ) {
    // Reply Context
    if (message.repliedToSender != null) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(6.dp))
          .background(if (isMe) Color.Black.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
          .padding(6.dp)
      ) {
        Column {
          Text(message.repliedToSender, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isMe) Color.White else MaterialTheme.colorScheme.primary)
          Text(message.repliedToText ?: "", fontSize = 11.sp, maxLines = 1, color = if (isMe) Color.White.copy(alpha = 0.8f) else Color.Gray)
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
    }

    // Role Indicator Header (if Group received)
    if (!isMe && message.senderId != "system" && message.senderId != "admin_broadcast") {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(message.senderRoleIcon, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = message.senderName,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }
      Spacer(modifier = Modifier.height(2.dp))
    }

    // Attachments Rendering
    message.attachment?.let { attach ->
      when (message.type) {
        MessageType.IMAGE -> {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color.LightGray)
          ) {
            Text("🖼️ [Compressed Photo File]", modifier = Modifier.align(Alignment.Center), fontSize = 11.sp, color = Color.White)
          }
          Spacer(modifier = Modifier.height(4.dp))
        }
        MessageType.VIDEO -> {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color.DarkGray)
          ) {
            Icon(
              Icons.Default.PlayArrow,
              contentDescription = "Play Video",
              tint = Color.White,
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(10.dp)
                .align(Alignment.Center)
            )
            Text(attach.name, modifier = Modifier.align(Alignment.BottomStart).padding(8.dp), fontSize = 10.sp, color = Color.White)
            Text("🎥 Video • 12.4 MB", modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp), fontSize = 10.sp, color = Color.White)
          }
          Spacer(modifier = Modifier.height(4.dp))
        }
        MessageType.DOCUMENT -> {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(if (isMe) Color.Black.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
              .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = if (isMe) Color.White else MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(attach.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
              Text("PDF Book • 4.5 MB", fontSize = 9.sp, color = if(isMe) Color.White.copy(alpha = 0.7f) else Color.Gray)
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
        }
        MessageType.VOICE -> {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(if (isMe) Color.Black.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
              .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = if (isMe) Color.White else MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(4.dp))
            // Simulated waveform animation bars
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
              val heights = listOf(8, 16, 24, 12, 18, 30, 14, 8, 22, 10, 4, 18, 12)
              heights.forEach { h ->
                Box(modifier = Modifier.width(3.dp).height(h.dp).background(if(isMe) Color.White else MaterialTheme.colorScheme.primary))
              }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "${attach.durationSec ?: 5}s", fontSize = 10.sp, color = textColor)
          }
          Spacer(modifier = Modifier.height(4.dp))
        }
        MessageType.STICKER -> {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(100.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("🌸", fontSize = 44.sp)
              Text("Blessed Pranam", fontSize = 9.sp, color = textColor, fontWeight = FontWeight.SemiBold)
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
        }
        else -> {}
      }
    }

    // Poll panel rendering
    message.poll?.let { poll ->
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color.White.copy(alpha = 0.8f))
          .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Text(poll.question, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        poll.options.forEach { opt ->
          val voteCount = opt.votes.size
          val optionPercentage = if (voteCount > 0) 100f else 0f
          
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
              .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
              .padding(8.dp)
          ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
              Text(opt.text, fontSize = 11.sp, color = Color.Black)
              Row {
                Text("$voteCount votes", fontSize = 10.sp, color = Color.Gray)
                if (voteCount > 0) {
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("✅", fontSize = 10.sp)
                }
              }
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
    }

    // Message Text
    if (message.type == MessageType.TEXT || message.type == MessageType.IMAGE || message.type == MessageType.DOCUMENT) {
      Text(
        text = message.content,
        color = textColor,
        fontSize = 13.sp,
        lineHeight = 18.sp
      )
    }

    // Timestamp & Read Status row
    Row(
      modifier = Modifier
        .align(Alignment.End)
        .padding(top = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (message.isStarred) {
        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(10.dp))
        Spacer(modifier = Modifier.width(4.dp))
      }
      if (message.isPinned) {
        Icon(Icons.Default.PushPin, contentDescription = null, tint = Color(0xFFFFEB3B), modifier = Modifier.size(10.dp))
        Spacer(modifier = Modifier.width(4.dp))
      }
      Text(
        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
        color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
        fontSize = 9.sp
      )
      if (isMe) {
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = statusIcon,
          color = Color.White,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // Reactions Pill Footer
    if (message.reactions.isNotEmpty()) {
      Spacer(modifier = Modifier.height(4.dp))
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(Color.White.copy(alpha = 0.6f))
          .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        message.reactions.map { it.emoji }.distinct().forEach { emo ->
          Text(emo, fontSize = 12.sp)
        }
        Text("${message.reactions.size}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
      }
    }
  }
}

@Composable
fun AttachmentDrawer(
  onImageSelect: () -> Unit,
  onVideoSelect: () -> Unit,
  onDocSelect: () -> Unit,
  onStickerSelect: (String, String) -> Unit,
  onCreatePoll: () -> Unit,
  onSchedule: () -> Unit
) {
  GlassmorphicCard(
    modifier = Modifier
      .fillMaxWidth()
      .padding(10.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text("Sacred Attachments & Media", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        AttachmentIcon(Icons.Default.Image, "Photos") { onImageSelect() }
        AttachmentIcon(Icons.Default.Videocam, "Videos") { onVideoSelect() }
        AttachmentIcon(Icons.Default.InsertDriveFile, "Holy Books") { onDocSelect() }
        AttachmentIcon(Icons.Default.BarChart, "Spiritual Poll") { onCreatePoll() }
        AttachmentIcon(Icons.Default.Timer, "Schedule Greeting") { onSchedule() }
      }

      Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))

      Text("Spiritual Stickers (Blessed Pack)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        StickerButton("Lotus", "🌸") { onStickerSelect("lotus", "🌸") }
        StickerButton("Temple", "🛕") { onStickerSelect("temple", "🛕") }
        StickerButton("Diya", "🪔") { onStickerSelect("diya", "🪔") }
        StickerButton("Om", "🕉️") { onStickerSelect("om", "🕉️") }
        StickerButton("Pranam", "🙏") { onStickerSelect("pranam", "🙏") }
      }
    }
  }
}

@Composable
fun AttachmentIcon(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable(onClick = onClick)
  ) {
    Box(
      modifier = Modifier
        .size(44.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
  }
}

@Composable
fun StickerButton(
  name: String,
  emoji: String,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable(onClick = onClick)
  ) {
    Text(emoji, fontSize = 28.sp)
    Text(name, fontSize = 9.sp, color = Color.Gray)
  }
}
