package bitc.fullstack502.android_studio.model

data class ChatMessage(
    val id: Long?,
    val roomId: String,
    val senderId: String,
    val receiverId: String?,
    val content: String,
    val type: String,
    val sentAt: String,
    val readByOther: Boolean? = null  // ✅ 새 필드 (null 허용)
)
