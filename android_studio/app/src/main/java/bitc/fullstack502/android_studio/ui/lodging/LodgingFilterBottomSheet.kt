package bitc.fullstack502.android_studio.ui.lodging

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.datepicker.MaterialDatePicker
import bitc.fullstack502.android_studio.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import android.widget.ImageButton   // 추가(수정)
class LodgingFilterBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val RESULT_KEY = "lodging_filter_result"
        const val EXTRA_CHECK_IN = "checkIn"
        const val EXTRA_CHECK_OUT = "checkOut"
        const val EXTRA_ADULTS = "adults"
        const val EXTRA_CHILDREN = "children"
    }

    private var checkInCal: Calendar? = null
    private var checkOutCal: Calendar? = null

    /** ✅ 성인 최소 1명으로 시작 */
    private var adults: Int = 1
    private var children: Int = 0

    private val displayFormat = SimpleDateFormat("MM.dd(E)", Locale.KOREA)
    private val resultFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.bottomsheet_lodging_filter, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val cardDate = view.findViewById<MaterialCardView>(R.id.cardDate)
        val btnChangeDate = view.findViewById<MaterialButton>(R.id.btnChangeDate)
        val txtDateSummary = view.findViewById<TextView>(R.id.txtDateSummary)

        val btnAdultMinus = view.findViewById<ImageButton>(R.id.btnAdultMinus) // 수정
        val btnAdultPlus = view.findViewById<ImageButton>(R.id.btnAdultPlus)    // 수정
        val txtAdultCount = view.findViewById<TextView>(R.id.txtAdultCount)
        val btnChildMinus = view.findViewById<ImageButton>(R.id.btnChildMinus)  // 수정
        val btnChildPlus = view.findViewById<ImageButton>(R.id.btnChildPlus)    // 수정
        val txtChildCount = view.findViewById<TextView>(R.id.txtChildCount)
        val txtGuestHeader = view.findViewById<TextView>(R.id.txtGuestHeader)

        fun daysBetween(s: Calendar, e: Calendar): Int {
            val ss = (s.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val ee = (e.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val diff = ee.timeInMillis - ss.timeInMillis
            return (diff / (24 * 60 * 60 * 1000)).toInt()
        }

        val updateDateSummary: () -> Unit = {
            val ci = checkInCal;
            val co = checkOutCal
            if (ci != null && co != null) {
                val nights = max(0, daysBetween(ci, co))
                txtDateSummary.text = "${displayFormat.format(ci.time)} ~ ${displayFormat.format(co.time)} • ${nights}박"
            } else txtDateSummary.text = "체크인 ~ 체크아웃 • 0박"
        }

        val openDatePicker: () -> Unit = {
            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("체크인/체크아웃 날짜 선택")
                .build()

            picker.addOnPositiveButtonClickListener { range ->
                val start = range.first ?: return@addOnPositiveButtonClickListener
                val end = range.second ?: return@addOnPositiveButtonClickListener
                checkInCal = Calendar.getInstance().apply { timeInMillis = start }
                checkOutCal = Calendar.getInstance().apply { timeInMillis = end }
                updateDateSummary()
            }
            picker.show(parentFragmentManager, "date_range_picker")
        }

        btnChangeDate.setOnClickListener { openDatePicker() }
        cardDate.setOnClickListener { openDatePicker() }
        updateDateSummary()

        fun renderGuests() {
            txtAdultCount.text = adults.toString()
            txtChildCount.text = children.toString()
            txtGuestHeader.text = "성인 $adults, 아동 $children"
        }
        renderGuests()

        btnAdultMinus.setOnClickListener { adults = max(1, adults - 1); renderGuests() } // ✅ 최소 1
        btnAdultPlus.setOnClickListener { adults += 1; renderGuests() }
        btnChildMinus.setOnClickListener { children = max(0, children - 1); renderGuests() }
        btnChildPlus.setOnClickListener { children += 1; renderGuests() }

        view.findViewById<MaterialButton>(R.id.btnApply).setOnClickListener {
            val ci = checkInCal;
            val co = checkOutCal
            if (ci == null || co == null) {
                Toast.makeText(requireContext(), "날짜를 선택하세요.", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }
            // 1박 이상 보장
            if (co.timeInMillis - ci.timeInMillis <= 0) {
                Toast.makeText(requireContext(), "체크아웃은 체크인 이후여야 합니다.", Toast.LENGTH_SHORT)
                    .show(); return@setOnClickListener
            }
            setFragmentResult(
                RESULT_KEY,
                bundleOf(
                    EXTRA_CHECK_IN to resultFormat.format(ci.time),
                    EXTRA_CHECK_OUT to resultFormat.format(co.time),
                    EXTRA_ADULTS to adults,
                    EXTRA_CHILDREN to children
                )
            )
            dismiss()
        }
    }
}