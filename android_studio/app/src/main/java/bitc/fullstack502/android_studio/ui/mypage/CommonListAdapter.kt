package bitc.fullstack502.android_studio.ui.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.model.CommonItem
import bitc.fullstack502.android_studio.util.fullUrl
import coil.load

class CommonListAdapter(
    private val items: MutableList<CommonItem>,
    private val onClick: (CommonItem) -> Unit
) : RecyclerView.Adapter<CommonListAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val iv: ImageView = v.findViewById(R.id.ivThumb)
        val tvTitle: TextView = v.findViewById(R.id.tvTitle)
        val tvSub: TextView = v.findViewById(R.id.tvSub)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_item_common, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvSub.text = item.subtitle

        val url = fullUrl(item.imageUrl)
        if (url.isNullOrEmpty()) {
            // 항공쪽은 imageUrl = null로 내려주니까 → 이미지뷰 숨기기
            holder.iv.visibility = View.GONE
        } else {
            holder.iv.visibility = View.VISIBLE
            holder.iv.load(url)
        }

        // 꺽쇠 아이콘 처리
        val arrow = holder.itemView.findViewById<ImageView>(R.id.ivArrow)
        if (item.clickable) {
            arrow.visibility = View.VISIBLE
            holder.itemView.setOnClickListener { onClick(item) }
        } else {
            arrow.visibility = View.GONE
            holder.itemView.setOnClickListener(null)
        }
    }


    override fun getItemCount(): Int = items.size

    fun submit(newItems: List<CommonItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
