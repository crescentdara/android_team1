package bitc.fullstack502.android_studio

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
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

class FlightReservationActivity : AppCompatActivity() {

    private val binding by lazy { ActivityFlightReservationBinding.inflate(layoutInflater) }

    private var adultCount = 1
    private var childCount = 0

    private val airports = listOf(
"김포(서울)", "인천", "김해(부산)", "대구", "광주", "무안", "여수",
        "울산", "청주", "원주", "양양", "사천(진주)", "포항", "군산"
    )

    private var lastNonJejuForDeparture: String = "김포(서울)"
    private var lastNonJejuForArrival: String = "김포(서울)"
    private var isRoundTrip = true

    private val viewModel: FlightReservationViewModel by viewModels()

    private lateinit var flightAdapter: FlightAdapter

//    private val viewModel: FlightViewModel by viewModels()

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
        flightAdapter = FlightAdapter(emptyList())
        binding.recyclerFlight.apply {
            layoutManager = LinearLayoutManager(this@FlightReservationActivity)
            adapter = flightAdapter
        }

        // ✅ ViewModel 에서 항공편 데이터 관찰
        viewModel.flights.observe(this, Observer { flightList ->
            if (flightList != null) {
                flightAdapter = FlightAdapter(flightList) // 새 리스트로 갱신
                binding.recyclerFlight.adapter = flightAdapter
            }
        })

        // 항공편 검색 버튼 클릭 시 -> 서버에서 데이터 요청
        binding.btnSearchFlight.setOnClickListener {
            val dep = binding.tvDeparture.text.toString()
            val arr = binding.tvArrival.text.toString()
            val date = binding.btnDate.text.toString()
            val passenger = binding.btnPassenger.text.toString()

            // ViewModel 호출해서 MySQL(API)에서 데이터 가져오기
            viewModel.searchFlights(dep, arr, date, passenger)

            Log.d("*** fullstack ***", "$dep , $arr , $date , $passenger")
        }

        // 편도/왕복 토글
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
        }
        binding.toggleTripType.check(R.id.btn_round_trip)

        // 날짜 선택
        binding.btnDate.setOnClickListener {
            if (isRoundTrip) showRangeDatePicker() else showSingleDatePicker()
        }

        // 인원 선택
        binding.btnPassenger.setOnClickListener { showPassengerPickerDialog() }

        // 제주가 아닌 경우에만 모달
        binding.tvDeparture.setOnClickListener {
            if (currentDeparture() != "제주") showAirportModalAll(true)
        }
        binding.tvArrival.setOnClickListener {
            if (currentArrival() != "제주") showAirportModalAll(false)
        }

        // ↔ 스왑
        binding.tvSwap.setOnClickListener { swapAirports() }
    }

    private fun setDeparture(value: String, recordNonJeju: Boolean) {
        binding.tvDeparture.text = value
        if (value != "제주" && recordNonJeju) lastNonJejuForDeparture = value
        if (value != "제주" && currentArrival() != "제주") {
            setArrival("제주", false)
        } else if (value == "제주" && currentArrival() == "제주") {
            setArrival(lastNonJejuForArrival.ifBlank { "김포(서울)" }, true)
        }
        
    }

    private fun setArrival(value: String, recordNonJeju: Boolean) {
        binding.tvArrival.text = value
        if (value != "제주" && recordNonJeju) lastNonJejuForArrival = value
        if (value != "제주" && currentDeparture() != "제주") {
            setDeparture("제주", false)
        } else if (value == "제주" && currentDeparture() == "제주") {
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
                if (forDeparture) {
                    setDeparture(chosen, chosen != "제주")
                } else {
                    setArrival(chosen, chosen != "제주")
                }
                dialog.dismiss()
            }.show()
    }

    // 편도
    private fun showSingleDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("날짜 선택")
            .setTheme(R.style.CustomDatePicker)
            .build()
        picker.addOnPositiveButtonClickListener { utcMillis ->
            val fmt = SimpleDateFormat("MM.dd(E)", Locale.KOREA)
            fmt.timeZone = TimeZone.getTimeZone("UTC")
            binding.btnDate.text = fmt.format(Date(utcMillis))
        }
        picker.show(supportFragmentManager, "single_date")
    }

    // 왕복
    private fun showRangeDatePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("가는 날과 오는 날 선택")
            .setTheme(R.style.CustomDatePicker)
            .build()
        picker.addOnPositiveButtonClickListener { range ->
            val start = range.first
            val end = range.second
            if (start != null && end != null) {
                val fmt = SimpleDateFormat("MM.dd(E)", Locale.KOREA)
                fmt.timeZone = TimeZone.getTimeZone("UTC")
                binding.btnDate.text = "${fmt.format(Date(start))} ~ ${fmt.format(Date(end))}"
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
}
