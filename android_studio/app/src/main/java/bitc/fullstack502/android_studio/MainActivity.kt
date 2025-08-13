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
    private val adapter = ChatMessagesAdapter(myUserId)
    private val gson = Gson()

    // 중복 방지 (inbox/room 두 군데에서 올 수 있으므로)
    private val seen = HashSet<String>()
    private fun keyOf(m: ChatMessage) = "${m.roomId}|${m.senderId}|${m.content}|${m.sentAt}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTitle = findViewById(R.id.tvTitle)
        rvChat   = findViewById(R.id.rvChat)
        etMsg    = findViewById(R.id.etMsg)
        btnSend  = findViewById(R.id.btnSend)

        tvTitle.text = partnerId

        rvChat.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rvChat.adapter = adapter

        stomp = StompManager(serverUrl)

        // 전역 인박스 연결
        stomp.connectGlobal(
            userId = myUserId,
            onMessage = { payload ->
                val m = runCatching { gson.fromJson(payload, ChatMessage::class.java) }.getOrNull()
                if (m != null && m.roomId == roomId) addMessage(m)
            },
            onError = { /* 로그 필요시 추가 */ }
        )

        // 히스토리 로드 + 읽음 처리
        loadHistoryAndMarkRead()

        // 전송
        btnSend.setOnClickListener {
            val content = etMsg.text.toString()
            if (content.isNotBlank()) {
                stomp.send(roomId, myUserId, partnerId, content)
                etMsg.setText("")
            }
        }
    }

    private fun addMessage(m: ChatMessage) {
        val key = keyOf(m)
        if (seen.add(key)) {
            adapter.addOne(m)
            rvChat.scrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun loadHistoryAndMarkRead() {
        lifecycleScope.launch {
            try {
                ApiClient.chat.markRead(roomId, myUserId)
                val list = withContext(Dispatchers.IO) {
                    ApiClient.chat.history(roomId, 50)
                }.sortedBy { it.sentAt } // 오래된→최신
                seen.clear()
                list.forEach { seen.add(keyOf(it)) }
                adapter.setAll(list)
                rvChat.scrollToPosition(adapter.itemCount - 1)
            } catch (_: Exception) {
                // 필요시 토스트/로그
            }
        }
    }
}