package bitc.fullstack502.android_studio

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.adapter.FlightAdapter
import bitc.fullstack502.android_studio.model.Flight
import bitc.fullstack502.android_studio.viewmodel.FlightReservationViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FlightReservationActivity : AppCompatActivity() {

    // 고정 운임 상수 (Itinerary/Payment와 동일 기준)
    private companion object {
        const val ADULT_PRICE    = 98_700
        const val CHILD_PRICE    = 78_700
        const val FUEL_SURCHARGE = 15_400   // 1인당
        const val FACILITY_FEE   = 8_000    // 1인당
    }

    // ▼▼▼ 스크롤/하단바
    private lateinit var scroll: NestedScrollView
    private lateinit var bottomBar: View
    private lateinit var tvTotalPrice: TextView
    private lateinit var btnProceed: MaterialButton

    private var selectedOut: Flight? = null
    private var selectedOutPrice: Int? = null

    private val viewModel: FlightReservationViewModel by viewModels()

    // 🔄 스위치/날짜
    private lateinit var switchTrip: SwitchCompat
    private lateinit var tvDate: TextView

    // 출/도착/스왑
    private lateinit var tvFrom: TextView
    private lateinit var tvTo: TextView
    private lateinit var btnSwap: com.google.android.material.floatingactionbutton.FloatingActionButton

    // 승객/검색/리스트
    private lateinit var tvPax: TextView
    private lateinit var btnSearch: MaterialButton
    private lateinit var rvResults: RecyclerView
    private lateinit var flightAdapter: FlightAdapter

    // 상태
    private var isRoundTrip = true
    private var adultCount = 1
    private var childCount = 0

    // API용 날짜(yyyy-MM-dd)
    private var outDateYmd: String? = null
    private var inDateYmd: String? = null

    // 제주↔제주 방지
    private var lastNonJejuForDeparture: String = "김포(서울)"
    private var lastNonJejuForArrival: String = "김포(서울)"

    private val airports = listOf(
        "김포(서울)", "인천", "김해(부산)", "대구", "청주", "광주", "무안",
        "여수", "울산", "원주", "양양", "사천(진주)", "포항", "군산", "제주"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_reservation)

        // ----- findViewById -----
        scroll       = findViewById(R.id.scroll)
        bottomBar    = findViewById(R.id.bottomBar)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        btnProceed   = findViewById(R.id.btnProceed)

        val rowDateView: View? = findViewById(R.id.rowDate)
        val rowPaxView: View?  = findViewById(R.id.rowPax)

        switchTrip = findViewById(R.id.switchTripType)
        tvDate     = findViewById(R.id.tvDate)
        tvFrom     = findViewById(R.id.tvFrom)
        tvTo       = findViewById(R.id.tvTo)
        btnSwap    = findViewById(R.id.btnSwap)
        tvPax      = findViewById(R.id.tvPax)
        btnSearch  = findViewById(R.id.btnSearch)
        rvResults  = findViewById(R.id.rvResults)

        // 초기 출/도착
        setDeparture("김포(서울)", recordNonJeju = true)
        setArrival("제주", recordNonJeju = false)

        // 리스트 + 콜백 연결 (item, position, price)
        flightAdapter = FlightAdapter(
            mutableListOf(),
            onSelect = { flight, position, price ->
                onFlightSelected(flight, position, price)
            },
            priceOf = { 98_700 }
        )
        rvResults.apply {
            layoutManager = LinearLayoutManager(this@FlightReservationActivity)
            itemAnimator  = DefaultItemAnimator()
            adapter       = flightAdapter
        }

        // 뷰모델 옵저브
        viewModel.flights.observe(this) { list ->
            Log.d("FLIGHT_UI", "observe size=${list?.size ?: 0}")
            flightAdapter.update(list ?: emptyList())
        }
        viewModel.error.observe(this) { msg ->
            msg?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }
        viewModel.loading.observe(this) { loading ->
            btnSearch.isEnabled = !loading
            btnSearch.text = if (loading) "검색 중…" else "항공편 검색"
        }

        // 편도/왕복 스위치
        isRoundTrip = switchTrip.isChecked
        tvDate.text = if (isRoundTrip) "가는 날 ~ 오는 날 선택" else "출발 날짜 선택"
        updateProceedCta()  // ⬅️ 초기 CTA 세팅

        switchTrip.setOnCheckedChangeListener { _, checked ->
            isRoundTrip = checked
            tvDate.text = if (checked) "가는 날 ~ 오는 날 선택" else "출발 날짜 선택"
            outDateYmd = null
            inDateYmd  = null
            updateProceedCta()  // ⬅️ 모드 바뀔 때 CTA 업데이트
        }

        // 날짜 선택
        val dateClicker = View.OnClickListener {
            if (isRoundTrip) showRangeDatePicker() else showSingleDatePicker()
        }
        rowDateView?.setOnClickListener(dateClicker)
        tvDate.setOnClickListener(dateClicker)

        // 인원수
        val paxClicker = View.OnClickListener { showPassengerPickerDialog() }
        rowPaxView?.setOnClickListener(paxClicker)
        tvPax.setOnClickListener(paxClicker)

        // 출/도착 선택 & 스왑
        tvFrom.setOnClickListener { showAirportModalAll(true) }
        tvTo.setOnClickListener   { showAirportModalAll(false) }
        btnSwap.setOnClickListener { swapAirports() }

        // 검색
        btnSearch.setOnClickListener {
            val depDisplay = tvFrom.text.toString()
            val arrDisplay = tvTo.text.toString()
            val dep = normalizeAirport(depDisplay)
            val arr = normalizeAirport(arrDisplay)

            Log.d("FLIGHT_BTN", "dep=$dep, arr=$arr, outDate=$outDateYmd, inDate=$inDateYmd, round=$isRoundTrip")

            if (isRoundTrip) {
                if (outDateYmd == null || inDateYmd == null) {
                    Toast.makeText(this, "가는 날과 오는 날을 선택하세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.searchFlights(dep, arr, outDateYmd!!, null)       // 가는편
                viewModel.searchInboundFlights(arr, dep, inDateYmd!!, null) // 오는편
            } else {
                if (outDateYmd == null) {
                    Toast.makeText(this, "출발 날짜를 선택하세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.searchFlights(dep, arr, outDateYmd!!, null)
            }
        }

        // 초기 텍스트
        tvPax.text = "총 1명"
    }

    /** 항공편 선택 시: 금액 갱신 + 하단바 노출 + CTA 보장 + 스크롤 살짝 내리기 */
    private fun onFlightSelected(item: Flight, position: Int, price: Int) {
        selectedOut = item
        selectedOutPrice = price

        // ✅ 하단바 띄우기
        showBottomBarSimple(
            bottomBar = bottomBar,
            tvTotal   = tvTotalPrice,
            btn       = btnProceed,
            amount    = price,
            buttonText = if (isRoundTrip) "오는 편 선택하기" else "결제하기"
        ) {
            if (isRoundTrip) openInboundSelection() else openPayment()
        }

        scroll.post { scroll.smoothScrollBy(0, dp(56)) }
    }

    /* ---------------------- 출/도착/스왑 ---------------------- */
    private fun setDeparture(value: String, recordNonJeju: Boolean) {
        tvFrom.text = value
        if (value != "제주" && recordNonJeju) lastNonJejuForDeparture = value
        if (value == "제주" && tvTo.text.toString() == "제주") {
            setArrival(lastNonJejuForArrival.ifBlank { "김포(서울)" }, true)
        }
    }

    private fun setArrival(value: String, recordNonJeju: Boolean) {
        tvTo.text = value
        if (value != "제주" && recordNonJeju) lastNonJejuForArrival = value
        if (value == "제주" && tvFrom.text.toString() == "제주") {
            setDeparture(lastNonJejuForDeparture.ifBlank { "김포(서울)" }, true)
        }
    }

    private fun swapAirports() {
        val dep = tvFrom.text.toString()
        val arr = tvTo.text.toString()
        setDeparture(arr, arr != "제주")
        setArrival(dep, dep != "제주")
        btnSwap.animate().rotationBy(180f).setDuration(200).start()
    }

    private fun showAirportModalAll(forDeparture: Boolean) {
        val items = airports.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(if (forDeparture) "출발지 선택" else "도착지 선택")
            .setItems(items) { dialog, which ->
                val chosen = items[which]
                if (forDeparture) setDeparture(chosen, chosen != "제주")
                else setArrival(chosen, chosen != "제주")
                dialog.dismiss()
            }.show()
    }

    /* ---------------------- 날짜 선택 ---------------------- */
    private fun showSingleDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("출발 날짜")
            .build()
        picker.addOnPositiveButtonClickListener { utcMillis ->
            val displayFmt = SimpleDateFormat("MM.dd(E)", Locale.KOREA).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val apiFmt = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            tvDate.text = displayFmt.format(Date(utcMillis))
            outDateYmd = apiFmt.format(Date(utcMillis))
            inDateYmd = null
        }
        picker.show(supportFragmentManager, "single_date")
    }

    private fun showRangeDatePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("가는 날과 오는 날")
            .build()
        picker.addOnPositiveButtonClickListener { range ->
            val start = range.first
            val end = range.second
            if (start != null && end != null) {
                val displayFmt = SimpleDateFormat("MM.dd(E)", Locale.KOREA).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val apiFmt = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                tvDate.text = "${displayFmt.format(Date(start))} ~ ${displayFmt.format(Date(end))}"
                outDateYmd = apiFmt.format(Date(start))
                inDateYmd  = apiFmt.format(Date(end))
            }
        }
        picker.show(supportFragmentManager, "range_date")
    }

    /* ---------------------- 승객 수 ---------------------- */
    private fun showPassengerPickerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_passenger_picker, null)
        val tvAdultCount = dialogView.findViewById<TextView>(R.id.tv_adult_count)
        val tvChildCount = dialogView.findViewById<TextView>(R.id.tv_child_count)
        val btnAdultMinus = dialogView.findViewById<Button>(R.id.btn_adult_minus)
        val btnAdultPlus = dialogView.findViewById<Button>(R.id.btn_adult_plus)
        val btnChildMinus = dialogView.findViewById<Button>(R.id.btn_child_minus)
        val btnChildPlus = dialogView.findViewById<Button>(R.id.btn_child_plus)
        val btnConfirmPassenger = dialogView.findViewById<Button>(R.id.btn_confirm_passenger)

        tvAdultCount.text = adultCount.toString()
        tvChildCount.text = childCount.toString()

        btnAdultMinus.setOnClickListener {
            if (adultCount > 1) adultCount--
            tvAdultCount.text = adultCount.toString()
        }
        btnAdultPlus.setOnClickListener {
            adultCount++
            tvAdultCount.text = adultCount.toString()
        }
        btnChildMinus.setOnClickListener {
            if (childCount > 0) childCount--
            tvChildCount.text = childCount.toString()
        }
        btnChildPlus.setOnClickListener {
            childCount++
            tvChildCount.text = childCount.toString()
        }

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        btnConfirmPassenger.setOnClickListener {
            val total = adultCount + childCount
            tvPax.text = "총 $total 명"
            dialog.dismiss()
        }
        dialog.show()
    }

    /* ---------------------- 매핑 ---------------------- */
    private fun normalizeAirport(display: String): String {
        val s = display.trim()
        return when {
            s.contains("김포")     -> "서울/김포"
            s.contains("인천")     -> "서울/인천"
            s.contains("김해") || s.contains("부산") -> "부산/김해"
            s.contains("사천") || s.contains("진주") -> "사천"
            else -> s
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    /** 편도/왕복에 따라 하단 버튼 문구/동작 세팅 */
    private fun updateProceedCta() {
        if (isRoundTrip) {
            btnProceed.text = "오는 편 선택하기"
            btnProceed.setOnClickListener { openInboundSelection() }
        } else {
            btnProceed.text = "결제하기"
            btnProceed.setOnClickListener { openPayment() }
        }
    }

    private fun openInboundSelection() {
        if (selectedOut == null) {
            Toast.makeText(this, "먼저 가는 편을 선택하세요", Toast.LENGTH_SHORT).show(); return
        }
        if (inDateYmd.isNullOrBlank()) {
            Toast.makeText(this, "오는 날을 선택하세요", Toast.LENGTH_SHORT).show(); return
        }

        val depInbound = normalizeAirport(tvTo.text.toString())   // IN dep = 기존 도착
        val arrInbound = normalizeAirport(tvFrom.text.toString()) // IN arr = 기존 출발

        startActivity(Intent(this, InboundSelectActivity::class.java).apply {
            putExtra(InboundSelectActivity.EXTRA_OUTBOUND, selectedOut)          // Flight: Serializable 필요
            putExtra(InboundSelectActivity.EXTRA_OUT_PRICE, selectedOutPrice ?: 0)
            putExtra(InboundSelectActivity.EXTRA_DEP,  depInbound)
            putExtra(InboundSelectActivity.EXTRA_ARR,  arrInbound)
            putExtra(InboundSelectActivity.EXTRA_DATE, inDateYmd)                // yyyy-MM-dd
            putExtra(InboundSelectActivity.EXTRA_ADULT, adultCount)
            putExtra(InboundSelectActivity.EXTRA_CHILD, childCount)
        })
    }

    /** 편도일 때 결제 화면으로 직행 */
    private fun openPayment() {
        val out = selectedOut
        if (out == null) {
            Toast.makeText(this, "먼저 항공편을 선택하세요", Toast.LENGTH_SHORT).show()
            return
        }

        val people        = adultCount + childCount
        val baseFare      = adultCount * ADULT_PRICE + childCount * CHILD_PRICE
        val fuelTotal     = people * FUEL_SURCHARGE
        val facilityTotal = people * FACILITY_FEE
        val total         = baseFare + fuelTotal + facilityTotal

        startActivity(Intent(this, PaymentActivity::class.java).apply {
            putExtra("EXTRA_TOTAL", total)
            putExtra("EXTRA_BASE", baseFare)
            putExtra("EXTRA_FUEL", fuelTotal)
            putExtra("EXTRA_FACILITY", facilityTotal)

            // 선택 정보(필요 시 Payment 화면에서 활용)
            putExtra(ItineraryActivity.EXTRA_OUT_FLIGHT, out)           // Flight은 Serializable이어야 함
            putExtra(ItineraryActivity.EXTRA_ADULT_COUNT, adultCount)
            putExtra(ItineraryActivity.EXTRA_CHILD_COUNT, childCount)
        })
    }

    // === 하단 고정 바 관련 helper ===
    private fun Int.asWon(): String = "₩%,d".format(this)

    private fun View.slideUpShow(offsetPxIfUnknown: Int = 160, duration: Long = 220) {
        if (visibility != View.VISIBLE) {
            visibility = View.VISIBLE
            translationY = (height.takeIf { it > 0 } ?: offsetPxIfUnknown).toFloat()
            animate().translationY(0f).setDuration(duration).start()
        }
    }

    private fun View.slideDownHide(duration: Long = 200) {
        if (visibility == View.VISIBLE) {
            animate().translationY(height.toFloat())
                .setDuration(duration)
                .withEndAction { visibility = View.GONE; translationY = 0f }
                .start()
        }
    }

    private fun showBottomBarSimple(
        bottomBar: View,
        tvTotal: TextView,
        btn: MaterialButton,
        amount: Int,
        buttonText: String,
        onClick: () -> Unit
    ) {
        tvTotal.text = amount.asWon()
        btn.text = buttonText
        btn.setOnClickListener { onClick() }
        bottomBar.slideUpShow()
    }
}
