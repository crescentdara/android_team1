package bitc.fullstack502.android_studio

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class Tab1Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_tab1.xml을 inflate해서 반환
        return inflater.inflate(R.layout.fragment_flight, container, false)
    }
}