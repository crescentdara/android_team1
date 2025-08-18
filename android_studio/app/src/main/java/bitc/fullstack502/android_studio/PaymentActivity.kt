package bitc.fullstack502.android_studio

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
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
            if (rbKakao.isChecked) {
                android.widget.Toast.makeText(this, "카카오페이 결제 진행!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}
