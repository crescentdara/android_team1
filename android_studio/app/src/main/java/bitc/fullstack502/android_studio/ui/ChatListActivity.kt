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
import bitc.fullstack502.android_studio.ChatRoomActivity
import bitc.fullstack502.android_studio.IdInputActivity
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.model.ChatMessage            // ✅ 추가
import bitc.fullstack502.android_studio.model.ConversationSummary
import bitc.fullstack502.android_studio.util.ForegroundRoom          // ✅ 추가
import bitc.fullstack502.android_studio.StompManager                 // ✅ 추가 (StompManager 위치에 맞춰 수정)
import bitc.fullstack502.android_studio.net.ApiClient
import com.google.gson.Gson                                          // ✅ 추가
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatListActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ROOM_ID = "roomId"
        const val EXTRA_PARTNER_ID = "partnerId"
    }

    private lateinit var myUserId: String
    private lateinit var rv: RecyclerView
    private lateinit var progress: ProgressBar
    private val adapter = ConversationsAdapter { openChat(it) }

    // ✅ 실시간 갱신용 필드
    private val serverUrl = "ws://10.0.2.2:8080/ws"
    private lateinit var stomp: StompManager
    private val gson = Gson()
    private val seenInbox = HashSet<String>()
    private fun keyOf(m: ChatMessage) = "${m.roomId}|${m.senderId}|${m.content}|${m.sentAt}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        // 내 ID (IdInputActivity에서 저장한 값)
        myUserId = getSharedPreferences("chat", MODE_PRIVATE)
            .getString("myId", null) ?: "android1"

        rv = findViewById(R.id.rv)
        progress = findViewById(R.id.progress)

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        // 최초 1회 로드
        loadData()
    }

    override fun onStart() {
        super.onStart()
//        // ✅ STOMP 연결 + 인박스 구독
//        stomp = StompManager(serverUrl)
//        stomp.connectGlobal(myUserId)

        // 기존처럼 첫 로드
        loadData()

        // ✅ STOMP 연결: connectGlobal에서 이미 /user/queue/inbox 구독까지 해줌
        stomp = StompManager(serverUrl)
        stomp.connectGlobal(
            userId = myUserId,
            onConnected = { /* 필요하면 연결완료 후 처리 */ },
            onMessage = { payload ->
                val msg = runCatching { gson.fromJson(payload, ChatMessage::class.java) }.getOrNull()
                msg?.let { onInboxMessage(it) }
            },
            onError = { err ->
                // 로그/토스트 등
            }
        )

        // (선택) 화면 복귀 시 최신화가 필요하면 아래 주석 해제
        // loadData()
    }

    override fun onStop() {
        // ✅ 구독/소켓 정리
        runCatching { stomp.disconnect() }
        super.onStop()
    }

    private fun loadData() {
        progress.isVisible = true
        lifecycleScope.launch {
            try {
                val list = withContext(Dispatchers.IO) {
                    ApiClient.chat.conversations(myUserId)
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

    // ✅ 실시간 수신 처리: 배지/내용/시간 갱신 + 맨 위로 이동
    private fun onInboxMessage(m: ChatMessage) {
        // 중복 방지 (방 브로드캐스트/유저 인박스 등 중복 가능성 대비)
        val k = keyOf(m)
        if (!seenInbox.add(k)) return

        // 현재 열려 있는 방이면 배지++ 안 함
        val isCurrentRoom = ForegroundRoom.current == m.roomId
        // 내가 보낸 메시지도 배지++ 안 함
        val shouldIncrementUnread = !isCurrentRoom && (m.senderId != myUserId)

        val updated = adapter.bumpAndUpdate(
            roomId = m.roomId,
            lastContent = m.content,
            lastAt = m.sentAt,
            incrementUnread = shouldIncrementUnread
        )

        // 목록에 없던 대화(첫 메시지 등)면 전체 재조회
        if (!updated) {
            loadData()
        }
    }

    private fun openChat(item: ConversationSummary) {
        val intent = Intent(this, ChatRoomActivity::class.java).apply {
            putExtra(EXTRA_ROOM_ID, item.roomId)
            putExtra(EXTRA_PARTNER_ID, item.partnerId)
            // 내 ID도 함께 전달
            putExtra(
                IdInputActivity.EXTRA_MY_ID,
                getSharedPreferences("chat", MODE_PRIVATE).getString("myId", null) ?: "android1"
            )
        }
        startActivity(intent)
    }
}
