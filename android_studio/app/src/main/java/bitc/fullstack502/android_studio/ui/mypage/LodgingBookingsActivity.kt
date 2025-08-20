package bitc.fullstack502.android_studio.ui.mypage

import android.content.Intent
import android.os.Bundle
import bitc.fullstack502.android_studio.model.CommonItem
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.network.dto.*
import bitc.fullstack502.android_studio.ui.lodging.LodgingPaymentCompleteActivity
import bitc.fullstack502.android_studio.util.fullUrl

class LodgingBookingsActivity : BaseListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "숙박 예약내역"
    }

    override suspend fun fetchItems(): List<CommonItem> {
        val list = ApiProvider.api.getLodgingBookings(userPk())
        return list.map {
            val nights = "${it.ckIn} ~ ${it.ckOut}"
            CommonItem(
                id = it.id ?: 0L,
                // 타이틀: 숙소명 + 룸타입 + 상태
                title = "${it.lodName ?: "숙소"} - ${it.roomType} (${it.status})",
                subtitle = "$nights • ${it.totalPrice}원",
                // 서버에서 lodImg 내려주니까 반영
                imageUrl = it.lodImg,
                // ✅ tag에 원본 DTO 저장
                tag = it
            )
        }
    }

    override fun onItemClick(item: CommonItem) {
        val dto = item.tag as LodgingBookingDto
        startActivity(
            Intent(this, LodgingPaymentCompleteActivity::class.java).apply {
                putExtra("lodgingName", dto.lodName)
                putExtra("lodgingAddr", dto.addrRd ?: dto.addrJb ?: "")
                putExtra("checkIn", dto.ckIn)
                putExtra("checkOut", dto.ckOut)
                putExtra("roomType", dto.roomType)
                putExtra("price", dto.totalPrice)
                // ✅ 여기서도 fullUrl 적용
                putExtra("lodgingImg", fullUrl(dto.lodImg))
                putExtra("status", dto.status)
            }
        )
    }


}
