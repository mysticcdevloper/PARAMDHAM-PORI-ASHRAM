package com.example.features.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.core.constants.AppConstants.AppLanguage
import com.example.core.models.*
import com.example.ui.components.GlassmorphicCard
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
  language: AppLanguage,
  viewModel: AppViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
  val scope = rememberCoroutineScope()
  
  // State from ViewModel
  val conversations by viewModel.chatConversations.collectAsState()
  val activeStatuses by viewModel.activeStatuses.collectAsState()
  val activeBroadcasts by viewModel.activeBroadcasts.collectAsState()
  val downloadQueue by viewModel.downloadQueue.collectAsState()
  val favoritedMessages by viewModel.favoritedMessages.collectAsState()
  val currentUserProfile by viewModel.currentUserProfile.collectAsState()

  // Selected chat room route
  var activeConversation by remember { mutableStateOf<ChatConversation?>(null) }
  
  // UI Tabs State
  var selectedTabIdx by remember { mutableStateOf(0) }
  val tabs = listOf("CHATS", "STATUSES", "ANNOUNCEMENTS", "DOWNLOADS", "STARRED")
  
  // Search query for rooms
  var searchQuery by remember { mutableStateOf("") }
  
  // Create Group Dialog State
  var showCreateGroupDialog by remember { mutableStateOf(false) }
  var newGroupName by remember { mutableStateOf("") }
  var newGroupDesc by remember { mutableStateOf("") }

  // Status Story View Dialog State
  var viewedStatus by remember { mutableStateOf<MemberStatusUpdate?>(null) }

  if (activeConversation != null) {
    ChatRoomView(
      viewModel = viewModel,
      conversation = activeConversation!!,
      onBack = { activeConversation = null }
    )
    return
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("chats_screen_content")
  ) {
    // Header Row with Title & Quick Invite/QR Option
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Spiritual Communion",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = "Connect with Paramdham Podhi Ashram",
          fontSize = 11.sp,
          color = Color.Gray
        )
      }

      // Group creator quick button
      IconButton(
        onClick = { showCreateGroupDialog = true },
        modifier = Modifier
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
      ) {
        Icon(Icons.Default.GroupAdd, contentDescription = "Create Group", tint = MaterialTheme.colorScheme.primary)
      }
    }

    // Scrollable Tab Row for Channels
    ScrollableTabRow(
      selectedTabIndex = selectedTabIdx,
      edgePadding = 12.dp,
      containerColor = Color.Transparent,
      contentColor = MaterialTheme.colorScheme.primary,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIdx]),
          color = MaterialTheme.colorScheme.primary
        )
      }
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTabIdx == index,
          onClick = { selectedTabIdx = index },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (selectedTabIdx == index) FontWeight.Bold else FontWeight.Normal
              )
              // Specific Badges
              if (title == "CHATS") {
                val totalUnread = conversations.sumOf { it.unreadCount }
                if (totalUnread > 0) {
                  Spacer(modifier = Modifier.width(4.dp))
                  Box(
                    modifier = Modifier
                      .size(16.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                  ) {
                    Text("$totalUnread", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                  }
                }
              }
              if (title == "DOWNLOADS") {
                val downloadingCount = downloadQueue.count { it.status == DownloadStatus.DOWNLOADING }
                if (downloadingCount > 0) {
                  Spacer(modifier = Modifier.width(4.dp))
                  Box(
                    modifier = Modifier
                      .size(16.dp)
                      .clip(CircleShape)
                      .background(Color.Red),
                    contentAlignment = Alignment.Center
                  ) {
                    Text("$downloadingCount", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Main workspace area
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
    ) {
      when (selectedTabIdx) {
        0 -> {
          // CHATS WORKSPACE
          Column {
            // Search field
            OutlinedTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              placeholder = { Text("Search spiritual rooms...", fontSize = 12.sp) },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
            )

            val filteredRooms = conversations.filter {
              it.name.contains(searchQuery, ignoreCase = true) || 
              (it.description != null && it.description.contains(searchQuery, ignoreCase = true))
            }

            if (filteredRooms.isEmpty()) {
              Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No spiritual channels found", color = Color.Gray, fontSize = 12.sp)
              }
            } else {
              LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
              ) {
                items(filteredRooms) { room ->
                  Card(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable { activeConversation = room },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                      1.dp,
                      MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    )
                  ) {
                    Row(
                      modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      // Avatar
                      Box(
                        modifier = Modifier
                          .size(44.dp)
                          .clip(CircleShape)
                          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                      ) {
                        Text(
                          text = if (room.isGroup) "🛕" else "🙏",
                          fontSize = 20.sp
                        )
                      }

                      Spacer(modifier = Modifier.width(12.dp))

                      // Room Text description
                      Column(modifier = Modifier.weight(1f)) {
                        Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceBetween,
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Text(
                            text = room.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                          )
                          Text(
                            text = if (room.isGroup) "Group" else "Private",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                          )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                          text = room.description ?: "Commence spiritual satsang chat.",
                          fontSize = 11.sp,
                          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                          maxLines = 1
                        )
                      }

                      // Badge unread
                      if (room.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                          modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                          contentAlignment = Alignment.Center
                        ) {
                          Text(
                            text = "${room.unreadCount}",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                          )
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
        
        1 -> {
          // STATUS STORIES WORKSPACE
          Column {
            // Post Status Card
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text("Express Daily Sadhana Status", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Button(
                    onClick = {
                      scope.launch {
                        viewModel.chatRepository.uploadStatus(
                          memberId = currentUserProfile?.id ?: "curr_user",
                          memberName = currentUserProfile?.fullName ?: "You",
                          type = StatusType.VANI,
                          content = "📿 Morning Jaap accomplished successfully at 04:30 AM! Boundless Serene Energy.",
                          mediaUrl = null
                        )
                        viewModel.showToast("Sadhana status uploaded for 24 hours!")
                      }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                  ) {
                    Text("Share Sadhana", fontSize = 11.sp)
                  }
                  
                  OutlinedButton(
                    onClick = {
                      scope.launch {
                        viewModel.chatRepository.uploadStatus(
                          memberId = currentUserProfile?.id ?: "curr_user",
                          memberName = currentUserProfile?.fullName ?: "You",
                          type = StatusType.PRAYER,
                          content = "🕉️ Sending healing prayers & pure positive vibrations to all ashram souls.",
                          mediaUrl = null
                        )
                        viewModel.showToast("Spiritual status shared!")
                      }
                    },
                    shape = RoundedCornerShape(12.dp)
                  ) {
                    Text("Share Prayer", fontSize = 11.sp)
                  }
                }
              }
            }

            // Feed
            Text("Recent Updates (expires in 24h)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              items(activeStatuses) { status ->
                Card(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewedStatus = status },
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                  Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(if (status.type == StatusType.VANI) "📜" else "🕉️")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Text(status.memberName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                      Text(status.content, fontSize = 11.sp, maxLines = 1, color = Color.Gray)
                    }
                    Text("View", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                  }
                }
              }
            }
          }
        }
        
        2 -> {
          // BROADCAST WORKSPACE
          Column {
            // Admin only composer
            val isAdmin = currentUserProfile?.roles?.any { it.name == "Admin" } == true
            if (isAdmin) {
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
              ) {
                var title by remember { mutableStateOf("") }
                var text by remember { mutableStateOf("") }
                
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Text("Admin Broadcast Console", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                  OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Announcement Title") }, modifier = Modifier.fillMaxWidth())
                  OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Broadcast Body Message") }, modifier = Modifier.fillMaxWidth())
                  Button(
                    onClick = {
                      if (title.isNotEmpty() && text.isNotEmpty()) {
                        scope.launch {
                          viewModel.chatRepository.sendBroadcast(title, text, emptyList())
                        }
                        title = ""
                        text = ""
                        viewModel.showToast("Broadcast transmitted successfully to all channels!")
                      }
                    },
                    modifier = Modifier.align(Alignment.End)
                  ) {
                    Text("Transmit Announcement")
                  }
                }
              }
            }

            // List of Broadcasts
            Text("Official Ashram Announcements", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
            if (activeBroadcasts.isEmpty()) {
              Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Text("No official announcements yet.", fontSize = 11.sp, color = Color.Gray)
              }
            } else {
              LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(activeBroadcasts) { b ->
                  Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                  ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                      Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(b.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("🚨 Admin", fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                      }
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(b.content, fontSize = 12.sp)
                    }
                  }
                }
              }
            }
          }
        }
        
        3 -> {
          // DOWNLOADS QUEUE WORKSPACE
          Column {
            Text("Sadhana Audio & PDF Books Download Manager", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
            
            // Add static seed downloads to list if queue is empty so user can test the UI nicely
            val activeQueue = if (downloadQueue.isEmpty()) {
              listOf(
                ChatDownloadTask("dl_static_1", "Siddhant_Vani_Full.pdf", 4500000L, 1.0f, DownloadStatus.COMPLETED, ""),
                ChatDownloadTask("dl_static_2", "Sunderkand_Morning_Chant.mp3", 12400000L, 0.45f, DownloadStatus.DOWNLOADING, "")
              )
            } else {
              downloadQueue
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              items(activeQueue) { task ->
                Card(
                  modifier = Modifier.fillMaxWidth(),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                  Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = if (task.status == DownloadStatus.COMPLETED) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                      contentDescription = null,
                      tint = if (task.status == DownloadStatus.COMPLETED) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Text(task.fileName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                      Text("Size: ${(task.size / 1024) / 1024} MB • Progress: ${(task.progress * 100).toInt()}%", fontSize = 10.sp, color = Color.Gray)
                      LinearProgressIndicator(progress = task.progress, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    if (task.status == DownloadStatus.DOWNLOADING) {
                      IconButton(onClick = { scope.launch { viewModel.chatRepository.pauseDownload(task.id) } }) {
                        Icon(Icons.Default.Pause, contentDescription = null)
                      }
                    } else if (task.status == DownloadStatus.PAUSED) {
                      IconButton(onClick = { scope.launch { viewModel.chatRepository.resumeDownload(task.id) } }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                      }
                    }
                  }
                }
              }
            }
          }
        }
        
        4 -> {
          // BOOKMARKED / STARRED MESSAGES WORKSPACE
          Column {
            Text("Your Saved Devotional Messages", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
            if (favoritedMessages.isEmpty()) {
              Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                Text("No starred verses or messages yet.", fontSize = 11.sp, color = Color.Gray)
              }
            } else {
              LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(favoritedMessages) { msg ->
                  Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                  ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                      Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(msg.senderName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("⭐ Starred", fontSize = 9.sp, color = Color(0xFFFFB300), fontWeight = FontWeight.Bold)
                      }
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(msg.content, fontSize = 12.sp)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Group creation dialog
  if (showCreateGroupDialog) {
    AlertDialog(
      onDismissRequest = { showCreateGroupDialog = false },
      title = { Text("Assemble New Channel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = newGroupName,
            onValueChange = { newGroupName = it },
            label = { Text("Channel Group Name") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = newGroupDesc,
            onValueChange = { newGroupDesc = it },
            label = { Text("Spiritual Objective / Description") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newGroupName.isNotEmpty()) {
              scope.launch {
                viewModel.chatRepository.createGroup(
                  name = newGroupName,
                  description = newGroupDesc,
                  creatorId = "curr_user",
                  members = listOf("member_1", "member_4")
                )
                viewModel.showToast("Holy assembly channel \"$newGroupName\" activated!")
              }
              newGroupName = ""
              newGroupDesc = ""
              showCreateGroupDialog = false
            }
          }
        ) {
          Text("Activate Channel")
        }
      }
    )
  }

  // Status Story Viewer popup
  if (viewedStatus != null) {
    val status = viewedStatus!!
    AlertDialog(
      onDismissRequest = { viewedStatus = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Text("📿")
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(status.memberName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("Active Sadhana Story", fontSize = 10.sp, color = Color.Gray)
          }
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
              .padding(16.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = status.content,
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold,
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.onBackground
            )
          }
          
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("👀 ${status.views.size + 1} views", fontSize = 11.sp, color = Color.Gray)
            
            // Send quick spiritual status reaction
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              val quickReactions = listOf("🙏", "🌸", "❤️")
              quickReactions.forEach { emo ->
                Box(
                  modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .clickable {
                      scope.launch {
                        viewModel.chatRepository.reactToStatus(status.id, emo)
                        viewModel.showToast("Blessed with $emo !")
                      }
                      viewedStatus = null
                    }
                    .padding(6.dp)
                ) {
                  Text(emo, fontSize = 12.sp)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { viewedStatus = null }) {
          Text("Close Status")
        }
      }
    )
  }
}
