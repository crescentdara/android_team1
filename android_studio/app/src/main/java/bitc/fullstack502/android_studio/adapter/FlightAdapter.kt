package bitc.fullstack502.android_studio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.databinding.ItemFlightTicketBinding
import bitc.fullstack502.android_studio.model.Flight

class FlightAdapter(private val flights: List<Flight>) :
    RecyclerView.Adapter<FlightAdapter.FlightViewHolder>() {

    // ViewBinding 을 사용하는 ViewHolder
    class FlightViewHolder(val binding: ItemFlightTicketBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val binding = ItemFlightTicketBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FlightViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        val flight = flights[position]
        with(holder.binding) {
            tvAirline.text = flight.airline
            tvFlNo.text = flight.flNo
            tvDepTime.text = flight.depTime.toString()
            tvArrTime.text = flight.arrTime.toString()
        }
    }

    override fun getItemCount(): Int = flights.size
}
