package bitc.fullstack502.android_studio.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.databinding.ItemFlightTicketBinding
import bitc.fullstack502.android_studio.model.Flight
import com.google.android.material.card.MaterialCardView
import java.text.NumberFormat
import java.util.Locale

class FlightAdapter(
    private val flights: MutableList<Flight> = mutableListOf(),
    // (item, position, price)
    private val onSelect: ((Flight, Int, Int) -> Unit)? = null,
    private val priceOf: (Flight) -> Int = { 98_700 }
) : RecyclerView.Adapter<FlightAdapter.FlightViewHolder>() {

    private var selectedPos: Int = RecyclerView.NO_POSITION

    inner class FlightViewHolder(val binding: ItemFlightTicketBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Flight, selected: Boolean) = with(binding) {
            // --- 상단 요약 ---
            tvAirline.text  = (item.airline ?: "").trim()
            tvFlightNo.text = (item.flNo    ?: "").trim()
            tvDep.text      = safeTime(item.depTime)
            tvArr.text      = safeTime(item.arrTime)
            tvDuration.text = calcDuration(item.depTime, item.arrTime)
            tvDuration.visibility = if (tvDuration.text.isNullOrBlank()) View.GONE else View.VISIBLE

            val price = priceOf(item)
            tvPrice.text    = formatWon(price)
            tvFareName.text = "이코노미"

            // 선택 상태 UI
            applySelectedState(cardRoot, selected)
            panelDetails.visibility = if (selected) View.VISIBLE else View.GONE

            // ✅ 카드 탭: 선택 토글 + 모달 트리거(onSelect 호출)
            root.setOnClickListener {
                val old = selectedPos
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                selectedPos = pos
                if (old != RecyclerView.NO_POSITION) notifyItemChanged(old)
                notifyItemChanged(selectedPos)

                val price = priceOf(item)
                onSelect?.invoke(item, pos, price)   // ✅ 모달 띄울 트리거
            }
        }

        private fun calcDuration(dep: String?, arr: String?): String {
            val d = safeTime(dep)
            val a = safeTime(arr)
            if (d.length != 5 || a.length != 5) return ""

            fun toMin(t: String): Int {
                val h = t.substring(0, 2).toIntOrNull() ?: return -1
                val m = t.substring(3, 5).toIntOrNull() ?: return -1
                return h * 60 + m
            }

            val dm = toMin(d)
            val am = toMin(a)
            if (dm < 0 || am < 0) return ""

            val diff = if (am >= dm) am - dm else (am + 24 * 60) - dm
            val hh = diff / 60
            val mm = diff % 60
            return when {
                hh > 0 && mm > 0 -> "${hh}시간 ${mm}분"
                hh > 0           -> "${hh}시간"
                else             -> "${mm}분"
            }
        }

        private fun applySelectedState(card: MaterialCardView, selected: Boolean) {
            val c = card.context
            val sel = c.getColor(R.color.jeju_primary)  // #206064
            val def = c.getColor(R.color.divider)
            val ink = c.getColor(R.color.ink_900)

            fun dp(c: Context, v: Float) =
                (v * c.resources.displayMetrics.density).toInt()

            if (selected) {
                card.strokeColor = sel
                card.strokeWidth = dp(c, 2f)
                binding.tvPrice.setTextColor(sel)
            } else {
                card.strokeColor = def
                card.strokeWidth = dp(c, 1f)
                binding.tvPrice.setTextColor(ink)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val binding = ItemFlightTicketBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FlightViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        holder.bind(flights[position], position == selectedPos)
    }

    override fun getItemCount(): Int = flights.size

    fun update(newItems: List<Flight>) {
        flights.clear()
        flights.addAll(newItems)
        selectedPos = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    fun getSelected(): Flight? =
        if (selectedPos in flights.indices) flights[selectedPos] else null

    // ===== helpers =====
    private fun safeTime(src: String?): String {
        val t = (src ?: "").trim()
        if (t.isEmpty()) return ""
        return when {
            t.length >= 5 && t[2] == ':' -> t.substring(0, 5)
            'T' in t                     -> t.substringAfter('T').take(5)
            else                         -> t.take(5)
        }
    }

    private fun formatWon(price: Int): String {
        val f = NumberFormat.getInstance(Locale.KOREA)
        return "₩" + f.format(price)
    }
}
