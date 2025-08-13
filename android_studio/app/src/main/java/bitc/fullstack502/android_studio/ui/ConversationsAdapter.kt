package bitc.fullstack502.android_studio.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.model.ConversationSummary
import bitc.fullstack502.android_studio.R

class ConversationsAdapter(
    private val onClick: (ConversationSummary) -> Unit
) : RecyclerView.Adapter<ConversationsAdapter.VH>() {

    private val items = mutableListOf<ConversationSummary>()

    fun submit(list: List<ConversationSummary>) {
        items.clear(); items.addAll(list); notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p: ViewGroup, vType: Int): VH {
        val v = LayoutInflater.from(p.context).inflate(R.layout.item_conversation, p, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(items[pos], onClick)
    override fun getItemCount() = items.size

    class VH(v: View): RecyclerView.ViewHolder(v) {
        private val tvPartner = v.findViewById<TextView>(R.id.tvPartner)
        private val tvLast = v.findViewById<TextView>(R.id.tvLast)
        private val tvTime = v.findViewById<TextView>(R.id.tvTime)
        private val badge = v.findViewById<TextView>(R.id.badge)

        fun bind(item: ConversationSummary, onClick: (ConversationSummary)->Unit) {
            tvPartner.text = item.partnerId
            tvLast.text = item.lastContent
            tvTime.text = item.lastAt.take(16).replace('T',' ') // 대충 보기 좋게
            if (item.unreadCount > 0) {
                badge.visibility = View.VISIBLE
                badge.text = item.unreadCount.toString()
            } else badge.visibility = View.GONE

            itemView.setOnClickListener { onClick(item) }
        }
    }
}