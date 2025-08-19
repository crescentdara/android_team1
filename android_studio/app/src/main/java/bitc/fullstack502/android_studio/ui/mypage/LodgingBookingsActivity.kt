package bitc.fullstack502.android_studio.ui.mypage

import android.os.Bundle
import bitc.fullstack502.android_studio.model.CommonItem
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.network.dto.*

class LodgingBookingsActivity : BaseListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); title = "숙박 예약내역"
    }
    override suspend fun fetchItems(): List<CommonItem> {
        val list = ApiProvider.api.getLodgingBookings(userPk())
        return list.map {
            val nights = try {
                // 간단 표기 (필요하면 실제 박수 계산 로직 넣어도 됨)
                "${it.ckIn} ~ ${it.ckOut}"
            } catch (_: Exception) { "${it.ckIn} ~ ${it.ckOut}" }
            CommonItem(
                id = it.id ?: 0L,
                title = "${it.roomType} (${it.status})",
                subtitle = "$nights • ${it.totalPrice}원",
                imageUrl = null
            )
        }
    }
}