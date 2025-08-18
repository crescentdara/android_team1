package bitc.fullstack502.android_studio

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class TicketSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_success)

        // 뒤로가기 / 완료 버튼
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnDone).setOnClickListener { finish() }

        val container = findViewById<LinearLayout>(R.id.ticketContainer)

        // 공통 데이터
        val passenger = intent.getStringExtra("EXTRA_PASSENGER") ?: "정보 없음"
        val isRoundTrip = intent.getBooleanExtra("EXTRA_ROUNDTRIP", false)

        // --- 가는 편 ---
        addTicket(
            container = container,
            badge = "가는 편",
            routeDep = intent.getStringExtra("EXTRA_DEP") ?: "출발지 미정",
            routeArr = intent.getStringExtra("EXTRA_ARR") ?: "도착지 미정",
            dateTime = intent.getStringExtra("EXTRA_DATETIME") ?: "시간 정보 없음",
            flightNo = intent.getStringExtra("EXTRA_FLIGHT_NO") ?: "편명 없음",
            gate = intent.getStringExtra("EXTRA_GATE") ?: "-",
            seat = intent.getStringExtra("EXTRA_SEAT") ?: "-",
            seatClass = intent.getStringExtra("EXTRA_CLASS") ?: "이코노미",
            passenger = passenger,
            status = "결제 완료"
        )

        // --- 오는 편 (왕복일 경우만) ---
        if (isRoundTrip) {
            addTicket(
                container = container,
                badge = "오는 편",
                routeDep = intent.getStringExtra("EXTRA_DEP_RETURN") ?: "출발지 미정",
                routeArr = intent.getStringExtra("EXTRA_ARR_RETURN") ?: "도착지 미정",
                dateTime = intent.getStringExtra("EXTRA_DATETIME_RETURN") ?: "시간 정보 없음",
                flightNo = intent.getStringExtra("EXTRA_FLIGHT_NO_RETURN") ?: "편명 없음",
                gate = intent.getStringExtra("EXTRA_GATE_RETURN") ?: "-",
                seat = intent.getStringExtra("EXTRA_SEAT_RETURN") ?: "-",
                seatClass = intent.getStringExtra("EXTRA_CLASS_RETURN") ?: "이코노미",
                passenger = passenger,
                status = "결제 완료"
            )
        }
    }

    private fun addTicket(
        container: LinearLayout,
        badge: String,
        routeDep: String,
        routeArr: String,
        dateTime: String,
        flightNo: String,
        gate: String,
        seat: String,
        seatClass: String,
        passenger: String,
        status: String
    ) {
        val v = layoutInflater.inflate(R.layout.item_ticket, container, false)
        v.findViewById<TextView>(R.id.tvBadge).text = badge
        v.findViewById<TextView>(R.id.tvStatus).text = status
        v.findViewById<TextView>(R.id.tvRoute).text = "$routeDep  →  $routeArr"
        v.findViewById<TextView>(R.id.tvPassenger).text = passenger
        v.findViewById<TextView>(R.id.tvDateTime).text = dateTime
        v.findViewById<TextView>(R.id.tvFlightNo).text = flightNo
        v.findViewById<TextView>(R.id.tvGate).text = gate
        v.findViewById<TextView>(R.id.tvClass).text = seatClass
        v.findViewById<TextView>(R.id.tvSeat).text = seat
        container.addView(v)
    }
}
