package bitc.fullstack502.android_studio

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.adapter.FlightAdapter
import bitc.fullstack502.android_studio.model.Flight
import bitc.fullstack502.android_studio.ui.ChatListActivity
import bitc.fullstack502.android_studio.ui.MainActivity
import bitc.fullstack502.android_studio.ui.lodging.LodgingSearchActivity
import bitc.fullstack502.android_studio.ui.mypage.LoginActivity
import bitc.fullstack502.android_studio.ui.mypage.MyPageActivity
import bitc.fullstack502.android_studio.ui.post.PostListActivity
import bitc.fullstack502.android_studio.viewmodel.FlightReservationViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView

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

        ////////////////////////////////////////////////////////////////////////////////////////////
        // ✅ Drawer & NavigationView
        val drawer = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navView = findViewById<NavigationView>(R.id.navigationView)

        // ✅ 공통 헤더 버튼 세팅
        val header = findViewById<View>(R.id.header)
        val btnBack: ImageButton = header.findViewById(R.id.btnBack)
        val imgLogo: ImageView = header.findViewById(R.id.imgLogo)
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

        // 드로어 헤더 인사말 세팅
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
                    true // 현재 항공권 화면이므로 그대로 두기
                }
                else -> false
            }.also { drawer.closeDrawers() }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////

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
}
