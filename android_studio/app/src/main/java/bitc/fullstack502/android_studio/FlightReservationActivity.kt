package bitc.fullstack502.android_studio

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
<<<<<<< HEAD
import bitc.fullstack502.android_studio.databinding.ActivityFlightReservationBinding
import bitc.fullstack502.android_studio.model.Flight
import bitc.fullstack502.android_studio.adapter.FlightAdapter
import bitc.fullstack502.android_studio.util.AuthManager
=======
import bitc.fullstack502.android_studio.FlightReservationActivity.Companion.EXTRA_INBOUND
import bitc.fullstack502.android_studio.FlightReservationActivity.Companion.EXTRA_INFANT
import bitc.fullstack502.android_studio.FlightReservationActivity.Companion.EXTRA_IN_PRICE
import bitc.fullstack502.android_studio.FlightReservationActivity.Companion.EXTRA_TRIP_TYPE
import bitc.fullstack502.android_studio.InboundSelectActivity.Companion.EXTRA_ADULT
import bitc.fullstack502.android_studio.InboundSelectActivity.Companion.EXTRA_CHILD
import bitc.fullstack502.android_studio.InboundSelectActivity.Companion.EXTRA_OUTBOUND
import bitc.fullstack502.android_studio.InboundSelectActivity.Companion.EXTRA_OUT_PRICE
import bitc.fullstack502.android_studio.adapter.FlightAdapter
import bitc.fullstack502.android_studio.databinding.ActivityFlightReservationBinding
import bitc.fullstack502.android_studio.databinding.ActivityPostListBinding
import bitc.fullstack502.android_studio.model.Flight
import bitc.fullstack502.android_studio.ui.ChatListActivity
import bitc.fullstack502.android_studio.ui.MainActivity
import bitc.fullstack502.android_studio.ui.lodging.LodgingSearchActivity
import bitc.fullstack502.android_studio.ui.mypage.LoginActivity
import bitc.fullstack502.android_studio.ui.mypage.MyPageActivity
import bitc.fullstack502.android_studio.ui.post.PostListActivity
>>>>>>> jgy/chat2
import bitc.fullstack502.android_studio.viewmodel.FlightReservationViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FlightReservationActivity : AppCompatActivity() {

<<<<<<< HEAD
    private val binding by lazy { ActivityFlightReservationBinding.inflate(layoutInflater) }
=======
    private lateinit var bind: ActivityFlightReservationBinding

    // 고정 요금 상수 (1인 기준)
    companion object {
        const val ADULT_PRICE    = 98_700      // 항공운임 - 성인
        const val CHILD_PRICE    = ADULT_PRICE - 20_000  // 항공운임 - 아동 (요청 반영)
        const val FUEL_SURCHARGE = 15_400      // 1인당 고정
        const val FACILITY_FEE   = 8_000       // 1인당 고정
>>>>>>> jgy/chat2

    companion object {
        const val ADULT_PRICE    = 98_700
        const val CHILD_PRICE    = ADULT_PRICE - 20_000
        const val FUEL_SURCHARGE = 15_400
        const val FACILITY_FEE   = 8_000

        const val EXTRA_TRIP_TYPE = "EXTRA_TRIP_TYPE"
        const val EXTRA_OUTBOUND  = "EXTRA_OUTBOUND"
        const val EXTRA_OUT_PRICE = "EXTRA_OUT_PRICE"
        const val EXTRA_INBOUND   = "EXTRA_INBOUND"
        const val EXTRA_IN_PRICE  = "EXTRA_IN_PRICE"
        const val EXTRA_ADULT     = "EXTRA_ADULT"
        const val EXTRA_CHILD     = "EXTRA_CHILD"
        const val EXTRA_INFANT    = "EXTRA_INFANT"
    }

    private var adultCount: Int = 1
    private var childCount: Int = 0
    private var infantCount: Int = 0

    private var isRoundTrip: Boolean = true

    private var selectedOut: Flight? = null
    private var selectedOutPrice: Int = 0

    private var selectedIn: Flight? = null
    private var selectedInPrice: Int = 0

    private lateinit var scroll: NestedScrollView
    private lateinit var bottomBar: View
    private lateinit var tvTotalPrice: TextView
    private lateinit var btnProceed: MaterialButton

    private lateinit var switchTrip: SwitchCompat
    private lateinit var tvDate: TextView
    private lateinit var tvFrom: TextView
    private lateinit var tvTo: TextView
    private lateinit var btnSwap: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var tvPax: TextView
    private lateinit var btnSearch: MaterialButton
    private lateinit var rvResults: RecyclerView
    private lateinit var flightAdapter: FlightAdapter

    private val viewModel: FlightReservationViewModel by viewModels()

    // API용 날짜
    private var outDateYmd: String? = null
    private var inDateYmd: String? = null

    private var lastNonJejuForDeparture: String = "김포(서울)"
    private var lastNonJejuForArrival: String = "김포(서울)"

    private val airports = listOf(
        "김포(서울)", "인천", "김해(부산)", "대구", "청주", "광주", "무안",
        "여수", "울산", "원주", "양양", "사천(진주)", "포항", "군산", "제주"
    )

<<<<<<< HEAD
=======


    // ===== 요금 계산 보조 =====
>>>>>>> jgy/chat2
    private fun unitTotalAdult()  = ADULT_PRICE + FUEL_SURCHARGE + FACILITY_FEE
    private fun unitTotalChild()  = CHILD_PRICE + FUEL_SURCHARGE + FACILITY_FEE
    private fun unitTotalInfant() = 0

    private fun calcTotal(adults: Int, children: Int, infants: Int): Int {
        return adults * unitTotalAdult() + children * unitTotalChild() + infants * unitTotalInfant()
    }

    private fun Int.asWon(): String = "₩%,d".format(this)
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        /////////////////////////////////////
        // ✅ Drawer & NavigationView
        val drawer = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navView = findViewById<NavigationView>(R.id.navigationView)

        // ✅ 공통 헤더 버튼 세팅
        val header = findViewById<View>(R.id.header)
        val btnBack: ImageButton = header.findViewById(R.id.btnBack)
        val imgLogo: ImageView   = header.findViewById(R.id.imgLogo)
        val btnMenu: ImageButton = header.findViewById(R.id.btnMenu)

        btnBack.setOnClickListener { finish() }  // 뒤로가기
        imgLogo.setOnClickListener {             // 로고 → 메인으로
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }
        btnMenu.setOnClickListener {             // 햄버거 → Drawer 열기
            drawer.openDrawer(GravityCompat.END)
        }

        // 드로어 헤더 인사말 세팅 (로그인 상태 반영)
        updateHeader(navView)

        // ✅ Drawer 메뉴 클릭 처리
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_hotel -> {
                    startActivity(Intent(this, LodgingSearchActivity::class.java)); true
                }
                R.id.nav_board -> {
                    startActivity(Intent(this, PostListActivity::class.java)); true
                }
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatListActivity::class.java)); true
                }
                R.id.nav_flight -> {
                    // 현재 FlightReservationActivity니까 따로 이동 안 해도 됨
                    true
                }
                else -> false
            }.also { drawer.closeDrawers() }
        }

        //////////////////////////////////////////////////////////////////////////////////////


        // findViewById
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

        // 인원수 초기값
        adultCount  = intent.getIntExtra(EXTRA_ADULT, 1)
        childCount  = intent.getIntExtra(EXTRA_CHILD, 0)
        infantCount = intent.getIntExtra(EXTRA_INFANT, 0)
        tvPax.text  = "총 ${adultCount + childCount + infantCount} 명"

        // 초기 출/도착
        setDeparture("김포(서울)", recordNonJeju = true)
        setArrival("제주", recordNonJeju = false)

        // 리스트 + 어댑터
        flightAdapter = FlightAdapter(
            mutableListOf(),
            onSelect = { flight, _, price -> onFlightSelected(flight, price) },
            priceOf = { ADULT_PRICE }
        )
        rvResults.apply {
            layoutManager = LinearLayoutManager(this@FlightReservationActivity)
            itemAnimator  = DefaultItemAnimator()
            adapter       = flightAdapter
        }

        // ViewModel
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

        // 편도/왕복 토글
        isRoundTrip = switchTrip.isChecked
        applyTripTypeText()
        switchTrip.setOnCheckedChangeListener { _, checked ->
            isRoundTrip = checked
            outDateYmd = null
            inDateYmd  = null
            applyTripTypeText()
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

        // 출/도착 & 스왑
        tvFrom.setOnClickListener { showAirportModalAll(true) }
        tvTo.setOnClickListener   { showAirportModalAll(false) }
        btnSwap.setOnClickListener { swapAirports() }

        // 검색
        btnSearch.setOnClickListener {
            val dep = normalizeAirport(tvFrom.text.toString())
            val arr = normalizeAirport(tvTo.text.toString())

            Log.d("FLIGHT_BTN", "dep=$dep, arr=$arr, outDate=$outDateYmd, inDate=$inDateYmd, round=$isRoundTrip")

            if (isRoundTrip) {
                if (outDateYmd == null || inDateYmd == null) {
                    Toast.makeText(this, "가는 날과 오는 날을 선택하세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.searchFlights(dep, arr, outDateYmd!!, null)
                viewModel.searchInboundFlights(arr, dep, inDateYmd!!, null)
            } else {
                if (outDateYmd == null) {
                    Toast.makeText(this, "출발 날짜를 선택하세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.searchFlights(dep, arr, outDateYmd!!, null)
            }
        }

        btnProceed.setOnClickListener { guardAndProceed() }
    }

    // 🔧 guardAndProceed() 함수 수정 (중요 부분만 발췌)
    private fun guardAndProceed() {
        val loggedIn = AuthManager.isLoggedIn()
        val userPk = AuthManager.id()
        if (!loggedIn || userPk <= 0L) {
            Toast.makeText(this, "로그인 후 이용해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        if (outDateYmd.isNullOrBlank()) {
            Toast.makeText(this, "출발 날짜를 선택하세요", Toast.LENGTH_SHORT).show()
            return
        }

        if (isRoundTrip) {
            if (inDateYmd.isNullOrBlank()) {
                Toast.makeText(this, "오는 날짜를 선택하세요", Toast.LENGTH_SHORT).show()
                return
            }
            openInboundSelection()
        } else {
            val out = selectedOut
            if (out == null) {
                Toast.makeText(this, "먼저 가는 편을 선택하세요", Toast.LENGTH_SHORT).show()
                return
            }
            openPassengerInput(out, null, selectedOutPrice, 0)
        }
    }

    private fun onFlightSelected(item: Flight, price: Int) {
        selectedOut = item
        selectedOutPrice = price

        val totalAmountOneWay = calcTotal(adultCount, childCount, infantCount)
        showBottomBar(
            amount = totalAmountOneWay,
            buttonText = if (isRoundTrip) "오는 편 선택하기" else "승객 정보 입력"
        )
        scroll.post { scroll.smoothScrollBy(0, dp(56)) }
    }

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
        btnSwap.animate().cancel()
        btnSwap.rotation = 0f
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
            }
            .show()
    }

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
        Log.d("*** flight ***", "$")
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
            val total = adultCount + childCount + infantCount
            tvPax.text = "총 $total 명"
            dialog.dismiss()
            if (selectedOut != null) {
                val totalAmountOneWay = calcTotal(adultCount, childCount, infantCount)
                tvTotalPrice.text = totalAmountOneWay.asWon()
            }
        }
        dialog.show()
    }

    private fun normalizeAirport(display: String): String {
        val s = display.trim()
        return when {
            s.contains("김포") -> "서울/김포"
            s.contains("인천") -> "서울/인천"
            s.contains("김해") || s.contains("부산") -> "부산/김해"
            s.contains("사천") || s.contains("진주") -> "사천"
            else -> s
        }
    }

    private fun applyTripTypeText() {
        tvDate.text = if (isRoundTrip) "가는 날 ~ 오는 날 선택" else "출발 날짜 선택"
        if (selectedOut == null) {
            btnProceed.text = if (isRoundTrip) "오는 편 선택하기" else "승객 정보 입력"
        }
    }

    private fun openInboundSelection() {
        if (selectedOut == null) {
            Toast.makeText(this, "먼저 가는 편을 선택하세요", Toast.LENGTH_SHORT).show()
            return
        }
        if (outDateYmd.isNullOrBlank() || inDateYmd.isNullOrBlank()) {
            Toast.makeText(this, "가는 날/오는 날을 선택하세요", Toast.LENGTH_SHORT).show()
            return
        }

        val depInbound = normalizeAirport(tvTo.text.toString())   // IN dep = 기존 도착
        val arrInbound = normalizeAirport(tvFrom.text.toString()) // IN arr = 기존 출발

        startActivity(Intent(this, InboundSelectActivity::class.java).apply {
            putExtra(InboundSelectActivity.EXTRA_OUTBOUND, selectedOut)
            putExtra(InboundSelectActivity.EXTRA_OUT_PRICE, selectedOutPrice)
            putExtra(InboundSelectActivity.EXTRA_DEP,  depInbound)
            putExtra(InboundSelectActivity.EXTRA_ARR,  arrInbound)
            putExtra(InboundSelectActivity.EXTRA_DATE, inDateYmd)     // yyyy-MM-dd
            putExtra(InboundSelectActivity.EXTRA_ADULT, adultCount)
            putExtra(InboundSelectActivity.EXTRA_CHILD, childCount)

            // 🔥 PassengerInputActivity가 읽을 날짜를 미리 넘겨둠(오는편 화면에서 그대로 전달)
            putExtra(PassengerInputActivity.EXTRA_OUT_DATE, outDateYmd)
            putExtra(PassengerInputActivity.EXTRA_IN_DATE,  inDateYmd)
        })
    }

    private fun openPassengerInput(
        outFlight: Flight,
        inFlight: Flight?,
        outPrice: Int,
        inPrice: Int
    ) {
        if (outDateYmd.isNullOrBlank()) {
            Toast.makeText(this, "출발 날짜를 선택하세요", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, PassengerInputActivity::class.java).apply {
            putExtra(EXTRA_TRIP_TYPE, if (isRoundTrip) "ROUND_TRIP" else "ONE_WAY")
            putExtra(EXTRA_OUTBOUND, outFlight)
            putExtra(EXTRA_OUT_PRICE, outPrice)
            putExtra(EXTRA_INBOUND, inFlight)
            putExtra(EXTRA_IN_PRICE, inPrice)
            putExtra(EXTRA_ADULT, adultCount)
            putExtra(EXTRA_CHILD, childCount)
            putExtra(EXTRA_INFANT, infantCount)

            // 🔥 핵심: 승객 입력 화면으로 날짜 전달
            putExtra(PassengerInputActivity.EXTRA_OUT_DATE, outDateYmd)
            putExtra(PassengerInputActivity.EXTRA_IN_DATE,  inDateYmd) // 편도면 null이어도 OK
        }
        startActivity(intent)
    }

    private fun showBottomBar(amount: Int, buttonText: String) {
        tvTotalPrice.text = amount.asWon()
        btnProceed.text = buttonText
        btnProceed.setOnClickListener { guardAndProceed() }
        bottomBar.slideUpShow()
    }

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
<<<<<<< HEAD
=======

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

    // ----------------- 로그인/헤더 처리 -----------------

    private fun isLoggedIn(): Boolean {
        val sp = getSharedPreferences("userInfo", MODE_PRIVATE)
        return !sp.getString("usersId", null).isNullOrBlank()
    }

    private fun currentUserName(): String {
        val sp = getSharedPreferences("userInfo", MODE_PRIVATE)
        return sp.getString("name", null) ?: sp.getString("usersId", "") ?: ""
    }

    private fun currentUserEmail(): String {
        val sp = getSharedPreferences("userInfo", MODE_PRIVATE)
        return sp.getString("email", "") ?: ""
    }

    private fun updateHeader(navView: NavigationView) {
        val header = navView.getHeaderView(0)
        val tvGreet = header.findViewById<TextView>(R.id.tvUserGreeting)
        val tvEmail = header.findViewById<TextView>(R.id.tvUserEmail)
        val btnMyPage = header.findViewById<MaterialButton>(R.id.btnMyPage)
        val btnLogout = header.findViewById<MaterialButton>(R.id.btnLogout)

        if (isLoggedIn()) {
            val name = currentUserName()
            val email = currentUserEmail()
            tvGreet.text = getString(R.string.greeting_fmt, if (name.isBlank()) "회원" else name)
            tvEmail.visibility = View.VISIBLE
            tvEmail.text = if (email.isNotBlank()) email else "로그인됨"

            btnLogout.visibility = View.VISIBLE
            btnMyPage.text = getString(R.string.mypage)
            btnMyPage.setOnClickListener {
                startActivity(Intent(this, MyPageActivity::class.java))
            }
            btnLogout.setOnClickListener {
                val sp = getSharedPreferences("userInfo", MODE_PRIVATE)
                sp.edit().clear().apply()
                Toast.makeText(this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
                updateHeader(navView)
            }
        } else {
            // 비로그인: “000님” 같은 더미 표시 제거하고 “로그인”만 노출
            tvGreet.text = "로그인"
            tvEmail.visibility = View.GONE

            btnLogout.visibility = View.GONE
            btnMyPage.text = "로그인"
            btnMyPage.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }
>>>>>>> jgy/chat2
}
