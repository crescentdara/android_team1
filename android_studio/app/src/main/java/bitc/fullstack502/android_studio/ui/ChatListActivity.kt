package bitc.fullstack502.android_studio.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.BuildConfig           // ✅ 추가
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.model.ChatMessage
import bitc.fullstack502.android_studio.model.ConversationSummary
import bitc.fullstack502.android_studio.util.ForegroundRoom
import bitc.fullstack502.android_studio.StompManager
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.util.AuthManager
import bitc.fullstack502.android_studio.util.ChatIds
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
                stomp.subscribeTopic(
                    "/topic/room.$rid",
                    onMessage = { payload ->
                        val msg = runCatching { gson.fromJson(payload, ChatMessage::class.java) }.getOrNull()
                        msg?.let { onInboxMessage(it) }
                    },
                    onError = { /* 필요시 로그 */ }
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
        // ✅ roomId가 비어 있을 수도 있으니 안전하게 생성
        val roomId = if (!item.roomId.isNullOrBlank()) item.roomId
        else ChatIds.roomIdFor(myUsersId, item.partnerId)

        startActivity(Intent(this, ChatRoomActivity::class.java).apply {
            putExtra("roomId", roomId)
            putExtra("partnerId", item.partnerId)
        })
    }

    override fun onResume() {
        super.onResume()
        loadData()   // 복귀할 때 최신 unread/미리보기 반영
    }

}
