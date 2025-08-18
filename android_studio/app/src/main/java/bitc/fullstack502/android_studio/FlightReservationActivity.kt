package bitc.fullstack502.android_studio

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.adapter.FlightAdapter
import bitc.fullstack502.android_studio.viewmodel.FlightReservationViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FlightReservationActivity : AppCompatActivity() {

    private val viewModel: FlightReservationViewModel by viewModels()

    // 🔄 스위치/날짜 라벨/행
    private lateinit var switchTrip: SwitchCompat
    private lateinit var tvDate: TextView
    private lateinit var rowDate: View

    // 출/도착/스왑
    private lateinit var tvFrom: TextView
    private lateinit var tvTo: TextView
    private lateinit var btnSwap: com.google.android.material.floatingactionbutton.FloatingActionButton

    // 승객/검색/리스트  ✅ Chip 제거
    private lateinit var tvPax: TextView
    private lateinit var rowPax: View
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
        switchTrip = findViewById(R.id.switchTripType)
        tvDate     = findViewById(R.id.tvDate)

        // (nullable 로 받기)
        val rowDateView: View? = findViewById(R.id.rowDate)

        tvFrom  = findViewById(R.id.tvFrom)
        tvTo    = findViewById(R.id.tvTo)
        btnSwap = findViewById(R.id.btnSwap)

        tvPax   = findViewById(R.id.tvPax)
        val rowPaxView: View? = findViewById(R.id.rowPax)

        btnSearch = findViewById(R.id.btnSearch)
        rvResults = findViewById(R.id.rvResults)


        // 초기 출/도착
        setDeparture("김포(서울)", recordNonJeju = true)
        setArrival("제주", recordNonJeju = false)

        // 리스트
        flightAdapter = FlightAdapter(mutableListOf())
        rvResults.apply {
            layoutManager = LinearLayoutManager(this@FlightReservationActivity)
            itemAnimator = DefaultItemAnimator()
            adapter = flightAdapter
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

        // ✅ 편도/왕복 스위치 (true=왕복 / false=편도)
        isRoundTrip = switchTrip.isChecked
        tvDate.text = if (isRoundTrip) "가는 날 ~ 오는 날 선택" else "출발 날짜 선택"

        switchTrip.setOnCheckedChangeListener { _, checked ->
            isRoundTrip = checked
            tvDate.text = if (checked) "가는 날 ~ 오는 날 선택" else "출발 날짜 선택"
            outDateYmd = null
            inDateYmd  = null
        }

// 날짜 선택
        val dateClicker = View.OnClickListener {
            if (isRoundTrip) showRangeDatePicker() else showSingleDatePicker()
        }
        rowDateView?.setOnClickListener(dateClicker) // ← nullable
        tvDate.setOnClickListener(dateClicker)

// 인원수
        val paxClicker = View.OnClickListener { showPassengerPickerDialog() }
        rowPaxView?.setOnClickListener(paxClicker)   // ← nullable
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
            tvPax.text = "총 $total 명"   // ✅ chip → tv 로 갱신
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
}
