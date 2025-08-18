package bitc.fullstack502.android_studio.ui.lodging

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.android_studio.ui.MainActivity
import bitc.fullstack502.android_studio.databinding.ActivityLodgingPaymentCompleteBinding
import java.text.NumberFormat
import java.util.Locale

class LodgingPaymentCompleteActivity : AppCompatActivity() {

    private lateinit var b: ActivityLodgingPaymentCompleteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLodgingPaymentCompleteBinding.inflate(layoutInflater)
        setContentView(b.root)

        val lodgingName = intent.getStringExtra("lodgingName") ?: ""
        val lodgingAddr = intent.getStringExtra("lodgingAddr") ?: ""
        val checkIn = intent.getStringExtra("checkIn") ?: ""
        val checkOut = intent.getStringExtra("checkOut") ?: ""
        val roomType = intent.getStringExtra("roomType") ?: ""
        val price = intent.getLongExtra("price", 0L) // ✅ Long 으로 받기

        val priceText = NumberFormat.getCurrencyInstance(Locale.KOREA).format(price)

        b.tvCompleteInfo.text = """
            숙소명: $lodgingName
            주소: $lodgingAddr
            체크인: $checkIn
            체크아웃: $checkOut
            객실 타입: $roomType
            총 결제금액: $priceText
        """.trimIndent()

        b.btnGoHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }
}
