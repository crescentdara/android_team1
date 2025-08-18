package bitc.fullstack502.android_studio.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.databinding.ItemFlightTicketBinding
import bitc.fullstack502.android_studio.model.Flight
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class FlightAdapter(
    private val flights: MutableList<Flight> = mutableListOf(),
    private val onItemClick: ((Flight) -> Unit)? = null
) : RecyclerView.Adapter<FlightAdapter.FlightViewHolder>() {

    class FlightViewHolder(val binding: ItemFlightTicketBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val binding = ItemFlightTicketBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FlightViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        val f = flights[position]
        with(holder.binding) {
            // 항공사
            tvAirline.text = (f.airline ?: "").trim()

            // 시간 (레이아웃 ID: tvDepartTime / tvArriveTime)
            val depT = safeTime(f.depTime)
            val arrT = safeTime(f.arrTime)
            tvDepartTime.text = depT
            tvArriveTime.text = arrT

            // 소요시간 (레이아웃 ID: tvDuration)
            tvDuration.text = computeDuration(depT, arrT) ?: ""

            // 편명 (레이아웃 ID: tvFlightNo)
            tvFlightNo.text = f.flNo

            // 가격 뷰는 레이아웃엔 있지만 데이터 모델에 가격이 없으므로 숨김
            tvPrice.visibility = View.GONE

            // 카드/버튼 클릭
            root.setOnClickListener { onItemClick?.invoke(f) }
            btnBook.setOnClickListener { onItemClick?.invoke(f) }
        }
    }

    override fun getItemCount(): Int = flights.size

    /** DiffUtil로 부드럽게 갱신 */
    fun update(newItems: List<Flight>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = flights.size
            override fun getNewListSize() = newItems.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                // ✅ Flight에 고유 id가 있으니 id로 비교 (가장 확실)
                return flights[oldItemPosition].id == newItems[newItemPosition].id
            }
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return flights[oldItemPosition] == newItems[newItemPosition]
            }
        })
        flights.clear()
        flights.addAll(newItems)
        diff.dispatchUpdatesTo(this)
    }

    /* -------------------- 유틸 -------------------- */

    /** "HH:mm:ss" / "HH:mm" / "yyyy-MM-ddTHH:mm:ss" 등 → "HH:mm" */
    private fun safeTime(src: String?): String {
        val t = (src ?: "").trim()
        if (t.isEmpty()) return ""
        if (t.contains('T')) {
            // ISO-8601 비슷한 포맷 처리
            val hhmm = t.substringAfter('T').take(5)
            if (hhmm.length == 5 && hhmm[2] == ':') return hhmm
        }
        return when {
            t.length >= 5 && t[2] == ':' -> t.substring(0, 5)
            else -> t.take(5)
        }
    }

    /** "HH:mm" 두 값으로 대략 소요시간 계산 (자정 넘김 보정) */
    private fun computeDuration(dep: String?, arr: String?): String? {
        if (dep.isNullOrBlank() || arr.isNullOrBlank()) return null
        val fmt = SimpleDateFormat("HH:mm", Locale.KOREA)
        return try {
            val d = fmt.parse(dep)!!
            val a = fmt.parse(arr)!!
            var diffMin = (a.time - d.time) / (60 * 1000)
            if (diffMin < 0) diffMin += 24 * 60 // 자정 넘김 보정
            val h = diffMin / 60
            val m = diffMin % 60
            if (h > 0) "${h}시간 ${m}분" else "${m}분"
        } catch (_: ParseException) {
            null
        }
    }
}
