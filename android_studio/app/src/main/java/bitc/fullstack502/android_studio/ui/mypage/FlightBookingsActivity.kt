package bitc.fullstack502.android_studio.ui.mypage

import android.content.Intent
import android.os.Bundle
import bitc.fullstack502.android_studio.TicketSuccessActivity
import bitc.fullstack502.android_studio.model.CommonItem
import bitc.fullstack502.android_studio.model.BookingResponse
import bitc.fullstack502.android_studio.network.ApiProvider
import java.text.NumberFormat
import java.util.Locale

class FlightBookingsActivity : BaseListActivity() {

    // 서버에서 받은 예약 목록을 저장
    private var bookingList: List<BookingResponse> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "항공 예매내역"
    }

    override suspend fun fetchItems(): List<CommonItem> {
        bookingList = ApiProvider.api.getFlightBookings(userPk())
        val nf = NumberFormat.getInstance(Locale.KOREA)

        return bookingList.map { b ->
            val dateText = if (!b.retDate.isNullOrBlank()) {
                "${b.depDate} ~ ${b.retDate}"
            } else {
                b.depDate
            }

            val flightText = if (b.inFlightId != null) {
                "항공편 ${b.outFlightId} ↔ ${b.inFlightId}"
            } else {
                "항공편 ${b.outFlightId}"
            }

            val title = "$flightText • $dateText"

            val sub = "좌석 ${b.seatCnt} • 성인 ${b.adult}" +
                    (if (b.child != null && b.child!! > 0) ", 소아 ${b.child}" else "") +
                    " • ${b.status} • ${nf.format(b.totalPrice)}원"

            CommonItem(
                id = b.bookingId,
                title = title,
                subtitle = sub,
                imageUrl = null,
                clickable = true
            )
        }
    }

    override fun onItemClick(item: CommonItem) {
        val booking = bookingList.find { it.bookingId == item.id } ?: return
        val intent = Intent(this, TicketSuccessActivity::class.java)
        intent.putExtra("EXTRA_BOOKING", booking) // BookingResponse 는 Serializable 필요
        startActivity(intent)
    }
}
