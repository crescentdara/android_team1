package bitc.fullstack502.android_studio.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import bitc.fullstack502.android_studio.TicketSuccessActivity
import bitc.fullstack502.android_studio.model.CommonItem
import bitc.fullstack502.android_studio.model.BookingResponse
import bitc.fullstack502.android_studio.network.ApiProvider
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.jvm.java

class FlightBookingsActivity : BaseListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "항공 예매내역"
    }

    override suspend fun fetchItems(): List<CommonItem> {
        // 1) 로그인 확인
        val uid = userPk() // 또는 AuthManager.id()
        if (uid <= 0L) {
            // 로그인 필요 안내가 있다면 여기서 처리
            return emptyList()
        }

        return try {
            // 2) 서버 호출
            val list: List<BookingResponse> = ApiProvider.api.getFlightBookings(uid)

            // 3) 디버깅 로그(필요시)
            android.util.Log.d("FlightBookings", "bookings.size = ${list.size}")

            val nf = NumberFormat.getInstance(Locale.KOREA)

            // 4) (선택) 완료 건만 보고 싶다면 .filter { it.status.equals("PAID", true) }
            list
                .sortedByDescending { it.bookingId }
                .map { b ->
                    val trip = if (b.retDate.isNullOrBlank()) "편도" else "왕복"
                    val title = buildString {
                        append("$trip • ")
                        append(b.depDate)
                        if (!b.retDate.isNullOrBlank()) append(" ~ ${b.retDate}")
                    }
                    val sub = buildString {
                        append("좌석 ${b.seatCnt} • 성인 ${b.adult}")
                        if ((b.child ?: 0) > 0) append(", 소아 ${b.child}")
                        append(" • ${b.status} • ${nf.format(b.totalPrice)}원")
                    }
                    CommonItem(
                        id = b.bookingId,
                        title = title,
                        subtitle = sub,
                        imageUrl = null,
                        clickable = true
                    )
                }
        } catch (e: Exception) {
            // 5) 에러 로깅 및 빈 리스트 반환
            android.util.Log.e("FlightBookings", "getFlightBookings failed", e)
            // (BaseListActivity에 토스트 훅이 있다면 거기서 안내)
            emptyList()
        }
    }


    override fun onItemClick(item: CommonItem) {
//        lifecycleScope.launch {
//            try {
//                val b = ApiProvider.api.getFlightBooking(item.id) // suspend
//                val out = ApiProvider.api.getFlight(b.flightId)
//                val isRoundTrip = (b.returnFlightId != null && !b.retDate.isNullOrBlank())
//                val `in` = if (isRoundTrip) ApiProvider.api.getFlight(b.returnFlightId!!) else null
//
//                startActivity(Intent(this@FlightBookingsActivity, TicketSuccessActivity::class.java).apply {
//                    putExtra("EXTRA_BOOKING_ID", b.bookingId)     // 기본
//                    putExtra("EXTRA_ROUNDTRIP", isRoundTrip)      // 선택
//
//                    // 원한다면 추가 정보도 넘길 수 있지만, TicketSuccess에서 중복 표시 제거 필요
//                    putExtra("EXTRA_DEP", out.dep)
//                    putExtra("EXTRA_ARR", out.arr)
//                    putExtra("EXTRA_DATETIME", "${b.depDate} ${out.depTime}")
//                    putExtra("EXTRA_FLIGHT_NO", out.flNo)
//
//                    if (isRoundTrip && `in` != null) {
//                        putExtra("EXTRA_DEP_RETURN", `in`.dep)
//                        putExtra("EXTRA_ARR_RETURN", `in`.arr)
//                        putExtra("EXTRA_DATETIME_RETURN", "${b.retDate} ${`in`.depTime}")
//                        putExtra("EXTRA_FLIGHT_NO_RETURN", `in`.flNo)
//                    }
//                })
//            } catch (e: Exception) {
//                android.util.Log.e("FlightBookings", "load booking failed", e)
//                Toast.makeText(this@FlightBookingsActivity, "예약 상세를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
//            }
//        }
        startActivity(
            Intent(this, TicketSuccessActivity::class.java).apply {
                putExtra("EXTRA_BOOKING_ID", item.id) // Long
            }
        )
    }

}
