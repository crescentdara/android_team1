package bitc.fullstack502.android_studio.ui.lodging

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.FlightReservationActivity
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.ui.ChatListActivity
import bitc.fullstack502.android_studio.ui.MainActivity
import bitc.fullstack502.android_studio.ui.mypage.LoginActivity
import bitc.fullstack502.android_studio.ui.mypage.MyPageActivity
import bitc.fullstack502.android_studio.ui.post.PostListActivity
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class LodgingSearchActivity : AppCompatActivity() {

    // XML 매칭 뷰들
    private lateinit var recyclerCity: RecyclerView
    private lateinit var recyclerTown: RecyclerView
    private lateinit var recyclerVill: RecyclerView
    private lateinit var btnDateGuest: MaterialButton
    private lateinit var btnSearch: MaterialButton
    private lateinit var layoutSelectedChips: FlexboxLayout
    private lateinit var tvDateSummary: TextView
    private lateinit var tvGuestSummary: TextView

    // 어댑터
    private lateinit var cityAdapter: LocationAdapter
    private lateinit var townAdapter: LocationAdapter
    private lateinit var villAdapter: LocationAdapter

    // 선택 값
    private var selectedCity: String = ""
    private val selectedTowns = mutableListOf<String>()
    private val selectedVills = mutableListOf<String>()

    // 날짜/인원
    private var checkIn: String = ""
    private var checkOut: String = ""
    private var adults: Int = 1
    private var children: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lodging_search)

        // XML 뷰 연결
        recyclerCity = findViewById(R.id.recyclerCity)
        recyclerTown = findViewById(R.id.recyclerTown)
        recyclerVill = findViewById(R.id.recyclerVill)
        btnDateGuest = findViewById(R.id.btnDateGuest)
        btnSearch = findViewById(R.id.btnSearch)
        layoutSelectedChips = findViewById(R.id.layoutSelectedChips)
        tvDateSummary = findViewById(R.id.tvDateSummary)
        tvGuestSummary = findViewById(R.id.tvGuestSummary)

        // ✅ LayoutManager 추가
        recyclerCity.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerTown.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerVill.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // ---------------- Adapter 연결 ----------------
        // ✅ 도시 : 단일 선택
        cityAdapter = LocationAdapter(multiSelect = false) { city ->
            selectedCity = city
            selectedTowns.clear()
            selectedVills.clear()
            loadTowns(city)

            // 리셋: 새 도시 선택 시, 읍/면/동과 리는 초기화
            recyclerTown.visibility = View.VISIBLE
            recyclerVill.visibility = View.GONE
            townAdapter.submitList(emptyList())
            villAdapter.submitList(emptyList())

            updateSearchButtonState()
            updateSelectedChips()
        }
        recyclerCity.adapter = cityAdapter

        // ✅ 읍/면/동 : 다중 선택
        townAdapter = LocationAdapter(multiSelect = true) { town ->
            if (selectedTowns.contains(town)) {
                selectedTowns.remove(town)
            } else {
                selectedTowns.add(town)
            }

            // 읍/면/동 새로 선택되면 리 목록 불러오기
            loadVills(selectedCity, selectedTowns.toList())
            recyclerVill.visibility = View.VISIBLE

            updateSearchButtonState()
            updateSelectedChips()
        }
        recyclerTown.adapter = townAdapter

        // ✅ 리 : 다중 선택
        villAdapter = LocationAdapter(multiSelect = true) { vill ->
            if (selectedVills.contains(vill)) {
                selectedVills.remove(vill)
            } else {
                selectedVills.add(vill)
            }
            updateSearchButtonState()
            updateSelectedChips()
        }
        recyclerVill.adapter = villAdapter


        // 초기 상태
        recyclerTown.visibility = View.GONE
        recyclerVill.visibility = View.GONE
        setSearchEnabled(false)

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

        // DB에서 도시 로드
        loadCities()

        // 날짜/인원 바텀시트 열기
        btnDateGuest.setOnClickListener { openDateGuestSheet() }

        // 날짜
        supportFragmentManager.setFragmentResultListener(
            LodgingFilterBottomSheet.RESULT_KEY,
            this
        ) { _, bundle ->
            checkIn   = bundle.getString(LodgingFilterBottomSheet.EXTRA_CHECK_IN, "")
            checkOut  = bundle.getString(LodgingFilterBottomSheet.EXTRA_CHECK_OUT, "")
            adults    = bundle.getInt(LodgingFilterBottomSheet.EXTRA_ADULTS, 1)
            children  = bundle.getInt(LodgingFilterBottomSheet.EXTRA_CHILDREN, 0)

            // ✅ UI 업데이트
            tvDateSummary.text = "$checkIn ~ $checkOut"
            tvGuestSummary.text = "성인 $adults, 아동 $children"

            updateSearchButtonState()
        }


        // 검색 버튼
        btnSearch.setOnClickListener {
            if (!hasValidDateGuest()) {
                toast("날짜와 인원을 먼저 선택하세요."); return@setOnClickListener
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

    // ---------------- DB 연동 ----------------
    private fun loadCities() {
        lifecycleScope.launch {
            val cities = withContext(Dispatchers.IO) {
                try { ApiProvider.api.getCities() } catch (_: Exception) { emptyList() }
            }
            cityAdapter.submitList(cities)
        }
    }

    private fun loadTowns(city: String) {
        recyclerTown.visibility = View.GONE
        lifecycleScope.launch {
            val towns = withContext(Dispatchers.IO) {
                try { ApiProvider.api.getTowns(city) } catch (_: Exception) { emptyList() }
            }
            townAdapter.submitList(towns)
            recyclerTown.visibility = if (towns.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun loadVills(city: String, towns: List<String>) {
        recyclerVill.visibility = View.GONE
        if (towns.isEmpty()) {
            villAdapter.submitList(emptyList())
            selectedVills.clear()   // ✅ 읍/면/동 없으면 리 선택도 초기화
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
            val filtered = allVills.filter { it.isNotBlank() && it != "없음" && it != "-" }

            // ✅ 기존 선택된 리 중에서 이번 목록에도 있는 것만 유지
            selectedVills.retainAll(filtered)

            villAdapter.submitList(filtered)
            recyclerVill.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE

            updateSearchButtonState()
            updateSelectedChips()
        }
    }

    // ---------------- 유틸 ----------------
    private fun openDateGuestSheet() {
        LodgingFilterBottomSheet().show(supportFragmentManager, "lodging_filter")
    }

    private fun hasValidDateGuest(): Boolean {
        if (checkIn.isBlank() || checkOut.isBlank()) return false
        if (adults < 1) return false
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val s = fmt.parse(checkIn)?.time ?: return false
            val e = fmt.parse(checkOut)?.time ?: return false
            e > s
        } catch (_: Exception) { false }
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

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    // ---------------- 칩 갱신 ----------------
    private fun updateSelectedChips() {
        layoutSelectedChips.removeAllViews()

        // ✅ 시 (단일 선택)
        if (selectedCity.isNotBlank()) {
            addChipToSelected(selectedCity, {
                selectedCity = ""
                selectedTowns.clear()
                selectedVills.clear()
                recyclerTown.visibility = View.GONE
                recyclerVill.visibility = View.GONE
                updateSearchButtonState()
                updateSelectedChips()
            }, cityAdapter) // 🔥 여기서 cityAdapter 넘겨줌
        }

        // ✅ 읍/면/동 (다중 선택)
        selectedTowns.forEach { town ->
            addChipToSelected(town, {
                selectedTowns.remove(town)
                updateSearchButtonState()
                updateSelectedChips()
            }, townAdapter) // 🔥 townAdapter 넘겨줌
        }

        // ✅ 리 (다중 선택)
        selectedVills.forEach { vill ->
            addChipToSelected(vill, {
                selectedVills.remove(vill)
                updateSearchButtonState()
                updateSelectedChips()
            }, villAdapter) // 🔥 villAdapter 넘겨줌
        }
    }

    private fun addChipToSelected(
        text: String,
        onRemove: () -> Unit,
        adapter: LocationAdapter? = null   // ✅ adapter 파라미터 추가
    ) {
        val chip = LayoutInflater.from(this)
            .inflate(R.layout.item_city_chip, layoutSelectedChips, false) as Chip
        chip.text = text
        chip.isCloseIconVisible = true

        chip.setOnCloseIconClickListener {
            onRemove()
            adapter?.deselectItem(text)   // ✅ 리스트에서도 선택 해제
            layoutSelectedChips.removeView(chip)
        }

        layoutSelectedChips.addView(chip)
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
