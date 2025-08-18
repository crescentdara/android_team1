package bitc.fullstack502.android_studio.ui.lodging

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
/**
 * 날짜 + 인원 선택 모달 (이미지처럼 개선)
 * 레이아웃: res/layout/bottomsheet_lodging_filter.xml
 *
 * 결과 전달: FragmentResult API
 *  - requestKey  : LodgingFilterBottomSheet.RESULT_KEY
 *  - result keys : EXTRA_CHECK_IN, EXTRA_CHECK_OUT, EXTRA_ADULTS, EXTRA_CHILDREN
 */
class LodgingFilterBottomSheet : BottomSheetDialogFragment() {
    companion object {
        const val RESULT_KEY = "lodging_filter_result"
        const val EXTRA_CHECK_IN = "checkIn"     // MM.dd(요일)
        const val EXTRA_CHECK_OUT = "checkOut"   // MM.dd(요일)
        const val EXTRA_ADULTS = "adults"
        const val EXTRA_CHILDREN = "children"
    }

    // ---- 상태값 (초기값: 성인 2, 아동 0) ----
    private var checkInCal: Calendar? = null
    private var checkOutCal: Calendar? = null

    private var adults: Int = 2
    private var children: Int = 0

    private val displayFormat = SimpleDateFormat("MM.dd(E)", Locale.KOREA)
    private val resultFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottomsheet_lodging_filter, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- 날짜 요약/변경 ---
        val cardDate = view.findViewById<MaterialCardView>(R.id.cardDate)
        val btnChangeDate = view.findViewById<MaterialButton>(R.id.btnChangeDate)
        val txtDateSummary = view.findViewById<TextView>(R.id.txtDateSummary)

        val updateDateSummary: () -> Unit = {
            val ci = checkInCal
            val co = checkOutCal
            if (ci != null && co != null) {
                val nights = max(0, daysBetween(ci, co))
                txtDateSummary.text =
                    "${displayFormat.format(ci.time)} ~ ${displayFormat.format(co.time)} • ${nights}박"
            } else {
                txtDateSummary.text = "체크인 ~ 체크아웃 • 0박"
            }
        }

        val openDatePickers: () -> Unit = {
            // Material Date Range Picker 사용
            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("체크인/체크아웃 날짜 선택")
                .setSelection(
                    androidx.core.util.Pair(
                        MaterialDatePicker.todayInUtcMilliseconds(),
                        MaterialDatePicker.todayInUtcMilliseconds() + (24 * 60 * 60 * 1000) // 기본 1박
                    )
                )
                .build()

            dateRangePicker.addOnPositiveButtonClickListener { selection ->
                val startDate = selection.first
                val endDate = selection.second
                
                checkInCal = Calendar.getInstance().apply { 
                    timeInMillis = startDate 
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                checkOutCal = Calendar.getInstance().apply { 
                    timeInMillis = endDate 
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                updateDateSummary()
            }

            dateRangePicker.show(parentFragmentManager, "date_range_picker")
        }

        btnChangeDate.setOnClickListener { openDatePickers() }
        cardDate.setOnClickListener { openDatePickers() } // 카드 전체 클릭도 허용
        updateDateSummary()

        // --- 인원 선택 ---
        val txtGuestHeader = view.findViewById<TextView>(R.id.txtGuestHeader)
        val btnGuestChange = view.findViewById<MaterialButton>(R.id.btnGuestChange)
        val btnAdultMinus = view.findViewById<MaterialButton>(R.id.btnAdultMinus)
        val btnAdultPlus = view.findViewById<MaterialButton>(R.id.btnAdultPlus)
        val txtAdultCount = view.findViewById<TextView>(R.id.txtAdultCount)

        val btnChildMinus = view.findViewById<MaterialButton>(R.id.btnChildMinus)
        val btnChildPlus = view.findViewById<MaterialButton>(R.id.btnChildPlus)
        val txtChildCount = view.findViewById<TextView>(R.id.txtChildCount)

        fun renderGuests() {
            txtAdultCount.text = adults.toString()
            txtChildCount.text = children.toString()
            txtGuestHeader.text = "성인 $adults, 아동 $children"
        }
        renderGuests()

        btnAdultMinus.setOnClickListener {
            adults = max(1, adults - 1)  // 성인은 최소 1명
            renderGuests()
        }
        btnAdultPlus.setOnClickListener {
            adults += 1
            renderGuests()
        }
        btnChildMinus.setOnClickListener {
            children = max(0, children - 1)
            renderGuests()
        }
        btnChildPlus.setOnClickListener {
            children += 1
            renderGuests()
        }
        // --- 적용하기 ---
        val btnApply = view.findViewById<MaterialButton>(R.id.btnApply)
        btnApply.setOnClickListener {
            val checkIn = checkInCal?.let { resultFormat.format(it.time) } ?: ""
            val checkOut = checkOutCal?.let { resultFormat.format(it.time) } ?: ""

            setFragmentResult(
                RESULT_KEY,
                bundleOf(
                    EXTRA_CHECK_IN to checkIn,
                    EXTRA_CHECK_OUT to checkOut,
                    EXTRA_ADULTS to adults,
                    EXTRA_CHILDREN to children
                )
            )
            dismiss() // 모달 닫기
        }
    }
    // 두 날짜 사이의 박수 계산(체크인~체크아웃)
    private fun daysBetween(start: Calendar, end: Calendar): Int {
        val s = start.clone() as Calendar
        val e = end.clone() as Calendar
        s.set(Calendar.HOUR_OF_DAY, 0); s.set(Calendar.MINUTE, 0); s.set(Calendar.SECOND, 0); s.set(Calendar.MILLISECOND, 0)
        e.set(Calendar.HOUR_OF_DAY, 0); e.set(Calendar.MINUTE, 0); e.set(Calendar.SECOND, 0); e.set(Calendar.MILLISECOND, 0)
        val diff = e.timeInMillis - s.timeInMillis
        return (diff / (24 * 60 * 60 * 1000)).toInt()
    }
}