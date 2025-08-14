package bitc.fullstack502.android_studio.model

data class ConversationSummary(
    val partnerId: String,
    val roomId: String,
    val lastContent: String,
    val lastAt: String,   // 서버가 ISO라면 String으로 받아도 OK
    val unreadCount: Long
)