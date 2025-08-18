package bitc.fullstack502.android_studio.model

data class Flight(
    val id: Long,
    val type: String?,
    val flNo: String,
    val airline: String?,
    val dep: String,
    val depTime: String,
    val arr: String,
    val arrTime: String,
    val days: String?,
    val totalSeat: Int
)
