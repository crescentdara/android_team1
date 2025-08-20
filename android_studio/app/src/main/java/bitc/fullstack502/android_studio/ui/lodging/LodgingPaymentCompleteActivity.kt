package bitc.fullstack502.android_studio.ui.lodging

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.android_studio.databinding.ActivityLodgingPaymentCompleteBinding
import bitc.fullstack502.android_studio.ui.MainActivity
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

class LodgingPaymentCompleteActivity : AppCompatActivity() {

    private lateinit var b: ActivityLodgingPaymentCompleteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLodgingPaymentCompleteBinding.inflate(layoutInflater)
        setContentView(b.root)

        // ✅ Intent 로 값 받기
        val lodgingName = intent.getStringExtra("lodgingName") ?: ""
        val lodgingAddr = intent.getStringExtra("lodgingAddr") ?: ""
        val checkIn = intent.getStringExtra("checkIn") ?: ""
        val checkOut = intent.getStringExtra("checkOut") ?: ""
        val roomType = intent.getStringExtra("roomType") ?: ""
        val price = intent.getLongExtra("price", 0L)
        val lodgingImg = intent.getStringExtra("lodgingImg") ?: ""
        val status = intent.getStringExtra("status") ?: ""  // 예약 상태도 전달받을 수 있음

        val priceText = NumberFormat.getCurrencyInstance(Locale.KOREA).format(price)

        // ✅ XML 각 뷰에 데이터 세팅
        Glide.with(this)
            .load(lodgingImg)
            .placeholder(android.R.color.darker_gray)
            .into(b.imgLodging)

        b.tvLodgingName.text = "$lodgingName - $roomType ($status)"
        b.tvLodgingAddr.text = "주소: $lodgingAddr"
        b.tvCheckInOut.text = "체크인: $checkIn  •  체크아웃: $checkOut"
        b.tvRoomType.text = "객실 타입: $roomType"
        b.tvTotalPrice.text = "총 결제금액: $priceText"

        // ✅ 홈으로 가기 버튼
        b.btnGoHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }
}
