package bitc.fullstack502.android_studio.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.model.ConversationSummary

class ConversationsAdapter(
    private val onClick: (ConversationSummary) -> Unit
) : RecyclerView.Adapter<ConversationsAdapter.VH>() {

    private val items = mutableListOf<ConversationSummary>()

    /** 서버에서 처음/다시 가져온 목록 전체 바인딩 */
    fun submit(list: List<ConversationSummary>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    /**
     * 실시간 새 메시지 수신 시:
     * - 해당 room 항목을 lastContent/lastAt 갱신
     * - (필요하면) unreadCount +1
     * - 리스트 맨 위(인덱스 0)로 이동
     * @return true: 기존 항목을 성공적으로 갱신/이동함, false: 목록에 해당 room이 없음
     */
    fun bumpAndUpdate(
        roomId: String,
        lastContent: String,
        lastAt: String,
        incrementUnread: Boolean
    ): Boolean {
        val idx = items.indexOfFirst { it.roomId == roomId }
        if (idx == -1) return false

        val cur = items[idx]
        val updated = cur.copy(
            lastContent = lastContent,
            lastAt = lastAt,
            unreadCount = cur.unreadCount + if (incrementUnread) 1 else 0
        )

        // 위치 이동: 기존 자리에서 제거 → 맨 앞에 삽입
        items.removeAt(idx)
        items.add(0, updated)

        // 애니메이션/갱신 알림
        notifyItemMoved(idx, 0)
        notifyItemChanged(0)
        return true
    }

    override fun onCreateViewHolder(p: ViewGroup, vType: Int): VH {
        val v = LayoutInflater.from(p.context).inflate(R.layout.item_conversation, p, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(items[pos], onClick)
    override fun getItemCount() = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvPartner = v.findViewById<TextView>(R.id.tvPartner)
        private val tvLast = v.findViewById<TextView>(R.id.tvLast)
        private val tvTime = v.findViewById<TextView>(R.id.tvTime)
        private val badge = v.findViewById<TextView>(R.id.badge)

        fun bind(item: ConversationSummary, onClick: (ConversationSummary) -> Unit) {
            tvPartner.text = item.partnerId
            tvLast.text = item.lastContent
            // 예: "2025-08-13T19:22:11Z" -> "2025-08-13 19:22"
            tvTime.text = item.lastAt.take(16).replace('T', ' ')

            if (item.unreadCount > 0) {
                badge.visibility = View.VISIBLE
                badge.text = item.unreadCount.toString()
            } else {
                badge.visibility = View.GONE
            }
            itemView.setOnClickListener { onClick(item) }
        }
    }
}
