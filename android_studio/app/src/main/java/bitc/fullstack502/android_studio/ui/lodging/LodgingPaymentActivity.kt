package bitc.fullstack502.android_studio.ui.lodging

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.android_studio.databinding.ActivityLodgingPaymentBinding
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.network.dto.LodgingBookingDto
import bitc.fullstack502.android_studio.network.dto.LodgingDetailDto
import bitc.fullstack502.android_studio.util.AuthManager
import bitc.fullstack502.android_studio.util.fullUrl
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class LodgingPaymentActivity : AppCompatActivity() {

    private lateinit var b: ActivityLodgingPaymentBinding
    private var lodgingAddr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLodgingPaymentBinding.inflate(layoutInflater)
        setContentView(b.root)

        // ✅ AuthManager 초기화
        AuthManager.init(this)

        val lodgingId = intent.getLongExtra("lodgingId", -1L)
        val checkIn = intent.getStringExtra("checkIn") ?: ""
        val checkOut = intent.getStringExtra("checkOut") ?: ""
        val roomType = intent.getStringExtra("roomType") ?: ""
        val totalPrice = intent.getLongExtra("totalPrice", 0L)

        if (lodgingId != -1L) {
            ApiProvider.api.getDetail(lodgingId)
                .enqueue(object : Callback<LodgingDetailDto> {
                    override fun onResponse(call: Call<LodgingDetailDto>, res: Response<LodgingDetailDto>) {
                        val d = res.body() ?: return
                        b.tvLodgingName.text = d.name ?: ""
                        lodgingAddr = d.addrRd ?: d.addrJb ?: ""
                        b.tvLodgingAddr.text = lodgingAddr
                        b.tvLodgingPhone.text = d.phone ?: ""
                        fullUrl(d.img)?.let {
                            Glide.with(this@LodgingPaymentActivity).load(it).into(b.imgLodgingCover)
                        }
                    }
                    override fun onFailure(call: Call<LodgingDetailDto>, t: Throwable) { }
                })
        }

        b.tvCheckInOut.text = "$checkIn ~ $checkOut"
        b.tvRoomType.text = "객실 타입: $roomType"
        b.tvTotalPrice.text =
            "총 결제금액: " + NumberFormat.getCurrencyInstance(Locale.KOREA).format(totalPrice)

        b.btnLodgingPay.setOnClickListener {
            if (!AuthManager.isLoggedIn()) {
                Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val userPk = AuthManager.id()
            if (userPk <= 0L) {
                Toast.makeText(this, "로그인 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val booking = LodgingBookingDto(
                userId = userPk,       // ✅ 변수명 userPk 사용, DTO 필드는 userId
                lodId = lodgingId,
                ckIn = checkIn,
                ckOut = checkOut,
                totalPrice = totalPrice,
                roomType = roomType,
                adult = 1,
                child = 0,
                status = "BOOKED"
            )

            ApiProvider.api.createBooking(booking).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        startActivity(
                            Intent(this@LodgingPaymentActivity, LodgingPaymentCompleteActivity::class.java).apply {
                                putExtra("lodgingName", b.tvLodgingName.text.toString())
                                putExtra("lodgingAddr", lodgingAddr)
                                putExtra("checkIn", checkIn)
                                putExtra("checkOut", checkOut)
                                putExtra("roomType", roomType)
                                putExtra("price", totalPrice)
                            }
                        )
                        finish()
                    } else {
                        Toast.makeText(this@LodgingPaymentActivity, "예약 저장 실패 (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@LodgingPaymentActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
