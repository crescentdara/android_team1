package bitc.fullstack502.android_studio.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.data.PostListItem
import bitc.fullstack502.android_studio.databinding.ItemPostBinding

class PostAdapter : RecyclerView.Adapter<PostAdapter.VH>() {
    private val data = mutableListOf<PostListItem>()

    fun submit(list: List<PostListItem>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(private val b: ItemPostBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: PostListItem) {
            b.tvTitle.text = item.title
            b.tvMeta.text = "${item.authorName ?: "익명"} · 조회 ${item.lookCount}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPostBinding.inflate(inflater, parent, false)
        return VH(binding)
    }
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(data[position])
    override fun getItemCount() = data.size
}
