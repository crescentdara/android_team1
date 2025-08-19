package bitc.fullstack502.android_studio.model

import com.google.gson.annotations.SerializedName

data class BookingResponse(
    val bookingId: Long,
    val userId: Long,
    val flightId: Long,   // 응답은 flightId
    val seatCnt: Int,
    val adult: Int,
    val child: Int?,
    val totalPrice: Long,
    val status: String,
    val tripDate: String  // 서버는 LocalDate → JSON string 으로 내려옴
)

