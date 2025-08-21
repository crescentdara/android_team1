package bitc.fullstack502.android_studio.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

import com.google.gson.annotations.SerializedName

// model/BookingResponse.kt (클라 모델)
//data class BookingResponse(
//    val bookingId: Long,
//    val userId: Long,
//    val flightId: Long,
//    val returnFlightId: Long?,   // 왕복이면 존재
//    val seatCnt: Int,
//    val adult: Int,
//    val child: Int?,
//    val totalPrice: Long,
//    val status: String,          // PAID/CANCEL
//    val depDate: String,         // "yyyy-MM-dd"
//    val retDate: String?         // "yyyy-MM-dd" or null
//)


@Parcelize
data class BookingResponse(
    val bookingId: Long,
    val userId: Long,              // 추가
    val outFlightId: Long,
    val inFlightId: Long?,
    val depDate: String,           // LocalDate → String 그대로 둬도 OK
    val retDate: String?,
    val adult: Int,
    val child: Int?,
    val seatCnt: Int,
    val status: String,
    val totalPrice: Long           // Int → Long 수정
) : Parcelable




