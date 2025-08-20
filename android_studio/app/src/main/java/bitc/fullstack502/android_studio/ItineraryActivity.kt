package bitc.fullstack502.android_studio

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import bitc.fullstack502.android_studio.model.Flight
import bitc.fullstack502.android_studio.FlightReservationActivity
import bitc.fullstack502.android_studio.model.Passenger
import bitc.fullstack502.android_studio.ui.ChatListActivity
import bitc.fullstack502.android_studio.ui.MainActivity
import bitc.fullstack502.android_studio.ui.lodging.LodgingSearchActivity
import bitc.fullstack502.android_studio.ui.mypage.LoginActivity
import bitc.fullstack502.android_studio.ui.mypage.MyPageActivity
import bitc.fullstack502.android_studio.ui.post.PostListActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView

class ItineraryActivity : AppCompatActivity() {

    companion object {
        // Intent keys (필요 시 이전 화면에서 넘겨줌)
        const val EXTRA_OUT_FLIGHT   = "EXTRA_OUT_FLIGHT"
        const val EXTRA_IN_FLIGHT    = "EXTRA_IN_FLIGHT"
        const val EXTRA_ADULT_COUNT  = "EXTRA_ADULT_COUNT"
        const val EXTRA_CHILD_COUNT  = "EXTRA_CHILD_COUNT"
        const val EXTRA_OUT_DATE     = "EXTRA_OUT_DATE"
        const val EXTRA_IN_DATE      = "EXTRA_IN_DATE"

        // ✅ 고정 운임 상수(1인)
        private const val ADULT_PRICE     = 98_700               // 항공운임(성인)
        private const val CHILD_PRICE     = ADULT_PRICE - 20_000 // 항공운임(아동)
        private const val FUEL_SURCHARGE  = 15_400               // 유류할증료(연령무관 1인)
        private const val FACILITY_FEE    = 8_000                // 공항시설사용료(연령무관 1인)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_itinerary_detail)

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

        // ===== include 카드 참조 =====
        val outCard = findViewById<View>(R.id.includeOutbound)
        val inCard  = findViewById<View>(R.id.includeInbound)

        val tvOutFlightNo = outCard.findViewById<TextView>(R.id.tvFlightNo)
        val tvOutFareType = outCard.findViewById<TextView>(R.id.tvFareType)
        val tvOutDepInfo  = outCard.findViewById<TextView>(R.id.tvDepInfo)
        val tvOutArrInfo  = outCard.findViewById<TextView>(R.id.tvArrInfo)
        val tvOutDepCity  = outCard.findViewById<TextView>(R.id.tvDepCity)
        val tvOutArrCity  = outCard.findViewById<TextView>(R.id.tvArrCity)

        val tvInFlightNo = inCard.findViewById<TextView>(R.id.tvFlightNo)
        val tvInFareType = inCard.findViewById<TextView>(R.id.tvFareType)
        val tvInDepInfo  = inCard.findViewById<TextView>(R.id.tvDepInfo)
        val tvInArrInfo  = inCard.findViewById<TextView>(R.id.tvArrInfo)
        val tvInDepCity  = inCard.findViewById<TextView>(R.id.tvDepCity)
        val tvInArrCity  = inCard.findViewById<TextView>(R.id.tvArrCity)

        // 운임/합계 영역
        val tvFareBase     = findViewById<TextView>(R.id.tvFareBase)
        val tvFareFuel     = findViewById<TextView>(R.id.tvFareFuel)
        val tvFareFacility = findViewById<TextView>(R.id.tvFareFacility)
        val tvTotal        = findViewById<TextView>(R.id.tvTotalPrice)

        // 체크박스
        val cbAll      = findViewById<CheckBox>(R.id.cbAll)
        val cbRule     = findViewById<CheckBox>(R.id.cbRule)
        val cbDomestic = findViewById<CheckBox>(R.id.cbDomestic)
        val cbProhibit = findViewById<CheckBox>(R.id.cbProhibited)
        val cbAddon    = findViewById<CheckBox>(R.id.cbAddon)
        val cbThird    = findViewById<CheckBox>(R.id.cbThird)
        val cbHazard   = findViewById<CheckBox>(R.id.cbHazard)
        val childCbs   = listOf(cbRule, cbDomestic, cbProhibit, cbAddon, cbThird, cbHazard)

        val btnNext = findViewById<Button>(R.id.btnNext)

        // ===== PassengerInputActivity에서 넘어온 값들 (키 통일!) =====
        val outFlight = intent.getSerializableExtra(FlightReservationActivity.EXTRA_OUTBOUND) as? Flight
        val inFlight  = intent.getSerializableExtra(FlightReservationActivity.EXTRA_INBOUND)  as? Flight
        val outPrice  = intent.getIntExtra(FlightReservationActivity.EXTRA_OUT_PRICE, 0)
        val inPrice   = intent.getIntExtra(FlightReservationActivity.EXTRA_IN_PRICE, 0)

        val adultCount = intent.getIntExtra(FlightReservationActivity.EXTRA_ADULT, 1)
        val childCount = intent.getIntExtra(FlightReservationActivity.EXTRA_CHILD, 0)
        val infantCount= intent.getIntExtra(FlightReservationActivity.EXTRA_INFANT, 0)

        // (옵션) 승객 리스트 – 다음 화면에서도 필요할 수 있음
        @Suppress("UNCHECKED_CAST")
        val passengers = intent.getSerializableExtra("PASSENGERS") as? ArrayList<Passenger>

        // ===== 카드 바인딩 =====
        outFlight?.let { f ->
            tvOutFlightNo.text = f.flNo
            tvOutFareType.text = "이코노미"
            tvOutDepInfo.text  = f.depTime
            tvOutArrInfo.text  = f.arrTime
            tvOutDepCity.text  = f.dep
            tvOutArrCity.text  = f.arr
        }

        if (inFlight == null) {
            inCard.visibility = View.GONE // 편도면 숨김
        } else {
            inCard.visibility = View.VISIBLE
            inFlight.let { f ->
                tvInFlightNo.text = f.flNo
                tvInFareType.text = "이코노미"
                tvInDepInfo.text  = f.depTime
                tvInArrInfo.text  = f.arrTime
                tvInDepCity.text  = f.dep
                tvInArrCity.text  = f.arr
            }
        }

        // ===== 운임 계산(성인/아동 부과, 유아 0원) =====
        val segments = if (inFlight == null) 1 else 2                // 편도=1, 왕복=2
        val chargeable = adultCount + childCount                      // 유아는 0원

        // 1인 총액(연령별)
        val perAdultTotal = ADULT_PRICE + FUEL_SURCHARGE + FACILITY_FEE   // 122,100
        val perChildTotal = CHILD_PRICE + FUEL_SURCHARGE + FACILITY_FEE   // 102,100
        val perInfantTotal = 0

        // 항목별 합계(구간수 적용 전)
        val baseFare      = adultCount * ADULT_PRICE + childCount * CHILD_PRICE
        val fuelTotal     = chargeable * FUEL_SURCHARGE
        val facilityTotal = chargeable * FACILITY_FEE

        // 화면 표시는 '구간수 적용 후' 금액으로
        val baseFareX      = baseFare * segments
        val fuelTotalX     = fuelTotal * segments
        val facilityTotalX = facilityTotal * segments
        val totalX         = (adultCount * perAdultTotal +
                childCount * perChildTotal +
                infantCount * perInfantTotal) * segments

        // 표시
        tvFareBase.text     = "%,d원".format(baseFareX)
        tvFareFuel.text     = "%,d원".format(fuelTotalX)
        tvFareFacility.text = "%,d원".format(facilityTotalX)
        tvTotal.text        = "%,d원".format(totalX)

        // ===== 전체 동의 연동 =====
        fun syncAllFromChildren() {
            val allChecked = childCbs.all { it.isChecked }
            if (cbAll.isChecked != allChecked) cbAll.isChecked = allChecked
            btnNext.isEnabled = allChecked
        }
        cbAll.setOnCheckedChangeListener { _, checked ->
            childCbs.forEach { it.setOnCheckedChangeListener(null) }
            childCbs.forEach { it.isChecked = checked }
            childCbs.forEach { child ->
                child.setOnCheckedChangeListener { _, _ -> syncAllFromChildren() }
            }
            btnNext.isEnabled = checked
        }
        childCbs.forEach { child ->
            child.setOnCheckedChangeListener { _, _ -> syncAllFromChildren() }
        }
        syncAllFromChildren()

        // ===== 다음(결제) =====
        btnNext.setOnClickListener {
            if (!btnNext.isEnabled) return@setOnClickListener
            startActivity(Intent(this, PaymentActivity::class.java).apply {
                // 👉 결제 화면으로는 '구간 반영 후' 금액 전달
                putExtra("EXTRA_TOTAL", totalX)
                putExtra("EXTRA_BASE", baseFareX)
                putExtra("EXTRA_FUEL", fuelTotalX)
                putExtra("EXTRA_FACILITY", facilityTotalX)

                putExtra(FlightReservationActivity.EXTRA_OUTBOUND, outFlight)
                putExtra(FlightReservationActivity.EXTRA_INBOUND, inFlight)
                intent.putExtra(FlightReservationActivity.EXTRA_ADULT, adultCount)
                intent.putExtra(FlightReservationActivity.EXTRA_CHILD, childCount)
                intent.putExtra(FlightReservationActivity.EXTRA_INFANT, infantCount)

                // 필요 시 승객 리스트도 넘김
                putExtra("PASSENGERS", passengers)
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
