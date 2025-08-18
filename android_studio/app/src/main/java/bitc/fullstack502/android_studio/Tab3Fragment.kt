package bitc.fullstack502.android_studio

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class Tab3Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_tab3.xml을 만든 뒤, 이걸 inflate 해줘야 해
        return inflater.inflate(R.layout.fragment_tab3, container, false)
    }
}