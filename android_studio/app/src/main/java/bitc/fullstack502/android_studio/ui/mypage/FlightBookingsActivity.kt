package bitc.fullstack502.android_studio.ui.mypage

import android.content.Intent
import android.os.Bundle
import bitc.fullstack502.android_studio.TicketSuccessActivity
import bitc.fullstack502.android_studio.model.CommonItem
import bitc.fullstack502.android_studio.model.BookingResponse
import bitc.fullstack502.android_studio.network.ApiProvider
import java.text.NumberFormat
import java.util.Locale
import kotlin.jvm.java

class FlightBookingsActivity : BaseListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "항공 예매내역"
    }

    override suspend fun fetchItems(): List<CommonItem> {
        val list: List<BookingResponse> = ApiProvider.api.getFlightBookings(userPk())
        val nf = NumberFormat.getInstance(Locale.KOREA)
        return list.map { b ->
            val title = "항공편 ${b.flightId} • ${b.tripDate}"
            val sub = "좌석 ${b.seatCnt} • 성인 ${b.adult}" +
                    (if (b.child != null && b.child!! > 0) ", 소아 ${b.child}" else "") +
                    " • ${b.status} • ${nf.format(b.totalPrice)}원"
            CommonItem(
                id = b.bookingId,
                title = title,
                subtitle = sub,
                imageUrl = null,
                clickable = true  // ✅ 예약내역은 클릭 가능
            )
        }
    }

    override fun onItemClick(item: CommonItem) {
        // 예약내역 클릭 → TicketSuccessActivity 이동
        val intent = Intent(this, TicketSuccessActivity::class.java)
        intent.putExtra("EXTRA_BOOKING_ID", item.id)
        startActivity(intent)
    }
}
