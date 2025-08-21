import android.util.Log
import io.reactivex.disposables.Disposable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import org.json.JSONObject
import bitc.fullstack502.android_studio.BuildConfig
import bitc.fullstack502.android_studio.model.ChatMessage
import com.google.gson.Gson

object StompManager {

    private var stompClient: StompClient? = null
    private var disposables = mutableListOf<Disposable>()

    /** 연결 */
    fun connectGlobal(
        userId: String,
        onConnected: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        // 이미 연결 중이면 무시
        if (stompClient?.isConnected == true) {
            onConnected()
            return
        }

        val url = BuildConfig.WS_BASE // ex: ws://<server>:8080/ws
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url)
        stompClient?.connect()

        stompClient?.lifecycle()?.subscribe { event ->
            when (event.type) {
                LifecycleEvent.Type.OPENED -> {
                    Log.d("STOMP", "Connected to $url")
                    onConnected()
                }
                LifecycleEvent.Type.ERROR -> {
                    Log.e("STOMP", "Error: ${event.exception}")
                    event.exception?.let(onError)
                }
                LifecycleEvent.Type.CLOSED -> {
                    Log.w("STOMP", "Connection closed")
                }
                else -> {}
            }
        }?.let { disposables.add(it) }
    }

    /** 구독 */
    fun subscribeTopic(
        topic: String,
        onMessage: (String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        stompClient?.topic(topic)
            ?.subscribe({ msg -> onMessage(msg.payload) }, { err -> onError(err) })
            ?.let { disposables.add(it) }
    }

    /** 메시지 전송 */
    fun send(roomId: String, senderId: String, receiverId: String, content: String) {
        val json = JSONObject()
            .put("roomId", roomId)
            .put("senderId", senderId)
            .put("receiverId", receiverId)
            .put("content", content)
            .toString()

        stompClient?.send("/app/chat.send", json)?.subscribe()
    }

    /** 연결 해제 */
    fun disconnect() {
        disposables.forEach { it.dispose() }
        disposables.clear()
        stompClient?.disconnect()
        stompClient = null
    }

    fun subscribeRoom(
        roomId: String,
        onMessage: (ChatMessage) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        subscribeTopic("/topic/room.$roomId", { payload ->
            val msg = Gson().fromJson(payload, ChatMessage::class.java)
            onMessage(msg)
        }, onError)
    }

    fun unsubscribeAll() {
        disposables.forEach { if (!it.isDisposed) it.dispose() }
        disposables.clear()
    }

}
