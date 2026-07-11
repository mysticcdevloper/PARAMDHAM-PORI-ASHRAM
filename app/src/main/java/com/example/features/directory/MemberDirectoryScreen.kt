package com.example.features.directory

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.models.MemberProfile
import com.example.core.models.MemberStatus
import com.example.core.models.OnlineStatus
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.RoleBadge
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDirectoryScreen(
  viewModel: AppViewModel
) {
  val allMembers by viewModel.allMembers.collectAsState()

  var searchQuery by remember { mutableStateOf("") }
  var selectedCityFilter by remember { mutableStateOf("") }
  var selectedStatusFilter by remember { mutableStateOf<MemberStatus?>(null) }
  var selectedOnlineFilter by remember { mutableStateOf<OnlineStatus?>(null) }
  var isFilterSheetOpen by remember { mutableStateOf(false) }

  // Extract unique cities
  val cities = remember(allMembers) {
    allMembers.map { it.city }.distinct().sorted()
  }

  // Filter list
  val filteredMembers = remember(allMembers, searchQuery, selectedCityFilter, selectedStatusFilter, selectedOnlineFilter) {
    allMembers.filter { member ->
      val matchesSearch = member.fullName.contains(searchQuery, ignoreCase = true) ||
          member.roles.any { it.name.contains(searchQuery, ignoreCase = true) }
      val matchesCity = selectedCityFilter.isEmpty() || member.city == selectedCityFilter
      val matchesStatus = selectedStatusFilter == null || member.status == selectedStatusFilter
      val matchesOnline = selectedOnlineFilter == null || member.onlineStatus == selectedOnlineFilter
      matchesSearch && matchesCity && matchesStatus && matchesOnline
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp, vertical = 12.dp)
      .testTag("member_directory_root")
  ) {
    Text(
      text = "Spiritual Seeker Directory",
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      fontFamily = FontFamily.Serif,
      modifier = Modifier.padding(bottom = 4.dp)
    )
    Text(
      text = "Connect with approved Ashram disciples, trustees and volunteers",
      fontSize = 11.sp,
      color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
      modifier = Modifier.padding(bottom = 12.dp)
    )

    // Search bar and filter button
    Row(
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search by name, role...", fontSize = 13.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
          }
        },
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        ),
        modifier = Modifier.weight(1f)
      )

      Spacer(modifier = Modifier.width(8.dp))

      IconButton(
        onClick = { isFilterSheetOpen = true },
        modifier = Modifier
          .size(48.dp)
          .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
      ) {
        Icon(
          imageVector = Icons.Default.FilterList,
          contentDescription = "Filters",
          tint = MaterialTheme.colorScheme.primary
        )
      }
    }

    // Active filters horizontal tags
    if (selectedCityFilter.isNotEmpty() || selectedStatusFilter != null || selectedOnlineFilter != null) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (selectedCityFilter.isNotEmpty()) {
          FilterChip(
            selected = true,
            onClick = { selectedCityFilter = "" },
            label = { Text("City: $selectedCityFilter", fontSize = 10.sp) },
            trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp)) }
          )
        }
        if (selectedStatusFilter != null) {
          FilterChip(
            selected = true,
            onClick = { selectedStatusFilter = null },
            label = { Text("Status: ${selectedStatusFilter?.name}", fontSize = 10.sp) },
            trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp)) }
          )
        }
        if (selectedOnlineFilter != null) {
          FilterChip(
            selected = true,
            onClick = { selectedOnlineFilter = null },
            label = { Text("Online: ${selectedOnlineFilter?.name}", fontSize = 10.sp) },
            trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp)) }
          )
        }
      }
    }

    // Members list
    if (filteredMembers.isEmpty()) {
      Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("🕉️", fontSize = 48.sp)
          Spacer(modifier = Modifier.height(8.dp))
          Text("No ashram members match your filters.", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
        }
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.weight(1f)
      ) {
        items(filteredMembers) { member ->
          MemberItemCard(member = member)
        }
      }
    }
  }

  // Bottom Filter Selection Panel simulation
  if (isFilterSheetOpen) {
    AlertDialog(
      onDismissRequest = { isFilterSheetOpen = false },
      title = { Text("Filter Directory", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          // City filter
          Column {
            Text("By City:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = { selectedCityFilter = "" },
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedCityFilter.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
              ) {
                Text("All Cities", fontSize = 10.sp, color = if (selectedCityFilter.isEmpty()) Color.White else MaterialTheme.colorScheme.onSurface)
              }
              cities.forEach { city ->
                val isSelected = selectedCityFilter == city
                Button(
                  onClick = { selectedCityFilter = city },
                  colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier.height(32.dp)
                ) {
                  Text(city, fontSize = 10.sp, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                }
              }
            }
          }

          // Online filter
          Column {
            Text("By Online Status:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = { selectedOnlineFilter = null },
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedOnlineFilter == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
              ) {
                Text("Any", fontSize = 10.sp, color = if (selectedOnlineFilter == null) Color.White else MaterialTheme.colorScheme.onSurface)
              }
              OnlineStatus.values().forEach { os ->
                val isSelected = selectedOnlineFilter == os
                Button(
                  onClick = { selectedOnlineFilter = os },
                  colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier.height(32.dp)
                ) {
                  Text(os.name, fontSize = 10.sp, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                }
              }
            }
          }

          // Approval Status filter
          Column {
            Text("By Member Status:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = { selectedStatusFilter = null },
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedStatusFilter == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
              ) {
                Text("Any", fontSize = 10.sp, color = if (selectedStatusFilter == null) Color.White else MaterialTheme.colorScheme.onSurface)
              }
              MemberStatus.values().forEach { ms ->
                val isSelected = selectedStatusFilter == ms
                Button(
                  onClick = { selectedStatusFilter = ms },
                  colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier.height(32.dp)
                ) {
                  Text(ms.name, fontSize = 10.sp, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(onClick = { isFilterSheetOpen = false }) { Text("Apply Filters") }
      }
    )
  }
}

@Composable
fun MemberItemCard(member: MemberProfile) {
  val borderGradient = Brush.sweepGradient(
    colors = listOf(
      Color(0xFFFFD54F), // Gold
      Color(0xFFFFB300),
      Color(0xFFFFB300).copy(alpha = 0.1f),
      Color(0xFFFFD54F)
    )
  )

  // Status indicators color mappings
  val statusColor = when (member.onlineStatus) {
    OnlineStatus.ONLINE -> Color(0xFF4CAF50)
    OnlineStatus.OFFLINE -> Color(0xFF9E9E9E)
    OnlineStatus.BUSY -> Color(0xFFF44336)
    OnlineStatus.DND -> Color(0xFFFF9800)
    OnlineStatus.INVISIBLE -> Color(0x00FFFFFF)
  }

  GlassmorphicCard(
    modifier = Modifier.fillMaxWidth(),
    borderColor = if (member.status == MemberStatus.APPROVED) Color(0xFFFFB300).copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.2f)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Avatar with Online Status Badge
      Box(
        modifier = Modifier.size(54.dp),
        contentAlignment = Alignment.BottomEnd
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .border(2.dp, borderGradient, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (member.gender == "Female") "🌸" else "🙏",
            fontSize = 24.sp
          )
        }

        // Online status dot
        Box(
          modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(Color.White)
            .padding(2.dp)
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
              .background(statusColor)
          )
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = member.fullName,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          
          if (member.status == MemberStatus.APPROVED) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Verified Seeker",
              tint = Color(0xFFFFB300),
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Text(
          text = "${member.city}, ${member.state} • Since ${member.memberSince}",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )

        // Display multiple badges in a wrapping row
        Row(
          modifier = Modifier.fillMaxWidth().padding(top = 6.dp).horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          member.roles.forEach { role ->
            RoleBadge(role = role)
          }
        }

        // Typing indicators or custom sub text
        if (member.isTyping) {
          Text(
            text = "✍️ Typing a holy text...",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp)
          )
        }
      }
    }
  }
}
