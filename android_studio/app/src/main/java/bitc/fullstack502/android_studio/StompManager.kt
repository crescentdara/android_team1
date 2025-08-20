package bitc.fullstack502.android_studio

import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.json.JSONObject
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import java.net.URLEncoder

class StompManager(
    /** 예: ws://<SERVER_IP>:8080/ws */
    private val serverUrl: String
) {
    companion object {
        private const val TAG = "STOMP"
        private const val PATH_SEND = "/app/chat.send"        // 서버 @MessageMapping
        private const val TOPIC_ROOM_PREFIX = "/topic/room."  // + {roomId}
        private const val TOPIC_READ_SUFFIX = ".read"         // /topic/room.{roomId}.read
        // 개인 큐는 이번 구조에서 사용하지 않음
        private const val USER_QUEUE_PREFIX = "/user/queue/"  // (미사용)
    }

    private var stompClient: StompClient? = null
    private val bag = CompositeDisposable()
    private val topicSubs = mutableMapOf<String, Disposable>() // 중복구독 방지

    /** 현재 연결 여부 */
    fun isConnected(): Boolean = stompClient?.isConnected == true

    /** 모든 구독 해제 */
    fun clearSubscriptions() {
        topicSubs.forEach { (path, d) ->
            runCatching { d.dispose() }.onFailure { Log.w(TAG, "dispose fail: $path", it) }
        }
        topicSubs.clear()
    }

    /** 특정 토픽 해제 */
    fun unsubscribe(path: String) {
        topicSubs.remove(path)?.let {
            runCatching { it.dispose() }.onFailure { e -> Log.w(TAG, "unsubscribe fail: $path", e) }
        }
    }

    /** (레거시) 단일 방 전용 연결 */
    fun connect(
        roomId: String,
        onConnected: () -> Unit = {},
        onMessage: (String) -> Unit = { },
        onError: (String) -> Unit = { }
    ) {
        disconnect() // 중복 연결 방지
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, serverUrl).apply {
            val life = lifecycle()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ e ->
                    when (e.type) {
                        LifecycleEvent.Type.OPENED -> {
                            Log.d(TAG, "Connected (room=$roomId)")
                            onConnected()
                            subscribeTopic(
                                "$TOPIC_ROOM_PREFIX$roomId",
                                onMessage = onMessage,
                                onError = { err -> onError("topic: $err") }
                            )
                        }
                        LifecycleEvent.Type.ERROR -> {
                            Log.e(TAG, "Error", e.exception)
                            onError(e.exception?.message ?: "stomp error")
                        }
                        LifecycleEvent.Type.CLOSED -> Log.d(TAG, "Closed")
                        else -> {}
                    }
                }, { err -> onError(err.message ?: "lifecycle error") })
            bag.add(life)
            connect()
        }
    }

    /** 전역 연결: userId 쿼리파라미터로 접속. 구독은 호출자가 선택적으로 추가. */
    fun connectGlobal(
        userId: String,
        onConnected: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        disconnect() // 기존 연결/구독 정리

        val url = serverUrl + "?userId=" + URLEncoder.encode(userId, "UTF-8")
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url)

        val life = stompClient!!.lifecycle()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ e ->
                when (e.type) {
                    LifecycleEvent.Type.OPENED -> {
                        Log.d(TAG, "Connected (global) userId=$userId")
                        onConnected()
                    }
                    LifecycleEvent.Type.ERROR -> {
                        Log.e(TAG, "Error", e.exception)
                        onError(e.exception?.message ?: "stomp error")
                    }
                    LifecycleEvent.Type.CLOSED -> Log.d(TAG, "Closed")
                    else -> {}
                }
            }, { err -> onError(err.message ?: "lifecycle error") })
        bag.add(life)

        stompClient!!.connect()
    }

    /** 일반 토픽 구독 (중복 구독 방지) */
    fun subscribeTopic(
        path: String,
        onMessage: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val c = stompClient ?: run { onError("stomp not connected"); return }
        if (topicSubs.containsKey(path)) {
            Log.d(TAG, "skip duplicate subscribe: $path")
            return
        }
        Log.d(TAG, "subscribe: $path")
        val d = c.topic(path)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ frame -> onMessage(frame.payload) },
                { err -> onError(err.message ?: "subscribe error") })
        topicSubs[path] = d
        bag.add(d)
    }

    /** 방 토픽 구독: /topic/room.{roomId} */
    fun subscribeRoom(roomId: String, onMessage: (String) -> Unit, onError: (String) -> Unit = {}) {
        subscribeTopic("$TOPIC_ROOM_PREFIX$roomId", onMessage, onError)
    }

    /** 방 읽음 토픽 구독: /topic/room.{roomId}.read */
    fun subscribeRoomRead(roomId: String, onMessage: (String) -> Unit, onError: (String) -> Unit = {}) {
        subscribeTopic("$TOPIC_ROOM_PREFIX$roomId$TOPIC_READ_SUFFIX", onMessage, onError)
    }

    /** (미사용) 사용자 큐 구독 — 이번 구조에선 쓰지 않음 */
    @Deprecated(
        message = "개인 큐는 사용하지 않습니다. 방 토픽(/topic/room.{id})을 구독하세요.",
        level = DeprecationLevel.WARNING
    )
    fun subscribeUserQueue(
        name: String,
        onMessage: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        // no-op: 토픽만 사용
        Log.w(TAG, "subscribeUserQueue called but ignored: $name")
        // 필요하면 아래 한 줄을 살리되, 서버가 /user/queue/* 브로캐스트를 보내는 경우에만.
        // subscribeTopic("$USER_QUEUE_PREFIX$name", onMessage, onError)
    }

    /** 메시지 전송 */
    fun send(roomId: String, senderId: String, receiverId: String?, content: String) {
        if (!isConnected()) {
            Log.w(TAG, "send ignored: not connected")
            return
        }
        val json = JSONObject().apply {
            put("roomId", roomId)
            put("senderId", senderId)
            if (!receiverId.isNullOrBlank()) put("receiverId", receiverId)
            put("content", content)
        }.toString()

        stompClient?.send(PATH_SEND, json)
            ?.compose { it.observeOn(AndroidSchedulers.mainThread()) }
            ?.subscribe({
                Log.d(TAG, "SENT: $json")
            }, { e ->
                Log.e(TAG, "Send error: ${e.message}", e)
            })?.let { bag.add(it) }
    }

    /** 연결 해제 및 구독 해제 */
    fun disconnect() {
        clearSubscriptions()
        runCatching { stompClient?.disconnect() }
        stompClient = null
        bag.clear()
    }
}
