package com.example.core.models

import androidx.compose.ui.graphics.Color

enum class MessageType {
  TEXT,
  IMAGE,
  VIDEO,
  AUDIO,
  VOICE,
  DOCUMENT,
  STICKER,
  POLL
}

enum class MessageStatus {
  SENDING,
  DELIVERED,
  READ
}

data class MessageReaction(
  val emoji: String,
  val senderId: String,
  val senderName: String
)

data class ChatAttachment(
  val id: String,
  val name: String,
  val type: String, // "image", "video", "audio", "document", "voice"
  val url: String,
  val size: Long,
  val durationSec: Int? = null,
  val progress: Float = 1f,
  val isDownloading: Boolean = false,
  val isDownloaded: Boolean = false
)

data class PollOption(
  val id: String,
  val text: String,
  val votes: List<String> = emptyList() // List of memberIds who voted for this option
)

data class PollData(
  val id: String,
  val question: String,
  val options: List<PollOption>,
  val isAnonymous: Boolean,
  val isMultipleChoice: Boolean
)

data class ChatMessage(
  val id: String,
  val conversationId: String,
  val senderId: String,
  val senderName: String,
  val senderRoleIcon: String = "🙏",
  val content: String,
  val type: MessageType = MessageType.TEXT,
  val status: MessageStatus = MessageStatus.READ,
  val timestamp: Long = System.currentTimeMillis(),
  val isEncrypted: Boolean = true,
  val repliedToId: String? = null,
  val repliedToText: String? = null,
  val repliedToSender: String? = null,
  val isForwarded: Boolean = false,
  val isPinned: Boolean = false,
  val isStarred: Boolean = false,
  val reactions: List<MessageReaction> = emptyList(),
  val attachment: ChatAttachment? = null,
  val poll: PollData? = null,
  val editHistory: List<String> = emptyList(),
  val scheduledTime: Long? = null
)

data class ChatConversation(
  val id: String,
  val name: String,
  val description: String? = null,
  val isGroup: Boolean = false,
  val avatarUrl: String? = null,
  val unreadCount: Int = 0,
  val isMuted: Boolean = false,
  val pinnedMessages: List<String> = emptyList(), // IDs of pinned messages
  val inviteLink: String? = null,
  val qrCodeText: String? = null,
  val groupMembers: List<String> = emptyList(), // IDs of member profiles
  val groupAdmins: List<String> = emptyList(), // IDs of admins
  val mutedMembers: List<String> = emptyList(), // IDs of muted members
  val createdBy: String? = null,
  val createdAt: Long = System.currentTimeMillis()
)

data class SpiritualSticker(
  val id: String,
  val name: String,
  val emojiRepresentation: String,
  val localizedName: String
)

enum class ChatWallpaper {
  GOLDEN_GLOW,
  HOLY_TEMPLE,
  LOTUS_TEMPLE,
  DEVOTIONAL_FESTIVAL,
  DARK_COSMIC,
  DEFAULT_SLATE
}

enum class StatusType {
  TEXT,
  PHOTO,
  VIDEO,
  AUDIO,
  PRAYER,
  VANI
}

data class StatusView(
  val viewerId: String,
  val viewerName: String,
  val timestamp: Long = System.currentTimeMillis()
)

data class MemberStatusUpdate(
  val id: String,
  val memberId: String,
  val memberName: String,
  val memberPhotoUrl: String? = null,
  val type: StatusType,
  val content: String, // caption or content text
  val mediaUrl: String? = null,
  val timestamp: Long = System.currentTimeMillis(),
  val views: List<StatusView> = emptyList(),
  val reactions: Map<String, Int> = emptyMap() // Emoji to count mapping
)

data class BroadcastCampaign(
  val id: String,
  val title: String,
  val content: String,
  val targetRoles: List<String> = emptyList(), // Role names, empty means all
  val sentAt: Long = System.currentTimeMillis(),
  val senderName: String = "Ashram Administration"
)

data class ChatDownloadTask(
  val id: String,
  val fileName: String,
  val size: Long,
  val progress: Float,
  val status: DownloadStatus,
  val url: String
)

enum class DownloadStatus {
  QUEUED,
  DOWNLOADING,
  PAUSED,
  COMPLETED,
  FAILED
}
