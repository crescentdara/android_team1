package bitc.fullstack502.android_studio.ui.lodging

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.network.dto.AvailabilityDto
import bitc.fullstack502.android_studio.network.dto.LodgingDetailDto
import bitc.fullstack502.android_studio.network.dto.LodgingWishStatusDto
import bitc.fullstack502.android_studio.ui.ChatListActivity
import bitc.fullstack502.android_studio.ui.MainActivity
import bitc.fullstack502.android_studio.ui.mypage.LoginActivity
import bitc.fullstack502.android_studio.ui.mypage.MyPageActivity
import bitc.fullstack502.android_studio.ui.post.PostListActivity
import bitc.fullstack502.android_studio.util.AuthManager
import bitc.fullstack502.android_studio.util.fullUrl
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class LodgingDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var naverMap: NaverMap? = null
    private var marker: Marker? = null

    private var lodgingId: Long = 0L
    private var lat = 0.0
    private var lon = 0.0
    private var name = ""

    private lateinit var imgCover: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvAddr: TextView
    private lateinit var tvPhone: TextView
    private lateinit var rgRoom: RadioGroup
    private lateinit var rbSingle: RadioButton
    private lateinit var rbDeluxe: RadioButton
    private lateinit var rbSuite: RadioButton
    private lateinit var tvSelection: TextView
    private lateinit var tvTotalPrice: TextView
    private lateinit var btnReserve: Button
    private lateinit var btnWish: ImageButton
    private lateinit var tvWishCount: TextView

    private var wished = false
    private var selectedNights = 0

    private var checkIn: String = ""
    private var checkOut: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lodging_detail)

        // ✅ AuthManager 초기화
        AuthManager.init(this)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        imgCover = findViewById(R.id.imgCover)
        tvName = findViewById(R.id.tvName)
        tvAddr = findViewById(R.id.tvAddr)
        tvPhone = findViewById(R.id.tvPhone)
        rgRoom = findViewById(R.id.rgRoom)
        rbSingle = findViewById(R.id.rbSingle)
        rbDeluxe = findViewById(R.id.rbDeluxe)
        rbSuite = findViewById(R.id.rbSuite)
        tvSelection = findViewById(R.id.tvSelection)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        btnReserve = findViewById(R.id.btnReserve)
        btnWish = findViewById(R.id.btnWish)
        tvWishCount = findViewById(R.id.tvWishCount)

        lodgingId = intent.getLongExtra("lodgingId", 0L)
        checkIn = intent.getStringExtra("checkIn") ?: ""
        checkOut = intent.getStringExtra("checkOut") ?: ""

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

        // 숙박일수 계산
        selectedNights = try {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val s = fmt.parse(checkIn)?.time ?: 0L
            val e = fmt.parse(checkOut)?.time ?: 0L
            ((e - s) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
        } catch (_: Exception) { 0 }

        setReserveEnabled(false)
        fetchDetail()

        rgRoom.setOnCheckedChangeListener { _, _ -> updateSelectionAndTotal() }

        btnReserve.setOnClickListener {
            if (!AuthManager.isLoggedIn()) {
                Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val roomType = when (rgRoom.checkedRadioButtonId) {
                R.id.rbSingle -> "싱글"
                R.id.rbDeluxe -> "디럭스"
                R.id.rbSuite -> "스위트"
                else -> ""
            }
            val total = pricePerNight() * selectedNights
            startActivity(Intent(this, LodgingPaymentActivity::class.java).apply {
                putExtra("lodgingId", lodgingId)
                putExtra("checkIn", checkIn)
                putExtra("checkOut", checkOut)
                putExtra("roomType", roomType)
                putExtra("totalPrice", total.toLong())
            })
        }

        btnWish.setOnClickListener {
            if (!AuthManager.isLoggedIn()) {
                Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            toggleWish()
        }
    }

    override fun onMapReady(nMap: NaverMap) {
        naverMap = nMap
        nMap.uiSettings.apply {
            isZoomControlEnabled = true
            isLocationButtonEnabled = false
            isCompassEnabled = false
            isScaleBarEnabled = true
        }
        updateMapMarkerIfReady()
    }

    private fun fetchDetail() {
        ApiProvider.api.getDetail(lodgingId).enqueue(object : Callback<LodgingDetailDto> {
            override fun onResponse(c: Call<LodgingDetailDto>, r: Response<LodgingDetailDto>) {
                val d = r.body() ?: return
                name = d.name.orEmpty()
                lat = d.lat ?: 0.0
                lon = d.lon ?: 0.0
                tvName.text = name
                tvAddr.text = listOfNotNull(d.addrRd, d.addrJb).joinToString(" / ")
                tvPhone.text = d.phone ?: ""

                fullUrl(d.img)?.let { Glide.with(this@LodgingDetailActivity).load(it).into(imgCover) }

                updateMapMarkerIfReady()
                refreshWish()
                checkAvailability(checkIn, checkOut)
            }
            override fun onFailure(c: Call<LodgingDetailDto>, t: Throwable) { }
        })
    }

    private fun updateMapMarkerIfReady() {
        val nMap = naverMap ?: return
        val pos = LatLng(lat, lon)
        nMap.moveCamera(CameraUpdate.scrollTo(pos))
        if (marker == null) marker = Marker()
        marker?.apply { position = pos; captionText = name; map = nMap }
    }

    private fun refreshWish() {
        if (!AuthManager.isLoggedIn()) {
            wished = false
            tvWishCount.text = "0"
            updateWishIcon()
            return
        }
        val userPk = AuthManager.id()
        if (userPk <= 0L) {
            wished = false
            tvWishCount.text = "0"
            updateWishIcon()
            return
        }
        ApiProvider.api.wishStatus(lodgingId, userPk)
            .enqueue(object : Callback<LodgingWishStatusDto> {
                override fun onResponse(call: Call<LodgingWishStatusDto>, res: Response<LodgingWishStatusDto>) {
                    res.body()?.let { s ->
                        wished = s.wished
                        tvWishCount.text = s.wishCount.toString()
                        updateWishIcon()
                    }
                }
                override fun onFailure(call: Call<LodgingWishStatusDto>, t: Throwable) { }
            })
    }

    private fun toggleWish() {
        val userPk = AuthManager.id()
        if (userPk <= 0L) {
            Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        ApiProvider.api.wishToggle(lodgingId, userPk)
            .enqueue(object : Callback<LodgingWishStatusDto> {
                override fun onResponse(call: Call<LodgingWishStatusDto>, res: Response<LodgingWishStatusDto>) {
                    res.body()?.let { s ->
                        wished = s.wished
                        tvWishCount.text = s.wishCount.toString()
                        updateWishIcon()
                    }
                }
                override fun onFailure(call: Call<LodgingWishStatusDto>, t: Throwable) { }
            })
    }

    private fun updateWishIcon() {
        btnWish.setImageResource(if (wished) R.drawable.ic_star_24 else R.drawable.ic_star_border_24)
    }

    private fun checkAvailability(ci: String, co: String) {
        ApiProvider.api.getAvailability(lodgingId, ci, co, null)
            .enqueue(object : Callback<AvailabilityDto> {
                override fun onResponse(c: Call<AvailabilityDto>, r: Response<AvailabilityDto>) {
                    val a = r.body() ?: return
                    val enable = a.availableRooms > 0
                    listOf(rbSingle, rbDeluxe, rbSuite).forEach { it.isEnabled = enable }
                    if (!enable) {
                        rgRoom.clearCheck()
                        tvSelection.text = "선택한 옵션: (만실)"
                        Toast.makeText(this@LodgingDetailActivity, a.reason ?: "만실", Toast.LENGTH_SHORT).show()
                    } else updateSelectionAndTotal()
                }
                override fun onFailure(c: Call<AvailabilityDto>, t: Throwable) { }
            })
    }

    private fun pricePerNight(): Int = when (rgRoom.checkedRadioButtonId) {
        R.id.rbSingle -> 100_000
        R.id.rbDeluxe -> 120_000
        R.id.rbSuite  -> 150_000
        else -> 0
    }

    private fun updateSelectionAndTotal() {
        val room = when (rgRoom.checkedRadioButtonId) {
            R.id.rbSingle -> "싱글"
            R.id.rbDeluxe -> "디럭스"
            R.id.rbSuite -> "스위트"
            else -> "-"
        }
        val won = NumberFormat.getNumberInstance(Locale.KOREA).format(pricePerNight())
        val nightsText = if (selectedNights > 0) " • ${selectedNights}박" else ""
        tvSelection.text = "선택한 옵션: $room${if (room == "-") "" else " / 1박: ₩$won"}$nightsText"

        val total = pricePerNight() * selectedNights
        tvTotalPrice.text = "총 결제금액: " +
                NumberFormat.getCurrencyInstance(Locale.KOREA).format(total.toLong())

        setReserveEnabled(room != "-" && selectedNights > 0)
    }

    private fun setReserveEnabled(enabled: Boolean) {
        btnReserve.isEnabled = enabled
        btnReserve.alpha = if (enabled) 1f else 0.5f
    }

    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume(); refreshWish() }
    override fun onPause() { mapView.onPause(); super.onPause() }
    override fun onStop() { mapView.onStop(); super.onStop() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
    override fun onDestroy() { mapView.onDestroy(); super.onDestroy() }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState)
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
