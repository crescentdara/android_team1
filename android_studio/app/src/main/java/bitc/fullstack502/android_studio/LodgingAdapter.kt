package bitc.fullstack502.android_studio.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.model.Lodging
import com.bumptech.glide.Glide


class LodgingAdapter(
    private val context: Context,
    private val lodgingList: List<Lodging>
) : RecyclerView.Adapter<LodgingAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.tvLodgingName)
        val lodgingImageView: ImageView = itemView.findViewById(R.id.ivLodgingImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_lodging, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lodging = lodgingList[position]
        holder.nameTextView.text = lodging.name

        Glide.with(context)
            .load(lodging.img)
            .into(holder.lodgingImageView)
    }

    override fun getItemCount(): Int = lodgingList.size
}
