package bitc.fullstack502.android_studio.model

data class ChatMessage(
    val roomId: String,
    val senderId: String,
    val receiverId: String?,
    val content: String,
    val sentAt: String?,        // 서버가 ISO-8601로 내려옴(예: 2025-08-12T10:22:33Z)
    val type: String? = "TEXT"  // 서버 DTO에 있으면 매핑됨, 아니면 무시
)