package bitc.fullstack502.android_studio.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.R

class ImageSliderAdapter(
    private val images: List<Int>
) : RecyclerView.Adapter<ImageSliderAdapter.VH>() {

    inner class VH(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_slider, parent, false)
    ) {
        val img: ImageView = itemView.findViewById(R.id.imgSlide)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(parent)

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.img.setImageResource(images[position])
        holder.img.scaleType = ImageView.ScaleType.CENTER_CROP
    }

    override fun getItemCount() = images.size
}
