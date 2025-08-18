package bitc.fullstack502.android_studio.ui

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
import bitc.fullstack502.android_studio.IdInputActivity
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatRoomActivity : AppCompatActivity() {

    private val serverUrl = "ws://10.0.2.2:8080/ws"

    private lateinit var myUserId: String
    private lateinit var partnerId: String
    private lateinit var roomId: String

    private lateinit var messageAdapter: ChatMessagesAdapter
    private lateinit var stomp: StompManager
    private lateinit var rvChat: RecyclerView
    private lateinit var tvTitle: TextView
    private lateinit var etMsg: EditText
    private lateinit var btnSend: Button

    // 무한 스크롤 상태
    private var isLoadingMore = false
    private var hasMore = true
    private lateinit var layoutManager: LinearLayoutManager

    // 생명주기 가드 (중지 상태에선 재연결 X)
    private var isActive = false

    private val gson = Gson()

    // ✅ 중복 수신 차단용
    private val seenIds = HashSet<Long>()

    // 읽음 영수증 최신값 저장 (상대가 읽은 마지막 메시지 id)
    private var lastReadByOtherId: Long = 0L


    // ✅ 읽음 처리 디바운스
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

        // 1) 인텐트 파라미터 수신 (IdInputActivity / ChatListActivity 양쪽 호환)
        myUserId = intent.getStringExtra(IdInputActivity.Companion.EXTRA_MY_ID) ?: "android1"
        partnerId = intent.getStringExtra(IdInputActivity.Companion.EXTRA_PARTNER_ID)
            ?: intent.getStringExtra(ChatListActivity.EXTRA_PARTNER_ID)
                    ?: "android2"
        roomId = intent.getStringExtra(IdInputActivity.Companion.EXTRA_ROOM_ID)
            ?: intent.getStringExtra(ChatListActivity.EXTRA_ROOM_ID)
                    ?: "testroom"

        // 2) 뷰 바인딩
        tvTitle = findViewById(R.id.tvTitle)
        rvChat  = findViewById(R.id.rvChat)
        etMsg   = findViewById(R.id.etMsg)
        btnSend = findViewById(R.id.btnSend)
        tvTitle.text = partnerId

        Log.d("CHAT", "room=$roomId partner=$partnerId me=$myUserId")

        // 3) 리스트 + 레이아웃 매니저
        messageAdapter = ChatMessagesAdapter(myUserId)
        layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
            reverseLayout = false // 절대 true 금지 (순서 꼬임 방지)
        }
        rvChat.layoutManager = layoutManager
        rvChat.adapter = messageAdapter

        // 🔥 변경 애니메이션으로 인한 고스트/점프 방지
        (rvChat.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

        // 3-1) 위로 스크롤 시 과거 더 불러오기 + 바닥 근처면 읽음 갱신
        rvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                val firstVisible = layoutManager.findFirstVisibleItemPosition()
                val reachedTop = !rv.canScrollVertically(-1) || firstVisible <= 1
                if (reachedTop && hasMore && !isLoadingMore && messageAdapter.itemCount > 0) {
                    loadOlder()
                }
                val atBottom = layoutManager.findLastVisibleItemPosition() >= (messageAdapter.itemCount - 3)
                if (atBottom) debounceMarkRead()
            }
        })

        // 4) 전송: 로컬 에코 → 실제 전송 → 읽음 포인터 갱신
        btnSend.setOnClickListener {
            val content = etMsg.text.toString().trim()
            if (content.isNotEmpty()) {
                messageAdapter.addLocalEcho(content, roomId, partnerId, myUserId) // 임시 음수 id
                rvChat.scrollToPosition(messageAdapter.itemCount - 1)
                etMsg.setText("")
                stomp.send(roomId, myUserId, partnerId, content)                  // 실제 전송
                debounceMarkRead()                                                // 내가 보고 있으니 읽음 갱신
            }
        }
    }

    override fun onStart() {
        super.onStart()
        isActive = true

        // ✅ 현재 보고 있는 방 기록(리스트 배지 증가 방지용)
        ForegroundRoom.current = roomId

        if (!::stomp.isInitialized) {
            stomp = StompManager(serverUrl)
        }
        connectStomp()

        if (messageAdapter.itemCount == 0) {
            loadHistoryAndMarkRead()
        } else {
            debounceMarkRead()
        }
    }

    override fun onStop() {
        super.onStop()
        isActive = false

        // ✅ 방에서 벗어나면 해제
        ForegroundRoom.current = null

        runCatching { stomp.disconnect() }
    }


    // STOMP 연결: 방 토픽 + 개인 인박스 + 읽음 영수증
    private fun connectStomp() {
        stomp.connectGlobal(
            userId = myUserId,
            onConnected = {
                // 1) 방 토픽 구독
                stomp.subscribeTopic(
                    "/topic/room.$roomId",
                    onMessage = { payload ->
                        val m = runCatching { gson.fromJson(payload, ChatMessage::class.java) }.getOrNull()
                        if (m != null && m.roomId == roomId) {
                            runOnUiThread { onIncoming(m) }
                        }
                    },
                    onError = { err -> Log.e("CHAT", "room topic err: $err") }
                )

                // 2) 읽음 영수증 구독
                stomp.subscribeTopic(
                    "/user/queue/read-receipt",
                    onMessage = { payload ->
                        val rc = runCatching { gson.fromJson(payload, ReadReceiptDTO::class.java) }.getOrNull()
                        if (rc != null && rc.roomId == roomId) {
                            // 내가 읽은 영수증은 무시, 상대가 읽은 것만 반영
                            if (rc.readerId != myUserId) {
                                // 최신값 저장 (경쟁조건 대비, 큰 값 유지)
                                if (rc.lastReadId > lastReadByOtherId) lastReadByOtherId = rc.lastReadId
                                runOnUiThread {
                                    messageAdapter.markReadByOtherUpTo(lastReadByOtherId)
                                }
                            }
                        }
                    },
                    onError = { err -> Log.e("CHAT", "read-receipt err: $err") }
                )
            },
            // 3) 개인 인박스 (/user/queue/inbox)
            onMessage = { payload ->
                val m = runCatching { gson.fromJson(payload, ChatMessage::class.java) }.getOrNull()
                if (m != null && m.roomId == roomId) {
                    runOnUiThread { onIncoming(m) }
                }
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

    /** 서버 수신 공통 처리 */
    private fun onIncoming(m: ChatMessage) {
        // ✅ 중복 차단 (방 토픽/인박스 양쪽 수신)
        m.id?.let { if (!seenIds.add(it)) return }

        // 로컬 에코를 교체하거나, 없으면 추가
        messageAdapter.reconcileIncoming(m)

        // ▼ 추가: 로컬 에코가 방금 '진짜 id'로 바뀐 경우도 커버
        if (lastReadByOtherId > 0) {
            messageAdapter.markReadByOtherUpTo(lastReadByOtherId)
        }

        // 바닥 근처면 자동 스크롤 + 읽음 갱신
        val atBottom = layoutManager.findLastVisibleItemPosition() >= (messageAdapter.itemCount - 3)
        if (atBottom) {
            rvChat.scrollToPosition(messageAdapter.itemCount - 1)
            debounceMarkRead()
        }
    }

    private fun loadHistoryAndMarkRead() {
        val rid = roomId
        lifecycleScope.launch {
            try {
                isLoadingMore = true
                hasMore = true

                val list = withContext(Dispatchers.IO) {
                    // 입장 시 읽음 처리
                    ApiProvider.api.markRead(rid, myUserId)
                    // 백엔드 변경 반영: me/other 전달
                    ApiProvider.api.history(rid, 50, null, myUserId, partnerId)
                }.sortedBy { it.id } // ASC

                // 중복 차단 id 세트 갱신
                seenIds.clear()
                list.forEach { it.id?.let(seenIds::add) }

                messageAdapter.setAll(list)

                // ▼ 추가: 저장해둔 읽음 지점 재적용(재입장/재로딩 대비)
                if (lastReadByOtherId > 0) {
                    messageAdapter.markReadByOtherUpTo(lastReadByOtherId)
                }

                rvChat.post {
                    rvChat.scrollToPosition((messageAdapter.itemCount - 1).coerceAtLeast(0))
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

        // 스크롤 위치 보존(점프 방지)
        val firstIndex = layoutManager.findFirstVisibleItemPosition()
        val firstTop = layoutManager.findViewByPosition(firstIndex)?.top ?: 0

        lifecycleScope.launch {
            try {
                val older = withContext(Dispatchers.IO) {
                    // 백엔드 변경 반영: me/other 전달
                    ApiProvider.api.history(roomId, 50, beforeId, myUserId, partnerId)
                }.sortedBy { it.id }

                if (older.isEmpty()) {
                    hasMore = false
                } else {
                    // 이미 있는 id 제거 후 프리펜드
                    val filtered = older.filter { it.id == null || !seenIds.contains(it.id!!) }
                    filtered.forEach { it.id?.let(seenIds::add) }

                    if (filtered.isNotEmpty()) {
                        messageAdapter.prependMany(filtered)
                        // 스크롤 위치 복원
                        rvChat.post {
                            layoutManager.scrollToPositionWithOffset(firstIndex + filtered.size, firstTop)
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
}