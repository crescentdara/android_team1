package bitc.fullstack502.android_studio.model

data class ReadReceiptDTO(
    val roomId: String,
    val userId: String,       // readerId → userId
    val lastReadId: Long,
    val lastReadAt: String    // at → lastReadAt
)
