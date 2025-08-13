package bitc.fullstack502.android_studio.ui.post

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.databinding.ItemCommentBinding
import bitc.fullstack502.android_studio.network.dto.CommDto

class CommentAdapter(
    private val onAuthorClick: (CommDto) -> Unit
) : ListAdapter<CommDto, CommentAdapter.VH>(DIFF) {
    companion object {
        val DIFF = object: DiffUtil.ItemCallback<CommDto>() {
            override fun areItemsTheSame(o: CommDto, n: CommDto) = o.id==n.id
            override fun areContentsTheSame(o: CommDto, n: CommDto) = o==n
        }
    }
    inner class VH(val b: ItemCommentBinding): RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(p: ViewGroup, v: Int): VH {
        return VH(ItemCommentBinding.inflate(LayoutInflater.from(p.context), p, false))
    }

    override fun onBindViewHolder(h: VH, i: Int) {
        val c = getItem(i)
        val prefix = if (c.parentId == null) "" else "↳ "
        h.b.tvAuthor.text = "$prefix${c.author}"
        h.b.tvContent.text = c.content
        h.b.tvDate.text = c.createdAt.replace('T',' ').substring(0,16)

        h.b.tvAuthor.setOnClickListener { onAuthorClick(c) } // ← 작성자 클릭 메뉴
    }
}
