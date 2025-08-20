package bitc.fullstack502.android_studio

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.adapter.PassengerSelectorAdapter
import bitc.fullstack502.android_studio.model.*
import bitc.fullstack502.android_studio.network.ApiProvider.api
import bitc.fullstack502.android_studio.util.AuthManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PassengerInputActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ADULTS = "EXTRA_ADULTS"      // (안씀: 기존 호환용)
        const val EXTRA_CHILDREN = "EXTRA_CHILDREN"  // (안씀: 기존 호환용)
        // ▼ 반드시 FlightReservationActivity에서 함께 넣어주기
        const val EXTRA_OUT_DATE = "EXTRA_OUT_DATE"   // "yyyy-MM-dd"
        const val EXTRA_IN_DATE  = "EXTRA_IN_DATE"    // 왕복일 때만, "yyyy-MM-dd"
    }

    private lateinit var rv: RecyclerView
    private lateinit var btnNext: MaterialButton
    private lateinit var adapter: PassengerSelectorAdapter
    private lateinit var passengers: MutableList<Passenger>

    private var selectedIndex = 0
    private var isBinding = false

    private lateinit var rgGender: RadioGroup

    // 폼 위젯
    private lateinit var etLast: TextInputEditText
    private lateinit var etFirst: TextInputEditText
    private lateinit var rbMale: MaterialRadioButton
    private lateinit var rbFemale: MaterialRadioButton
    private lateinit var etBirth: TextInputEditText
    private lateinit var etPassNo: TextInputEditText
    private lateinit var etPassExp: TextInputEditText
    private lateinit var etNation: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etEmgName: TextInputEditText
    private lateinit var etEmgPhone: TextInputEditText

    private fun current(): Passenger = passengers[selectedIndex]

    private fun bindEmptyForm() {
        isBinding = true
        etLast.setText("")
        etFirst.setText("")
        rgGender.clearCheck()
        etBirth.setText("")
        etPassNo.setText("")
        etPassExp.setText("")
        etNation.setText("")
        etPhone.setText("")
        etEmail.setText("")
        etEmgName.setText("")
        etEmgPhone.setText("")
        isBinding = false
        validateAll()
    }

    private fun bindForm(p: Passenger) {
        isBinding = true
        etLast.setText(p.lastNameEn)
        etFirst.setText(p.firstNameEn)
        rgGender.clearCheck()
        when (p.gender) {
            "M" -> rgGender.check(R.id.rbMale)
            "F" -> rgGender.check(R.id.rbFemale)
        }
        etBirth.setText(p.birth)
        etPassNo.setText(p.passportNo)
        etPassExp.setText(p.passportExpiry)
        etNation.setText(p.nationality)
        etPhone.setText(p.phone)
        etEmail.setText(p.email)
        etEmgName.setText(p.emergencyName)
        etEmgPhone.setText(p.emergencyPhone)
        isBinding = false
        validateAll()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passenger_input)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // 0) 버튼 초기화
        btnNext = findViewById(R.id.btnNext)
        btnNext.isEnabled = false
        btnNext.alpha = 0.5f

        // 1) 인텐트 수신
        val tripType  = intent.getStringExtra(FlightReservationActivity.EXTRA_TRIP_TYPE)
        val outFlight = intent.getSerializableExtra(FlightReservationActivity.EXTRA_OUTBOUND) as? Flight
        val inFlight  = intent.getSerializableExtra(FlightReservationActivity.EXTRA_INBOUND)  as? Flight
        val outPrice  = intent.getIntExtra(FlightReservationActivity.EXTRA_OUT_PRICE, 0)
        val inPrice   = intent.getIntExtra(FlightReservationActivity.EXTRA_IN_PRICE, 0)
        val adults    = intent.getIntExtra(FlightReservationActivity.EXTRA_ADULT, 1)
        val children  = intent.getIntExtra(FlightReservationActivity.EXTRA_CHILD, 0)
        val outDate   = intent.getStringExtra(EXTRA_OUT_DATE)  // ★ "yyyy-MM-dd"
        val inDate    = intent.getStringExtra(EXTRA_IN_DATE)   // 왕복일 때만

        if (outDate.isNullOrBlank()) {
            Toast.makeText(this, "출발 날짜 정보가 없습니다. 처음 화면에서 다시 선택하세요.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // 2) 리스트 초기화
        var idx = 0
        passengers = mutableListOf<Passenger>().apply {
            repeat(adults)   { add(Passenger(idx++, PassengerType.ADULT)) }
            repeat(children) { add(Passenger(idx++, PassengerType.CHILD)) }
        }
        rv = findViewById(R.id.rvPassengers)
        rv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        adapter = PassengerSelectorAdapter(passengers) { pos ->
            if (pos == selectedIndex) return@PassengerSelectorAdapter
            val old = selectedIndex
            if (old in passengers.indices) saveFormToModel(old)
            selectedIndex = pos
            adapter.setSelected(pos)
            val target = passengers[pos]
            if (!target.edited) bindEmptyForm() else bindForm(target)
        }
        rv.adapter = adapter

        // 3) 폼 findViewById
        etLast    = findViewById(R.id.etLastNameEn)
        etFirst   = findViewById(R.id.etFirstNameEn)
        rbMale    = findViewById(R.id.rbMale)
        rbFemale  = findViewById(R.id.rbFemale)
        etBirth   = findViewById(R.id.etBirth)
        etPassNo  = findViewById(R.id.etPassportNo)
        etPassExp = findViewById(R.id.etPassportExpiry)
        etNation  = findViewById(R.id.etNationality)
        etPhone   = findViewById(R.id.etPhone)
        etEmail   = findViewById(R.id.etEmail)
        etEmgName = findViewById(R.id.etEmergencyName)
        etEmgPhone= findViewById(R.id.etEmergencyPhone)
        rgGender  = findViewById(R.id.rgGender)

        // 4) 날짜 피커/워처
        findViewById<TextInputLayout>(R.id.tilBirth)
            .setEndIconOnClickListener { showMaterialDatePicker(etBirth) }
        findViewById<TextInputLayout>(R.id.tilPassportExpiry)
            .setEndIconOnClickListener { showMaterialDatePicker(etPassExp) }
        etBirth.setOnClickListener { showMaterialDatePicker(etBirth) }
        etPassExp.setOnClickListener { showMaterialDatePicker(etPassExp) }

        // 5) 기본 폼 바인딩 + 워처 연결
        bindForm(passengers[0])
        listOf(etLast, etFirst, etPassNo, etNation, etPhone, etEmail, etEmgName, etEmgPhone)
            .forEach { it.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) { if (!isBinding) syncCurrentPassengerAndValidate() }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }) }
        rgGender.setOnCheckedChangeListener { _, _ -> if (!isBinding) syncCurrentPassengerAndValidate() }

        // 6) 다음(=예약) 버튼
        btnNext.setOnClickListener {
            val allOk = passengers.all { it.isRequiredFilled() }
            if (!allOk) {
                Toast.makeText(this, "모든 승객의 필수 정보를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val uid = AuthManager.id()
            if (!AuthManager.isLoggedIn() || uid <= 0L) {
                Toast.makeText(this, "로그인 후 이용해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val flight = outFlight ?: run {
                Toast.makeText(this, "선택된 항공편 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 날짜 필수
            val tripDate = outDate ?: run {
                Toast.makeText(this, "출발 날짜 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val seatCnt = adults + children
            val total   = ((outPrice + inPrice) * seatCnt).toLong()

            val req = BookingRequest(
                userId = uid,
                flId = flight.id,
                seatCnt = seatCnt,
                adult = adults,
                child = children,
                tripDate = tripDate,
                totalPrice = total
            )

            api.createFlightBooking(req).enqueue(object : Callback<BookingResponse> {
                override fun onResponse(
                    call: Call<BookingResponse>,
                    response: Response<BookingResponse>
                ) {
                    if (!response.isSuccessful) {
                        val msg = when (response.code()) {
                            409 -> "잔여좌석이 부족합니다. 다른 항공편을 선택해주세요."
                            400 -> "예약 정보가 올바르지 않습니다."
                            else -> "예약 실패: ${response.code()}"
                        }
                        Toast.makeText(this@PassengerInputActivity, msg, Toast.LENGTH_SHORT).show()
                        return
                    }

                    startActivity(Intent(this@PassengerInputActivity, TicketSuccessActivity::class.java).apply {
                        putExtra("PASSENGERS", ArrayList(passengers))
                        putExtra(FlightReservationActivity.EXTRA_TRIP_TYPE, tripType)
                        putExtra(FlightReservationActivity.EXTRA_OUTBOUND, outFlight)
                        putExtra(FlightReservationActivity.EXTRA_OUT_PRICE, outPrice)
                        putExtra(FlightReservationActivity.EXTRA_INBOUND, inFlight)
                        putExtra(FlightReservationActivity.EXTRA_IN_PRICE, inPrice)
                    })
                    finish()
                }

                override fun onFailure(call: Call<BookingResponse>, t: Throwable) {
                    Toast.makeText(this@PassengerInputActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // 7) 초기 검증 반영
        validateAll()
    }

    private fun syncCurrentPassengerAndValidate() {
        val p = current()
        p.lastNameEn     = etLast.text?.toString()?.trim().orEmpty()
        p.firstNameEn    = etFirst.text?.toString()?.trim().orEmpty()
        p.gender         = when (rgGender.checkedRadioButtonId) {
            R.id.rbMale -> "M"
            R.id.rbFemale -> "F"
            else -> ""
        }
        p.birth          = etBirth.text?.toString()?.trim().orEmpty()
        p.passportNo     = etPassNo.text?.toString()?.trim().orEmpty()
        p.passportExpiry = etPassExp.text?.toString()?.trim().orEmpty()
        p.nationality    = etNation.text?.toString()?.trim().orEmpty()
        p.phone          = etPhone.text?.toString()?.trim().orEmpty()
        p.email          = etEmail.text?.toString()?.trim().orEmpty()
        p.emergencyName  = etEmgName.text?.toString()?.trim().orEmpty()
        p.emergencyPhone = etEmgPhone.text?.toString()?.trim().orEmpty()

        adapter.updateNameAt(selectedIndex, p.displayName())
        validateAll()
    }

    private fun saveFormToModel(index: Int) {
        val p = passengers[index]
        p.lastNameEn     = etLast.text?.toString()?.trim().orEmpty()
        p.firstNameEn    = etFirst.text?.toString()?.trim().orEmpty()
        p.gender         = when (rgGender.checkedRadioButtonId) {
            R.id.rbMale -> "M"
            R.id.rbFemale -> "F"
            else -> ""
        }
        p.birth          = etBirth.text?.toString()?.trim().orEmpty()
        p.passportNo     = etPassNo.text?.toString()?.trim().orEmpty()
        p.passportExpiry = etPassExp.text?.toString()?.trim().orEmpty()
        p.nationality    = etNation.text?.toString()?.trim().orEmpty()
        p.phone          = etPhone.text?.toString()?.trim().orEmpty()
        p.email          = etEmail.text?.toString()?.trim().orEmpty()
        p.emergencyName  = etEmgName.text?.toString()?.trim().orEmpty()
        p.emergencyPhone = etEmgPhone.text?.toString()?.trim().orEmpty()

        p.edited = listOf(
            p.lastNameEn, p.firstNameEn, p.gender, p.birth, p.passportNo,
            p.passportExpiry, p.nationality, p.phone, p.email, p.emergencyName, p.emergencyPhone
        ).any { it.isNotBlank() }
    }

    private fun validateAll() {
        val everyFilled = passengers.all { it.isRequiredFilled() }
        if (::btnNext.isInitialized) {
            btnNext.isEnabled = everyFilled
            btnNext.alpha = if (everyFilled) 1f else 0.5f
        }
    }

    private fun showMaterialDatePicker(target: TextInputEditText) {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("날짜 선택")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()
        picker.addOnPositiveButtonClickListener { millis ->
            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            target.setText(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
            syncCurrentPassengerAndValidate()
        }
        picker.show(supportFragmentManager, "date_picker")
    }
}
