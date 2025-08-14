package bitc.fullstack502.android_studio.data

import java.time.LocalDate

data class BookingRequest (
    val departureDate: LocalDate,
    val returnDate: LocalDate,
    val seatCnt: Int
)