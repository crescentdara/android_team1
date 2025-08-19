package bitc.fullstack502.android_studio

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.adapter.FlightAdapter
import bitc.fullstack502.android_studio.model.Flight
import bitc.fullstack502.android_studio.viewmodel.FlightReservationViewModel
import com.google.android.material.button.MaterialButton

class InboundSelectActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_OUTBOUND  = "EXTRA_OUTBOUND"
        const val EXTRA_OUT_PRICE = "EXTRA_OUT_PRICE"
        const val EXTRA_DEP       = "EXTRA_DEP"    // inbound dep (기존 도착)
        const val EXTRA_ARR       = "EXTRA_ARR"    // inbound arr (기존 출발)
        const val EXTRA_DATE      = "EXTRA_DATE"   // yyyy-MM-dd
        const val EXTRA_ADULT     = "EXTRA_ADULT"
        const val EXTRA_CHILD     = "EXTRA_CHILD"
    }

    private val vm: FlightReservationViewModel by viewModels()

    private lateinit var scroll: NestedScrollView
    private lateinit var rv: RecyclerView
    private lateinit var bottomBar: View
    private lateinit var tvTotal: TextView
    private lateinit var btnPay: MaterialButton
    private lateinit var tvFrom: TextView
    private lateinit var tvTo: TextView
    private lateinit var tvDate: TextView

    private lateinit var tvPaxSummary: TextView

    private lateinit var adapter: FlightAdapter

    private var outFlight: Flight? = null
    private var outPrice: Int = 0
    private var inFlight: Flight? = null
    private var inPrice: Int = 0

    // 1인 기준(편도 한 구간)
    private fun perAdultOneWay(): Int =
        FlightReservationActivity.ADULT_PRICE +
                FlightReservationActivity.FUEL_SURCHARGE +
                FlightReservationActivity.FACILITY_FEE

    private fun perChildOneWay(): Int {
        val childFare = FlightReservationActivity.ADULT_PRICE - 20_000
        return childFare +
                FlightReservationActivity.FUEL_SURCHARGE +
                FlightReservationActivity.FACILITY_FEE
    }

    private fun roundTripTotal(adults: Int, children: Int, infants: Int): Int {
        val oneWay = adults * perAdultOneWay() + children * perChildOneWay()
        // 유아는 0원(좌석 미점유 가정)
        return oneWay * 2 // 왕복
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbound_select)

        val adult = intent.getIntExtra(EXTRA_ADULT, 1)
        val child = intent.getIntExtra(EXTRA_CHILD, 0)
        // 유아 UI는 없으니 0으로 고정
        val infant = 0

        // ---- findViewById
        scroll = findViewById(R.id.scroll)
        rv = findViewById(R.id.rvResults)
        bottomBar = findViewById(R.id.bottomBar)
        tvTotal = findViewById(R.id.tvTotalPrice)
        btnPay = findViewById(R.id.btnProceed)
        tvFrom = findViewById(R.id.tvFrom)
        tvTo = findViewById(R.id.tvTo)
        tvDate = findViewById(R.id.tvDate)
        tvPaxSummary = findViewById(R.id.tvPax)

        // ---- 인텐트 값 읽기 (없으면 종료)
        outFlight = intent.getSerializableExtra(EXTRA_OUTBOUND) as? Flight
        outPrice = intent.getIntExtra(EXTRA_OUT_PRICE, 0)
        val dep = intent.getStringExtra(EXTRA_DEP)
        val arr = intent.getStringExtra(EXTRA_ARR)
        val date = intent.getStringExtra(EXTRA_DATE)

        if (dep.isNullOrBlank() || arr.isNullOrBlank() || date.isNullOrBlank()) {
            Toast.makeText(this, "오는 편 정보가 부족합니다.", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        // 상단 요약 표시
        tvFrom.text = dep
        tvTo.text = arr
        tvDate.text = date
        tvPaxSummary.text = "총 ${adult + child + infant}명"

        // ---- 어댑터 세팅 (같은 FlightAdapter 재사용)
        adapter = FlightAdapter(
            mutableListOf(),
            onSelect = { flight, _, price ->
                inFlight = flight
                inPrice  = price

                // ▼▼▼ 여기 변경: "결제하기" → "승객 정보 입력", openPayment() → openPassengerInput()
                val totalAmountRoundTrip = roundTripTotal(adult, child, infant)

                showBottomBar(
                    totalWon = totalAmountRoundTrip,
                    buttonText = "승객 정보 입력"
                ) {
                    val out = outFlight
                    val inbound = inFlight
                    if (out == null || inbound == null) {
                        Toast.makeText(this, "오는 편을 먼저 선택하세요.", Toast.LENGTH_SHORT).show()
                        return@showBottomBar
                    }
                    openPassengerInput(
                        outFlight = out,
                        inFlight  = inbound,
                        outPrice  = outPrice,
                        inPrice   = inPrice,
                        adult     = adult,
                        child     = child,
                        infant    = infant // 현재 0
                    )
                }
                // 살짝 밀어 올려서 바텀바가 가리지 않게
                runCatching { scroll.post { scroll.smoothScrollBy(0, dp(56)) } }
            },
            priceOf = { 98_700 }
        )
        rv.layoutManager = LinearLayoutManager(this)
        rv.itemAnimator = DefaultItemAnimator()
        rv.adapter = adapter

        // ---- 결과 구독 (오는편 리스트 구독!)
        vm.inFlights.observe(this) { list ->
            hideBottomBar()               // 새로 로딩될 때 모달은 숨김
            adapter.update(list ?: emptyList())
        }
        vm.error.observe(this) { it?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() } }

        // ✅ 들어오자마자 '오는 편' 검색
        vm.searchInboundFlights(dep, arr, date, null)
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    // === 하단 모달(바텀바) 헬퍼 ===
    private fun showBottomBar(
        totalWon: Int,
        buttonText: String,
        onClick: () -> Unit
    ) {
        // 금액/버튼 세팅
        tvTotal.text = "₩%,d".format(totalWon)
        btnPay.text = buttonText
        btnPay.setOnClickListener { onClick() }

        // 애니메이션으로 등장
        if (bottomBar.visibility != View.VISIBLE) {
            bottomBar.visibility = View.VISIBLE
            bottomBar.translationY =
                (bottomBar.height.takeIf { it > 0 } ?: dp(120)).toFloat()
            bottomBar.animate().translationY(0f).setDuration(220).start()
        }
    }

    private fun hideBottomBar() {
        if (bottomBar.visibility == View.VISIBLE) {
            bottomBar.animate()
                .translationY(bottomBar.height.toFloat())
                .setDuration(180)
                .withEndAction {
                    bottomBar.visibility = View.GONE
                    bottomBar.translationY = 0f
                }
                .start()
        }
    }

    private fun openPassengerInput(
        outFlight: Flight,
        inFlight: Flight,
        outPrice: Int,
        inPrice: Int,
        adult: Int,
        child: Int,
        infant: Int
    ) {
        startActivity(
            Intent(this, PassengerInputActivity::class.java).apply {
                putExtra(FlightReservationActivity.EXTRA_TRIP_TYPE, "ROUND_TRIP")
                putExtra(FlightReservationActivity.EXTRA_OUTBOUND, outFlight)
                putExtra(FlightReservationActivity.EXTRA_OUT_PRICE, outPrice)
                putExtra(FlightReservationActivity.EXTRA_INBOUND, inFlight)
                putExtra(FlightReservationActivity.EXTRA_IN_PRICE, inPrice)
                putExtra(FlightReservationActivity.EXTRA_ADULT, adult)
                putExtra(FlightReservationActivity.EXTRA_CHILD, child)
                putExtra(FlightReservationActivity.EXTRA_INFANT, infant) //=0
            }
        )
    }


}
