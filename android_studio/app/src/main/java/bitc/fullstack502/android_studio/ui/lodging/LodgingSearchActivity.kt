package bitc.fullstack502.android_studio.ui.lodging

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.network.ApiProvider
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Intent
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import bitc.fullstack502.android_studio.FlightReservationActivity
import bitc.fullstack502.android_studio.ui.ChatListActivity
import bitc.fullstack502.android_studio.ui.MainActivity
import bitc.fullstack502.android_studio.ui.mypage.LoginActivity
import bitc.fullstack502.android_studio.ui.mypage.MyPageActivity
import bitc.fullstack502.android_studio.ui.post.PostListActivity
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.Locale

class LodgingSearchActivity : AppCompatActivity() {

    private lateinit var layoutCity: FlexboxLayout
    private lateinit var layoutTown: FlexboxLayout
    private lateinit var layoutVill: FlexboxLayout
    private lateinit var btnDateGuest: MaterialButton
    private lateinit var btnSearch: MaterialButton

    private var selectedCity: String = ""
    private val selectedTowns = mutableListOf<String>()
    private val selectedVills = mutableListOf<String>()

    // 날짜/인원 (바텀시트 결과 저장)
    private var checkIn: String = ""
    private var checkOut: String = ""
    private var adults: Int = 1
    private var children: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lodging_search)

        layoutCity = findViewById(R.id.layoutCity)
        layoutTown = findViewById(R.id.layoutTown)
        layoutVill = findViewById(R.id.layoutVill)
        btnDateGuest = findViewById(R.id.btnDateGuest)
        btnSearch = findViewById(R.id.btnSearch)

        // 초기 상태
        layoutTown.visibility = View.GONE
        layoutVill.visibility = View.GONE
        setSearchEnabled(false)

        /////////////////////////////////////
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
                    startActivity(Intent(this, FlightReservationActivity::class.java)); true
                }
                else -> false
            }.also { drawer.closeDrawers() }
        }
        /////////////////////////////////////

        // DB에서 도시 로드
        loadCities()

        // 바텀시트 결과 리스너 (한 번만 등록)
        supportFragmentManager.setFragmentResultListener(
            LodgingFilterBottomSheet.RESULT_KEY,
            this
        ) { _, bundle ->
            checkIn = bundle.getString(LodgingFilterBottomSheet.EXTRA_CHECK_IN).orEmpty()
            checkOut = bundle.getString(LodgingFilterBottomSheet.EXTRA_CHECK_OUT).orEmpty()
            adults = bundle.getInt(LodgingFilterBottomSheet.EXTRA_ADULTS, 1)
            children = bundle.getInt(LodgingFilterBottomSheet.EXTRA_CHILDREN, 0)

            val dateText =
                if (checkIn.isNotBlank() && checkOut.isNotBlank()) "$checkIn ~ $checkOut" else "날짜 미선택"
            btnDateGuest.text = "날짜/인원: $dateText, 성인 $adults, 아동 $children"

            // ✅ 날짜/인원 선택 후 상태 재계산
            updateSearchButtonState()
        }

        // 날짜/인원 바텀시트 열기
        btnDateGuest.setOnClickListener { openDateGuestSheet() }

        // 검색
        btnSearch.setOnClickListener {
            if (!hasValidDateGuest()) {
                toast("날짜와 인원을 먼저 선택하세요.")
                return@setOnClickListener
            }
            if (selectedCity.isBlank()) {
                toast("도시를 선택하세요."); return@setOnClickListener
            }
            if (selectedTowns.isEmpty()) {
                toast("읍/면/동을 1개 이상 선택하세요."); return@setOnClickListener
            }
            if (selectedVills.isEmpty()) {
                toast("리를 1개 이상 선택하세요."); return@setOnClickListener
            }

            val i = Intent(this, LodgingListActivity::class.java).apply {
                putExtra("city", selectedCity)
                putExtra("town", selectedTowns.joinToString(","))
                putExtra("vill", selectedVills.joinToString(","))
                putExtra(LodgingFilterBottomSheet.EXTRA_CHECK_IN, checkIn)
                putExtra(LodgingFilterBottomSheet.EXTRA_CHECK_OUT, checkOut)
                putExtra(LodgingFilterBottomSheet.EXTRA_ADULTS, adults)
                putExtra(LodgingFilterBottomSheet.EXTRA_CHILDREN, children)
            }
            startActivity(i)
        }
    }

    // ---------- DB 연동 ----------

    private fun loadCities() {
        layoutCity.removeAllViews()
        lifecycleScope.launch {
            val cities = withContext(Dispatchers.IO) {
                try { ApiProvider.api.getCities() } catch (_: Exception) { emptyList() }
            }
            if (cities.isEmpty()) {
                toast("도시 목록을 불러오지 못했습니다."); return@launch
            }
            cities.forEach { city ->
                val tv = chip(city)
                tv.setOnClickListener {
                    selectedCity = city
                    highlightSingle(layoutCity, tv)

                    // 하위 선택 초기화
                    selectedTowns.clear()
                    selectedVills.clear()
                    layoutVill.removeAllViews()
                    layoutVill.visibility = View.GONE

                    loadTowns(city)
                    updateSearchButtonState()
                }
                layoutCity.addView(tv)
            }
        }
    }

    private fun loadTowns(city: String) {
        layoutTown.removeAllViews()
        layoutTown.visibility = View.GONE
        lifecycleScope.launch {
            val towns = withContext(Dispatchers.IO) {
                try { ApiProvider.api.getTowns(city) } catch (_: Exception) { emptyList() }
            }
            if (towns.isEmpty()) {
                toast("읍/면/동 목록이 없습니다."); return@launch
            }
            towns.forEach { town ->
                val tv = chip(town)
                tv.setOnClickListener {
                    toggleMulti(tv, selectedTowns)
                    loadVills(selectedCity, selectedTowns.toList())
                    updateSearchButtonState()
                }
                layoutTown.addView(tv)
            }
            layoutTown.visibility = View.VISIBLE
        }
    }

    private fun loadVills(city: String, towns: List<String>) {
        layoutVill.removeAllViews()
        selectedVills.clear()
        layoutVill.visibility = View.GONE

        if (towns.isEmpty()) {
            updateSearchButtonState()
            return
        }

        lifecycleScope.launch {
            val allVills = withContext(Dispatchers.IO) {
                try {
                    val deferred = towns.map { t -> async { ApiProvider.api.getVills(city, t) } }
                    deferred.flatMap { it.await() }.toSet().toList().sorted()
                } catch (_: Exception) { emptyList() }
            }
            // ✅ '없음' 등 표시 원치 않는 항목 필터링 + 빈 목록이면 GONE (문구 노출 없음)
//            if (allVills.isEmpty()) {
//                toast("선택한 읍/면/동에 해당하는 리가 없습니다."); return@launch
//            }
            val filtered = allVills     // 여기부터~
                .filter { it.isNotBlank() && it != "없음" && it != "-" }
                .sorted()

            if (filtered.isEmpty()) {
                layoutVill.removeAllViews()
                layoutVill.visibility = View.GONE
                updateSearchButtonState()
                return@launch                       // ~여기까지 추가(수정)
            }

            filtered.forEach { v ->         // filtered(단어 수정)
                val tv = chip(v)
                tv.setOnClickListener {
                    toggleMulti(tv, selectedVills)
                    updateSearchButtonState()
                }
                layoutVill.addView(tv)
            }
            layoutVill.visibility = View.VISIBLE
        }
    }

    // ---------- 날짜/인원 바텀시트 ----------

    private fun openDateGuestSheet() {
        LodgingFilterBottomSheet().show(supportFragmentManager, "lodging_filter")
    }

    // ---------- 검증 & 상태 ----------

    private fun hasValidDateGuest(): Boolean {
        if (checkIn.isBlank() || checkOut.isBlank()) return false
        if (adults < 1) return false
        // yyyy-MM-dd 형식이므로 문자열 비교도 가능하지만 혹시 몰라 파싱
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val s = fmt.parse(checkIn)?.time ?: return false
            val e = fmt.parse(checkOut)?.time ?: return false
            e > s // 최소 1박
        } catch (_: Exception) {
            false
        }
    }

    private fun updateSearchButtonState() {
        val locationOk = selectedCity.isNotBlank() && selectedTowns.isNotEmpty() && selectedVills.isNotEmpty()
        val enabled = locationOk && hasValidDateGuest()
        setSearchEnabled(enabled)
    }

    private fun setSearchEnabled(enabled: Boolean) {
        btnSearch.isEnabled = enabled
        btnSearch.alpha = if (enabled) 1f else 0.5f
    }

    // ---------- UI 유틸 ----------

    private fun chip(text: String): TextView {
        val tv = LayoutInflater.from(this)
            .inflate(R.layout.item_city_chip, layoutCity, false) as TextView
        tv.text = text
        tv.setBackgroundResource(R.drawable.bg_chip_unselected)
        tv.setTextColor(Color.BLACK)
        return tv
    }

    private fun highlightSingle(parent: FlexboxLayout, selectedView: TextView) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i) as TextView
            child.setBackgroundResource(R.drawable.bg_chip_unselected)
            child.setTextColor(Color.BLACK)
        }
        selectedView.setBackgroundResource(R.drawable.bg_chip_selected)
        selectedView.setTextColor(Color.WHITE)
    }

    private fun toggleMulti(tv: TextView, bag: MutableList<String>) {
        val value = tv.text.toString()
        if (bag.contains(value)) {
            bag.remove(value)
            tv.setBackgroundResource(R.drawable.bg_chip_unselected)
            tv.setTextColor(Color.BLACK)
        } else {
            bag.add(value)
            tv.setBackgroundResource(R.drawable.bg_chip_selected)
            tv.setTextColor(Color.WHITE)
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

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
