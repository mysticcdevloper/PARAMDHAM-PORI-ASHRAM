package com.example.core.repositories

import android.util.Log
import com.example.core.config.SupabaseConfig
import com.example.core.models.*
import com.example.core.api.SupabaseApi
import com.example.core.api.DbMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.collect

// Keep previous models to prevent breakages, mapping where needed
data class SupabaseMessage(
  val id: String,
  val roomId: String,
  val senderId: String,
  val senderName: String,
  val content: String,
  val timestamp: Long = System.currentTimeMillis()
)

data class SupabaseSabha(
  val id: String,
  val title: String,
  val speaker: String,
  val description: String,
  val isLive: Boolean,
  val scheduledTime: Long,
  val meetingUrl: String? = null
)

data class SupabaseBook(
  val id: String,
  val title: String,
  val author: String,
  val coverUrl: String? = null,
  val description: String
)

interface SupabaseDbRepository {
  fun observeMessages(roomId: String): Flow<List<SupabaseMessage>>
  suspend fun sendMessage(roomId: String, senderId: String, senderName: String, content: String): Result<SupabaseMessage>
  suspend fun fetchActiveSabhas(): Result<List<SupabaseSabha>>
  suspend fun fetchHolyBooks(): Result<List<SupabaseBook>>

  // --- Module 2: Member and Admin management interfaces ---
  suspend fun fetchAllMembers(): Result<List<MemberProfile>>
  suspend fun updateMemberStatus(profileId: String, status: MemberStatus, reviewerId: String, reason: String? = null): Result<Unit>
  suspend fun updateMemberRoles(profileId: String, roles: List<SpiritualRole>): Result<Unit>
  suspend fun generateInviteCode(code: String, createdBy: String, expiry: Long, maxUses: Int, role: SpiritualRole?): Result<InviteCode>
  suspend fun fetchInviteCodes(): Result<List<InviteCode>>
  suspend fun verifyInviteCode(code: String): Result<InviteCode>
  suspend fun fetchNotifications(profileId: String): Result<List<RealtimeNotification>>
  suspend fun sendSystemNotification(profileId: String, title: String, message: String, category: String): Result<Unit>
  suspend fun fetchActivityLogs(): Result<List<ActivityLog>>
  suspend fun addActivityLog(actorId: String, actorName: String, action: String, description: String): Result<Unit>
}

class SupabaseDbRepositoryImpl : SupabaseDbRepository {
  private val _messages = MutableStateFlow<List<SupabaseMessage>>(emptyList())
  
  // Simulated relational storage for Member Management Module
  private val _members = MutableStateFlow<List<MemberProfile>>(emptyList())
  private val _inviteCodes = MutableStateFlow<List<InviteCode>>(emptyList())
  private val _notifications = MutableStateFlow<List<RealtimeNotification>>(emptyList())
  private val _activityLogs = MutableStateFlow<List<ActivityLog>>(emptyList())

  init {
    if (SupabaseConfig.isConfigured()) {
      Log.d("SupabaseDb", "Database Repository Initialized in LIVE SUPABASE MODE.")
    } else {
      Log.d("SupabaseDb", "Database Repository Initialized in SANDBOX/DEMO FALLBACK MODE (No keys configured).")
    }
    seedData()
  }

  private fun seedData() {
    // Seed initial invite codes
    _inviteCodes.value = listOf(
      InviteCode("inv_1", "GURU77", "admin_user_uuid", System.currentTimeMillis() + 86400000 * 7, 5, 1, SpiritualRole.DHARMA_PRACHARAK),
      InviteCode("inv_2", "YOUTH2026", "admin_user_uuid", System.currentTimeMillis() + 86400000 * 3, 20, 4, SpiritualRole.YUVA_MANDAL),
      InviteCode("inv_3", "SEVA99", "admin_user_uuid", System.currentTimeMillis() - 3600000, 10, 10, SpiritualRole.VOLUNTEER) // Expired
    )

    // Seed diverse members with various roles, cities, and verification levels
    _members.value = listOf(
      MemberProfile(
        id = "member_1",
        fullName = "Mahadev Pranami",
        phoneNumber = "+919876500001",
        email = "mahadev.pranami@podhiashram.org",
        gender = "Male",
        dob = "1968-05-15",
        city = "Jamnagar",
        state = "Gujarat",
        bio = "Satsang path finder and spiritual server. Serving since 1995.",
        emergencyContact = "+919876500009",
        status = MemberStatus.APPROVED,
        roles = listOf(SpiritualRole.MAIN_SANCHALAK, SpiritualRole.DHARMA_PRACHARAK, SpiritualRole.SENIOR_MEMBER),
        onlineStatus = OnlineStatus.ONLINE,
        memberSince = "April 1998",
        participationLevel = 5,
        achievements = listOf("Sadhana Samman", "25 Years Seva Badge")
      ),
      MemberProfile(
        id = "member_2",
        fullName = "Aditi Vyas",
        phoneNumber = "+919876500002",
        email = "aditi.vyas@gmail.com",
        gender = "Female",
        dob = "1985-09-24",
        city = "Ahmedabad",
        state = "Gujarat",
        bio = "Active organizer of Podhi Ashram Mahila Mandal.",
        emergencyContact = "+919876500020",
        status = MemberStatus.APPROVED,
        roles = listOf(SpiritualRole.MAHILA_MANDAL, SpiritualRole.VERIFIED_MEMBER),
        onlineStatus = OnlineStatus.BUSY,
        memberSince = "June 2018",
        participationLevel = 4,
        achievements = listOf("Pravachan Coordinator")
      ),
      MemberProfile(
        id = "member_3",
        fullName = "Gaurav Shastri",
        phoneNumber = "+919876500003",
        email = "gaurav.shastri@outlook.com",
        gender = "Male",
        dob = "1972-12-05",
        city = "Haridwar",
        state = "Uttarakhand",
        bio = "Preacher of Shrimad Bhagwat and Tartam Vani.",
        emergencyContact = "+919876500030",
        status = MemberStatus.APPROVED,
        roles = listOf(SpiritualRole.DHARMA_PRACHARAK, SpiritualRole.ADHYAKSH),
        onlineStatus = OnlineStatus.OFFLINE,
        memberSince = "March 2010",
        participationLevel = 5,
        achievements = listOf("Spiritual Doctorate", "Veda Acharya")
      ),
      MemberProfile(
        id = "member_4",
        fullName = "Kabir Patel",
        phoneNumber = "+919876500004",
        email = "kabir.patel@podhiashram.org",
        gender = "Male",
        dob = "1997-03-30",
        city = "Surat",
        state = "Gujarat",
        bio = "Yuva Mandal lead and digital tech volunteer.",
        emergencyContact = "+919876500040",
        status = MemberStatus.APPROVED,
        roles = listOf(SpiritualRole.YUVA_MANDAL, SpiritualRole.MEDIA_TEAM, SpiritualRole.VOLUNTEER),
        onlineStatus = OnlineStatus.ONLINE,
        isTyping = true,
        memberSince = "July 2021",
        participationLevel = 4,
        achievements = listOf("Digital Pioneer", "Active Volunteer")
      ),
      MemberProfile(
        id = "member_5",
        fullName = "Rajeshwari Joshi",
        phoneNumber = "+919876500005",
        email = "rajeshwari@gmail.com",
        gender = "Female",
        dob = "1990-07-12",
        city = "Mumbai",
        state = "Maharashtra",
        bio = "Passionate seva worker with interest in spiritual chants.",
        emergencyContact = "+919876500050",
        status = MemberStatus.PENDING,
        roles = listOf(SpiritualRole.GUEST),
        onlineStatus = OnlineStatus.DND,
        memberSince = "July 2026",
        participationLevel = 1
      ),
      MemberProfile(
        id = "member_6",
        fullName = "Madan Lal Verma",
        phoneNumber = "+919876500006",
        email = "madanlal@yahoo.com",
        gender = "Male",
        dob = "1960-01-01",
        city = "Indore",
        state = "Madhya Pradesh",
        bio = "Devoted soul looking forward to active temple building seva.",
        emergencyContact = "+919876500060",
        status = MemberStatus.PENDING,
        roles = listOf(SpiritualRole.GUEST),
        onlineStatus = OnlineStatus.OFFLINE,
        memberSince = "July 2026",
        participationLevel = 1
      ),
      MemberProfile(
        id = "member_7",
        fullName = "Sanjay Koshadhyaksh",
        phoneNumber = "+919876500007",
        email = "finance.podhi@ashram.org",
        gender = "Male",
        dob = "1978-11-19",
        city = "Ahmedabad",
        state = "Gujarat",
        bio = "Managing temple financial records and donations book transparency.",
        emergencyContact = "+919876500070",
        status = MemberStatus.APPROVED,
        roles = listOf(SpiritualRole.KOSHADHYAKSH, SpiritualRole.SACHIV),
        onlineStatus = OnlineStatus.ONLINE,
        memberSince = "January 2015",
        participationLevel = 5,
        achievements = listOf("Finance Auditor")
      ),
      MemberProfile(
        id = "member_8",
        fullName = "Dharmesh Bhagat",
        phoneNumber = "+919876500008",
        email = "dharmesh@gmail.com",
        gender = "Male",
        dob = "1983-02-18",
        city = "Patan",
        state = "Gujarat",
        bio = "Violator of community chat guidelines. Suspended temporarily.",
        emergencyContact = "+919876500080",
        status = MemberStatus.SUSPENDED,
        roles = listOf(SpiritualRole.GUEST),
        onlineStatus = OnlineStatus.OFFLINE,
        memberSince = "February 2023",
        participationLevel = 2
      )
    )

    // Seed notifications
    _notifications.value = listOf(
      RealtimeNotification("n_1", "Welcome to Paramdham", "Your registration was successful. Waiting for admin approval.", "approval"),
      RealtimeNotification("n_2", "System Active", "Row Level Security (RLS) is fully active on your profile database.", "system")
    )

    // Seed activity logs
    _activityLogs.value = listOf(
      ActivityLog("l_1", "Acharya Dev", "ASSIGN_ROLE", "Assigned 'Main Sanchalak' role to Mahadev Pranami."),
      ActivityLog("l_2", "Acharya Dev", "APPROVE_MEMBER", "Approved membership request of Kabir Patel.")
    )
  }

  override fun observeMessages(roomId: String): Flow<List<SupabaseMessage>> = flow {
    if (SupabaseConfig.isConfigured()) {
      while (true) {
        try {
          Log.d("SupabaseDb", "Polling real-time messages from database...")
          val api = SupabaseApi.get()
          // Filter by conversation_id equals roomId
          val response = api.getMessages("eq.$roomId")
          if (response.isSuccessful && response.body() != null) {
            val mapped = response.body()!!.map { dbMsg ->
              SupabaseMessage(
                id = dbMsg.id ?: "msg_${System.currentTimeMillis()}",
                roomId = dbMsg.conversation_id,
                senderId = dbMsg.sender_id,
                senderName = dbMsg.sender_name,
                content = dbMsg.content,
                timestamp = System.currentTimeMillis()
              )
            }
            emit(mapped)
          }
        } catch (e: Exception) {
          Log.e("SupabaseDb", "Error polling messages: ${e.message}")
        }
        delay(4000)
      }
    } else {
      _messages.collect { emit(it) }
    }
  }

  override suspend fun sendMessage(
    roomId: String,
    senderId: String,
    senderName: String,
    content: String
  ): Result<SupabaseMessage> {
    if (SupabaseConfig.isConfigured()) {
      return try {
        Log.d("SupabaseDb", "Sending live message to database: $content")
        val api = SupabaseApi.get()
        val dbMsg = DbMessage(
          conversation_id = roomId,
          sender_id = senderId,
          sender_name = senderName,
          content = content
        )
        val response = api.insertMessage(dbMsg)
        if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
          val added = response.body()!!.first()
          Result.success(
            SupabaseMessage(
              id = added.id ?: "",
              roomId = added.conversation_id,
              senderId = added.sender_id,
              senderName = added.sender_name,
              content = added.content
            )
          )
        } else {
          Result.failure(Exception("Could not post message: ${response.errorBody()?.string()}"))
        }
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

    delay(300)
    val msg = SupabaseMessage(
      id = "msg_${System.currentTimeMillis()}",
      roomId = roomId,
      senderId = senderId,
      senderName = senderName,
      content = content
    )
    val current = _messages.value.toMutableList()
    current.add(msg)
    _messages.value = current
    return Result.success(msg)
  }

  override suspend fun fetchActiveSabhas(): Result<List<SupabaseSabha>> {
    delay(300)
    return Result.success(
      listOf(
        SupabaseSabha(
          id = "sabha_1",
          title = "Daily Evening Sunderkand Path",
          speaker = "Mahadev Pranami",
          description = "Chanting Sunderkand with sweet devotion & spiritual commentary.",
          isLive = true,
          scheduledTime = System.currentTimeMillis(),
          meetingUrl = "https://meet.jit.si/ParamdhamAshramSabha"
        )
      )
    )
  }

  override suspend fun fetchHolyBooks(): Result<List<SupabaseBook>> {
    delay(300)
    return Result.success(
      listOf(
        SupabaseBook("book_1", "Siddhant Vani", "Raj Shyam Ji Maharaj", null, "Siddhant Sagar bindings"),
        SupabaseBook("book_2", "Sunderkand Devotion", "Valmiki Rishi", null, "Sacred Epic Devotion")
      )
    )
  }

  // --- Module 2 Implementations ---

  override suspend fun fetchAllMembers(): Result<List<MemberProfile>> {
    delay(500)
    return Result.success(_members.value)
  }

  override suspend fun updateMemberStatus(
    profileId: String,
    status: MemberStatus,
    reviewerId: String,
    reason: String?
  ): Result<Unit> {
    delay(800)
    val currentMembers = _members.value.map {
      if (it.id == profileId) {
        val updated = it.copy(status = status)
        sendSystemNotification(
          profileId = profileId,
          title = "Membership Status Updated",
          message = "Your account status has been changed to: ${status.name}. Reason: ${reason ?: "N/A"}",
          category = "approval"
        )
        updated
      } else {
        it
      }
    }
    _members.value = currentMembers
    addActivityLog(reviewerId, "Admin / Reviewer", "UPDATE_STATUS", "Changed member status of $profileId to ${status.name}")
    return Result.success(Unit)
  }

  override suspend fun updateMemberRoles(profileId: String, roles: List<SpiritualRole>): Result<Unit> {
    delay(800)
    val currentMembers = _members.value.map {
      if (it.id == profileId) {
        val updated = it.copy(roles = roles)
        sendSystemNotification(
          profileId = profileId,
          title = "Roles Updated",
          message = "Your spiritual badges have been updated to: ${roles.joinToString { r -> r.icon + " " + r.name }}",
          category = "role_changed"
        )
        updated
      } else {
        it
      }
    }
    _members.value = currentMembers
    return Result.success(Unit)
  }

  override suspend fun generateInviteCode(
    code: String,
    createdBy: String,
    expiry: Long,
    maxUses: Int,
    role: SpiritualRole?
  ): Result<InviteCode> {
    delay(600)
    val newCode = InviteCode(
      id = "inv_${System.currentTimeMillis()}",
      code = code.uppercase(),
      createdBy = createdBy,
      expiryDate = expiry,
      maxUses = maxUses,
      roleRestriction = role
    )
    val currentCodes = _inviteCodes.value.toMutableList()
    currentCodes.add(newCode)
    _inviteCodes.value = currentCodes
    
    addActivityLog(createdBy, "Admin", "GENERATE_INVITE", "Created invite code ${code.uppercase()} restricted to ${role?.name ?: "No Role Limit"}")
    return Result.success(newCode)
  }

  override suspend fun fetchInviteCodes(): Result<List<InviteCode>> {
    delay(300)
    return Result.success(_inviteCodes.value)
  }

  override suspend fun verifyInviteCode(code: String): Result<InviteCode> {
    delay(500)
    val invite = _inviteCodes.value.firstOrNull { it.code.equals(code, ignoreCase = true) }
      ?: return Result.failure(Exception("The invite code you entered is invalid."))

    if (invite.isExpired) {
      return Result.failure(Exception("This invite code has expired."))
    }
    if (invite.isExceeded) {
      return Result.failure(Exception("This invite code has exceeded its maximum use limit."))
    }

    _inviteCodes.value = _inviteCodes.value.map {
      if (it.code.equals(code, ignoreCase = true)) {
        it.copy(usesCount = it.usesCount + 1)
      } else {
        it
      }
    }

    return Result.success(invite)
  }

  override suspend fun fetchNotifications(profileId: String): Result<List<RealtimeNotification>> {
    return Result.success(_notifications.value)
  }

  override suspend fun sendSystemNotification(
    profileId: String,
    title: String,
    message: String,
    category: String
  ): Result<Unit> {
    val updatedNotifications = _notifications.value.toMutableList()
    updatedNotifications.add(
      RealtimeNotification(
        id = "not_${System.currentTimeMillis()}",
        title = title,
        message = message,
        category = category
      )
    )
    _notifications.value = updatedNotifications
    return Result.success(Unit)
  }

  override suspend fun fetchActivityLogs(): Result<List<ActivityLog>> {
    return Result.success(_activityLogs.value)
  }

  override suspend fun addActivityLog(
    actorId: String,
    actorName: String,
    action: String,
    description: String
  ): Result<Unit> {
    val updatedLogs = _activityLogs.value.toMutableList()
    updatedLogs.add(
      ActivityLog(
        id = "log_${System.currentTimeMillis()}",
        actorName = actorName,
        action = action,
        description = description
      )
    )
    _activityLogs.value = updatedLogs
    return Result.success(Unit)
  }
}

