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
import bitc.fullstack502.android_studio.ui.ChatRoomActivity
import bitc.fullstack502.android_studio.IdInputActivity
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.model.ChatMessage
import bitc.fullstack502.android_studio.model.ConversationSummary
import bitc.fullstack502.android_studio.util.ForegroundRoom
import bitc.fullstack502.android_studio.StompManager
import bitc.fullstack502.android_studio.network.ApiProvider
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatListActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ROOM_ID = "roomId"
        const val EXTRA_PARTNER_ID = "partnerId"
    }

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

        myUsersId = getSharedPreferences("chat", MODE_PRIVATE)
            .getString("myId", null) ?: "android1"

        rv = findViewById(R.id.rv)
        progress = findViewById(R.id.progress)

        rv.layoutManager = LinearLayoutManager(this)
        rv.setHasFixedSize(true)
        rv.itemAnimator = null
        rv.adapter = adapter

        // 최초 1회 로드만 여기서
        loadData()
    }

    override fun onStart() {
        super.onStart()
        // STOMP 연결 (중복 방지 위해 기존 연결 정리)
        if (::stomp.isInitialized) runCatching { stomp.disconnect() }
        seenInbox.clear()

        stomp = StompManager(serverUrl)

        // NOTE: 너희 StompManager가 콜백 인자를 받는 오버로드를 지원한다면 아래처럼 사용.
        // 그렇지 않고 connectGlobal(userId: String) 한 가지만 있다면, 그 구현에서
        // /user/queue/inbox 구독 후 전달해주는 리스너를 설정하는 메서드를 사용해도 됨.
        stomp.connectGlobal(
            userId = myUsersId,
            onConnected = { /* 필요시 UI 처리 */ },
            onMessage = { payload ->
                val msg = runCatching { gson.fromJson(payload, ChatMessage::class.java) }.getOrNull()
                msg?.let { onInboxMessage(it) }
            },
            onError = { /* 로그/토스트 등 원하면 처리 */ }
        )
    }

    override fun onStop() {
        // 소켓 해제
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
        val intent = Intent(this, ChatRoomActivity::class.java).apply {
            putExtra(EXTRA_ROOM_ID, item.roomId)
            putExtra(EXTRA_PARTNER_ID, item.partnerId)
            putExtra(
                IdInputActivity.EXTRA_MY_ID,
                getSharedPreferences("chat", MODE_PRIVATE).getString("myId", null) ?: "android1"
            )
        }
        startActivity(intent)
    }
}
