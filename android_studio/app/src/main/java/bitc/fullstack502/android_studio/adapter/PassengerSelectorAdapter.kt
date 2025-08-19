package bitc.fullstack502.android_studio.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.model.Passenger
import bitc.fullstack502.android_studio.model.PassengerType
import com.google.android.material.card.MaterialCardView

class PassengerSelectorAdapter(
    private val items: MutableList<Passenger>,
    private val onSelected: (position: Int) -> Unit
) : RecyclerView.Adapter<PassengerSelectorAdapter.VH>() {

    /** 현재 선택된 탭 인덱스 */
    var selected: Int = 0
        private set

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val card: MaterialCardView = v as MaterialCardView
        val tvTitle: TextView = v.findViewById(R.id.tvTitle)   // "성인 1" / "소아 1" (고정)
        val tvName: TextView  = v.findViewById(R.id.tvName)    // 이름(회색, 변동)
        val imgChecked: ImageView = v.findViewById(R.id.imgChecked)

        fun bindSelection(isSelected: Boolean) {
            imgChecked.visibility = if (isSelected) View.VISIBLE else View.GONE
            val color = ContextCompat.getColor(
                card.context,
                if (isSelected) R.color.jeju_primary else R.color.divider
            )
            card.strokeColor = color
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_passenger_chip, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    /** 현재 아이템의 '이름(서브텍스트)'만 부분 갱신 (payload 사용) */
    fun updateNameAt(pos: Int, display: String) {
        if (pos in 0 until itemCount) {
            notifyItemChanged(pos, display)
        }
    }

    /** 외부에서 선택 인덱스를 바꿔야 할 때 */
    fun setSelectedIndex(pos: Int, triggerCallback: Boolean = false) {
        if (pos !in 0 until itemCount || pos == selected) return
        val old = selected
        selected = pos
        notifyItemChanged(old)
        notifyItemChanged(selected)
        if (triggerCallback) onSelected(pos)
    }

    /** payload가 있으면 이름(tvName)만 업데이트 */
    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val name = payloads.lastOrNull() as? String ?: return
            holder.tvName.text = if (name.isBlank()) DEFAULT_SUBTITLE else name
            return
        }
        // payload 없으면 전체 바인딩으로 위임
        onBindViewHolder(holder, position)
    }

    /** 전체 바인딩 */
    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]

        // 제목(고정)
        holder.tvTitle.text = when (p.type) {
            PassengerType.ADULT -> "성인 ${p.index + 1}"
            PassengerType.CHILD -> "소아 ${p.index + 1}"
        }

        // 이름(변동)
        val name = p.displayName()
        holder.tvName.text = if (name.isBlank()) DEFAULT_SUBTITLE else name

        // 선택 표시
        holder.bindSelection(position == selected)

        // 클릭 시 선택 변경 + 콜백
// 변경 후 (어댑터는 클릭을 "알리기만")
        holder.itemView.setOnClickListener {
            onSelected(position)   // ★ 내부에서 selected를 바꾸지 않음
        }
    }

    fun setSelected(index: Int) {
        val old = selected
        selected = index
        notifyItemChanged(old)
        notifyItemChanged(selected)
    }

    companion object {
        /** 회색 서브텍스트 기본 문구 (원하면 "이름 입력"으로 바꿔도 됨) */
        private const val DEFAULT_SUBTITLE = ""
    }
}
