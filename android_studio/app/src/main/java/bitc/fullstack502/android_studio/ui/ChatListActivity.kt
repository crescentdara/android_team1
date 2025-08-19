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

    // 실시간 갱신
    private val serverUrl = "ws://10.0.2.2:8080/ws"
    private lateinit var stomp: StompManager
    private val gson = Gson()
    private val seenInbox = HashSet<String>()
    private fun keyOf(m: ChatMessage) = "${m.roomId}|${m.senderId}|${m.content}|${m.sentAt}"

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

        stomp = StompManager(serverUrl)

        // ✅ 전역 연결만 수행 (onMessage 인자 제거)
        stomp.connectGlobal(
            userId = myUsersId,
            onConnected = {
                // 연결되면 인박스 구독 시작
                stomp.subscribeUserQueue(
                    name = "inbox",
                    onMessage = { payload ->
                        val msg = runCatching { gson.fromJson(payload, ChatMessage::class.java) }.getOrNull()
                        msg?.let { onInboxMessage(it) }
                    },
                    onError = { /* 필요시 로그 */ }
                )
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
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ChatListActivity, "목록을 불러오지 못했어요", Toast.LENGTH_SHORT).show()
            } finally {
                progress.isVisible = false
            }
        }
    }

    // 인박스 수신 처리: 배지/내용/시간 갱신 + 맨 위로 이동
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

        // 새로운 대화면 전체 재조회
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
}
