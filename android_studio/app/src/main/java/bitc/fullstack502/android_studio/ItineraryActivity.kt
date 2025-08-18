package bitc.fullstack502.android_studio

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.android_studio.model.Flight

class ItineraryActivity : AppCompatActivity() {

    companion object {
        // Intent keys (필요 시 이전 화면에서 넘겨줌)
        const val EXTRA_OUT_FLIGHT   = "EXTRA_OUT_FLIGHT"
        const val EXTRA_IN_FLIGHT    = "EXTRA_IN_FLIGHT"
        const val EXTRA_ADULT_COUNT  = "EXTRA_ADULT_COUNT"
        const val EXTRA_CHILD_COUNT  = "EXTRA_CHILD_COUNT"
        const val EXTRA_OUT_DATE     = "EXTRA_OUT_DATE"   // optional: "2025.08.19(화)" 같은 문자열
        const val EXTRA_IN_DATE      = "EXTRA_IN_DATE"

        // ✅ 고정 운임 상수
        private const val ADULT_PRICE     = 98_700
        private const val CHILD_PRICE     = 78_700
        private const val FUEL_SURCHARGE  = 15_400   // per person
        private const val FACILITY_FEE    = 8_000    // per person
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // include 2개가 들어있는 레이아웃
        setContentView(R.layout.activity_itinerary_detail)

        // ========== include 카드 참조 ==========
        val outCard = findViewById<View>(R.id.includeOutbound)
        val inCard  = findViewById<View>(R.id.includeInbound)

        // 가는 편 카드 내부
        val tvOutFlightNo = outCard.findViewById<TextView>(R.id.tvFlightNo)
        val tvOutFareType = outCard.findViewById<TextView>(R.id.tvFareType)
        val tvOutDepInfo  = outCard.findViewById<TextView>(R.id.tvDepInfo)
        val tvOutArrInfo  = outCard.findViewById<TextView>(R.id.tvArrInfo)
        val tvOutDepCity  = outCard.findViewById<TextView>(R.id.tvDepCity)
        val tvOutArrCity  = outCard.findViewById<TextView>(R.id.tvArrCity)

        // 오는 편 카드 내부
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

        // ========== 전달값 ==========
        val outFlight = intent.getSerializableExtra(EXTRA_OUT_FLIGHT) as? Flight
        val inFlight  = intent.getSerializableExtra(EXTRA_IN_FLIGHT)  as? Flight

        val adultCount = intent.getIntExtra(EXTRA_ADULT_COUNT, 1)
        val childCount = intent.getIntExtra(EXTRA_CHILD_COUNT, 0)
        val outDate    = intent.getStringExtra(EXTRA_OUT_DATE).orEmpty()
        val inDate     = intent.getStringExtra(EXTRA_IN_DATE).orEmpty()

        // ========== 카드 바인딩 ==========
        outFlight?.let { f ->
            // 좌상단 편명 / 우상단 요금제
            // 가는 편
            tvOutFlightNo.text = outFlight?.flNo ?: ""
            tvOutFareType.text = "이코노미"

            // 날짜가 넘어오면 "날짜 시간", 없으면 시간만
            tvOutDepInfo.text  = listOf(outDate, f.depTime).filter { it.isNotBlank() }.joinToString(" ")
            tvOutArrInfo.text  = listOf(outDate, f.arrTime).filter { it.isNotBlank() }.joinToString(" ")

            tvOutDepCity.text  = f.dep
            tvOutArrCity.text  = f.arr
        }

        inFlight?.let { f ->
            tvInFlightNo.text = inFlight?.flNo ?: ""
            tvInFareType.text = "이코노미"

            tvInDepInfo.text  = listOf(inDate, f.depTime).filter { it.isNotBlank() }.joinToString(" ")
            tvInArrInfo.text  = listOf(inDate, f.arrTime).filter { it.isNotBlank() }.joinToString(" ")

            tvInDepCity.text  = f.dep
            tvInArrCity.text  = f.arr
        }

        // ========== 운임 계산(고정요금) ==========
        val people        = adultCount + childCount
        val baseFare      = adultCount * ADULT_PRICE + childCount * CHILD_PRICE
        val fuelTotal     = people * FUEL_SURCHARGE
        val facilityTotal = people * FACILITY_FEE
        val total         = baseFare + fuelTotal + facilityTotal

        tvFareBase.text     = "%,d원".format(baseFare)
        tvFareFuel.text     = "%,d원".format(fuelTotal)
        tvFareFacility.text = "%,d원".format(facilityTotal)
        tvTotal.text        = "%,d원".format(total)

        // ========== 전체 동의 ↔ 하위 동의 연동 ==========
        fun syncAllFromChildren() {
            val allChecked = childCbs.all { it.isChecked }
            if (cbAll.isChecked != allChecked) cbAll.isChecked = allChecked
            btnNext.isEnabled = allChecked
        }

        cbAll.setOnCheckedChangeListener { _, checked ->
            // 리스너 일시 해제 후 일괄 반영 → 다시 연결
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
        // 초기 상태 반영
        syncAllFromChildren()

        // ========== 다음 ==========
        btnNext.setOnClickListener {
            if (!btnNext.isEnabled) return@setOnClickListener

            startActivity(
                Intent(this, PaymentActivity::class.java).apply {
                    putExtra("EXTRA_TOTAL", total)
                    putExtra("EXTRA_BASE", baseFare)
                    putExtra("EXTRA_FUEL", fuelTotal)
                    putExtra("EXTRA_FACILITY", facilityTotal)
                    putExtra(EXTRA_OUT_FLIGHT, outFlight)
                    putExtra(EXTRA_IN_FLIGHT, inFlight)
                    putExtra(EXTRA_ADULT_COUNT, adultCount)
                    putExtra(EXTRA_CHILD_COUNT, childCount)
                }
            )
        }
    }
}
