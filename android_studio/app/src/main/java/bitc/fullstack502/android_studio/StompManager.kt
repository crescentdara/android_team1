package bitc.fullstack502.android_studio

import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.json.JSONObject
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent

class StompManager(
    private val serverUrl: String // 예: ws://10.0.2.2:8080/ws (실기기는 PC의 LAN IP)
) {
    private var stompClient: StompClient? = null
    private val bag = CompositeDisposable()

    fun connect(
        roomId: String,
        onConnected: () -> Unit = {},
        onMessage: (String) -> Unit = { },
        onError: (String) -> Unit = { }
    ) {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, serverUrl).apply {
            val life = lifecycle()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ e ->
                    when (e.type) {
                        LifecycleEvent.Type.OPENED -> {
                            Log.d("STOMP", "Connected")
                            onConnected()
                            val topicPath = "/topic/room.$roomId"
                            val sub = topic(topicPath)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({ msg ->
                                    Log.d("STOMP", "RECV: ${msg.payload}")
                                    onMessage(msg.payload)
                                }, { err ->
                                    Log.e("STOMP", "Topic error: ${err.message}", err)
                                    onError("topic: ${err.message}")
                                })
                            bag.add(sub)
                        }
                        LifecycleEvent.Type.ERROR -> {
                            Log.e("STOMP", "Error", e.exception)
                            onError(e.exception?.message ?: "stomp error")
                        }
                        LifecycleEvent.Type.CLOSED -> Log.d("STOMP", "Closed")
                        else -> {}
                    }
                }, { err -> onError(err.message ?: "lifecycle error") })
            bag.add(life)

            connect()
        }
    }

    fun send(roomId: String, senderId: String, receiverId: String, content: String) {
        val json = JSONObject().apply {
            put("roomId", roomId)
            put("senderId", senderId)
            put("receiverId", receiverId)   // ✅ 추가
            put("content", content)
        }.toString()

        stompClient?.send("/app/chat.send", json)
            ?.compose { it.observeOn(AndroidSchedulers.mainThread()) }
            ?.subscribe({
                Log.d("STOMP", "SENT: $json")
            }, { e ->
                Log.e("STOMP", "Send error: ${e.message}", e)
            })?.let { bag.add(it) }
    }


    fun disconnect() {
        stompClient?.disconnect()
        bag.clear()
    }

    // StompManager.kt — 추가
    fun connectGlobal(
        userId: String,
        onConnected: () -> Unit = {},
        onMessage: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val url = serverUrl + "?userId=" + java.net.URLEncoder.encode(userId, "UTF-8")
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url)
        val life = stompClient!!.lifecycle()
            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe({ e ->
                when (e.type) {
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED -> {
                        onConnected() // ✅ OPENED 알림
                        // 기본적으로 내 인박스 구독
                        subscribeUserQueue("/user/queue/inbox", onMessage, onError)
                    }
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.ERROR ->
                        onError(e.exception?.message ?: "stomp error")
                    else -> {}
                }
            }, { err -> onError(err.message ?: "lifecycle error") })
        bag.add(life)
        stompClient!!.connect()
    }

    fun subscribeTopic(path: String, onMessage: (String) -> Unit, onError: (String) -> Unit = {}) {
        val c = stompClient ?: run { onError("stomp not connected"); return }
        val d = c.topic(path)
            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe({ frame -> onMessage(frame.payload) },
                { err -> onError(err.message ?: "subscribe error") })
        bag.add(d)
    }


    /** 필요하면 사용자 큐(/user/queue/...)도 일반화해서 구독 */
    fun subscribeUserQueue(
        path: String = "/user/queue/inbox",
        onMessage: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) = subscribeTopic(path, onMessage, onError)
}