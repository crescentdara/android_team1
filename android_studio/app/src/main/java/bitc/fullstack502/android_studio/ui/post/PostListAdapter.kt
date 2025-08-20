package bitc.fullstack502.android_studio.ui.post

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.databinding.ItemPostBinding
import bitc.fullstack502.android_studio.network.dto.PostDto
import com.bumptech.glide.Glide
import bitc.fullstack502.android_studio.util.fullUrl
import com.bumptech.glide.request.RequestOptions

class PostListAdapter(private val onClick:(PostDto)->Unit)
    : ListAdapter<PostDto, PostListAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object: DiffUtil.ItemCallback<PostDto>() {
            override fun areItemsTheSame(o: PostDto, n: PostDto) = o.id == n.id
            override fun areContentsTheSame(o: PostDto, n: PostDto) = o == n
        }
    }

    inner class VH(val b: ItemPostBinding): RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(p: ViewGroup, v: Int): VH {
        val b = ItemPostBinding.inflate(LayoutInflater.from(p.context), p, false)
        return VH(b)
    }

    override fun onBindViewHolder(h: VH, i: Int) {
        val item = getItem(i)
        h.b.tvTitle.text = item.title
        h.b.tvMeta.text = "♥ ${item.likeCount} · 조회 ${item.lookCount} · by ${item.author}"

        // 이미지 초기화
        h.b.img.setImageDrawable(null)
        h.b.img.clearColorFilter()

        val url = fullUrl(item.imgUrl)   // ✅ util 사용
        if (url.isNullOrBlank()) {
            h.b.img.setImageResource(R.drawable.ic_launcher_foreground)
        } else {
            Glide.with(h.b.img)
                .load(url)
                .apply(RequestOptions.centerCropTransform())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(h.b.img)
        }

        h.b.root.setOnClickListener { onClick(item) }
    }
}
