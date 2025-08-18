package bitc.fullstack502.android_studio.model

data class Flight(
    val id: Long,
    val type: String?,
    val flNo: String,
    val airline: String?,
    val dep: String,
    val depTime: String,   // ✅ String으로
    val arr: String,
    val arrTime: String,   // ✅ String으로
    val days: String,
    val totalSeat: Int
)

