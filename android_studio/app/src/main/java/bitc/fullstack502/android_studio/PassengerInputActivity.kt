package bitc.fullstack502.android_studio

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.adapter.PassengerSelectorAdapter
import bitc.fullstack502.android_studio.model.Flight
import bitc.fullstack502.android_studio.model.Passenger
import bitc.fullstack502.android_studio.model.PassengerType
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ✅ 추가
import bitc.fullstack502.android_studio.ui.PhoneHyphenTextWatcher
import com.google.android.material.appbar.MaterialToolbar
import kotlin.collections.all

class PassengerInputActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ADULTS = "EXTRA_ADULTS"
        const val EXTRA_CHILDREN = "EXTRA_CHILDREN"
    }

    private lateinit var rv: RecyclerView
    private lateinit var btnNext: MaterialButton
    private lateinit var adapter: PassengerSelectorAdapter
    private lateinit var passengers: MutableList<Passenger>

    private var selectedIndex = 0
    private var isBinding = false   // ★ setText/체크 중에는 워처/리스너 무시

    private lateinit var rgGender: RadioGroup


    private fun currentIndex(): Int = selectedIndex
    private fun current(): Passenger = passengers[currentIndex()]

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

    private val watcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) { syncCurrentPassengerAndValidate() }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun resetPassenger(p: Passenger) {
        p.lastNameEn = ""
        p.firstNameEn = ""
        p.gender = ""
        p.birth = ""
        p.passportNo = ""
        p.passportExpiry = ""
        p.nationality = ""
        p.phone = ""
        p.email = ""
        p.emergencyName = ""
        p.emergencyPhone = ""
        p.edited = false
    }

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

//        ViewCompat.setImportantForAutofill(findViewById(R.id.formContainer),
//            View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 0) onCreate 맨 위에서 버튼 먼저 초기화
        btnNext = findViewById(R.id.btnNext)
        btnNext.isEnabled = false
        btnNext.alpha = 0.5f

        // 1) 인텐트 수신
        val tripType  = intent.getStringExtra(FlightReservationActivity.EXTRA_TRIP_TYPE)
        val outFlight = intent.getSerializableExtra(FlightReservationActivity.EXTRA_OUTBOUND) as? Flight
        val inFlight  = intent.getSerializableExtra(FlightReservationActivity.EXTRA_INBOUND) as? Flight
        val outPrice  = intent.getIntExtra(FlightReservationActivity.EXTRA_OUT_PRICE, 0)
        val inPrice   = intent.getIntExtra(FlightReservationActivity.EXTRA_IN_PRICE, 0)
        val adults    = intent.getIntExtra(FlightReservationActivity.EXTRA_ADULT, 1)
        val children  = intent.getIntExtra(FlightReservationActivity.EXTRA_CHILD, 0)

        // 2) 리스트/리사이클러 초기화 (index 연속 부여)
        var idx = 0
        passengers = mutableListOf<Passenger>().apply {
            repeat(adults)   { add(Passenger(idx++, PassengerType.ADULT)) }
            repeat(children) { add(Passenger(idx++, PassengerType.CHILD)) }
        }
        rv = findViewById(R.id.rvPassengers)
        rv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        adapter = PassengerSelectorAdapter(passengers) { pos ->
            if (pos == selectedIndex) return@PassengerSelectorAdapter

            // ① 떠나는 탭 인덱스를 먼저 보관
            val old = selectedIndex

            // ② 떠나는 탭 저장(★ 반드시 old 사용!)
            if (old in passengers.indices) saveFormToModel(old)

            // ③ 선택 변경 + 어댑터 갱신
            selectedIndex = pos
            adapter.setSelected(pos)

            // ④ 처음 진입이면 빈칸, 아니면 저장값 로드
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

        rgGender = findViewById(R.id.rgGender)
        rbMale   = findViewById(R.id.rbMale)
        rbFemale = findViewById(R.id.rbFemale)

        // ✅ 전화번호 자동 하이픈: onCreate에서 "한 번만" 붙여라
        etPhone.addTextChangedListener(PhoneHyphenTextWatcher(etPhone))
        etEmgPhone.addTextChangedListener(PhoneHyphenTextWatcher(etEmgPhone))

        // 날짜 피커
        findViewById<TextInputLayout>(R.id.tilBirth)
            .setEndIconOnClickListener { showMaterialDatePicker(etBirth) }
        findViewById<TextInputLayout>(R.id.tilPassportExpiry)
            .setEndIconOnClickListener { showMaterialDatePicker(etPassExp) }
        etBirth.setOnClickListener { showMaterialDatePicker(etBirth) }
        etPassExp.setOnClickListener { showMaterialDatePicker(etPassExp) }

        // 4) 먼저 기본 폼 바인딩
        bindForm(passengers[0])

        // 5) 텍스트워처/체크 리스너 연결
        listOf(etLast, etFirst, etPassNo, etNation, etPhone, etEmail, etEmgName, etEmgPhone)
            .forEach { it.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (isBinding) return
                    syncCurrentPassengerAndValidate()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }) }

        // 라디오 그룹 (한 곳에서만 리스너)
        rgGender.setOnCheckedChangeListener {  _, _ ->
            if (isBinding) return@setOnCheckedChangeListener
            syncCurrentPassengerAndValidate()
        }


        // 6) 다음 버튼
        btnNext.setOnClickListener {
            val allOk = passengers.all { it.isRequiredFilled() }
            if (!allOk) {
                Toast.makeText(this, "모든 승객의 필수 정보를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, ItineraryActivity::class.java).apply {
                putExtra("PASSENGERS", ArrayList(passengers))

                // ✅ 키 통일: ItineraryActivity가 실제로 읽는 키로 보내기
                putExtra(FlightReservationActivity.EXTRA_ADULT, adults)
                putExtra(FlightReservationActivity.EXTRA_CHILD, children)

                putExtra(FlightReservationActivity.EXTRA_TRIP_TYPE, tripType)
                putExtra(FlightReservationActivity.EXTRA_OUTBOUND, outFlight)
                putExtra(FlightReservationActivity.EXTRA_OUT_PRICE, outPrice)
                putExtra(FlightReservationActivity.EXTRA_INBOUND, inFlight)
                putExtra(FlightReservationActivity.EXTRA_IN_PRICE, inPrice)
            }
            startActivity(intent)
        }

        // 7) 초기 검증 반영
        validateAll()
    }

    private fun removeAllWatchers() {
        listOf(etLast, etFirst, etPassNo, etNation, etPhone, etEmail, etEmgName, etEmgPhone)
            .forEach { it.removeTextChangedListener(watcher) }
        rbMale.setOnCheckedChangeListener(null)
        rbFemale.setOnCheckedChangeListener(null)
    }

    private fun addAllWatchers() {
        listOf(etLast, etFirst, etPassNo, etNation, etPhone, etEmail, etEmgName, etEmgPhone)
            .forEach { it.addTextChangedListener(watcher) }
        rbMale.setOnCheckedChangeListener { _, _ -> syncCurrentPassengerAndValidate() }
        rbFemale.setOnCheckedChangeListener { _, _ -> syncCurrentPassengerAndValidate() }
    }


    private fun syncCurrentPassengerAndValidate() {
        val p = passengers[selectedIndex]
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
        // index로 명시 저장 (선택 변경 전에 old 인덱스로 호출됨)
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

        // 한 번이라도 값이 있으면 '편집됨'
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
            val date = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            target.setText(date.format(DateTimeFormatter.ISO_LOCAL_DATE)) // YYYY-MM-DD
            syncCurrentPassengerAndValidate()
        }

        picker.show(supportFragmentManager, "date_picker")
    }
}
