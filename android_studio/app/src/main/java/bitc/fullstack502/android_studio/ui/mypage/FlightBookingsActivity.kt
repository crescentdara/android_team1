package bitc.fullstack502.android_studio.ui.mypage

import android.os.Bundle
import bitc.fullstack502.android_studio.model.CommonItem
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.network.dto.*

class FlightBookingsActivity : BaseListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); title = "항공 예매내역"
    }
    override suspend fun fetchItems(): List<CommonItem> {
        val list = ApiProvider.api.getFlightBookings(userPk())
        return list.map {
            CommonItem(
                id = it.id ?: 0L,
                title = "${it.airline} ${it.flightNo} (${it.status})",
                subtitle = "${it.depart}→${it.arrive} ${it.departTime} ~ ${it.arriveTime}",
                imageUrl = null
            )
        }
    }
}