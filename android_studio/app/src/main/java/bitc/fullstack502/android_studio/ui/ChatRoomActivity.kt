package bitc.fullstack502.android_studio.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import bitc.fullstack502.android_studio.BuildConfig
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.StompManager
import bitc.fullstack502.android_studio.model.ChatMessage
import bitc.fullstack502.android_studio.model.ReadReceiptDTO
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.util.ForegroundRoom
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.addCallback
import bitc.fullstack502.android_studio.model.ConversationSummary
import bitc.fullstack502.android_studio.util.ChatIds
import org.json.JSONObject

class ChatRoomActivity : AppCompatActivity() {

    private val serverUrl = BuildConfig.WS_BASE

    private var isFinishingByBack = false

    private lateinit var myUserId: String
    private lateinit var partnerId: String
    private lateinit var roomId: String

    private lateinit var messageAdapter: ChatMessagesAdapter
    private lateinit var stomp: StompManager
    private lateinit var rvChat: RecyclerView
    private lateinit var tvTitle: TextView
    private lateinit var etMsg: EditText
    private lateinit var btnSend: Button

    private var isLoadingMore = false
    private var hasMore = true
    private lateinit var layoutManager: LinearLayoutManager

    private var isActive = false
    private val gson = Gson()

    private val seenIds = HashSet<Long>()
    private var lastReadByOtherId: Long = 0L

    private var readJob: Job? = null
    private fun debounceMarkRead() {
        readJob?.cancel()
        readJob = lifecycleScope.launch {
            delay(300)
            // ✅ 내가 보낸 메시지는 제외하고 마지막 상대 메시지 ID 찾기
            val lastId = messageAdapter.getLastVisibleOtherId(myUserId) ?: return@launch
            runCatching {
                ApiProvider.api.markRead(roomId, myUserId, lastId)
            }.onSuccess {
                Log.d("CHAT", "읽음 처리 성공: $lastId")
            }.onFailure {
                Log.e("CHAT", "읽음 처리 실패", it)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        val r = intent.getStringExtra("roomId")
        val p = intent.getStringExtra("partnerId")
        if (r.isNullOrBlank() || p.isNullOrBlank()) {
            finish(); return
        }
        roomId = r
        partnerId = p

        myUserId = bitc.fullstack502.android_studio.util.AuthManager.usersId()
        if (myUserId.isBlank()) {
            finish(); return
        }

        // ✅ 여기서 stomp 초기화 필수
        stomp = StompManager(serverUrl)
        stomp.connectGlobal(myUserId)

        tvTitle = findViewById(R.id.tvTitle)
        rvChat = findViewById(R.id.rvChat)
        etMsg = findViewById(R.id.etMsg)
        btnSend = findViewById(R.id.btnSend)
        tvTitle.text = partnerId

        Log.d("CHAT", "room=$roomId partner=$partnerId me=$myUserId serverUrl=$serverUrl")

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        messageAdapter = ChatMessagesAdapter(myUserId)
        layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
            reverseLayout = false
        }
        rvChat.layoutManager = layoutManager
        rvChat.adapter = messageAdapter
        (rvChat.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

        rvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)

                val firstVisible = layoutManager.findFirstVisibleItemPosition()
                val reachedTop = !rv.canScrollVertically(-1) || firstVisible <= 1
                if (reachedTop && hasMore && !isLoadingMore && messageAdapter.itemCount > 0) {
                    loadOlder()
                }

                // ✅ 마지막 메시지가 화면에 완전히 보여야만 읽음 처리
                val lastVisible = layoutManager.findLastCompletelyVisibleItemPosition()
                val lastIndex = messageAdapter.itemCount - 1
                if (lastVisible == lastIndex && ForegroundRoom.current == roomId) {
                    debounceMarkRead()
                }
            }
        })


        btnSend.setOnClickListener {
            val content = etMsg.text.toString().trim()
            if (content.isNotEmpty()) {
                // ✅ myUserId는 어댑터 내부에서 이미 처리하므로 넘기지 않음
                messageAdapter.addLocalEcho(content, roomId, partnerId)
                rvChat.scrollToPosition(messageAdapter.itemCount - 1)
                etMsg.setText("")
                stomp.send(roomId, myUserId, partnerId, content) // 이건 그대로
            }
        }

        // ✅ STOMP 구독
        stomp.subscribeRoom(
            roomId,
            onMessage = { payload ->
                Log.d("CHAT", "msg=$payload")
                // TODO: payload -> ChatMessage 파싱 후 messageAdapter.addServerMessage() 호출
            },
            onError = { err -> Log.e("CHAT", "room sub error: $err") }
        )

        stomp.subscribeRoom(
            roomId,
            onMessage = { payload ->
                Log.d("CHAT", "msg=$payload")
                // TODO: payload 파싱해서 adapter.addServerMessage()
            },
            onError = { err -> Log.e("CHAT", "room sub error: $err") }
        )

        onBackPressedDispatcher.addCallback(this) {
            if (isFinishingByBack) return@addCallback
            isFinishingByBack = true

            lifecycleScope.launch {
                val lastId = messageAdapter.getLastIdOrNull() ?: 0L
                runCatching { ApiProvider.api.markRead(roomId, myUserId, lastId) }
                ForegroundRoom.current = null
                finish()
            }
        }
    }



    override fun onPause() {
        super.onPause()
        // ❌ 자동 읽음 처리 제거
    }

    override fun onStart() {
        super.onStart()
        isActive = true
        ForegroundRoom.current = roomId

        if (!::stomp.isInitialized) {
            stomp = StompManager(serverUrl)
        }
        connectStomp()

        if (messageAdapter.itemCount == 0) {
            loadHistoryAndMarkRead()
        } else {
            // ✅ 방에 들어오면 무조건 마지막 메시지까지 읽음 처리
            val lastId = messageAdapter.getLastIdOrNull()
            if (lastId != null) {
                lifecycleScope.launch {
                    runCatching { ApiProvider.api.markRead(roomId, myUserId, lastId) }
                        .onSuccess { Log.d("CHAT", "입장 시 읽음 처리: $lastId") }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        isActive = false
        ForegroundRoom.current = null
        runCatching { stomp.disconnect() }
    }

    private fun connectStomp() {
        stomp.connectGlobal(
            userId = myUserId,
            onConnected = {
                stomp.subscribeTopic(
                    "/topic/room.$roomId",
                    onMessage = { payload ->
                        val m = runCatching {
                            gson.fromJson(payload, ChatMessage::class.java)
                        }.getOrNull()
                        if (m != null && m.roomId == roomId) {
                            runOnUiThread { onIncoming(m) }
                        }
                    },
                    onError = { err -> Log.e("CHAT", "room topic err: $err") }
                )

                stomp.subscribeTopic(
                    "/topic/room.$roomId.read",
                    onMessage = { payload ->
                        val rc = runCatching {
                            gson.fromJson(payload, ReadReceiptDTO::class.java)
                        }.getOrNull()
                        if (rc != null && rc.roomId == roomId && rc.userId != myUserId) {
                            // ✅ lastReadId가 같아도 갱신하도록 수정
                            if (rc.lastReadId >= lastReadByOtherId) {
                                lastReadByOtherId = rc.lastReadId
                                runOnUiThread { messageAdapter.markReadByOtherUpTo(lastReadByOtherId) }
                            }
                        }
                    },
                    onError = { err -> Log.e("CHAT", "read-receipt topic err: $err") }
                )
            },
            onError = { err ->
                Log.e("CHAT", "STOMP err: $err")
                lifecycleScope.launch {
                    delay(1500)
                    if (isActive) connectStomp()
                }
            }
        )
    }

    private fun onIncoming(m: ChatMessage) {
        m.id?.let { if (!seenIds.add(it)) return }
        messageAdapter.reconcileIncoming(m)

        if (lastReadByOtherId > 0) {
            messageAdapter.markReadByOtherUpTo(lastReadByOtherId)
        }

        if (m.senderId != myUserId) {
            // ✅ 내가 방 보고 있고 + 스크롤이 정확히 바닥일 때만 읽음 처리
            val lastVisible = layoutManager.findLastCompletelyVisibleItemPosition()
            val lastIndex = messageAdapter.itemCount - 1
            if (ForegroundRoom.current == roomId && lastVisible == lastIndex) {
                debounceMarkRead()
            }
        }

        val lastVisible = layoutManager.findLastVisibleItemPosition()
        val lastIndex = messageAdapter.itemCount - 1
        if (lastVisible >= lastIndex - 1) {
            rvChat.scrollToPosition(lastIndex)
        }
    }


    private fun loadHistoryAndMarkRead() {
        val rid = roomId
        lifecycleScope.launch {
            try {
                isLoadingMore = true
                hasMore = true

                // ✅ 서버에서 최근 50개 불러오기
                val list = withContext(Dispatchers.IO) {
                    ApiProvider.api.history(rid, 50, null, myUserId, partnerId)
                }.sortedBy { it.id }

                // ✅ 메시지 세팅
                seenIds.clear()
                list.forEach { it.id?.let(seenIds::add) }
                messageAdapter.setAll(list)

                // ✅ 상대방 읽음 처리 반영
                if (lastReadByOtherId > 0) {
                    messageAdapter.markReadByOtherUpTo(lastReadByOtherId)
                }

                rvChat.post {
                    // ✅ 마지막 메시지 위치로 스크롤
                    rvChat.scrollToPosition((messageAdapter.itemCount - 1).coerceAtLeast(0))

                    val lastId = messageAdapter.getLastIdOrNull()
                    if (lastId != null) {
                        lifecycleScope.launch {
                            runCatching {
                                ApiProvider.api.markRead(rid, myUserId, lastId)
                            }.onSuccess {
                                Log.d("CHAT", "입장 시 읽음 처리 성공: $lastId")

                                // ✅ 여기서 바로 어댑터에 반영해야 함
                                messageAdapter.markReadByOtherUpTo(lastId)   // ✅ 바로 UI 반영

                            }.onFailure { e ->
                                Log.e("CHAT", "입장 시 읽음 처리 실패: ${e.message}", e)
                            }
                        }
                    } else {
                        Log.w("CHAT", "입장 시 읽음 처리 건너뜀 (lastId=null)")
                    }
                }

            } catch (e: Exception) {
                Log.e("CHAT", "history/read error: ${e.message}", e)
            } finally {
                isLoadingMore = false
            }
        }
}

    private fun loadOlder() {
        val beforeId = messageAdapter.getFirstIdOrNull() ?: return
        isLoadingMore = true

        val firstIndex = layoutManager.findFirstVisibleItemPosition()
        val firstTop = layoutManager.findViewByPosition(firstIndex)?.top ?: 0

        lifecycleScope.launch {
            try {
                val older = withContext(Dispatchers.IO) {
                    ApiProvider.api.history(roomId, 50, beforeId, myUserId, partnerId)
                }.sortedBy { it.id }

                if (older.isEmpty()) {
                    hasMore = false
                } else {
                    val filtered = older.filter { it.id == null || !seenIds.contains(it.id!!) }
                    filtered.forEach { it.id?.let(seenIds::add) }

                    if (filtered.isNotEmpty()) {
                        messageAdapter.prependMany(filtered)
                        rvChat.post {
                            layoutManager.scrollToPositionWithOffset(
                                firstIndex + filtered.size,
                                firstTop
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CHAT", "older history error: ${e.message}", e)
            } finally {
                isLoadingMore = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { stomp.disconnect() }
    }

    override fun finish() {
        val intent = Intent().apply {
            putExtra("CLEARED_ROOM_ID", roomId) // 현재 채팅방 ID
        }
        setResult(RESULT_OK, intent)
        super.finish()
    }



}
