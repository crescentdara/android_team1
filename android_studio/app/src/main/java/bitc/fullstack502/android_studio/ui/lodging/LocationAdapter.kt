package bitc.fullstack502.android_studio.ui.lodging

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.R

class LocationAdapter(
    private val multiSelect: Boolean = false,   // ✅ true면 다중선택, false면 단일선택
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<LocationAdapter.VH>() {

    private val items = mutableListOf<String>()
    private val selectedItems = mutableSetOf<String>() // 다중선택
    private var selectedItem: String? = null           // 단일선택

    inner class VH(val tv: TextView) : RecyclerView.ViewHolder(tv) {
        fun bind(text: String) {
            tv.text = text
            val ctx = tv.context

            // ✅ 선택 상태 반영
            val isSelected = if (multiSelect) {
                selectedItems.contains(text)
            } else {
                text == selectedItem
            }

            if (isSelected) {
                tv.setBackgroundColor(ContextCompat.getColor(ctx, R.color.jeju_tint))
                tv.setTextColor(Color.BLACK)   // jeju_tint는 연한색이라 글씨는 검정이 잘 보임
            } else {
                tv.setBackgroundColor(Color.TRANSPARENT)
                tv.setTextColor(Color.BLACK)
            }

            // ✅ 클릭 이벤트
            tv.setOnClickListener {
                if (multiSelect) {
                    // 다중 선택 모드
                    if (selectedItems.contains(text)) {
                        selectedItems.remove(text)
                    } else {
                        selectedItems.add(text)
                    }
                    notifyItemChanged(adapterPosition)
                } else {
                    // 단일 선택 모드
                    val prev = selectedItem
                    selectedItem = text

                    prev?.let {
                        val prevIdx = items.indexOf(it)
                        if (prevIdx != -1) notifyItemChanged(prevIdx)
                    }
                    notifyItemChanged(adapterPosition)
                }
                onClick(text)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location, parent, false) as TextView
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)

        // 기존 선택된 것 중 이번 리스트에 없는 건 제거
        selectedItems.retainAll(newItems)

        // 단일선택 모드일 때 선택이 사라진 경우 처리
        if (selectedItem != null && !newItems.contains(selectedItem)) {
            selectedItem = null
        }

        notifyDataSetChanged()
    }


    // ✅ 선택 값 가져오기
    fun getSelectedItems(): List<String> = selectedItems.toList()
    fun getSelectedItem(): String? = selectedItem

    // LocationAdapter.kt 안에 추가
    fun deselectItem(item: String) {
        if (multiSelect) {
            if (selectedItems.remove(item)) {
                val index = items.indexOf(item)
                if (index != -1) notifyItemChanged(index)
            }
        } else {
            if (selectedItem == item) {
                val index = items.indexOf(item)
                selectedItem = null
                if (index != -1) notifyItemChanged(index)
            }
        }
    }

}
