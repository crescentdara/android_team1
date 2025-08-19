package bitc.fullstack502.android_studio.model

import java.time.LocalDate

data class BookingRequest (
    val userId: Long,
    val flId: Long,
    val seatCnt: Int,
    val adult: Int,
    val child: Int,
    val tripDate: LocalDate,
    val totalPrice: Long
)