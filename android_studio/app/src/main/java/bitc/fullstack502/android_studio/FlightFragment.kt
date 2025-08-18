package bitc.fullstack502.android_studio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class FlightFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_flight.xml 을 inflate
        return inflater.inflate(R.layout.fragment_flight, container, false)
    }
}
