package bitc.fullstack502.android_studio.ui

import StompManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import bitc.fullstack502.android_studio.BuildConfig   // ✅ 추가
import bitc.fullstack502.android_studio.R
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

class ChatRoomActivity : AppCompatActivity() {

    private val serverUrl = BuildConfig.WS_BASE   // ✅ ws://<공용서버IP>:8080/ws

    private var isFinishingByBack = false
    private lateinit var myUserId: String
    private lateinit var partnerId: String
    private lateinit var roomId: String

    private lateinit var messageAdapter: ChatMessagesAdapter
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
            runCatching { ApiProvider.api.markRead(roomId, myUserId) }
                .onFailure { Log.w("CHAT", "markRead failed: ${it.message}") }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        // intent params
        val r = intent.getStringExtra("roomId")
        val p = intent.getStringExtra("partnerId")
        if (r.isNullOrBlank() || p.isNullOrBlank()) { finish(); return }
        roomId = r
        partnerId = p

        // 로그인 사용자
        myUserId = bitc.fullstack502.android_studio.util.AuthManager.usersId()
        if (myUserId.isBlank()) { finish(); return }

        // view binding
        tvTitle = findViewById(R.id.tvTitle)
        rvChat = findViewById(R.id.rvChat)
        etMsg = findViewById(R.id.etMsg)
        btnSend = findViewById(R.id.btnSend)
        tvTitle.text = partnerId

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
                val atBottom =
                    layoutManager.findLastVisibleItemPosition() >= (messageAdapter.itemCount - 3)
                if (atBottom) debounceMarkRead()
            }
        })

        btnSend.setOnClickListener {
            val content = etMsg.text.toString().trim()
            if (content.isNotEmpty()) {
                messageAdapter.addLocalEcho(content, roomId, partnerId, myUserId)
                rvChat.scrollToPosition(messageAdapter.itemCount - 1)
                etMsg.setText("")
                StompManager.send(roomId, myUserId, partnerId, content)   // ✅ 변경
                debounceMarkRead()
            }
        }

        // ChatRoomActivity
        onBackPressedDispatcher.addCallback(this) {
            if (isFinishingByBack) return@addCallback
            isFinishingByBack = true
            lifecycleScope.launch {
                runCatching { ApiProvider.api.markRead(roomId, myUserId) }
                ForegroundRoom.current = null

                // ✅ 결과 전달
                val result = Intent().putExtra("roomId", roomId)
                setResult(Activity.RESULT_OK, result)

                finish()
            }
        }


        StompManager.subscribeRoom(roomId, { msg ->
            messageAdapter.reconcileIncoming(msg)
            rvChat.scrollToPosition(messageAdapter.itemCount - 1)
        }, { err ->
            Log.e("CHAT", "Error", err)
        })

    }


    override fun onPause() {
        super.onPause()
        lifecycleScope.launch { runCatching { ApiProvider.api.markRead(roomId, myUserId) } }
    }

    override fun onStart() {
        super.onStart()
        ForegroundRoom.current = roomId

        // 기존 connectGlobal(userId, onConnected, onError) 시그니처에 맞춤
        StompManager.connectGlobal(
            myUserId,
            onConnected = {
                // 1) 메시지 구독
                StompManager.subscribeTopic(
                    "/topic/room.$roomId",
                    { payload ->
                        val m = gson.fromJson(payload, ChatMessage::class.java)
                        runOnUiThread { onIncoming(m) }
                    },
                    { err -> Log.e("CHAT", "subscribe error", err) }
                )

                // 2) 읽음 영수증 구독
                StompManager.subscribeTopic(
                    "/topic/room.$roomId.read",
                    { payload ->
                        val rc = gson.fromJson(payload, ReadReceiptDTO::class.java)
                        runOnUiThread { messageAdapter.markReadByOtherUpTo(rc.lastReadId) }
                    },
                    { err -> Log.e("CHAT", "subscribe(read) error", err) }
                )
            },
            onError = { Log.e("CHAT", "stomp error: ${it.message}") }
        )
    }

    override fun onStop() {
        super.onStop()
        ForegroundRoom.current = null
        StompManager.unsubscribeAll()
    }

    /** 서버 수신 공통 처리 */
    private fun onIncoming(m: ChatMessage) {
        m.id?.let { if (!seenIds.add(it)) return }
        messageAdapter.reconcileIncoming(m)
        if (lastReadByOtherId > 0) {
            messageAdapter.markReadByOtherUpTo(lastReadByOtherId)
        }
        debounceMarkRead()
        val atBottom = layoutManager.findLastVisibleItemPosition() >= (messageAdapter.itemCount - 3)
        if (atBottom) rvChat.scrollToPosition(messageAdapter.itemCount - 1)
    }

    private fun loadHistoryAndMarkRead() {
        val rid = roomId
        lifecycleScope.launch {
            try {
                isLoadingMore = true
                hasMore = true

                val list = withContext(Dispatchers.IO) {
                    ApiProvider.api.markRead(rid, myUserId)                 // 입장 시 읽음 처리
                    ApiProvider.api.history(rid, 50, null, myUserId, partnerId) // 히스토리
                }.sortedBy { it.id } // ASC

                seenIds.clear()
                list.forEach { it.id?.let(seenIds::add) }
                messageAdapter.setAll(list)

                if (lastReadByOtherId > 0) {
                    messageAdapter.markReadByOtherUpTo(lastReadByOtherId)
                }

                rvChat.post {
                    rvChat.scrollToPosition((messageAdapter.itemCount - 1).coerceAtLeast(0))
                    debounceMarkRead() // ✅ 입장 직후 읽음도 보장
                }
            } catch (e: Exception) {
                Log.e("CHAT", "history/read error: ${e.message}", e)
            } finally {
                isLoadingMore = false
            }
        }
    }

    /** 과거 메시지 추가 로드 (beforeId 사용) */
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
        runCatching { StompManager.disconnect() }   // ✅ 변경
    }
}

