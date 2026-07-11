package com.example.core.models

import androidx.compose.ui.graphics.Color

enum class MemberStatus {
  PENDING,
  APPROVED,
  REJECTED,
  SUSPENDED,
  BLOCKED
}

enum class OnlineStatus {
  ONLINE,
  OFFLINE,
  BUSY,
  DND,
  INVISIBLE
}

data class SpiritualRole(
  val name: String,
  val icon: String,
  val badgeColorHex: String,
  val borderColorHex: String,
  val animationType: String = "pulse",
  val description: String = ""
) {
  val badgeColor: Color get() = Color(android.graphics.Color.parseColor(badgeColorHex))
  val borderColor: Color get() = Color(android.graphics.Color.parseColor(borderColorHex))

  companion object {
    val ADMIN = SpiritualRole("Admin", "👑", "#FF5722", "#FFD54F", "glow", "Complete application and ashram override control.")
    val MAIN_SANCHALAK = SpiritualRole("Main Sanchalak", "🛕", "#E65100", "#FFD54F", "pulse", "Satsang and live audio/video sabha coordinator.")
    val ADHYAKSH = SpiritualRole("Adhyaksh", "🏛️", "#1A237E", "#FFB300", "scale", "Ashram president with announcement management rights.")
    val UPADHYAKSH = SpiritualRole("Upadhyaksh", "🌿", "#2E7D32", "#A5D6A7", "fade", "Vice president supporting all admin activities.")
    val DHARMA_PRACHARAK = SpiritualRole("Dharma Pracharak", "📖", "#D84315", "#FFE082", "shimmer", "Authorized spiritual speaker. Can upload Pravachan, texts.")
    val SACHIV = SpiritualRole("Sachiv", "📜", "#4E342E", "#BCAAA4", "none", "General secretary maintaining records and directory.")
    val SAH_SACHIV = SpiritualRole("Sah Sachiv", "📂", "#37474F", "#B0BEC5", "none", "Joint secretary assist.")
    val KOSHADHYAKSH = SpiritualRole("Koshadhyaksh", "💰", "#2E7D32", "#FFD54F", "scale", "Treasurer managing ashram donations and balance books.")
    val VYAVASTHAPAK = SpiritualRole("Vyavasthapak", "⚙️", "#424242", "#E0E0E0", "none", "Logistics and physical ashram operations manager.")
    val MEDIA_TEAM = SpiritualRole("Media Team", "📷", "#00838F", "#80DEEA", "rotate", "Allowed to upload satsang photographs, clips, and event videos.")
    val VOLUNTEER = SpiritualRole("Volunteer", "🙏", "#F57C00", "#FFCC80", "pulse", "Helper responsible for active Seva event management.")
    val MAHILA_MANDAL = SpiritualRole("Mahila Mandal", "🌸", "#C2185B", "#F48FB1", "fade", "Women spiritual and cultural events organizer.")
    val YUVA_MANDAL = SpiritualRole("Yuva Mandal", "🔥", "#D84315", "#FFAB91", "shimmer", "Youth coordinator driving energetic youth sabhas.")
    val VERIFIED_MEMBER = SpiritualRole("Verified Member", "⭐", "#FFB300", "#FFFFFF", "none", "Regular verified Ashram member with standard access.")
    val SENIOR_MEMBER = SpiritualRole("Senior Member", "🎖️", "#9C27B0", "#E1BEE7", "glow", "Senior ashram follower with enhanced social standing.")
    val GUEST = SpiritualRole("Guest", "🕉️", "#757575", "#B0BEC5", "none", "Temporary ashram visitor with read-only access.")

    val ALL_ROLES = listOf(
      ADMIN, MAIN_SANCHALAK, ADHYAKSH, UPADHYAKSH, DHARMA_PRACHARAK,
      SACHIV, SAH_SACHIV, KOSHADHYAKSH, VYAVASTHAPAK, MEDIA_TEAM,
      VOLUNTEER, MAHILA_MANDAL, YUVA_MANDAL, VERIFIED_MEMBER,
      SENIOR_MEMBER, GUEST
    )

    fun fromName(name: String): SpiritualRole {
      return ALL_ROLES.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: GUEST
    }
  }
}

data class MemberProfile(
  val id: String,
  val fullName: String,
  val phoneNumber: String,
  val email: String,
  val gender: String,
  val dob: String,
  val city: String,
  val state: String,
  val country: String = "India",
  val ashramMemberId: String? = null,
  val occupation: String? = null,
  val bio: String,
  val emergencyContact: String,
  val profilePhotoUrl: String? = null,
  val status: MemberStatus = MemberStatus.PENDING,
  val roles: List<SpiritualRole> = listOf(SpiritualRole.GUEST),
  val onlineStatus: OnlineStatus = OnlineStatus.OFFLINE,
  val lastSeen: Long = System.currentTimeMillis(),
  val isTyping: Boolean = false,
  val memberSince: String = "July 2026",
  val participationLevel: Int = 1, // 1 to 5 stars
  val achievements: List<String> = emptyList()
) {
  fun hasPermission(permission: String): Boolean {
    if (roles.contains(SpiritualRole.ADMIN)) return true
    return when (permission) {
      "manage_meetings" -> roles.contains(SpiritualRole.MAIN_SANCHALAK)
      "create_announcements" -> roles.contains(SpiritualRole.ADHYAKSH)
      "upload_pravachan" -> roles.contains(SpiritualRole.DHARMA_PRACHARAK)
      "upload_media" -> roles.contains(SpiritualRole.MEDIA_TEAM)
      "manage_seva" -> roles.contains(SpiritualRole.VOLUNTEER)
      "read_content" -> true
      "post_chats" -> status == MemberStatus.APPROVED
      else -> false
    }
  }
}

data class InviteCode(
  val id: String,
  val code: String,
  val createdBy: String,
  val expiryDate: Long,
  val maxUses: Int,
  val usesCount: Int = 0,
  val roleRestriction: SpiritualRole? = null
) {
  val isExpired: Boolean get() = System.currentTimeMillis() > expiryDate
  val isExceeded: Boolean get() = usesCount >= maxUses
}

data class ApprovalRequest(
  val id: String,
  val profileId: String,
  val fullName: String,
  val phone: String,
  val requestedAt: Long = System.currentTimeMillis(),
  var status: MemberStatus = MemberStatus.PENDING,
  var rejectionReason: String? = null
)

data class DeviceSession(
  val id: String,
  val deviceName: String,
  val osVersion: String,
  val lastActive: Long = System.currentTimeMillis()
)

data class PrivacySettings(
  val showPhoneNumber: Boolean = false,
  val showProfilePhoto: Boolean = true,
  val showLastSeen: Boolean = true,
  val showStatus: Boolean = true,
  val showEmail: Boolean = false
)

data class RealtimeNotification(
  val id: String,
  val title: String,
  val message: String,
  val category: String, // 'approval', 'role_changed', 'invite', 'system'
  val isRead: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)

data class ActivityLog(
  val id: String,
  val actorName: String,
  val action: String,
  val description: String,
  val timestamp: Long = System.currentTimeMillis()
)
