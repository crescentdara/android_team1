package bitc.fullstack502.android_studio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.databinding.ItemFlightTicketBinding
import bitc.fullstack502.android_studio.model.Flight

class FlightAdapter(
    private val flights: MutableList<Flight> = mutableListOf()
) : RecyclerView.Adapter<FlightAdapter.FlightViewHolder>() {

    class FlightViewHolder(val binding: ItemFlightTicketBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val binding = ItemFlightTicketBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FlightViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        val f = flights[position]
        with(holder.binding) {
            tvAirline.text = f.airline ?: ""
            tvFlNo.text = f.flNo

            // ✅ 출발/도착 공항명도 바인딩 (하드코딩 금지)
            tvDep.text = toDisplayAirport(f.dep)
            tvArr.text = toDisplayAirport(f.arr)

            // 시간은 "HH:mm"만 표시
            tvDepTime.text = f.depTime.take(5)
            tvArrTime.text = f.arrTime.take(5)
        }
    }

    override fun getItemCount(): Int = flights.size

    /** 새 결과로 갱신 */
    fun update(newItems: List<Flight>) {
        flights.clear()
        flights.addAll(newItems)
        notifyDataSetChanged()
    }

    private fun toDisplayAirport(dbName: String): String = when (dbName.trim()) {
        "서울/김포" -> "김포"
        "서울/인천" -> "인천"
        "부산/김해" -> "김해(부산)"
        else -> dbName // "제주", "대구", "무안", "청주" 등은 그대로
    }
}
