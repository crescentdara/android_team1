package bitc.fullstack502.android_studio.ui.lodging

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.android_studio.databinding.ActivityLodgingPaymentBinding
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import bitc.fullstack502.android_studio.network.RetrofitProvider
import bitc.fullstack502.android_studio.network.dto.LodgingDetailDto
import bitc.fullstack502.android_studio.network.dto.LodgingBookingDto

class LodgingPaymentActivity : AppCompatActivity() {

    private lateinit var b: ActivityLodgingPaymentBinding
    private var lodgingAddr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLodgingPaymentBinding.inflate(layoutInflater)
        setContentView(b.root)

        val lodgingId = intent.getLongExtra("lodgingId", -1)
        val checkIn = intent.getStringExtra("checkIn") ?: ""
        val checkOut = intent.getStringExtra("checkOut") ?: ""
        val roomType = intent.getStringExtra("roomType") ?: ""
        val totalPrice = intent.getIntExtra("totalPrice", 0)

        // 숙소 상세 조회 (주소 상세 표시)
        if (lodgingId != -1L) {
            RetrofitProvider.lodgingDetailApi.getDetail(lodgingId)
                .enqueue(object : Callback<LodgingDetailDto> {
                    override fun onResponse(call: Call<LodgingDetailDto>, response: Response<LodgingDetailDto>) {
                        val lodging = response.body() ?: return
                        b.tvLodgingName.text = lodging.name ?: ""
                        lodgingAddr = lodging.addrRd ?: lodging.addrJb ?: ""
                        b.tvLodgingAddr.text = lodgingAddr
                        b.tvLodgingPhone.text = lodging.phone ?: ""
                        Glide.with(this@LodgingPaymentActivity)
                            .load(lodging.img)
                            .into(b.imgLodgingCover)
                    }
                    override fun onFailure(call: Call<LodgingDetailDto>, t: Throwable) {
                        t.printStackTrace()
                    }
                })
        }

        b.tvCheckInOut.text = "$checkIn ~ $checkOut"
        b.tvRoomType.text = "객실 타입: $roomType"
        b.tvTotalPrice.text = "총 결제금액: ₩${totalPrice}"

        b.btnLodgingPay.setOnClickListener {
            val bookingData = LodgingBookingDto(
                userId = 1L,
                lodId = lodgingId,
                ckIn = checkIn,
                ckOut = checkOut,
                totalPrice = totalPrice,
                roomType = roomType,
                adult = 2,
                child = 0,
                status = "BOOKED"
            )

            RetrofitProvider.lodgingApi.createBooking(bookingData)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            startActivity(Intent(this@LodgingPaymentActivity, LodgingPaymentCompleteActivity::class.java).apply {
                                putExtra("lodgingName", b.tvLodgingName.text.toString())
                                putExtra("lodgingAddr", lodgingAddr) // 주소 전달
                                putExtra("checkIn", checkIn)
                                putExtra("checkOut", checkOut)
                                putExtra("roomType", roomType)
                                putExtra("price", totalPrice)
                            })
                            finish()
                        } else {
                            Toast.makeText(this@LodgingPaymentActivity, "예약 저장 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@LodgingPaymentActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
