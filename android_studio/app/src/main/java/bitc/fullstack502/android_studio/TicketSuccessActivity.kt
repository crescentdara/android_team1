package bitc.fullstack502.android_studio

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import bitc.fullstack502.android_studio.model.BookingResponse
import bitc.fullstack502.android_studio.model.Passenger
import bitc.fullstack502.android_studio.ui.ChatListActivity
import bitc.fullstack502.android_studio.ui.MainActivity
import bitc.fullstack502.android_studio.ui.lodging.LodgingSearchActivity
import bitc.fullstack502.android_studio.ui.mypage.LoginActivity
import bitc.fullstack502.android_studio.ui.mypage.MyPageActivity
import bitc.fullstack502.android_studio.ui.post.PostListActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

class TicketSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_success)

        // ===== Drawer & NavigationView =====
        val drawer = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navView = findViewById<NavigationView>(R.id.navigationView)

        val header = findViewById<View>(R.id.header)
        val btnBack: ImageButton = header.findViewById(R.id.btnBack)
        val imgLogo: ImageView   = header.findViewById(R.id.imgLogo)
        val btnMenu: ImageButton = header.findViewById(R.id.btnMenu)

        btnBack.setOnClickListener { finish() }
        imgLogo.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }
        btnMenu.setOnClickListener { drawer.openDrawer(GravityCompat.END) }

        updateHeader(navView)

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_hotel -> { startActivity(Intent(this, LodgingSearchActivity::class.java)); true }
                R.id.nav_board -> { startActivity(Intent(this, PostListActivity::class.java)); true }
                R.id.nav_chat  -> { startActivity(Intent(this, ChatListActivity::class.java)); true }
                R.id.nav_flight -> true
                else -> false
            }.also { drawer.closeDrawers() }
        }

        // 완료 버튼
        findViewById<MaterialButton>(R.id.btnDone).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        val container = findViewById<LinearLayout>(R.id.ticketContainer)
        container.removeAllViews()

        // ✅ 예약 객체 받기
        val booking = intent.getSerializableExtra("EXTRA_BOOKING") as? BookingResponse
        if (booking == null) {
            Toast.makeText(this, "예약 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val passengers = arrayListOf<Passenger>() // 필요시 확장
        val paxDisplay = "성인 ${booking.adult}" +
                (if (booking.child != null && booking.child!! > 0) ", 소아 ${booking.child}" else "")

//        // 가는 편
//        addTicket(
//            container, "가는 편",
//            booking.outDep ?: "출발지",
//            booking.outArr ?: "도착지",
//            booking.depDate,
//            booking.outFlNo ?: "편명없음",
//            randomGate(), randomSeat(),
//            "이코노미", paxDisplay, booking.status
//        )
//
//// 오는 편
//        if (!booking.retDate.isNullOrBlank() && booking.inFlightId != null) {
//            addTicket(
//                container, "오는 편",
//                booking.inDep ?: "도착지",
//                booking.inArr ?: "출발지",
//                booking.retDate!!,
//                booking.inFlNo ?: "편명없음",
//                randomGate(), randomSeat(),
//                "이코노미", paxDisplay, booking.status
//            )
//        }


    }

    // ----------------- 티켓 뷰 -----------------
    private fun addTicket(
        container: LinearLayout,
        badge: String,
        routeDep: String,
        routeArr: String,
        dateTime: String,
        flightNo: String,
        gate: String,
        seat: String,
        seatClass: String,
        passenger: String,
        status: String
    ) {
        val v = layoutInflater.inflate(R.layout.item_ticket, container, false)

        v.findViewById<TextView>(R.id.tvBadge).text     = badge
        v.findViewById<TextView>(R.id.tvStatus).text    = status
        v.findViewById<TextView>(R.id.tvRoute).text     = "$routeDep → $routeArr"
        v.findViewById<TextView>(R.id.tvPassenger).text = passenger
        v.findViewById<TextView>(R.id.tvDateTime).text  = dateTime
        v.findViewById<TextView>(R.id.tvFlightNo).text  = flightNo
        v.findViewById<TextView>(R.id.tvGate).text      = gate
        v.findViewById<TextView>(R.id.tvClass).text     = seatClass
        v.findViewById<TextView>(R.id.tvSeat).text      = seat

        val payload = listOf(routeDep, routeArr, flightNo, dateTime.replace(" ", "T"), seat, passenger)
            .joinToString("|")

        v.findViewById<ImageView?>(R.id.ivQr)?.setImageBitmap(makeQrCode(payload))

        container.addView(v)
    }

    private fun randomSeat(): String {
        val row = (10..45).random()
        val col = ('A'..'F').random()
        return "$row$col"
    }

    private fun randomGate(): String {
        val n = (1..40).random()
        val wing = listOf("A", "B", "C").random()
        return "${n}${wing}"
    }

    private fun makeQrCode(data: String, size: Int = 600): Bitmap {
        val hints = hashMapOf<EncodeHintType, Any>(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 1
        )
        val matrix: BitMatrix = MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, size, size, hints)
        val w = matrix.width
        val h = matrix.height
        val pixels = IntArray(w * h)
        val black = 0xFF000000.toInt()
        val white = 0xFFFFFFFF.toInt()
        var off = 0
        for (y in 0 until h) {
            for (x in 0 until w) pixels[off + x] = if (matrix[x, y]) black else white
            off += w
        }
        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, w, 0, 0, w, h)
        }
    }

    // ----------------- 로그인/헤더 -----------------
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
