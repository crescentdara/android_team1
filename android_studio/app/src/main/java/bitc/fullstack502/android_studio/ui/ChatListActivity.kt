package bitc.fullstack502.android_studio.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.MainActivity
import bitc.fullstack502.android_studio.model.ConversationSummary
import bitc.fullstack502.android_studio.net.ApiClient
import bitc.fullstack502.android_studio.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatListActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ROOM_ID = "roomId"
        const val EXTRA_PARTNER_ID = "partnerId"
    }

    private val myUserId = "android1" // 임시

    private lateinit var rv: RecyclerView
    private lateinit var progress: ProgressBar
    private val adapter = ConversationsAdapter { openChat(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        rv = findViewById(R.id.rv)
        progress = findViewById(R.id.progress)

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            progress.visibility = View.VISIBLE
            try {
                val list = withContext(Dispatchers.IO) {
                    ApiClient.chat.conversations(myUserId)
                }
                adapter.submit(list)
            } catch (e: Exception) {
                e.printStackTrace()
                // 필요하면 Toast 등으로 보여줘도 됨
            } finally {
                progress.visibility = View.GONE
            }
        }
    }

    private fun openChat(item: ConversationSummary) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(EXTRA_ROOM_ID, item.roomId)
            putExtra(EXTRA_PARTNER_ID, item.partnerId)
        }
        startActivity(intent)
    }
}