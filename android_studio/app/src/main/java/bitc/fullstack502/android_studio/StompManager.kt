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
    /** 예: ws://10.0.2.2:8080/ws  (실기기는 PC의 LAN IP/도메인) */
    private val serverUrl: String
) {
    companion object {
        private const val TAG = "STOMP"
        private const val PATH_SEND = "/app/chat.send"        // 서버의 @MessageMapping 경로
        private const val TOPIC_ROOM_PREFIX = "/topic/room."  // + {roomId}
        private const val USER_QUEUE_PREFIX = "/user/queue/"  // + inbox / read-receipt 등
    }

    private var stompClient: StompClient? = null
    private val bag = CompositeDisposable()
    private val topicSubs = mutableMapOf<String, Disposable>() // 중복구독 방지

    /** 현재 연결 여부 */
    fun isConnected(): Boolean = stompClient?.isConnected == true

    /** 모든 구독 해제 (특정 토픽만 해제하고 싶으면 unsubscribe(path) 사용) */
    fun clearSubscriptions() {
        topicSubs.values.forEach { runCatching { it.dispose() } }
        topicSubs.clear()
    }

    /** 개별 토픽 해제 */
    fun unsubscribe(path: String) {
        topicSubs.remove(path)?.let { runCatching { it.dispose() } }
    }

    /** 기존 연결 정리 후 지정 room 토픽 하나만 구독해 연결 (레거시) */
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
                            Log.d(TAG, "Connected")
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

    /** 전역 연결: userId 쿼리파라미터로 접속. 구독은 별도 헬퍼로 호출자가 선택적으로 추가. */
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
                        Log.d(TAG, "Connected (global)")
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
        // 이미 구독 중이면 스킵
        if (topicSubs.containsKey(path)) return

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

    /** 사용자 큐 구독: /user/queue/{name} (예: inbox, read-receipt) */
    fun subscribeUserQueue(
        name: String,
        onMessage: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        subscribeTopic("$USER_QUEUE_PREFIX$name", onMessage, onError)
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
            if (!receiverId.isNullOrBlank()) put("receiverId", receiverId) // 서버가 필요 시 사용
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
