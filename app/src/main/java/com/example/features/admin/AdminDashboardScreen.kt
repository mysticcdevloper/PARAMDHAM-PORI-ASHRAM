package com.example.features.admin

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.models.*
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.RoleBadge
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
  viewModel: AppViewModel
) {
  val allMembers by viewModel.allMembers.collectAsState()
  val inviteCodes by viewModel.inviteCodes.collectAsState()
  val activityLogs by viewModel.activityLogs.collectAsState()

  var selectedSection by remember { mutableStateOf(0) } // 0 = Members, 1 = Invite Generator, 2 = Audit Logs

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 12.dp)
      .testTag("admin_dashboard_root")
  ) {
    Text(
      text = "Ashram Administration Console",
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      fontFamily = FontFamily.Serif
    )
    Text(
      text = "Manage pending approvals, roles, invite codes, and system compliance",
      fontSize = 11.sp,
      color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
      modifier = Modifier.padding(bottom = 16.dp)
    )

    // Top horizontal tabs
    TabRow(
      selectedTabIndex = selectedSection,
      containerColor = Color.Transparent,
      contentColor = MaterialTheme.colorScheme.primary,
      modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
      Tab(selected = selectedSection == 0, onClick = { selectedSection = 0 }) {
        Text("Disciples", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
      }
      Tab(selected = selectedSection == 1, onClick = { selectedSection = 1 }) {
        Text("Invites", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
      }
      Tab(selected = selectedSection == 2, onClick = { selectedSection = 2 }) {
        Text("Audit Logs", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
      }
    }

    when (selectedSection) {
      0 -> MembersManagementTab(viewModel = viewModel, members = allMembers)
      1 -> InviteGeneratorTab(viewModel = viewModel, inviteCodes = inviteCodes)
      2 -> AuditLogsTab(logs = activityLogs)
    }
  }
}

@Composable
fun MembersManagementTab(viewModel: AppViewModel, members: List<MemberProfile>) {
  var selectedMemberForAction by remember { mutableStateOf<MemberProfile?>(null) }
  var isStatusDialogOpen by remember { mutableStateOf(false) }
  var isRoleDialogOpen by remember { mutableStateOf(false) }

  // Action variables
  var actionStatusSelected by remember { mutableStateOf(MemberStatus.APPROVED) }
  var actionReasonInput by remember { mutableStateOf("") }
  val selectedRolesToAssign = remember { mutableStateListOf<SpiritualRole>() }

  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Seeker Accounts List (${members.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

      Button(
        onClick = {
          // Export CSV Simulation
          viewModel.showToast("CSV Compilation Complete! File exported: ashram_disciples_${System.currentTimeMillis() / 1000}.csv")
        },
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
        modifier = Modifier.height(32.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("CSV Export", fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
        }
      }
    }

    members.forEach { member ->
      GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = when (member.status) {
          MemberStatus.PENDING -> Color(0xFFFF9800).copy(alpha = 0.3f)
          MemberStatus.APPROVED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
          else -> Color.Red.copy(alpha = 0.2f)
        }
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(member.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                  modifier = Modifier
                    .background(
                      when (member.status) {
                        MemberStatus.PENDING -> Color(0xFFFF9800)
                        MemberStatus.APPROVED -> Color(0xFF4CAF50)
                        else -> Color(0xFFF44336)
                      }.copy(alpha = 0.15f),
                      RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = member.status.name,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (member.status) {
                      MemberStatus.PENDING -> Color(0xFFE65100)
                      MemberStatus.APPROVED -> Color(0xFF2E7D32)
                      else -> Color(0xFFC2185B)
                    }
                  )
                }
              }
              Text(member.email, fontSize = 11.sp, color = Color.Gray)
              Text("City: ${member.city} • Phone: ${member.phoneNumber}", fontSize = 11.sp, color = Color.Gray)
            }

            // Quick actions buttons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              IconButton(
                onClick = {
                  selectedMemberForAction = member
                  actionStatusSelected = member.status
                  isStatusDialogOpen = true
                },
                modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
              ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = "Status Change", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
              }

              IconButton(
                onClick = {
                  selectedMemberForAction = member
                  selectedRolesToAssign.clear()
                  selectedRolesToAssign.addAll(member.roles)
                  isRoleDialogOpen = true
                },
                modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape)
              ) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = "Roles Change", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            member.roles.forEach { role ->
              RoleBadge(role = role)
            }
          }
        }
      }
    }
  }

  // STATUS DIALOG
  if (isStatusDialogOpen && selectedMemberForAction != null) {
    AlertDialog(
      onDismissRequest = { isStatusDialogOpen = false },
      title = { Text("Update Seeker Status", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Modify status for ${selectedMemberForAction!!.fullName}:", fontSize = 12.sp)

          Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MemberStatus.values().forEach { ms ->
              val isSelected = actionStatusSelected == ms
              Button(
                onClick = { actionStatusSelected = ms },
                colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
              ) {
                Text(ms.name, fontSize = 10.sp, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
              }
            }
          }

          OutlinedTextField(
            value = actionReasonInput,
            onValueChange = { actionReasonInput = it },
            label = { Text("Reason / Review Comments") },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.approveRejectMember(selectedMemberForAction!!.id, actionStatusSelected, actionReasonInput)
            isStatusDialogOpen = false
            actionReasonInput = ""
          }
        ) {
          Text("Commit Status")
        }
      },
      dismissButton = {
        TextButton(onClick = { isStatusDialogOpen = false }) { Text("Cancel") }
      }
    )
  }

  // ROLES DIALOG
  if (isRoleDialogOpen && selectedMemberForAction != null) {
    AlertDialog(
      onDismissRequest = { isRoleDialogOpen = false },
      title = { Text("Assign Spiritual Badges", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Select roles for ${selectedMemberForAction!!.fullName}:", fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))

          Box(modifier = Modifier.height(200.dp).verticalScroll(rememberScrollState())) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              SpiritualRole.ALL_ROLES.forEach { role ->
                val isChecked = selectedRolesToAssign.any { it.name == role.name }
                Row(
                  modifier = Modifier.fillMaxWidth().clickable {
                    if (isChecked) selectedRolesToAssign.removeIf { it.name == role.name }
                    else selectedRolesToAssign.add(role)
                  },
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Checkbox(
                    checked = isChecked,
                    onCheckedChange = { checked ->
                      if (checked) selectedRolesToAssign.add(role)
                      else selectedRolesToAssign.removeIf { it.name == role.name }
                    }
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(role.icon, fontSize = 14.sp)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(role.name, fontSize = 12.sp)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.assignMemberRoles(selectedMemberForAction!!.id, selectedRolesToAssign.toList())
            isRoleDialogOpen = false
          }
        ) {
          Text("Update Badges")
        }
      },
      dismissButton = {
        TextButton(onClick = { isRoleDialogOpen = false }) { Text("Cancel") }
      }
    )
  }
}

@Composable
fun InviteGeneratorTab(viewModel: AppViewModel, inviteCodes: List<InviteCode>) {
  var codeName by remember { mutableStateOf("") }
  var expiryDays by remember { mutableStateOf(7) }
  var usageLimit by remember { mutableStateOf(5) }
  var selectedRoleConstraint by remember { mutableStateOf<SpiritualRole?>(null) }
  var isRoleDropdownOpen by remember { mutableStateOf(false) }

  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    // Code Creator Form
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Generate Invite Keys", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
          value = codeName,
          onValueChange = { codeName = it },
          label = { Text("Invite Code Name (e.g. SANTS26)") },
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
            value = expiryDays.toString(),
            onValueChange = { expiryDays = it.toIntOrNull() ?: 7 },
            label = { Text("Expires (Days)") },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f).padding(end = 4.dp)
          )

          OutlinedTextField(
            value = usageLimit.toString(),
            onValueChange = { usageLimit = it.toIntOrNull() ?: 5 },
            label = { Text("Max Uses") },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f).padding(start = 4.dp)
          )
        }

        // Role restriction selection
        Column {
          Text("Role Restriction:", fontSize = 11.sp, color = Color.Gray)
          Row(
            modifier = Modifier.fillMaxWidth().clickable { isRoleDropdownOpen = true }.padding(top = 4.dp).border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(10.dp)).padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(selectedRoleConstraint?.name ?: "None - Standard Guest Role", fontSize = 13.sp)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
          }
        }

        Button(
          onClick = {
            if (codeName.isNotEmpty()) {
              viewModel.generateInviteCode(codeName, expiryDays, usageLimit, selectedRoleConstraint)
              codeName = ""
            } else {
              viewModel.showToast("Please enter an invite code.")
            }
          },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Create Secure Code", fontWeight = FontWeight.Bold)
        }
      }
    }

    // Existing active code tables
    Text("Active Invitations", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

    inviteCodes.forEach { invite ->
      GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(invite.code, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Text("Assigned Role: ${invite.roleRestriction?.name ?: "Standard Seeker"}", fontSize = 11.sp, color = Color.Gray)
          }

          Column(horizontalAlignment = Alignment.End) {
            Text("Uses: ${invite.usesCount} / ${invite.maxUses}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(if (invite.isExpired) "Expired" else "Active", fontSize = 10.sp, color = if (invite.isExpired) Color.Red else Color(0xFF4CAF50))
          }
        }
      }
    }
  }

  // Custom role selection sheet simulator
  if (isRoleDropdownOpen) {
    AlertDialog(
      onDismissRequest = { isRoleDropdownOpen = false },
      title = { Text("Restrict to Role", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
      text = {
        Box(modifier = Modifier.height(200.dp).verticalScroll(rememberScrollState())) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Option for No restrict
            Row(
              modifier = Modifier.fillMaxWidth().clickable {
                selectedRoleConstraint = null
                isRoleDropdownOpen = false
              }.padding(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("None - Standard Guest Seeker", fontSize = 12.sp)
            }
            SpiritualRole.ALL_ROLES.forEach { role ->
              Row(
                modifier = Modifier.fillMaxWidth().clickable {
                  selectedRoleConstraint = role
                  isRoleDropdownOpen = false
                }.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(role.icon, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(role.name, fontSize = 12.sp)
              }
            }
          }
        }
      },
      confirmButton = {}
    )
  }
}

@Composable
fun AuditLogsTab(logs: List<ActivityLog>) {
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Text("Administrative Activity Audit Trail", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

    logs.forEach { log ->
      GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(log.action, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            Text("Actor: ${log.actorName}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(log.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
        }
      }
    }
  }
}
