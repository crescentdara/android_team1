package bitc.fullstack502.android_studio

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import bitc.fullstack502.android_studio.adapter.FlightAdapter
import bitc.fullstack502.android_studio.databinding.ActivityFlightReservationBinding
import bitc.fullstack502.android_studio.viewmodel.FlightReservationViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ✅ 유틸 import 추가
import bitc.fullstack502.android_studio.util.displayToYmd
import bitc.fullstack502.android_studio.util.displayRangeToYmdPair

class FlightReservationActivity : AppCompatActivity() {

    private val binding by lazy { ActivityFlightReservationBinding.inflate(layoutInflater) }

    private var adultCount = 1
    private var childCount = 0

    private val airports = listOf(
        "김포(서울)", "인천", "김해(부산)", "대구", "광주", "무안", "여수",
        "울산", "청주", "원주", "양양", "사천(진주)", "포항", "군산", "제주"
    )

    private var lastNonJejuForDeparture: String = "김포(서울)"
    private var lastNonJejuForArrival: String = "김포(서울)"
    private var isRoundTrip = true

    private val viewModel: FlightReservationViewModel by viewModels()

    private lateinit var flightAdapter: FlightAdapter

    // ✅ API에 보낼 “yyyy-MM-dd” 값 저장
    private var outDateYmd: String? = null
    private var inDateYmd: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }

        setDeparture("김포(서울)", recordNonJeju = true)
        setArrival("제주", recordNonJeju = false)

        // ✅ RecyclerView 초기화
        // ➊ 어댑터를 "한 번만" 만들고 계속 update()로 갱신
        flightAdapter = FlightAdapter(mutableListOf())
        binding.recyclerFlight.apply {
            layoutManager = LinearLayoutManager(this@FlightReservationActivity)
            adapter = flightAdapter
        }

        // ✅ 결과 관찰: 어댑터 update()
        viewModel.flights.observe(this) { list ->
            Log.d("FLIGHT_UI", "observe size=${list?.size ?: 0}")
            flightAdapter.update(list ?: emptyList())
        }
        viewModel.error.observe(this) { msg ->
            msg?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }

        // ✅ 편도/왕복 토글
        binding.toggleTripType.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            isRoundTrip = (checkedId == R.id.btn_round_trip)
            binding.btnDate.text = if (isRoundTrip) "가는 날 ~ 오는 날" else "출발 날짜"
            // 색상 업데이트
            for (i in 0 until group.childCount) {
                val btn = group.getChildAt(i) as MaterialButton
                if (btn.id == checkedId) {
                    btn.setBackgroundColor(getColor(R.color.jeju_primary_dark))
                    btn.setTextColor(getColor(android.R.color.white))
                } else {
                    btn.setBackgroundColor(getColor(R.color.jeju_tint))
                    btn.setTextColor(getColor(R.color.jeju_primary))
                }
            }
            // 모드 바뀌면 날짜 초기화
            outDateYmd = null
            inDateYmd = null
        }
        binding.toggleTripType.check(R.id.btn_round_trip)

        // ✅ 날짜 선택
        binding.btnDate.setOnClickListener {
            if (isRoundTrip) showRangeDatePicker() else showSingleDatePicker()
        }

        // ✅ 인원 선택
        binding.btnPassenger.setOnClickListener { showPassengerPickerDialog() }


        binding.tvDeparture.setOnClickListener { showAirportModalAll(true) }
        binding.tvArrival.setOnClickListener   { showAirportModalAll(false) }

        // ↔ 스왑
        binding.tvSwap.setOnClickListener { swapAirports() }

        // ✅ 항공편 검색 버튼 (한 번만 등록)
        binding.btnSearchFlight.setOnClickListener {
            val depDisplay = binding.tvDeparture.text.toString()
            val arrDisplay = binding.tvArrival.text.toString()

            val dep = normalizeAirport(depDisplay)
            val arr = normalizeAirport(arrDisplay)

            // 디버깅 로그로 "실제로 무엇을 보내는지" 확정
            Log.d("FLIGHT_BTN", "depDisplay=$depDisplay, arrDisplay=$arrDisplay")
            Log.d("FLIGHT_BTN", "dep=$dep, arr=$arr, outDate=$outDateYmd, inDate=$inDateYmd, round=$isRoundTrip")

            if (isRoundTrip) {
                if (outDateYmd == null || inDateYmd == null) {
                    Toast.makeText(this, "가는 날과 오는 날을 선택하세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.searchFlights(dep, arr, outDateYmd!!, null)  // 가는편
                viewModel.searchFlights(arr, dep, inDateYmd!!,  null)  // 오는편
            } else {
                if (outDateYmd == null) {
                    Toast.makeText(this, "출발 날짜를 선택하세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.searchFlights(dep, arr, outDateYmd!!, null)
            }
        }
    }


    // 교체 ✅: 한쪽이 제주면 OK, 둘 다 제주일 때만 최근 비제주로 복원
    private fun setDeparture(value: String, recordNonJeju: Boolean) {
        binding.tvDeparture.text = value
        if (value != "제주" && recordNonJeju) lastNonJejuForDeparture = value

        if (value == "제주" && currentArrival() == "제주") {
            setArrival(lastNonJejuForArrival.ifBlank { "김포(서울)" }, true)
        }
    }

    private fun setArrival(value: String, recordNonJeju: Boolean) {
        binding.tvArrival.text = value
        if (value != "제주" && recordNonJeju) lastNonJejuForArrival = value

        if (value == "제주" && currentDeparture() == "제주") {
            setDeparture(lastNonJejuForDeparture.ifBlank { "김포(서울)" }, true)
        }
    }

    private fun currentDeparture() = binding.tvDeparture.text.toString()
    private fun currentArrival() = binding.tvArrival.text.toString()

    private fun swapAirports() {
        val dep = currentDeparture()
        val arr = currentArrival()
        setDeparture(arr, arr != "제주")
        setArrival(dep, dep != "제주")
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

    // ✅ 편도 DatePicker: 표시 + API용 yyyy-MM-dd 저장
    private fun showSingleDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("날짜 선택")
            .setTheme(R.style.CustomDatePicker)
            .build()
        picker.addOnPositiveButtonClickListener { utcMillis ->
            val displayFmt = SimpleDateFormat("MM.dd(E)", Locale.KOREA).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val apiFmt = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            binding.btnDate.text = displayFmt.format(Date(utcMillis))
            outDateYmd = apiFmt.format(Date(utcMillis))
            inDateYmd = null
        }
        picker.show(supportFragmentManager, "single_date")
    }

    // ✅ 왕복 DatePicker: 표시 + API용 yyyy-MM-dd 저장(두 개)
    private fun showRangeDatePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("가는 날과 오는 날 선택")
            .setTheme(R.style.CustomDatePicker)
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
                binding.btnDate.text = "${displayFmt.format(Date(start))} ~ ${displayFmt.format(Date(end))}"
                outDateYmd = apiFmt.format(Date(start))
                inDateYmd = apiFmt.format(Date(end))
            }
        }
        picker.show(supportFragmentManager, "range_date")
    }

    @SuppressLint("MissingInflatedId")
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
            binding.btnPassenger.text = "총 $total 명"
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun normalizeAirport(display: String): String {
        val s = display.trim()
        return when {
            s.contains("김포")     -> "서울/김포"
            s.contains("인천")     -> "서울/인천"
            s.contains("김해") || s.contains("부산") -> "부산/김해"
            s.contains("사천") || s.contains("진주") -> "사천"
            else -> s // "제주","무안","청주","대구","광주","여수","울산","원주","양양","포항","군산" 등
        }
    }
}
