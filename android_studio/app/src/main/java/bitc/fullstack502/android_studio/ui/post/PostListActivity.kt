package bitc.fullstack502.android_studio.ui.post

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import bitc.fullstack502.android_studio.FlightReservationActivity
import bitc.fullstack502.android_studio.databinding.ActivityPostListBinding
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.network.dto.PagePostDto
import bitc.fullstack502.android_studio.ui.ChatListActivity
import bitc.fullstack502.android_studio.ui.lodging.LodgingSearchActivity
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.ui.MainActivity
import bitc.fullstack502.android_studio.ui.mypage.LoginActivity
import bitc.fullstack502.android_studio.ui.mypage.MyPageActivity
import com.google.android.material.button.MaterialButton

class PostListActivity : AppCompatActivity() {
    private lateinit var bind: ActivityPostListBinding
    private val adapter = PostListAdapter { post ->
        if (!isLoggedIn()) {
            showLoginRequiredDialog()
            return@PostListAdapter
        }
        startActivity(Intent(this, PostDetailActivity::class.java).putExtra("id", post.id))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityPostListBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.recycler.layoutManager = LinearLayoutManager(this)
        bind.recycler.adapter = adapter

        // 엔터로 검색
        bind.etQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch()
                true
            } else false
        }

        // 글쓰기 버튼
        bind.fab.setOnClickListener {
            if (!isLoggedIn()) {
                showLoginRequiredDialog()
            } else {
                startActivity(Intent(this, PostWriteActivity::class.java))
            }
        }

        bind.swipe.setOnRefreshListener { reload() }
        bind.swipe.isRefreshing = true
        reload()

        // 헤더 기능
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        val imgLogo: ImageView = findViewById(R.id.imgLogo)
        val btnMenu: ImageButton = findViewById(R.id.btnMenu)   // ← 여기 꼭 btnMenu로 수정!

        // Drawer 연결
        val drawer: DrawerLayout = findViewById(R.id.drawerLayout)
        val navView: NavigationView = findViewById(R.id.navigationView)

        updateHeader(navView)

        btnMenu.setOnClickListener {
            drawer.openDrawer(GravityCompat.END)
        }

        // 🔙 뒤로가기 → 현재 화면 종료
        btnBack.setOnClickListener {
            finish()
        }

        // 🏠 로고 클릭 → 홈(MainActivity)으로 이동
        imgLogo.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // 스택 정리 후 메인으로
            })
        }

        // 드로어 헤더 인사말 세팅 (로그인 상태 반영)
        updateHeader(navView)

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_hotel -> { startActivity(Intent(this, LodgingSearchActivity::class.java)); true }
                R.id.nav_board -> { startActivity(Intent(this, PostListActivity::class.java)); true }
                R.id.nav_chat -> { startActivity(Intent(this, ChatListActivity::class.java)); true }
                R.id.nav_flight -> { startActivity(Intent(this, FlightReservationActivity::class.java)); true }
                else -> false
            }.also { drawer.closeDrawers() }
        }
    }

    override fun onResume() {
        super.onResume()
        bind.swipe.isRefreshing = true
        reload()
    }

    private fun showLoginRequiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("로그인이 필요합니다")
            .setMessage("해당 기능을 이용하려면 로그인 해주세요.")
            .setPositiveButton("확인", null)
            .show()
    }

    private fun reload() {
        val q = bind.etQuery.text.toString().trim()
        if (q.isEmpty()) load() else doSearch()
    }

    private fun fieldKey(): String = when (bind.spnField.selectedItemPosition) {
        0 -> "title"
        1 -> "content"
        else -> "author"
    }

    private fun doSearch() {
        val q = bind.etQuery.text.toString().trim()
        if (q.isEmpty()) { load(); return }
        ApiProvider.api.searchPosts(fieldKey(), q).enqueue(object : Callback<PagePostDto> {
            override fun onResponse(call: Call<PagePostDto>, res: Response<PagePostDto>) {
                if (isFinishing || isDestroyed) return
                bind.swipe.isRefreshing = false
                val list = res.body()?.content ?: emptyList()
                adapter.submitList(list.toList())
            }
            override fun onFailure(call: Call<PagePostDto>, t: Throwable) {
                if (isFinishing || isDestroyed) return
                bind.swipe.isRefreshing = false
                adapter.submitList(emptyList())
            }
        })
    }

    private fun load() {

        // ✅ 로딩 시작 → 푸터 감추기
        findViewById<View>(R.id.footerRoot)?.visibility = View.GONE

        ApiProvider.api.list().enqueue(object : Callback<PagePostDto> {
            override fun onResponse(call: Call<PagePostDto>, res: Response<PagePostDto>) {
                if (isFinishing || isDestroyed) return
                bind.swipe.isRefreshing = false
                val list = res.body()?.content ?: emptyList()
                adapter.submitList(list.toList())

                // ✅ 로딩 끝 → 푸터 보이기
                findViewById<View>(R.id.footerRoot)?.visibility = View.VISIBLE
            }
            override fun onFailure(call: Call<PagePostDto>, t: Throwable) {
                if (isFinishing || isDestroyed) return
                bind.swipe.isRefreshing = false
                adapter.submitList(emptyList())

                // ✅ 실패해도 푸터는 보여주도록
                findViewById<View>(R.id.footerRoot)?.visibility = View.VISIBLE
            }
        })
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
