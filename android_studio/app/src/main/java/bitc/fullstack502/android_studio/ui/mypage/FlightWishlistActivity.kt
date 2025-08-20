package bitc.fullstack502.android_studio.ui.mypage

import android.os.Bundle
import bitc.fullstack502.android_studio.model.CommonItem
import bitc.fullstack502.android_studio.network.ApiProvider

class FlightWishlistActivity : BaseListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "항공 즐겨찾기"
    }

    override suspend fun fetchItems(): List<CommonItem> {
        val list = ApiProvider.api.getFlightWishlist(userPk())
        return list.map { w ->
            CommonItem(
                id = w.id ?: 0L,
                title = "${w.airline ?: "항공사"} ${w.flightNo ?: ""}",
                subtitle = "${w.depart ?: ""} → ${w.arrive ?: ""}",
                imageUrl = null,
                clickable = false   // ✅ 즐겨찾기는 클릭 불가, 꺽쇠 숨김
            )
        }
    }

    // 클릭 막기
    override fun onItemClick(item: CommonItem) {
        // 아무 동작 안 함
    }
}
