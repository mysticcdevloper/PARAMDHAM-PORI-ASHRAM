package com.example.features.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
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
import com.example.core.constants.AppConstants.AppLanguage
import com.example.ui.components.GlassmorphicCard

data class AshramChatRoom(
  val id: String,
  val name: String,
  val lastMessage: String,
  val time: String,
  val unreadCount: Int,
  val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
  language: AppLanguage
) {
  var searchQuery by remember { mutableStateOf("") }
  
  val rooms = listOf(
    AshramChatRoom(
      "1",
      "Daily Sunderkand Reciters ✨",
      "Pranam, tomorrow's chanting will begin at 05:00 AM sharp.",
      "10:30 AM",
      4,
      "Sadhana"
    ),
    AshramChatRoom(
      "2",
      "Siddhant Vani Scholar Circle",
      "What is the deeper inner meaning of Verse 12 in Chapter 1?",
      "09:15 AM",
      0,
      "Study"
    ),
    AshramChatRoom(
      "3",
      "Ashram Seva Volunteers",
      "Mahadev Pranami is coordinating the free medical camp setup.",
      "Yesterday",
      2,
      "Seva"
    ),
    AshramChatRoom(
      "4",
      "General Satsang Announcements",
      "Official: Special Guru Purnima invitation circular is uploaded.",
      "Yesterday",
      0,
      "Official"
    ),
    AshramChatRoom(
      "5",
      "Daily Prarthana Chanting Group",
      "Seva Ji completed 5 malas of chanting. Hariom!",
      "2 days ago",
      12,
      "Sadhana"
    )
  )

  val filteredRooms = rooms.filter {
    it.name.contains(searchQuery, ignoreCase = true) || 
    it.lastMessage.contains(searchQuery, ignoreCase = true)
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp, vertical = 12.dp)
      .testTag("chats_screen_content")
  ) {
    Text(
      text = "Spiritual Communion Channels",
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.padding(bottom = 12.dp)
    )

    // Search bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search spiritual rooms...", fontSize = 13.sp) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp)
    )

    // Chat room list
    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.weight(1f)
    ) {
      items(filteredRooms) { room ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { },
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(20.dp),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
          )
        ) {
          Row(
            modifier = Modifier
              .padding(16.dp)
              .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Group Avatar
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Group,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text info
            Column(modifier = Modifier.weight(1f)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = room.name,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                  text = room.time,
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                )
              }

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = room.lastMessage,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                maxLines = 1
              )

              Spacer(modifier = Modifier.height(6.dp))

              // Room category badge
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                  .padding(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text(
                  text = room.category.uppercase(),
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }

            // Unread Badge
            if (room.unreadCount > 0) {
              Spacer(modifier = Modifier.width(8.dp))
              Box(
                modifier = Modifier
                  .size(20.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "${room.unreadCount}",
                  color = Color.White,
                  fontSize = 10.sp,
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
