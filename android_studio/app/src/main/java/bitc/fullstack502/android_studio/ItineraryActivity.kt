package bitc.fullstack502.android_studio

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.android_studio.model.Flight
import bitc.fullstack502.android_studio.FlightReservationActivity
import bitc.fullstack502.android_studio.model.Passenger

class ItineraryActivity : AppCompatActivity() {

    companion object {
        // Intent keys (필요 시 이전 화면에서 넘겨줌)
        const val EXTRA_OUT_FLIGHT   = "EXTRA_OUT_FLIGHT"
        const val EXTRA_IN_FLIGHT    = "EXTRA_IN_FLIGHT"
        const val EXTRA_ADULT_COUNT  = "EXTRA_ADULT_COUNT"
        const val EXTRA_CHILD_COUNT  = "EXTRA_CHILD_COUNT"
        const val EXTRA_OUT_DATE     = "EXTRA_OUT_DATE"
        const val EXTRA_IN_DATE      = "EXTRA_IN_DATE"

        // ✅ 고정 운임 상수(1인)
        private const val ADULT_PRICE     = 98_700               // 항공운임(성인)
        private const val CHILD_PRICE     = ADULT_PRICE - 20_000 // 항공운임(아동)
        private const val FUEL_SURCHARGE  = 15_400               // 유류할증료(연령무관 1인)
        private const val FACILITY_FEE    = 8_000                // 공항시설사용료(연령무관 1인)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_itinerary_detail)

        // ===== include 카드 참조 =====
        val outCard = findViewById<View>(R.id.includeOutbound)
        val inCard  = findViewById<View>(R.id.includeInbound)

        val tvOutFlightNo = outCard.findViewById<TextView>(R.id.tvFlightNo)
        val tvOutFareType = outCard.findViewById<TextView>(R.id.tvFareType)
        val tvOutDepInfo  = outCard.findViewById<TextView>(R.id.tvDepInfo)
        val tvOutArrInfo  = outCard.findViewById<TextView>(R.id.tvArrInfo)
        val tvOutDepCity  = outCard.findViewById<TextView>(R.id.tvDepCity)
        val tvOutArrCity  = outCard.findViewById<TextView>(R.id.tvArrCity)

        val tvInFlightNo = inCard.findViewById<TextView>(R.id.tvFlightNo)
        val tvInFareType = inCard.findViewById<TextView>(R.id.tvFareType)
        val tvInDepInfo  = inCard.findViewById<TextView>(R.id.tvDepInfo)
        val tvInArrInfo  = inCard.findViewById<TextView>(R.id.tvArrInfo)
        val tvInDepCity  = inCard.findViewById<TextView>(R.id.tvDepCity)
        val tvInArrCity  = inCard.findViewById<TextView>(R.id.tvArrCity)

        // 운임/합계 영역
        val tvFareBase     = findViewById<TextView>(R.id.tvFareBase)
        val tvFareFuel     = findViewById<TextView>(R.id.tvFareFuel)
        val tvFareFacility = findViewById<TextView>(R.id.tvFareFacility)
        val tvTotal        = findViewById<TextView>(R.id.tvTotalPrice)

        // 체크박스
        val cbAll      = findViewById<CheckBox>(R.id.cbAll)
        val cbRule     = findViewById<CheckBox>(R.id.cbRule)
        val cbDomestic = findViewById<CheckBox>(R.id.cbDomestic)
        val cbProhibit = findViewById<CheckBox>(R.id.cbProhibited)
        val cbAddon    = findViewById<CheckBox>(R.id.cbAddon)
        val cbThird    = findViewById<CheckBox>(R.id.cbThird)
        val cbHazard   = findViewById<CheckBox>(R.id.cbHazard)
        val childCbs   = listOf(cbRule, cbDomestic, cbProhibit, cbAddon, cbThird, cbHazard)

        val btnNext = findViewById<Button>(R.id.btnNext)

        // ===== PassengerInputActivity 에서 넘어온 값들 (키 통일!) =====
        val outFlight = intent.getSerializableExtra(FlightReservationActivity.EXTRA_OUTBOUND) as? Flight
        val inFlight  = intent.getSerializableExtra(FlightReservationActivity.EXTRA_INBOUND)  as? Flight
        val outPrice  = intent.getIntExtra(FlightReservationActivity.EXTRA_OUT_PRICE, 0)
        val inPrice   = intent.getIntExtra(FlightReservationActivity.EXTRA_IN_PRICE, 0)

        val adultCount = intent.getIntExtra(FlightReservationActivity.EXTRA_ADULT, 1)
        val childCount = intent.getIntExtra(FlightReservationActivity.EXTRA_CHILD, 0)
        val infantCount= intent.getIntExtra(FlightReservationActivity.EXTRA_INFANT, 0)

        // (옵션) 승객 리스트 – 다음 화면에서도 필요할 수 있음
        @Suppress("UNCHECKED_CAST")
        val passengers = intent.getSerializableExtra("PASSENGERS") as? ArrayList<Passenger>

        // ===== 카드 바인딩 =====
        outFlight?.let { f ->
            tvOutFlightNo.text = f.flNo
            tvOutFareType.text = "이코노미"
            tvOutDepInfo.text  = f.depTime
            tvOutArrInfo.text  = f.arrTime
            tvOutDepCity.text  = f.dep
            tvOutArrCity.text  = f.arr
        }

        if (inFlight == null) {
            inCard.visibility = View.GONE // 편도면 숨김
        } else {
            inCard.visibility = View.VISIBLE
            inFlight.let { f ->
                tvInFlightNo.text = f.flNo
                tvInFareType.text = "이코노미"
                tvInDepInfo.text  = f.depTime
                tvInArrInfo.text  = f.arrTime
                tvInDepCity.text  = f.dep
                tvInArrCity.text  = f.arr
            }
        }

        // ===== 운임 계산(성인/아동 부과, 유아 0원) =====
        val segments = if (inFlight == null) 1 else 2                // 편도=1, 왕복=2
        val chargeable = adultCount + childCount                      // 유아는 0원

        // 1인 총액(연령별)
        val perAdultTotal = ADULT_PRICE + FUEL_SURCHARGE + FACILITY_FEE   // 122,100
        val perChildTotal = CHILD_PRICE + FUEL_SURCHARGE + FACILITY_FEE   // 102,100
        val perInfantTotal = 0

        // 항목별 합계(구간수 적용 전)
        val baseFare      = adultCount * ADULT_PRICE + childCount * CHILD_PRICE
        val fuelTotal     = chargeable * FUEL_SURCHARGE
        val facilityTotal = chargeable * FACILITY_FEE

        // 화면 표시는 '구간수 적용 후' 금액으로
        val baseFareX      = baseFare * segments
        val fuelTotalX     = fuelTotal * segments
        val facilityTotalX = facilityTotal * segments
        val totalX         = (adultCount * perAdultTotal +
                childCount * perChildTotal +
                infantCount * perInfantTotal) * segments

        // 표시
        tvFareBase.text     = "%,d원".format(baseFareX)
        tvFareFuel.text     = "%,d원".format(fuelTotalX)
        tvFareFacility.text = "%,d원".format(facilityTotalX)
        tvTotal.text        = "%,d원".format(totalX)

        // ===== 전체 동의 연동 =====
        fun syncAllFromChildren() {
            val allChecked = childCbs.all { it.isChecked }
            if (cbAll.isChecked != allChecked) cbAll.isChecked = allChecked
            btnNext.isEnabled = allChecked
        }
        cbAll.setOnCheckedChangeListener { _, checked ->
            childCbs.forEach { it.setOnCheckedChangeListener(null) }
            childCbs.forEach { it.isChecked = checked }
            childCbs.forEach { child ->
                child.setOnCheckedChangeListener { _, _ -> syncAllFromChildren() }
            }
            btnNext.isEnabled = checked
        }
        childCbs.forEach { child ->
            child.setOnCheckedChangeListener { _, _ -> syncAllFromChildren() }
        }
        syncAllFromChildren()

        // ===== 다음(결제) =====
        btnNext.setOnClickListener {
            if (!btnNext.isEnabled) return@setOnClickListener

            val outDate = intent.getStringExtra(PassengerInputActivity.EXTRA_OUT_DATE)
            val inDate  = intent.getStringExtra(PassengerInputActivity.EXTRA_IN_DATE)

            startActivity(Intent(this, PaymentActivity::class.java).apply {
                // 👉 결제 화면으로는 '구간 반영 후' 금액 전달
                putExtra("EXTRA_TOTAL", totalX)
                putExtra("EXTRA_BASE", baseFareX)
                putExtra("EXTRA_FUEL", fuelTotalX)
                putExtra("EXTRA_FACILITY", facilityTotalX)

                putExtra(FlightReservationActivity.EXTRA_OUTBOUND, outFlight)
                putExtra(FlightReservationActivity.EXTRA_INBOUND, inFlight)
                putExtra(FlightReservationActivity.EXTRA_ADULT, adultCount)
                putExtra(FlightReservationActivity.EXTRA_CHILD, childCount)
                putExtra(FlightReservationActivity.EXTRA_INFANT, infantCount)

                // 필요 시 승객 리스트도 넘김
                putExtra("PASSENGERS", passengers)

                // ✅ 날짜 전달(핵심)
                putExtra(PassengerInputActivity.EXTRA_OUT_DATE, outDate)
                inDate?.let { putExtra(PassengerInputActivity.EXTRA_IN_DATE, it) }
            })
        }
    }
}
