package bitc.fullstack502.android_studio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val tvTotal = findViewById<TextView>(R.id.tvTotalPrice)
        val tvFuel = findViewById<TextView>(R.id.tvFuel)
        val tvFacility = findViewById<TextView>(R.id.tvFacility)
        val tvBase = findViewById<TextView>(R.id.tvBaseFare)
        val rbKakao = findViewById<RadioButton>(R.id.rbKakaoPay)
        val btnPay = findViewById<Button>(R.id.btnPay)

        val total = intent.getIntExtra("EXTRA_TOTAL", 0)
        val baseFare = intent.getIntExtra("EXTRA_BASE", 0)
        val fuel = intent.getIntExtra("EXTRA_FUEL", 0)
        val facility = intent.getIntExtra("EXTRA_FACILITY", 0)

        tvBase.text = "항공운임: %,d원".format(baseFare)
        tvFuel.text = "유류할증료: %,d원".format(fuel)
        tvFacility.text = "공항시설사용료: %,d원".format(facility)
        tvTotal.text = "총 결제금액: %,d원".format(total)

        btnPay.setOnClickListener {
            if (!rbKakao.isChecked) {
                Toast.makeText(this, "결제수단을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "카카오페이 결제 진행", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, TicketSuccessActivity::class.java).apply {
                // 편도/왕복 여부
                putExtra("EXTRA_ROUNDTRIP", true)  // true면 왕복, false면 편도

                // 가는 편
                putExtra("EXTRA_DEP", "무안")
                putExtra("EXTRA_ARR", "제주")
                putExtra("EXTRA_PASSENGER", "홍길동")
                putExtra("EXTRA_DATETIME", "2025-08-21 10:30")
                putExtra("EXTRA_FLIGHT_NO", "JJ 8868")
                putExtra("EXTRA_GATE", "32B")
                putExtra("EXTRA_SEAT", "36A")

                // 오는 편 (왕복일 경우만)
                putExtra("EXTRA_DEP_RETURN", "제주")
                putExtra("EXTRA_ARR_RETURN", "무안")
                putExtra("EXTRA_DATETIME_RETURN", "2025-08-28 14:00")
                putExtra("EXTRA_FLIGHT_NO_RETURN", "JJ 8869")
                putExtra("EXTRA_GATE_RETURN", "15A")
                putExtra("EXTRA_SEAT_RETURN", "21C")
            }
            startActivity(intent)
        }
    }
}
