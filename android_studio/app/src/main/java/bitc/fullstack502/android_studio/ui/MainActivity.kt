package bitc.fullstack502.android_studio.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import bitc.fullstack502.android_studio.FlightReservationActivity
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.ui.lodging.LodgingListActivity
import bitc.fullstack502.android_studio.ui.lodging.LodgingSearchActivity
import bitc.fullstack502.android_studio.ui.mypage.LoginActivity
import bitc.fullstack502.android_studio.ui.mypage.MyPageActivity
import bitc.fullstack502.android_studio.ui.post.PostListActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import kotlin.jvm.java
import bitc.fullstack502.android_studio.ui.common.BaseFooterActivity

class MainActivity : BaseFooterActivity() {

    private lateinit var drawer: DrawerLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var dots: LinearLayout

    private val handler = Handler(Looper.getMainLooper())
    private var autoRunnable: Runnable? = null

    // 프로젝트에 있는 이미지 리소스에 맞춰 사용하세요(존재하는 파일명으로 유지)
    private val slideImages = listOf(
        R.drawable.slide1, R.drawable.slide2, R.drawable.slide3,
        R.drawable.slide4, R.drawable.slide5
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawer = findViewById(R.id.drawerLayout)
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        val navView: NavigationView = findViewById(R.id.navigationView)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        // 드로어 헤더 인사말 세팅 (로그인 상태 반영)
        updateHeader(navView)

        // --- 이미지 슬라이더(기존 리소스/어댑터를 그대로 사용) ---
        viewPager = findViewById(R.id.viewPager)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        dots = findViewById(R.id.dotsContainer)

        // 프로젝트에 포함된 어댑터 이름 그대로 사용
        viewPager.adapter = ImageSliderAdapter(slideImages)
        setupDots(slideImages.size)
        updateDots(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateDots(position)
            }
        })

        btnPrev.setOnClickListener {
            val prev = (viewPager.currentItem - 1 + slideImages.size) % slideImages.size
            viewPager.currentItem = prev
        }
        btnNext.setOnClickListener {
            val next = (viewPager.currentItem + 1) % slideImages.size
            viewPager.currentItem = next
        }

        // 자동 슬라이딩
        startAutoSlide()

        // --- 메인 카드 연결 (항공 제외) ---
        findViewById<MaterialCardView>(R.id.cardHotel).setOnClickListener {
            startActivity(Intent(this, LodgingSearchActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardBoard).setOnClickListener {
            startActivity(Intent(this, PostListActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardChat).setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardFlight).setOnClickListener {
            startActivity(Intent(this, FlightReservationActivity::class.java))
        }

        // --- 드로어 메뉴 연결 ---
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
    }

    // 우상단 햄버거 메뉴
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_drawer -> {
                drawer.openDrawer(GravityCompat.END); true
            }
            else -> super.onOptionsItemSelected(item)
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

    // ----------------- 슬라이더 보조 함수 -----------------

    private fun startAutoSlide() {
        stopAutoSlide()
        autoRunnable = object : Runnable {
            override fun run() {
                if (!this@MainActivity::viewPager.isInitialized) return
                val next = (viewPager.currentItem + 1) % slideImages.size
                viewPager.currentItem = next
                handler.postDelayed(this, 3000)
            }
        }
        handler.postDelayed(autoRunnable!!, 3000)
    }

    private fun stopAutoSlide() {
        autoRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun setupDots(count: Int) {
        dots.removeAllViews()
        val size = dp(6)
        val margin = dp(4)
        repeat(count) {
            val v = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).also { lp ->
                    lp.leftMargin = margin
                    lp.rightMargin = margin
                }
                background = ContextCompat.getDrawable(this@MainActivity, R.drawable.dot_inactive)
            }
            dots.addView(v)
        }
    }

    private fun updateDots(position: Int) {
        for (i in 0 until dots.childCount) {
            val v = dots.getChildAt(i)
            v.background = ContextCompat.getDrawable(
                this, if (i == position) R.drawable.dot_active else R.drawable.dot_inactive
            )
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        super.onDestroy()
        stopAutoSlide()
    }
}
