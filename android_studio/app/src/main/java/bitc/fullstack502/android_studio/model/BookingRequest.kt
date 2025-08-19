package bitc.fullstack502.android_studio.model

import com.google.gson.annotations.SerializedName

data class BookingRequest(
    val userId: Long,
    val flId: Long,       // 요청은 flId
    val seatCnt: Int,
    val adult: Int,
    val child: Int?,
    val tripDate: String, // yyyy-MM-dd
    val totalPrice: Long
)

