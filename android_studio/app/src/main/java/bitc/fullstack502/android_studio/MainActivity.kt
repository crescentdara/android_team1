package bitc.fullstack502.android_studio

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import bitc.fullstack502.android_studio.model.ChatMessage
import bitc.fullstack502.android_studio.net.ApiClient
import bitc.fullstack502.android_studio.ui.ChatListActivity
import bitc.fullstack502.android_studio.ui.ChatMessagesAdapter
import com.google.gson.Gson

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val serverUrl = "ws://10.0.2.2:8080/ws"
    private val myUserId = "android1"
    private val messageAdapter = ChatMessagesAdapter(myUserId)

    private val roomId: String by lazy {
        intent.getStringExtra(ChatListActivity.EXTRA_ROOM_ID) ?: "testroom"
    }
    private val partnerId: String by lazy {
        intent.getStringExtra(ChatListActivity.EXTRA_PARTNER_ID) ?: "android2"
    }

    private lateinit var stomp: StompManager
    private lateinit var rvChat: RecyclerView
    private lateinit var tvTitle: TextView
    private lateinit var etMsg: EditText
    private lateinit var btnSend: Button

//    private val adapter = ChatMessagesAdapter(myUserId)
    private val gson = Gson()

    // 중복 방지 (inbox/room 두 군데에서 올 수 있으므로)
    private val seen = HashSet<String>()
//    private fun keyOf(m: ChatMessage) = "${m.roomId}|${m.senderId}|${m.content}|${m.sentAt}"

    private fun keyOf(m: ChatMessage) =
        "${m.roomId}|${m.senderId}|${m.sentAt}|${m.content.hashCode()}"

    // 메시지를 리스트에 추가하고 스크롤 맨 아래로
    private fun addMessage(m: ChatMessage) {
        val key = keyOf(m)
        if (seen.add(key)) {
            // ↓ 어댑터 변수명이 messageAdapter라면 거기로 바꿔주세요
            messageAdapter.addOne(m)
            rvChat.scrollToPosition(messageAdapter.itemCount - 1)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTitle = findViewById(R.id.tvTitle)
        rvChat  = findViewById(R.id.rvChat)
        etMsg   = findViewById(R.id.etMsg)
        btnSend = findViewById(R.id.btnSend)

        tvTitle.text = partnerId

        Log.d("CHAT", "🔎 roomId from intent = $roomId, partner=$partnerId")


        rvChat.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rvChat.adapter = messageAdapter                    // ✅ 메시지 어댑터가 붙었는지 확인!

        stomp = StompManager(serverUrl)
        stomp.connectGlobal(
            userId = myUserId,
            onMessage = { payload ->
                val m = runCatching { gson.fromJson(payload, ChatMessage::class.java) }.getOrNull()
                if (m != null && m.roomId == roomId) addMessage(m)
            },
            onError = { err -> Log.e("CHAT", "INBOX err: $err") }
        )

        loadHistoryAndMarkRead() // onCreate에서 1회 호출(중복 호출 OK)

        btnSend.setOnClickListener {
            val content = etMsg.text.toString()
            if (content.isNotBlank()) {
                stomp.send(roomId, myUserId, partnerId, content)
                etMsg.setText("")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("CHAT", "🔁 onStart roomId=$roomId, partner=$partnerId")
        loadHistoryAndMarkRead()
    }

    private fun loadHistoryAndMarkRead() {
        val rid = roomId
        Log.d("CHAT", "🔎 loadHistory roomId=$rid")

        lifecycleScope.launch {
            try {
                val list = withContext(Dispatchers.IO) {
                    ApiClient.chat.markRead(rid, myUserId)      // 읽음 처리
                    ApiClient.chat.history(rid, 50)             // 히스토리 REST


                }.sortedBy {
                    runCatching { java.time.Instant.parse(it.sentAt) }
                        .getOrDefault(java.time.Instant.EPOCH)
                }

                Log.d("CHAT", "📜 history loaded: ${list.size} items for rid=$rid")

                seen.clear(); list.forEach { seen.add(keyOf(it)) }

                messageAdapter.setAll(list)                     // ✅ 메시지 어댑터
                rvChat.post {
                    rvChat.scrollToPosition(
                        (messageAdapter.itemCount - 1).coerceAtLeast(0)
                    )
                }
            } catch (e: Exception) {
                Log.e("CHAT", "❌ history/read error: ${e.message}", e)
            }
        }
    }
}