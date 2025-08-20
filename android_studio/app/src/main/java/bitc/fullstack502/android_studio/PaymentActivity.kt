package bitc.fullstack502.android_studio

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import bitc.fullstack502.android_studio.FlightReservationActivity
import bitc.fullstack502.android_studio.model.Flight
import bitc.fullstack502.android_studio.model.Passenger
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.ui.ChatListActivity
import bitc.fullstack502.android_studio.ui.MainActivity
import bitc.fullstack502.android_studio.ui.lodging.LodgingSearchActivity
import bitc.fullstack502.android_studio.ui.mypage.LoginActivity
import bitc.fullstack502.android_studio.ui.mypage.MyPageActivity
import bitc.fullstack502.android_studio.ui.post.PostListActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView

class PaymentActivity : AppCompatActivity() {

    // === 랜덤 좌석/게이트(데모) ===
    private fun randomSeat(): String {
        val row = (10..45).random()
        val col = ('A'..'F').random()
        return "$row$col"
    }
    private fun randomGate(): String {
        val n = (1..40).random()
        val wing = listOf("A","B","C").random()
        return "${n}${wing}"
    }

    // === 1인/편도 기준 단가 계산 ===
    private fun perAdultOneWay(): Int =
        FlightReservationActivity.ADULT_PRICE +
                FlightReservationActivity.FUEL_SURCHARGE +
                FlightReservationActivity.FACILITY_FEE

    private fun perChildOneWay(): Int {
        val childFare = FlightReservationActivity.ADULT_PRICE - 20_000 // 아동 = 성인-2만원
        return childFare +
                FlightReservationActivity.FUEL_SURCHARGE +
                FlightReservationActivity.FACILITY_FEE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)


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

        // === View refs ===
        val tvTotal    = findViewById<TextView>(R.id.tvTotalPrice)
        val tvFuel     = findViewById<TextView>(R.id.tvFuel)
        val tvFacility = findViewById<TextView>(R.id.tvFacility)
        val tvBase     = findViewById<TextView>(R.id.tvBaseFare)
        val rbKakao    = findViewById<RadioButton>(R.id.rbKakaoPay)
        val btnPay     = findViewById<Button>(R.id.btnPay)

        // (선택) 헤더 “총 n명”이 있으면 업데이트
        val tvPaxHeader: TextView? = findViewById(R.id.tvPax)

        // === ItineraryActivity에서 넘어온 금액(구간 반영 후 기대) ===
        var totalX = intent.getIntExtra("EXTRA_TOTAL", 0)
        var base   = intent.getIntExtra("EXTRA_BASE", 0)
        var fuel   = intent.getIntExtra("EXTRA_FUEL", 0)
        var fac    = intent.getIntExtra("EXTRA_FACILITY", 0)

        // === 여정/승객 ===
        val outFlight  = intent.getSerializableExtra(FlightReservationActivity.EXTRA_OUTBOUND) as? Flight
        val inFlight   = intent.getSerializableExtra(FlightReservationActivity.EXTRA_INBOUND)  as? Flight
        val isRoundTrip = inFlight != null

        val adults   = intent.getIntExtra(FlightReservationActivity.EXTRA_ADULT, 1)
        val children = intent.getIntExtra(FlightReservationActivity.EXTRA_CHILD, 0)
        val infants  = intent.getIntExtra(FlightReservationActivity.EXTRA_INFANT, 0)
        val paxTotal = adults + children + infants
        tvPaxHeader?.text = "총 ${paxTotal}명"

        @Suppress("UNCHECKED_CAST")
        val passengers = intent.getSerializableExtra("PASSENGERS") as? ArrayList<Passenger>
        val mainPaxName = passengers?.firstOrNull()?.displayName().orEmpty()

        // === 금액 신뢰성 보정: Itinerary 쪽에서 값이 0이거나 누락되면 여기서 재계산 ===
        val segments = if (isRoundTrip) 2 else 1
        if (totalX == 0 || base == 0 || fuel == 0 || fac == 0) {
            val chargeable = adults + children // 유아 0원
            base = adults * FlightReservationActivity.ADULT_PRICE +
                    children * (FlightReservationActivity.ADULT_PRICE - 20_000)
            fuel = chargeable * FlightReservationActivity.FUEL_SURCHARGE
            fac  = chargeable * FlightReservationActivity.FACILITY_FEE

            base *= segments
            fuel *= segments
            fac  *= segments
            totalX = (adults * perAdultOneWay() + children * perChildOneWay()) * segments
        }

        // === 화면 표시 ===
        tvBase.text     = "항공운임: %,d원".format(base)
        tvFuel.text     = "유류할증료: %,d원".format(fuel)
        tvFacility.text = "공항시설사용료: %,d원".format(fac)
        tvTotal.text    = "총 결제금액: %,d원".format(totalX)

        // === 결제 처리 ===
        btnPay.setOnClickListener {
            if (!rbKakao.isChecked) {
                Toast.makeText(this, "결제수단을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (outFlight == null) {
                Toast.makeText(this, "가는 편 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 결제 성공 화면으로 이동
            startActivity(Intent(this, TicketSuccessActivity::class.java).apply {
                putExtra("EXTRA_ROUNDTRIP", isRoundTrip)
                // 가는 편
                putExtra("EXTRA_DEP", outFlight.dep)
                putExtra("EXTRA_ARR", outFlight.arr)
                putExtra("EXTRA_DATETIME", outFlight.depTime)
                putExtra("EXTRA_FLIGHT_NO", outFlight.flNo)
                putExtra("EXTRA_GATE", randomGate())
                putExtra("EXTRA_SEAT", randomSeat())
                putExtra("EXTRA_CLASS", "이코노미")
                // 오는 편
                if (inFlight != null) {
                    putExtra("EXTRA_DEP_RETURN", inFlight.dep)
                    putExtra("EXTRA_ARR_RETURN", inFlight.arr)
                    putExtra("EXTRA_DATETIME_RETURN", inFlight.depTime)
                    putExtra("EXTRA_FLIGHT_NO_RETURN", inFlight.flNo)
                    putExtra("EXTRA_GATE_RETURN", randomGate())
                    putExtra("EXTRA_SEAT_RETURN", randomSeat())
                    putExtra("EXTRA_CLASS_RETURN", "이코노미")
                }
                // 승객들
                putExtra("PASSENGERS", passengers)
                putExtra("EXTRA_PASSENGER", mainPaxName.ifBlank { "승객 1" })
                putExtra("EXTRA_PAX_COUNT", passengers?.size ?: paxTotal)
            })
        }
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
