package bitc.fullstack502.android_studio.model

data class ReadReceiptDTO(
    val roomId: String,
    val readerId: String,
    val lastReadId: Long,
    val at: String
)