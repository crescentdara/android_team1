package bitc.fullstack502.android_studio.ui.mypage

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import bitc.fullstack502.android_studio.model.CommonItem
import bitc.fullstack502.android_studio.model.BookingResponse
import bitc.fullstack502.android_studio.network.ApiProvider
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch

class FlightBookingsActivity : BaseListActivity() {
    private lateinit var bookingAdapter: BookingListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "항공 예매내역"

        bookingAdapter = BookingListAdapter(mutableListOf())
        recycler.adapter = bookingAdapter

        load()
    }

    private fun load() {
        lifecycleScope.launch {
            val data = fetchItems()
            bookingAdapter.submit(data)
        }
    }

    override suspend fun fetchItems(): List<CommonItem> {
        val uid = userPk()
        if (uid <= 0L) return emptyList()

        return try {
            val list: List<BookingResponse> = ApiProvider.api.getFlightBookings(uid)
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
                        clickable = false
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
