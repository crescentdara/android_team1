package bitc.fullstack502.android_studio.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.BuildConfig           // ✅ 추가
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.model.ChatMessage
import bitc.fullstack502.android_studio.model.ConversationSummary
import bitc.fullstack502.android_studio.util.ForegroundRoom
import bitc.fullstack502.android_studio.StompManager
import bitc.fullstack502.android_studio.model.ReadReceiptDTO
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.ui.lodging.LodgingSearchActivity
import bitc.fullstack502.android_studio.ui.mypage.LoginActivity
import bitc.fullstack502.android_studio.ui.mypage.MyPageActivity
import bitc.fullstack502.android_studio.ui.post.PostListActivity
import bitc.fullstack502.android_studio.util.AuthManager
import bitc.fullstack502.android_studio.util.ChatIds
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatListActivity : AppCompatActivity() {

    private lateinit var myUsersId: String
    private lateinit var rv: RecyclerView
    private lateinit var progress: ProgressBar
    private val adapter = ConversationsAdapter { openChat(it) }

    // ✅ 공용 서버 WS 주소 사용
    private val serverUrl = BuildConfig.WS_BASE
    private lateinit var stomp: StompManager
    private val gson = Gson()

    private val seenInbox = HashSet<String>()
    private fun keyOf(m: ChatMessage) = "${m.roomId}|${m.senderId}|${m.content}|${m.sentAt}"

    // ✅ 방 토픽 구독 관리
    private val roomsToSubscribe = LinkedHashSet<String>()
    private val subscribedRooms = HashSet<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)


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

        // ✅ 로그인 유저 아이디 (하드코딩 제거)
        myUsersId = AuthManager.usersId()
        if (myUsersId.isBlank()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        rv = findViewById(R.id.rv)
        progress = findViewById(R.id.progress)

        rv.layoutManager = LinearLayoutManager(this)
        rv.setHasFixedSize(true)
        rv.itemAnimator = null
        rv.adapter = adapter

        // 최초 1회 로드
        loadData()
    }

    // ✅ registerForActivityResult 로 결과 받기
    private val openChatRoom =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val roomId = result.data?.getStringExtra("CLEARED_ROOM_ID")
                if (!roomId.isNullOrBlank()) {
                    adapter.clearUnread(roomId) // ✅ 배지 제거
                }
            }
        }


    override fun onStart() {
        super.onStart()
        if (::stomp.isInitialized) runCatching { stomp.disconnect() }
        seenInbox.clear()
        subscribedRooms.clear() // 재입장 시 다시 구독 구성

        stomp = StompManager(serverUrl)
        stomp.connectGlobal(
            userId = myUsersId,
            onConnected = {
                // 연결되면 대기 중인 방들 구독 시도
                subscribePendingRooms()
            },
            onError = { /* 필요시 로그 */ }
        )
    }

    override fun onStop() {
        runCatching { if (::stomp.isInitialized) stomp.disconnect() }
        super.onStop()
    }

    private fun loadData() {
        progress.isVisible = true
        lifecycleScope.launch {
            try {
                val list = withContext(Dispatchers.IO) {
                    ApiProvider.api.conversations(myUsersId)
                }
                adapter.submit(list)

                // ✅ 서버가 개인큐를 안 보내므로, 내 대화방들의 토픽을 구독
                roomsToSubscribe.clear()
                list.forEach { item ->
                    val rid = if (!item.roomId.isNullOrBlank()) item.roomId
                    else ChatIds.roomIdFor(myUsersId, item.partnerId)
                    roomsToSubscribe += rid
                }
                subscribePendingRooms()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ChatListActivity, "목록을 불러오지 못했어요", Toast.LENGTH_SHORT).show()
            } finally {
                progress.isVisible = false
            }
        }
    }

    /** 아직 구독 안 한 방 토픽을 구독한다 */
    private fun subscribePendingRooms() {
        if (!::stomp.isInitialized) return
        for (rid in roomsToSubscribe) {
            if (subscribedRooms.add(rid)) {
                // ✅ 메시지 구독
                stomp.subscribeTopic(
                    "/topic/room.$rid",
                    onMessage = { payload ->
                        val msg = runCatching { gson.fromJson(payload, ChatMessage::class.java) }.getOrNull()
                        msg?.let { onInboxMessage(it) }
                    }
                )

                // ✅ 읽음 이벤트 구독 추가
                stomp.subscribeTopic(
                    "/topic/room.$rid.read",
                    onMessage = { payload ->
                        val receipt = runCatching { gson.fromJson(payload, ReadReceiptDTO::class.java) }.getOrNull()
                        receipt?.let { onReadEvent(it) }
                    }
                )
            }
        }
    }


    // 방 토픽 수신: 배지/내용/시간 갱신 + 맨 위로 이동
    private fun onInboxMessage(m: ChatMessage) {
        val k = keyOf(m)
        if (!seenInbox.add(k)) return

        val isCurrentRoom = ForegroundRoom.current == m.roomId
        val shouldIncrementUnread = !isCurrentRoom && (m.senderId != myUsersId)

        val updated = adapter.bumpAndUpdate(
            roomId = m.roomId,
            lastContent = m.content,
            lastAt = m.sentAt,
            incrementUnread = shouldIncrementUnread
        )

        // 새로운 대화(처음 보는 roomId)면 다음 로드에서 반영
        if (!updated) loadData()
    }

    private fun openChat(item: ConversationSummary) {
        val roomId = if (!item.roomId.isNullOrBlank()) item.roomId
        else ChatIds.roomIdFor(myUsersId, item.partnerId)

        val intent = Intent(this, ChatRoomActivity::class.java).apply {
            putExtra("roomId", roomId)
            putExtra("partnerId", item.partnerId)
        }
        openChatRoom.launch(intent)   // ✅ 여기서 launch
    }

    override fun onResume() {
        super.onResume()
        loadData()   // 복귀할 때 최신 unread/미리보기 반영
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

    private fun onReadEvent(receipt: ReadReceiptDTO) {
        // 내가 아닌 상대방이 읽었을 때만 배지 제거
        if (receipt.userId != myUsersId) {
            adapter.clearUnread(receipt.roomId)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            val clearedRoomId = data?.getStringExtra("CLEARED_ROOM_ID") ?: return
            adapter.clearUnread(clearedRoomId)  // ✅ 안읽음 배지 제거
        }
    }

}
