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
import androidx.lifecycle.lifecycleScope
import bitc.fullstack502.android_studio.model.BookingResponse
import bitc.fullstack502.android_studio.model.Flight
import com.google.android.material.button.MaterialButton
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import bitc.fullstack502.android_studio.model.Passenger   //
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.ui.MainActivity
import bitc.fullstack502.android_studio.ui.ChatListActivity
import bitc.fullstack502.android_studio.ui.lodging.LodgingSearchActivity
import bitc.fullstack502.android_studio.ui.mypage.LoginActivity
import bitc.fullstack502.android_studio.ui.mypage.MyPageActivity
import bitc.fullstack502.android_studio.ui.post.PostListActivity
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class TicketSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_success)

        val container = findViewById<LinearLayout>(R.id.ticketContainer)
        container.removeAllViews()

        val bookingId = intent.getLongExtra("EXTRA_BOOKING_ID", -1L)
        if (bookingId <= 0L) {
            Toast.makeText(this, "예약 번호가 없습니다.", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        lifecycleScope.launch {
            try {
                // 예약 상세
                val b: BookingResponse = ApiProvider.api.getFlightBooking(bookingId)

// 항공편 조회(가는 편)
                val out: Flight = ApiProvider.api.getFlight(b.outFlightId)   // ✅ 수정

// 왕복이면 오는 편 조회
                val isRound = (b.inFlightId != null && b.retDate != null)    // ✅ 수정
                val `in` = if (isRound) ApiProvider.api.getFlight(b.inFlightId!!) else null


                // 4) 표시용 문자열
                val passengerTitle = "승객 ${b.seatCnt}명 (성인 ${b.adult}" +
                        (if ((b.child ?: 0) > 0) ", 소아 ${b.child}" else "") + ")"
                val status = if (b.status.equals("PAID", true)) "결제 완료" else b.status

                // 5) 가는 편 티켓
                addTicket(
                    container = container,
                    badge = "가는 편",
                    routeDep = out.dep,
                    routeArr = out.arr,
                    dateTime = "${b.depDate} ${out.depTime}",   // 예: 2025-08-28 09:05
                    flightNo = out.flNo,
                    gate = randomGate(),
                    seat = randomSeat(),
                    seatClass = "이코노미",
                    passenger = passengerTitle,
                    status = status
                )

                // 6) 오는 편(왕복일 때만)
                if (isRound && `in` != null) {
                    addTicket(
                        container = container,
                        badge = "오는 편",
                        routeDep = `in`.dep,
                        routeArr = `in`.arr,
                        dateTime = "${b.retDate} ${`in`.depTime}",
                        flightNo = `in`.flNo,
                        gate = randomGate(),
                        seat = randomSeat(),
                        seatClass = "이코노미",
                        passenger = passengerTitle,
                        status = status
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@TicketSuccessActivity, "예약 내역을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
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
                    // 현재 FlightReservationActivity 니까 따로 이동 안 해도 됨
                    true
                }
                else -> false
            }.also { drawer.closeDrawers() }
        }

        //////////////////////////////////////////////////////////////////////////////////////

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnDone).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // 스택에 TicketSuccessActivity 포함된 이전 화면들 다 날리고 메인으로
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        container.removeAllViews()

        val isRoundTrip = intent.getBooleanExtra("EXTRA_ROUNDTRIP", false)

        // 가는 편 공통 데이터
        val dep      = intent.getStringExtra("EXTRA_DEP") ?: "출발지 미정"
        val arr      = intent.getStringExtra("EXTRA_ARR") ?: "도착지 미정"
        val dt       = intent.getStringExtra("EXTRA_DATETIME") ?: "시간 정보 없음"
        val flightNo = intent.getStringExtra("EXTRA_FLIGHT_NO") ?: "편명 없음"
        val seatCls  = intent.getStringExtra("EXTRA_CLASS") ?: "이코노미"

        // 오는 편
        val depR      = intent.getStringExtra("EXTRA_DEP_RETURN") ?: ""
        val arrR      = intent.getStringExtra("EXTRA_ARR_RETURN") ?: ""
        val dtR       = intent.getStringExtra("EXTRA_DATETIME_RETURN") ?: ""
        val flightNoR = intent.getStringExtra("EXTRA_FLIGHT_NO_RETURN") ?: ""
        val seatClsR  = intent.getStringExtra("EXTRA_CLASS_RETURN") ?: "이코노미"

        @Suppress("UNCHECKED_CAST")
        val passengers = intent.getSerializableExtra("PASSENGERS") as? ArrayList<Passenger> ?: arrayListOf()

        val paxNameFallback  = intent.getStringExtra("EXTRA_PASSENGER")
        val paxCountFallback = intent.getIntExtra("EXTRA_PAX_COUNT", if (passengers.isEmpty()) 1 else passengers.size)

        if (passengers.isEmpty()) {
            // 단일 이름만 받은 경우
            val display = if (paxCountFallback > 1 && !paxNameFallback.isNullOrBlank())
                "$paxNameFallback 외 ${paxCountFallback - 1}명" else (paxNameFallback ?: "승객")
            // 가는 편
            addTicket(container, "가는 편", dep, arr, dt, flightNo,
                randomGate(), randomSeat(), seatCls, display, "결제 완료")
            // 오는 편
            if (isRoundTrip) {
                addTicket(container, "오는 편", depR, arrR, dtR, flightNoR,
                    randomGate(), randomSeat(), seatClsR, display, "결제 완료")
            }
        } else {
            // 다인원: 승객별
            passengers.forEach { p ->
                val name = p.displayName().ifBlank { "승객 ${p.index + 1}" }
                // 가는 편
                addTicket(container, "가는 편", dep, arr, dt, flightNo,
                    randomGate(), randomSeat(), seatCls, name, "결제 완료")
                // 오는 편
                if (isRoundTrip) {
                    addTicket(container, "오는 편", depR, arrR, dtR, flightNoR,
                        randomGate(), randomSeat(), seatClsR, name, "결제 완료")
                }
            }
        }
    }

    // 위치 인자 기준 시그니처
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
        v.findViewById<TextView>(R.id.tvRoute).text     = "$routeDep  →  $routeArr"
        v.findViewById<TextView>(R.id.tvPassenger).text = passenger
        v.findViewById<TextView>(R.id.tvDateTime).text  = dateTime
        v.findViewById<TextView>(R.id.tvFlightNo).text  = flightNo
        v.findViewById<TextView>(R.id.tvGate).text      = gate
        v.findViewById<TextView>(R.id.tvClass).text     = seatClass
        v.findViewById<TextView>(R.id.tvSeat).text      = seat

        // QR payload
        val payload = listOf(
            routeDep, routeArr, flightNo, dateTime.replace(" ", "T"), seat, passenger
        ).joinToString("|")

        v.findViewById<ImageView?>(R.id.ivQr)?.apply {
            setImageBitmap(makeQrCode(payload))
        }

        container.addView(v)
    }

    private fun randomSeat(): String {
        val row = (10..45).random()
        val col = ('A'..'F').random()
        return "$row$col"
    }
    private fun randomGate(): String {
        val n = (1..40).random()
        val wing = listOf("A","B","C").random()
        return "${n}${wing}"
    }

    // ZXing 사용 (build.gradle에 의존성 필요)
    private fun makeQrCode(data: String, size: Int = 600): Bitmap {
        val hints = hashMapOf<_root_ide_package_.com.google.zxing.EncodeHintType, Any>(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 1
        )
        val matrix: BitMatrix =
            MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, size, size, hints)
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
