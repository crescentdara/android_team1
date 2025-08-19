package bitc.fullstack502.android_studio.ui.mypage

import android.os.Bundle
import bitc.fullstack502.android_studio.model.CommonItem
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.network.dto.*

class FlightWishlistActivity : BaseListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); title = "항공 즐겨찾기"
    }
    override suspend fun fetchItems(): List<CommonItem> {
        val list = ApiProvider.api.getFlightWishlist(userPk())
        return list.map {
            CommonItem(
                id = it.id,
                title = "${it.airline} ${it.flightNo}",
                subtitle = "${it.depart} → ${it.arrive}",
                imageUrl = it.thumb
            )
        }
    }
}