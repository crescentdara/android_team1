package bitc.fullstack502.android_studio.ui.post

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.databinding.ItemCommentBinding
import bitc.fullstack502.android_studio.network.dto.CommDto
import kotlin.math.roundToInt

class CommentAdapter(
    private val onAuthorClick: (CommDto) -> Unit
) : ListAdapter<CommDto, CommentAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<CommDto>() {
            override fun areItemsTheSame(o: CommDto, n: CommDto) = o.id == n.id
            override fun areContentsTheSame(o: CommDto, n: CommDto) = o == n
        }
    }

    inner class VH(val b: ItemCommentBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(h: VH, i: Int) {
        val c = getItem(i)
        val isReply = c.parentId != null

        // 텍스트
        val prefix = if (isReply) "↳ " else ""
        h.b.tvAuthor.text = "$prefix${c.author}"
        h.b.tvContent.text = c.content
        h.b.tvDate.text = c.createdAt.replace('T', ' ').substring(0, 16)

        // 들여쓰기 (대댓글이면 16dp)
        val dp = h.b.root.resources.displayMetrics.density
        val startPad = if (isReply) (16 * dp).roundToInt() else 0
        h.b.root.setPadding(startPad, h.b.root.paddingTop, h.b.root.paddingRight, h.b.root.paddingBottom)

        // 작성자 클릭 → 메뉴(여기서 1:1 채팅 선택)
        h.b.tvAuthor.setOnClickListener { onAuthorClick(c) }
    }
}
