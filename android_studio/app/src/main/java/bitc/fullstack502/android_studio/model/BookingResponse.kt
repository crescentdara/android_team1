package bitc.fullstack502.android_studio.model

import java.io.Serializable

data class BookingResponse(
    val bookingId: Long,
    val userId: Long? = null,

    val outFlightId: Long,
    val inFlightId: Long? = null,   // 왕복일 때만

    val seatCnt: Int,               // 좌석 수
    val adult: Int,
    val child: Int? = null,

    val totalPrice: Long,
    val status: String,

    val depDate: String,
    val retDate: String? = null
) : Serializable
