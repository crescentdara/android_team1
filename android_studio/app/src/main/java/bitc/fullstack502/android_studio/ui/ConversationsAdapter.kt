package bitc.fullstack502.android_studio.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.model.ConversationSummary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class ConversationsAdapter(
    private val onClick: (ConversationSummary) -> Unit
) : RecyclerView.Adapter<ConversationsAdapter.VH>() {

    private val items = mutableListOf<ConversationSummary>()

    init { setHasStableIds(true) }

    /* ---------- Public API ---------- */

    /** 서버에서 처음/다시 가져온 목록 전체 갱신 (DiffUtil로 매끈하게) */
    fun submit(list: List<ConversationSummary>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = list.size

            override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
                // roomId 기준 동일 대화
                return items[oldPos].roomId == list[newPos].roomId
            }

            override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
                val a = items[oldPos]; val b = list[newPos]
                return a.partnerId == b.partnerId &&
                        a.lastContent == b.lastContent &&
                        a.lastAt == b.lastAt &&
                        a.unreadCount == b.unreadCount
            }

            override fun getChangePayload(oldPos: Int, newPos: Int): Any? {
                val a = items[oldPos]; val b = list[newPos]
                val changed = mutableSetOf<String>()
                if (a.lastContent != b.lastContent) changed += "lastContent"
                if (a.lastAt != b.lastAt)         changed += "lastAt"
                if (a.unreadCount != b.unreadCount) changed += "unread"
                if (a.partnerId != b.partnerId)     changed += "partner"
                return if (changed.isEmpty()) null else changed
            }
        })
        items.clear()
        items.addAll(list)
        diff.dispatchUpdatesTo(this)
    }

    /**
     * 실시간 새 메시지 수신 시:
     * - 해당 room의 lastContent/lastAt 갱신
     * - 필요 시 unreadCount +1
     * - 맨 위(인덱스 0)로 이동
     * @return true: 기존 항목 갱신/이동 성공, false: 목록에 없음
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

        if (idx == 0) {
            // 맨 위면 자리 이동 없이 부분 갱신만
            items[0] = updated
            notifyItemChanged(0, setOf("lastContent", "lastAt", "unread"))
            return true
        }

        // 자리 이동: 제거 → 맨 앞 삽입
        items.removeAt(idx)
        items.add(0, updated)
        notifyItemMoved(idx, 0)
        // 맨 앞 아이템만 부분 갱신
        notifyItemChanged(0, setOf("lastContent", "lastAt", "unread"))
        return true
    }

    /* ---------- RecyclerView ---------- */

    override fun getItemId(position: Int): Long = items[position].roomId.hashCode().toLong()

    override fun onCreateViewHolder(p: ViewGroup, vType: Int): VH {
        val v = LayoutInflater.from(p.context).inflate(R.layout.item_conversation, p, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(items[pos], onClick)

    override fun onBindViewHolder(h: VH, pos: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(h, pos)
            return
        }

        val changed = payloads.flatMap { it as Set<*> }.toSet()
        val item = items[pos]

        if ("partner" in changed) h.tvPartner.text = item.partnerId
        if ("lastContent" in changed) h.tvLast.text = ellipsize1Line(item.lastContent)
        if ("lastAt" in changed) h.tvTime.text = formatTime(item.lastAt)

        // ✅ unread + read_receipt 둘 다 처리
        if ("unread" in changed || "read_receipt" in changed) {
            if (item.unreadCount > 0) {
                h.badge.visibility = View.VISIBLE
                h.badge.text = if (item.unreadCount > 99) "99+" else item.unreadCount.toString()
            } else {
                h.badge.visibility = View.GONE
            }
        }

        h.itemView.setOnClickListener { onClick(item) }
    }


    override fun getItemCount() = items.size

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvPartner: TextView = v.findViewById(R.id.tvPartner)
        val tvLast: TextView    = v.findViewById(R.id.tvLast)
        val tvTime: TextView    = v.findViewById(R.id.tvTime)
        val badge: TextView     = v.findViewById(R.id.badge)

        fun bind(item: ConversationSummary, onClick: (ConversationSummary) -> Unit) {
            tvPartner.text = item.partnerId
            tvLast.text = ellipsize1Line(item.lastContent)
            tvTime.text = formatTime(item.lastAt)
            if (item.unreadCount > 0) {
                badge.visibility = View.VISIBLE
                badge.text = if (item.unreadCount > 99) "99+" else item.unreadCount.toString()
            } else {
                badge.visibility = View.GONE
            }
            itemView.setOnClickListener { onClick(item) }
        }
    }

    /* ---------- Helpers ---------- */

    private fun ellipsize1Line(text: String, maxLen: Int = 40): CharSequence {
        // 레이아웃 xml에서 singleLine/ellipsize="end"가 있으면 생략 가능
        return if (text.length <= maxLen) text else text.take(maxLen - 1) + "…"
    }

    private val AMPM_FMT = DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREA)
    
    private fun formatTime(iso: String, zone: ZoneId = ZoneId.systemDefault()): String {
        return try {
            val zdt = Instant.parse(iso).atZone(zone)
            val d = zdt.toLocalDate()
            val today = LocalDate.now(zone)
            when (d) {
                today -> zdt.format(AMPM_FMT)                            // 오늘 → "오전 08:09"
                today.minusDays(1) -> "어제"                             // 어제 → "어제"
                else -> d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.KOREA))
            }
        } catch (_: Exception) {
            iso.take(16).replace('T',' ')
        }
    }

    /** 특정 roomId의 안읽음 배지를 0으로 만든다 */
    fun clearUnread(roomId: String) {
        val idx = items.indexOfFirst { it.roomId == roomId }
        Log.d("CHAT", "clearUnread 호출됨, 찾은 idx=$idx, roomId=$roomId")
        if (idx == -1) return

        val cur = items[idx]
        Log.d("CHAT", "기존 unreadCount=${cur.unreadCount}")
        if (cur.unreadCount == 0L) return

        val updated = cur.copy(unreadCount = 0)
        items[idx] = updated
        notifyItemChanged(idx, setOf("unread"))
        Log.d("CHAT", "✅ $roomId unreadCount 0으로 갱신됨")
    }



}
