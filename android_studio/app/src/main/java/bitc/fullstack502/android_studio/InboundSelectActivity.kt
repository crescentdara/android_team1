package bitc.fullstack502.android_studio

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

    private lateinit var adapter: FlightAdapter

    private var outFlight: Flight? = null
    private var outPrice: Int = 0
    private var inFlight: Flight? = null
    private var inPrice: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbound_select)

        // ---- findViewById
        scroll      = findViewById(R.id.scroll)
        rv        = findViewById(R.id.rvResults)
        bottomBar = findViewById(R.id.bottomBar)
        tvTotal   = findViewById(R.id.tvTotalPrice)
        btnPay    = findViewById(R.id.btnProceed)
        tvFrom    = findViewById(R.id.tvFrom)
        tvTo      = findViewById(R.id.tvTo)
        tvDate    = findViewById(R.id.tvDate)

        // ---- 인텐트 값 읽기 (없으면 종료)
        outFlight = intent.getSerializableExtra(EXTRA_OUTBOUND) as? Flight
        outPrice  = intent.getIntExtra(EXTRA_OUT_PRICE, 0)
        val dep   = intent.getStringExtra(EXTRA_DEP)
        val arr   = intent.getStringExtra(EXTRA_ARR)
        val date  = intent.getStringExtra(EXTRA_DATE)

        if (dep.isNullOrBlank() || arr.isNullOrBlank() || date.isNullOrBlank()) {
            Toast.makeText(this, "오는 편 정보가 부족합니다.", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        // 상단 요약 표시
        tvFrom.text = dep
        tvTo.text   = arr
        tvDate.text = date

        // ---- 어댑터 세팅 (같은 FlightAdapter 재사용)
        adapter = FlightAdapter(
            mutableListOf(),
            onSelect = { flight, _, price ->
                inFlight = flight
                inPrice  = price
                // 카드 선택 시 하단 모달 표시 + “결제하기”
                showBottomBar(totalWon = outPrice + inPrice, buttonText = "결제하기") {
                    // 결제 클릭
                    openPayment(outFlight, inFlight!!, outPrice, inPrice)
                }
                // 살짝 밀어 올려서 바텀바가 가리지 않게
                runCatching { scroll.post { scroll.smoothScrollBy(0, dp(56)) } }
            },
            priceOf = { 98_700 }
        )
        rv.layoutManager = LinearLayoutManager(this)
        rv.itemAnimator  = DefaultItemAnimator()
        rv.adapter       = adapter

        // ---- 결과 구독 (오는편 리스트 구독!)
        vm.inFlights.observe(this) { list ->
            hideBottomBar()               // 새로 로딩될 때 모달은 숨김
            adapter.update(list ?: emptyList())
        }
        vm.error.observe(this) { it?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() } }

        // ✅ 들어오자마자 '오는 편' 검색
        vm.searchInboundFlights(dep, arr, date, null)
    }

    private fun openPayment(out: Flight?, `in`: Flight, outP: Int, inP: Int) {
        // TODO: 결제 화면 이동
    }

/*    // ===== 바텀바(모달) 컨트롤 =====
    private fun showBottomBar(totalWon: Int, buttonText: String, onClick: () -> Unit) {
        tvTotal.text = "₩%,d".format(totalWon)
        btnPay.text  = buttonText
        btnPay.setOnClickListener { onClick() }

        if (bottomBar.visibility != View.VISIBLE) {
            bottomBar.visibility = View.VISIBLE
            // 최초 높이 0일 수 있으니 안전한 기본값 사용
            bottomBar.translationY = (bottomBar.height.takeIf { it > 0 } ?: dp(120)).toFloat()
            bottomBar.animate().translationY(0f).setDuration(220).start()
        }
    }*/

//    private fun hideBottomBar() {
//        if (bottomBar.visibility == View.VISIBLE) {
//            bottomBar.animate()
//                .translationY(dp(120).toFloat())
//                .setDuration(180)
//                .withEndAction { bottomBar.visibility = View.GONE }
//                .start()
//        }
//    }

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

}
