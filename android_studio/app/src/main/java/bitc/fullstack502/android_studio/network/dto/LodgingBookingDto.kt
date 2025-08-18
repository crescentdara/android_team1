package bitc.fullstack502.android_studio.network.dto

data class LodgingBookingDto(
    val userId: Long,
    val lodId: Long,
    val ckIn: String,
    val ckOut: String,
    val totalPrice: Long,
    val roomType: String,
    val adult: Int,
    val child: Int,
    val status: String
)
