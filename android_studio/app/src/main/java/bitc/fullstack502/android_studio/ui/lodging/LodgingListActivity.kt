package bitc.fullstack502.android_studio.ui.lodging

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.model.LodgingItem
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.ui.ChatListActivity
import bitc.fullstack502.android_studio.ui.MainActivity
import bitc.fullstack502.android_studio.ui.mypage.LoginActivity
import bitc.fullstack502.android_studio.ui.mypage.MyPageActivity
import bitc.fullstack502.android_studio.ui.post.PostListActivity
import bitc.fullstack502.android_studio.util.fullUrl
import coil.load
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.*

class LodgingListActivity : AppCompatActivity() {
    private lateinit var rv: RecyclerView
    private lateinit var progress: View
    private lateinit var empty: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lodging_list)

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

        val checkIn = intent.getStringExtra(LodgingFilterBottomSheet.EXTRA_CHECK_IN).orEmpty()
        val checkOut = intent.getStringExtra(LodgingFilterBottomSheet.EXTRA_CHECK_OUT).orEmpty()
        val adults = intent.getIntExtra(LodgingFilterBottomSheet.EXTRA_ADULTS, 1)
        val children = intent.getIntExtra(LodgingFilterBottomSheet.EXTRA_CHILDREN, 0)
        val city = intent.getStringExtra("city")
        val town = intent.getStringExtra("town")
        val vill = intent.getStringExtra("vill")

        findViewById<TextView>(R.id.tvLocation).text =
            "위치: ${listOfNotNull(city, town, vill).joinToString(" ") { it.ifBlank { "-" } }}"
        findViewById<TextView>(R.id.tvDates).text =
            if (checkIn.isNotBlank() && checkOut.isNotBlank())
                "날짜: $checkIn ~ $checkOut" else "날짜: -"
        findViewById<TextView>(R.id.tvGuests).text =
            "인원: 성인 $adults, 아동 $children"

        rv = findViewById(R.id.rvLodgings)
        rv.layoutManager = LinearLayoutManager(this)
        progress = findViewById(R.id.progressBar)
        empty = findViewById(R.id.placeholderEmpty)

        loadLodgings(city, town, vill, checkIn, checkOut, adults, children)
    }

    private fun loadLodgings(
        city: String?, town: String?, vill: String?,
        checkIn: String?, checkOut: String?,
        adults: Int, children: Int
    ) {
        progress.visibility = View.VISIBLE
        empty.visibility = View.GONE
        rv.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val items = withContext(Dispatchers.IO) {
                    ApiProvider.api.getLodgings(
                        city = city?.ifBlank { null },
                        town = town?.ifBlank { null },
                        vill = vill?.ifBlank { null },
                        checkIn = checkIn?.ifBlank { null },
                        checkOut = checkOut?.ifBlank { null },
                        adults = adults,
                        children = children
                    )
                }
                progress.visibility = View.GONE
                if (items.isEmpty()) {
                    empty.visibility = View.VISIBLE
                } else {
                    rv.adapter = LodgingAdapter(items) { selected ->
                        val i = Intent(this@LodgingListActivity, LodgingDetailActivity::class.java).apply {
                            putExtra("lodgingId", selected.id)
                            putExtra("checkIn", checkIn)
                            putExtra("checkOut", checkOut)
                        }
                        startActivity(i)
                    }
                    rv.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                progress.visibility = View.GONE
                empty.visibility = View.VISIBLE
                rv.visibility = View.GONE
                Toast.makeText(this@LodgingListActivity, "숙소 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private class LodgingAdapter(
        private val items: List<LodgingItem>,
        private val onItemClick: (LodgingItem) -> Unit
    ) : RecyclerView.Adapter<LodgingAdapter.VH>() {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val iv: ImageView = v.findViewById(R.id.ivLodgingThumb)
            val tvName: TextView = v.findViewById(R.id.tvLodgingName)
            val tvAddr: TextView = v.findViewById(R.id.tvLodgingAddr)
            val tvPrice: TextView = v.findViewById(R.id.tvPrice) // ⚠ row_lodging.xml 과 id 일치
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.row_lodging, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, position: Int) {
            val item = items[position]
            h.tvName.text = item.name
            h.tvAddr.text = listOfNotNull(item.city, item.town).joinToString(" ")

            // ⚡ 가격을 고정된 값으로 표시
            h.tvPrice.text = "￦100,000~"

            val url = fullUrl(item.img)
            if (url == null) h.iv.setImageResource(R.drawable.ic_launcher_foreground)
            else h.iv.load(url)

            h.itemView.setOnClickListener { onItemClick(item) }
        }


        override fun getItemCount(): Int = items.size
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
