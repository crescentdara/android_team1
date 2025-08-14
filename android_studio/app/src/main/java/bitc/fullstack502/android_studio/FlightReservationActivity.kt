package bitc.fullstack502.android_studio

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bitc.fullstack502.android_studio.databinding.ActivityFlightReservationBinding
import androidx.core.util.Pair
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import kotlin.jvm.java

class FlightReservationActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityFlightReservationBinding.inflate(layoutInflater)
    }

    private var adultCount = 1
    private var childCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        val dateRangeButton = findViewById<MaterialButton>(binding.btnDateRange)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 날짜 선택 버튼 클릭
        binding.btnDate.setOnClickListener {
            showDatePickerDialog()
//            showDateRangePickerModal()
        }

        // 인원 선택 버튼 클릭
        binding.btnPassenger.setOnClickListener {
            showPassengerPickerDialog()
        }
        
    }

//    /** 날짜 선택 모달창 **/
    private fun showDatePickerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_date_picker, null)
        val datePicker = dialogView.findViewById<DatePicker>(R.id.datePicker)
        val btnConfirmDate = dialogView.findViewById<Button>(R.id.btnConfirmDate)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnConfirmDate.setOnClickListener {
            val year = datePicker.year
            val month = datePicker.month + 1
            val day = datePicker.dayOfMonth
            binding.btnDate.text = "$year-$month-$day"
            dialog.dismiss()
        }

        dialog.show()
    }


//    /** 인원 선택 모달창 **/
    @SuppressLint("MissingInflatedId")
    private fun showPassengerPickerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_passenger_picker, null)
        val tvAdultCount = dialogView.findViewById<TextView>(R.id.tv_adult_count)
        val tvChildCount = dialogView.findViewById<TextView>(R.id.tv_child_count)
        val btnAdultMinus = dialogView.findViewById<Button>(R.id.btn_adult_minus)
        val btnAdultPlus = dialogView.findViewById<Button>(R.id.btn_adult_plus)
        val btnChildMinus = dialogView.findViewById<Button>(R.id.btn_child_minus)
        val btnChildPlus = dialogView.findViewById<Button>(R.id.btn_child_plus)
        val btnConfirmPassenger = dialogView.findViewById<Button>(R.id.btn_confirm_passenger)

        tvAdultCount.text = adultCount.toString()
        tvChildCount.text = childCount.toString()

        btnAdultMinus.setOnClickListener {
            if (adultCount > 1) {
                adultCount--
                tvAdultCount.text = adultCount.toString()
            }
        }

        btnAdultPlus.setOnClickListener {
            adultCount++
            tvAdultCount.text = adultCount.toString()
        }

        btnChildMinus.setOnClickListener {
            if (childCount > 0) {
                childCount--
                tvChildCount.text = childCount.toString()
            }
        }

        btnChildPlus.setOnClickListener {
            childCount++
            tvChildCount.text = childCount.toString()
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnConfirmPassenger.setOnClickListener {
            val total = adultCount + childCount
            binding.btnPassenger.text = "총 $total 명"
            dialog.dismiss()
        }

        dialog.show()
    }

}
