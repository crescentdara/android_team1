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



//package bitc.fullstack502.android_studio.adapter
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import bitc.fullstack502.android_studio.R
//import bitc.fullstack502.android_studio.model.Flight
//
//class FlightAdapter(
//    private var flightList: List<Flight>,
//    private val onBookClick: (Flight) -> Unit
//) : RecyclerView.Adapter<FlightAdapter.FlightViewHolder>() {
//
//    inner class FlightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val tvAirline: TextView = itemView.findViewById(R.id.tv_airline)
//        val tvFlNo: TextView = itemView.findViewById(R.id.tv_fl_no)
//        val tvDep: TextView = itemView.findViewById(R.id.tv_dep)
//        val tvArr: TextView = itemView.findViewById(R.id.tv_arr)
//        val tvDepTime: TextView = itemView.findViewById(R.id.tv_dep_time)
//        val tvArrTime: TextView = itemView.findViewById(R.id.tv_arr_time)
//        val btnBook: Button = itemView.findViewById(R.id.btn_book)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_flight_ticket, parent, false)
//        return FlightViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
//        val flight = flightList[position]
//        holder.tvAirline.text = flight.airline
//        holder.tvFlNo.text = flight.flNo
//        holder.tvDep.text = flight.dep
//        holder.tvArr.text = flight.arr
//        holder.tvDepTime.text = flight.depTime
//        holder.tvArrTime.text = flight.arrTime
//
//        holder.btnBook.setOnClickListener {
//            onBookClick(flight)
//        }
//    }
//
//    override fun getItemCount(): Int = flightList.size
//
//    fun updateFlights(newFlights: List<Flight>) {
//        flightList = newFlights
//        notifyDataSetChanged()
//    }
//}

