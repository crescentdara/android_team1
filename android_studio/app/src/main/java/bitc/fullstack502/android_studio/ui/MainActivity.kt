package bitc.fullstack502.android_studio.ui

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.viewpager2.widget.ViewPager2
import bitc.fullstack502.android_studio.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout

class MainActivity : AppCompatActivity() {

    private lateinit var drawer: DrawerLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var dots: LinearLayout

    private val handler = Handler(Looper.getMainLooper())
    private var autoRunnable: Runnable? = null
    private var autoRunning = true

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

        // 드로어 헤더 인사말 세팅 (로그인 전 기본값)
        val header = navView.getHeaderView(0)
        header.findViewById<TextView>(R.id.tvUserGreeting)
            .text = getString(R.string.greeting_fmt, "000")
        header.findViewById<TextView>(R.id.tvUserEmail).text = "guest@example.com"

        // 슬라이더
        viewPager = findViewById(R.id.viewPager)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        dots = findViewById(R.id.dotsContainer)

        viewPager.adapter = ImageSliderAdapter(slideImages)
        setupDots(slideImages.size)
        updateDots(0)

        // slide1 비율로 높이 자동 조정
        viewPager.post {
            val width = viewPager.width
            val d = ContextCompat.getDrawable(this, R.drawable.slide1)!!   // ✅ 기준 이미지(가장 큰 이미지)
            val ratio = d.intrinsicHeight.toFloat() / d.intrinsicWidth.toFloat()
            val targetHeight = (width * ratio).toInt()
            viewPager.layoutParams.height = targetHeight
            findViewById<View>(R.id.sliderContainer).layoutParams.height = targetHeight
            viewPager.requestLayout()
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateDots(position)
            }
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                autoRunning = state == ViewPager2.SCROLL_STATE_IDLE
            }
        })

        // 좌/우 버튼
        btnPrev.setOnClickListener {
            val prev = (viewPager.currentItem - 1 + slideImages.size) % slideImages.size
            viewPager.setCurrentItem(prev, true)
        }
        btnNext.setOnClickListener {
            val next = (viewPager.currentItem + 1) % slideImages.size
            viewPager.setCurrentItem(next, true)
        }

        startAutoSlide()

        // 드로어 메뉴 클릭
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_flight -> { /* TODO */ }
                R.id.nav_hotel  -> { /* TODO */ }
                R.id.nav_board  -> { /* TODO */ }
                R.id.nav_chat   -> { startActivity(Intent(this, ChatListActivity::class.java)) }
            }
            drawer.closeDrawer(GravityCompat.END)
            true
        }

        // 메인 2×2 카드 클릭
        findViewById<MaterialCardView>(R.id.cardFlight).setOnClickListener { /* TODO */ }
        findViewById<MaterialCardView>(R.id.cardHotel).setOnClickListener  { /* TODO */ }
        findViewById<MaterialCardView>(R.id.cardBoard).setOnClickListener  { /* TODO */ }
        findViewById<MaterialCardView>(R.id.cardChat).setOnClickListener   {
            startActivity(Intent(this, ChatListActivity::class.java))
        }
    }

    // 오른쪽 햄버거 메뉴
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_drawer -> { drawer.openDrawer(GravityCompat.END); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startAutoSlide() {
        autoRunnable = object : Runnable {
            override fun run() {
                if (autoRunning) {
                    val next = (viewPager.currentItem + 1) % slideImages.size
                    viewPager.setCurrentItem(next, true)
                }
                handler.postDelayed(this, 3000)
            }
        }
        handler.postDelayed(autoRunnable!!, 3000)
    }

    private fun setupDots(count: Int) {
        dots.removeAllViews()
        for (i in 0 until count) {
            val v = View(this)
            val size = dp(8)
            val lp = LinearLayout.LayoutParams(size, size)
            lp.marginStart = dp(4); lp.marginEnd = dp(4)
            v.layoutParams = lp
            v.background = ContextCompat.getDrawable(this, R.drawable.dot_inactive)
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
        autoRunnable?.let { handler.removeCallbacks(it) }
    }
}
