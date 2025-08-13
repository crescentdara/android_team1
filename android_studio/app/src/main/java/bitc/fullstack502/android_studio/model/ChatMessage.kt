package bitc.fullstack502.android_studio.model

data class ChatMessage(
    val roomId: String,
    val senderId: String,
    val receiverId: String?,
    val content: String,
    val type: String,        // 서버 enum이 "TEXT"로 오므로 String으로 받으면 편함
    val sentAt: String       // 일단 String으로 받으면 파싱 이슈 없음
)