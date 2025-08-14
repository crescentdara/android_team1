package bitc.fullstack502.android_studio.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.model.ChatMessage

class ChatMessagesAdapter(private val myUserId: String)
    : RecyclerView.Adapter<ChatMessagesAdapter.VH>() {

    private val items = mutableListOf<ChatMessage>()

    companion object {
        private const val TYPE_ME = 1
        private const val TYPE_OTHER = 2
    }

    fun setAll(list: List<ChatMessage>) {
        items.clear(); items.addAll(list); notifyDataSetChanged()
    }
    fun addOne(m: ChatMessage) {
        items.add(m); notifyItemInserted(items.size - 1)
    }

    override fun getItemViewType(position: Int): Int =
        if (items[position].senderId == myUserId) TYPE_ME else TYPE_OTHER

    override fun onCreateViewHolder(p: ViewGroup, type: Int): VH {
        val layout = if (type == TYPE_ME) R.layout.item_msg_me else R.layout.item_msg_other
        val v = LayoutInflater.from(p.context).inflate(layout, p, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val m = items[pos]
        h.tvContent.text = m.content
        h.tvTime.text = (m.sentAt ?: "").replace('T',' ').take(16)
    }

    override fun getItemCount() = items.size

    class VH(v: View): RecyclerView.ViewHolder(v) {
        val tvContent: TextView = v.findViewById(R.id.tvContent)
        val tvTime: TextView = v.findViewById(R.id.tvTime)
    }
}