package bitc.fullstack502.android_studio.ui.mypage

import android.os.Bundle
import bitc.fullstack502.android_studio.model.CommonItem
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.network.dto.*

class LodgingWishlistActivity : BaseListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); title = "숙소 즐겨찾기"
    }
    override suspend fun fetchItems(): List<CommonItem> {
        val list = ApiProvider.api.getLodgingWishlist(userPk())
        return list.map {
            CommonItem(
                id = it.id,
                title = it.name,
                subtitle = listOfNotNull(it.city, it.town).filter { s -> s.isNotBlank() }
                    .joinToString(" "),
                imageUrl = it.img
            )
        }
    }
}