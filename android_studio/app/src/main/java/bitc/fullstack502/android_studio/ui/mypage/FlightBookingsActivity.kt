package bitc.fullstack502.android_studio.ui.mypage

import android.os.Bundle
import bitc.fullstack502.android_studio.model.CommonItem
import bitc.fullstack502.android_studio.model.BookingResponse
import bitc.fullstack502.android_studio.network.ApiProvider
import java.text.NumberFormat
import java.util.Locale

class FlightBookingsActivity : BaseListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "항공 예매내역"
    }

    override suspend fun fetchItems(): List<CommonItem> {
        // 1) 로그인 확인
        val uid = userPk()
        if (uid <= 0L) {
            return emptyList()
        }

        return try {
            val list: List<BookingResponse> = ApiProvider.api.getFlightBookings(uid)
            android.util.Log.d("FlightBookings", "bookings.size = ${list.size}")

            val nf = NumberFormat.getInstance(Locale.KOREA)

            list.sortedByDescending { it.bookingId }
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
                        clickable = false   // ✅ 꺽쇠 숨김 & 클릭 막기
                    )
                }
        } catch (e: Exception) {
            android.util.Log.e("FlightBookings", "getFlightBookings failed", e)
            emptyList()
        }
    }

    override fun onItemClick(item: CommonItem) {
        // 아무 동작도 하지 않음
    }
}
