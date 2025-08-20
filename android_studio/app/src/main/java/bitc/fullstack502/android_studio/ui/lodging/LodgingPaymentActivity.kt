package bitc.fullstack502.android_studio.ui.lodging

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.databinding.ActivityLodgingPaymentBinding
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.network.dto.LodgingBookingDto
import bitc.fullstack502.android_studio.network.dto.LodgingDetailDto
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale
import bitc.fullstack502.android_studio.R

class LodgingPaymentActivity : AppCompatActivity() {

    private lateinit var b: ActivityLodgingPaymentBinding
    private var lodgingAddr = ""
    private var lodgingImg: String? = null
    private var lodgingName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLodgingPaymentBinding.inflate(layoutInflater)
        setContentView(b.root)

        // ✅ AuthManager 초기화
        AuthManager.init(this)

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

        val lodgingId = intent.getLongExtra("lodgingId", -1L)
        val checkIn = intent.getStringExtra("checkIn") ?: ""
        val checkOut = intent.getStringExtra("checkOut") ?: ""
        val roomType = intent.getStringExtra("roomType") ?: ""
        val totalPrice = intent.getLongExtra("totalPrice", 0L)

        if (lodgingId != -1L) {
            ApiProvider.api.getDetail(lodgingId)
                .enqueue(object : Callback<LodgingDetailDto> {
                    override fun onResponse(
                        call: Call<LodgingDetailDto>,
                        res: Response<LodgingDetailDto>
                    ) {
                        val d = res.body() ?: return
                        lodgingName = d.name
                        lodgingAddr = d.addrRd ?: d.addrJb ?: ""
                        lodgingImg = d.img

                        b.tvLodgingName.text = lodgingName ?: ""
                        b.tvLodgingAddr.text = lodgingAddr
                        b.tvLodgingPhone.text = d.phone ?: ""

                        fullUrl(lodgingImg)?.let {
                            Glide.with(this@LodgingPaymentActivity).load(it).into(b.imgLodgingCover)
                        }
                    }

                    override fun onFailure(call: Call<LodgingDetailDto>, t: Throwable) {}
                })
        }

        b.tvCheckInOut.text = "$checkIn ~ $checkOut"
        b.tvRoomType.text = "객실 타입: $roomType"
        b.tvTotalPrice.text =
            "총 결제금액: " + NumberFormat.getCurrencyInstance(Locale.KOREA).format(totalPrice)

        b.btnLodgingPay.setOnClickListener {
            if (!AuthManager.isLoggedIn()) {
                Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userPk = AuthManager.id()
            if (userPk <= 0L) {
                Toast.makeText(this, "로그인 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ 결제수단 선택 확인
            val checkedId = b.rgPayment.checkedRadioButtonId
            if (checkedId == -1) {
                Toast.makeText(this, "결제수단을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val paymentMethod = when (checkedId) {
                R.id.rbKakaoPay -> "카카오페이"
                R.id.rbCard -> "신용카드"
                R.id.rbBank -> "무통장입금"
                else -> ""
            }

            // ✅ 실제 예약 저장 로직 (기존 코드 그대로)
            val booking = LodgingBookingDto(
                id = null,
                userId = userPk,
                lodId = lodgingId,
                ckIn = checkIn,
                ckOut = checkOut,
                totalPrice = totalPrice,
                roomType = roomType,
                adult = 1,
                child = 0,
                status = "BOOKED",
                lodName = lodgingName,
                lodImg = lodgingImg,
                addrRd = null,
                addrJb = null
            )

            ApiProvider.api.createBooking(booking).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        startActivity(
                            Intent(this@LodgingPaymentActivity, LodgingPaymentCompleteActivity::class.java).apply {
                                putExtra("lodgingName", lodgingName ?: "")
                                putExtra("lodgingAddr", lodgingAddr)
                                // ✅ 여기서 풀 URL 로 변환해서 넘김
                                putExtra("lodgingImg", fullUrl(lodgingImg))
                                putExtra("checkIn", checkIn)
                                putExtra("checkOut", checkOut)
                                putExtra("roomType", roomType)
                                putExtra("price", totalPrice)
                                putExtra("paymentMethod", paymentMethod)
                            }
                        )
                        finish()
                    } else {
                        Toast.makeText(this@LodgingPaymentActivity, "예약 저장 실패 (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@LodgingPaymentActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
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
