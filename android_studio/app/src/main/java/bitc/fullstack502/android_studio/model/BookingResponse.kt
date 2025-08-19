package bitc.fullstack502.android_studio.model

import java.time.LocalDate

data class BookingResponse(
    val bookingId: Long,
    val userId: Long,
    val seatCnt: Int,
    val adult: Int,
    val child: Int,
    val totalPrice: Long,
    val tripDate: LocalDate,
    val status: String
)