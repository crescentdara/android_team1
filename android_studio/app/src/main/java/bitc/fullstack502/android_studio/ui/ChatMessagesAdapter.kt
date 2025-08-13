package bitc.fullstack502.android_studio.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.model.ChatMessage
import java.time.Instant
import kotlin.math.max

class ChatMessagesAdapter(private val myUserId: String)
    : RecyclerView.Adapter<ChatMessagesAdapter.VH>() {

    /** 항상 “메시지 id 오름차순(과거→현재)”을 유지한다 (임시는 맨 아래 취급) */
    private val items = mutableListOf<ChatItem>()

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ME     = 1
        private const val TYPE_OTHER  = 2

        // 헤더는 절대 메시지 id(양수)와 겹치지 않도록 "아주 작은 음수" 영역을 씀
        private const val HEADER_BASE: Long = Long.MIN_VALUE / 4
    }

    init { setHasStableIds(true) }

    /* ---------- 외부 API ---------- */

    /** 초기/갱신: 정렬 + 헤더 재생성 */
    fun setAll(list: List<ChatMessage>) {
        val asc = list.sortedWith(compareBy<ChatMessage> { sortKey(it) }.thenBy { it.sentAt })
        items.clear()
        items.addAll(withHeaders(asc))
        notifyDataSetChanged()
    }

    /** 실시간 수신(서버에서 온 '진짜' 메시지) */
    fun addOne(m: ChatMessage) {
        addOneSortedById(m)
    }

    /** 과거 N개 프리펜드 (olderAsc는 id ASC라고 가정) */
    fun prependMany(olderAsc: List<ChatMessage>) {
        if (olderAsc.isEmpty()) return
        // 안전하게 동일 정렬 규칙 재적용
        val sorted = olderAsc.sortedWith(compareBy<ChatMessage> { sortKey(it) }.thenBy { it.sentAt })
        val before = withHeaders(sorted).toMutableList()

        // 경계 헤더 중복 제거
        if (before.isNotEmpty() && items.isNotEmpty()) {
            val lastOfBefore = before.last()
            val firstOfCurrent = items.first()
            if (lastOfBefore is ChatItem.Header && firstOfCurrent is ChatItem.Header &&
                lastOfBefore.label == firstOfCurrent.label) {
                before.removeAt(before.size - 1)
            }
        }

        items.addAll(0, before)
        notifyItemRangeInserted(0, before.size)
    }

    /** 가장 오래된(첫) 메시지 id (무한 스크롤 beforeId 용) */
    fun getFirstIdOrNull(): Long? =
        items.firstOrNull { it is ChatItem.Msg }?.let { (it as ChatItem.Msg).m.id }

    /** 로컬 에코: 전송 즉시 화면에 임시 말풍선 표시
     *  - 임시 id는 음수 사용 (정렬에서는 맨 아래로 취급하도록 처리)
     */
    fun addLocalEcho(content: String, roomId: String, partnerId: String, myUserId: String): Long {
        val tempId = -System.nanoTime() // 거의 충돌 없음 (음수)
        val nowIso = Instant.now().toString()
        val local = ChatMessage(
            id = tempId,
            roomId = roomId,
            senderId = myUserId,
            receiverId = partnerId,
            content = content,
            type = "TEXT",
            sentAt = nowIso,
            readByOther = false
        )
        addOneSortedById(local)   // ASC 정렬 유지, 하지만 임시는 맨 아래 취급
        return tempId
    }

    /** 서버에서 진짜가 오면 임시(음수) 버블을 교체. 못 찾으면 그냥 추가 */
    fun reconcileIncoming(real: ChatMessage) {
        // 뒤에서부터(가장 최근 쪽) 임시 버블을 찾는다: 음수 id + 같은 보낸이 + 동일 내용
        for (i in items.indices.reversed()) {
            val it = items[i]
            if (it is ChatItem.Msg) {
                val m = it.m
                if ((m.id ?: 0L) < 0 &&
                    m.senderId == real.senderId &&
                    m.content == real.content) {
                    // 교체: 서버 값으로 갱신
                    it.m = it.m.copy(
                        id = real.id,
                        sentAt = real.sentAt,
                        readByOther = real.readByOther
                    )
                    // 정렬상 위치가 달라질 수 있으므로 제거 후 다시 삽입
                    val updated = it.m
                    items.removeAt(i)
                    notifyItemRemoved(i)
                    addOneSortedById(updated)
                    return
                }
            }
        }
        // 못 찾으면 정상 추가
        addOne(real)
    }

    /** 읽음 영수증: 상대 lastReadId 이하의 "내 메시지"를 모두 읽힘 처리 */
    fun markReadByOtherUpTo(lastReadId: Long) {
        var firstChanged = -1
        var lastChanged = -1
        for (idx in items.indices) {
            val row = items[idx]
            if (row is ChatItem.Msg) {
                val msg = row.m
                val id = msg.id
                if (msg.senderId == myUserId && id != null && id > 0 && id <= lastReadId) {
                    if (msg.readByOther != true) {
                        row.m = msg.copy(readByOther = true)
                        if (firstChanged == -1) firstChanged = idx
                        lastChanged = idx
                    }
                }
            }
        }
        if (firstChanged != -1) {
            notifyItemRangeChanged(firstChanged, lastChanged - firstChanged + 1)
        }
    }

    /* ---------- 내부 로직 ---------- */

    /** 정렬 키: 임시(음수 id)인 경우 가장 뒤로 보내기 위해 Long.MAX_VALUE 사용 */
    private fun sortKey(m: ChatMessage): Long {
        val id = m.id ?: Long.MIN_VALUE
        return if (id < 0) Long.MAX_VALUE else id
    }

    /** 한 건 삽입(항상 id ASC 유지, 임시는 맨 아래 취급, 헤더 자동 처리); 반환 = 삽입 위치 */
    private fun addOneSortedById(m: ChatMessage): Int {
        val key = sortKey(m)

        // 1) 삽입 인덱스 탐색 (메시지들만 기준으로 비교)
        var insertAt = items.size
        for (i in items.indices) {
            val it = items[i]
            if (it is ChatItem.Msg) {
                val cur = sortKey(it.m)
                if (cur > key) { insertAt = i; break }
            }
        }
        val needLabel = DateLabels.labelOf(m.sentAt)

        // 2) 삽입 위치 앞 구간에 같은 라벨 헤더가 있는지 확인
        var hasHeader = false
        var j = insertAt - 1
        while (j >= 0) {
            when (val it = items[j]) {
                is ChatItem.Header -> { hasHeader = (it.label == needLabel); break }
                is ChatItem.Msg    -> {
                    if (DateLabels.labelOf(it.m.sentAt) == needLabel) hasHeader = true
                    break
                }
            }
            j--
        }

        var pos = insertAt
        if (!hasHeader) {
            items.add(insertAt, ChatItem.Header(needLabel))
            notifyItemInserted(insertAt)
            pos++
        }
        items.add(pos, ChatItem.Msg(m))
        notifyItemInserted(pos)
        // 좌우 중복 헤더 안정화(희귀 케이스)
        dedupAround(pos)
        return pos
    }

    private fun dedupAround(pos: Int) {
        fun headerAt(i: Int) = (items.getOrNull(i) as? ChatItem.Header)?.label
        val left = max(0, pos - 1)
        val right = (pos + 1).coerceAtMost(items.lastIndex)

        if (left - 1 >= 0) {
            val a = headerAt(left)
            val b = headerAt(left - 1)
            if (a != null && a == b) { items.removeAt(left - 1); notifyItemRemoved(left - 1) }
        }
        if (right + 1 <= items.lastIndex) {
            val a = headerAt(right)
            val b = headerAt(right + 1)
            if (a != null && a == b) { items.removeAt(right + 1); notifyItemRemoved(right + 1) }
        }
    }

    /** 날짜 헤더 섞기 (입력은 ASC 가정) */
    private fun withHeaders(list: List<ChatMessage>): List<ChatItem> {
        if (list.isEmpty()) return emptyList()
        val out = ArrayList<ChatItem>(list.size + 8)
        var last: String? = null
        for (m in list) {
            val label = DateLabels.labelOf(m.sentAt)
            if (label != last) {
                out.add(ChatItem.Header(label))
                last = label
            }
            out.add(ChatItem.Msg(m))
        }
        return out
    }

    /* ---------- RecyclerView ---------- */

    override fun getItemViewType(position: Int): Int = when (val it = items[position]) {
        is ChatItem.Header -> TYPE_HEADER
        is ChatItem.Msg    -> if (it.m.senderId == myUserId) TYPE_ME else TYPE_OTHER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layout = when (viewType) {
            TYPE_HEADER -> R.layout.item_date_header
            TYPE_ME     -> R.layout.item_msg_me
            else        -> R.layout.item_msg_other
        }
        val v = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return VH(v, viewType)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        when (val it = items[position]) {
            is ChatItem.Header -> holder.tvDate?.text = it.label
            is ChatItem.Msg -> {
                val m = it.m
                holder.tvContent?.text = m.content
                holder.tvTime?.text = m.sentAt.replace('T',' ').take(16)

                // ✅ 읽음 배지: 내 메시지이고 아직 안 읽혔으면 "1" 표시
                if (holder.tvUnread != null) {
                    if (m.senderId == myUserId && (m.readByOther != true)) {
                        holder.tvUnread.visibility = View.VISIBLE
                        holder.tvUnread.text = "1"
                    } else {
                        holder.tvUnread.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    /** ⚠️ ID 충돌 금지: 메시지는 양수/임시는 음수, 헤더는 매우 작은 음수의 “날짜 키” */
    override fun getItemId(position: Int): Long = when (val it = items[position]) {
        is ChatItem.Header -> headerIdOf(it.label)
        is ChatItem.Msg -> {
            val id = it.m.id
            if (id != null) id
            else {
                // 임시 id가 null인 경우 대비(거의 없음): 내용 기반 해시
                val seed = (it.m.sentAt + "|" + it.m.senderId + "|" + it.m.content).hashCode().toLong()
                -0x2000_0000_0000_0000L + seed   // 항상 음수 & 거의 고유
            }
        }
    }

    private fun headerIdOf(label: String): Long {
        // 간단 해시로 헤더 전용 영역 제공 (충분히 고유)
        return HEADER_BASE + (label.hashCode().toLong() and 0x3FFF_FFFF_FFFF_FFFFL)
    }

    class VH(v: View, type: Int) : RecyclerView.ViewHolder(v) {
        // 헤더
        val tvDate: TextView?    = if (type == TYPE_HEADER) v.findViewById(R.id.tvDate) else null
        // 메시지
        val tvContent: TextView? = if (type != TYPE_HEADER) v.findViewById(R.id.tvContent) else null
        val tvTime: TextView?    = if (type != TYPE_HEADER) v.findViewById(R.id.tvTime) else null
        // ✅ 내 메시지 말풍선 오른쪽의 읽음 배지 (item_msg_me.xml에 존재해야 함)
        val tvUnread: TextView?  = if (type == TYPE_ME) v.findViewById(R.id.tvUnread) else null
    }

    /** 리스트의 한 줄: 헤더 또는 메시지 */
    sealed class ChatItem {
        data class Header(val label: String) : ChatItem()
        data class Msg(var m: ChatMessage)  : ChatItem()
    }
}
