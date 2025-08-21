package bitc.fullstack502.android_studio.ui.lodging

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import bitc.fullstack502.android_studio.R

class LocationOptionAdapter(
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<LocationOptionAdapter.VH>() {

    private val items = mutableListOf<String>()

    fun submitList(list: List<String>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val textView: TextView) : RecyclerView.ViewHolder(textView) {
        fun bind(item: String) {
            textView.text = item
            textView.setOnClickListener {
                onClick(item)   // ✅ 클릭 시 콜백 전달
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location_option, parent, false) as MaterialButton
        return VH(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
}
