package bitc.fullstack502.android_studio.ui.lodging

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.view.children
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.network.RetrofitProvider
import bitc.fullstack502.android_studio.ui.lodging.LodgingFilterBottomSheet
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class LodgingSearchActivity : AppCompatActivity() {

    private lateinit var rgCity: RadioGroup
    private lateinit var layoutTown: FlexboxLayout
    private lateinit var layoutVill: FlexboxLayout
    private lateinit var btnDateGuest: MaterialButton
    private lateinit var btnSearch: MaterialButton

    // 선택 상태
    private var selectedCity = ""
    private val selectedTowns = mutableListOf<String>()
    private val selectedVills = mutableListOf<String>()

    // 날짜/인원
    private var checkInDate = ""
    private var checkOutDate = ""
    private var adults = 1
    private var children = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lodging_search)

        rgCity = findViewById(R.id.rgCity)
        layoutTown = findViewById(R.id.layoutTown)
        layoutVill = findViewById(R.id.layoutVill)
        btnDateGuest = findViewById(R.id.btnDateGuest)
        btnSearch = findViewById(R.id.btnSearch)

        setupCityListener()
        setupDateGuestButton()
        setupSearchButton()

        // 달력/인원 바텀시트 결과 수신
        supportFragmentManager.setFragmentResultListener(
            LodgingFilterBottomSheet.RESULT_KEY, this
        ) { _, bundle ->
            checkInDate = bundle.getString(LodgingFilterBottomSheet.EXTRA_CHECK_IN) ?: ""
            checkOutDate = bundle.getString(LodgingFilterBottomSheet.EXTRA_CHECK_OUT) ?: ""
            adults = bundle.getInt(LodgingFilterBottomSheet.EXTRA_ADULTS, 1)
            children = bundle.getInt(LodgingFilterBottomSheet.EXTRA_CHILDREN, 0)
            updateDateGuestButtonText()
        }
    }

    private fun setupCityListener() {
        rgCity.setOnCheckedChangeListener { _, checkedId ->
            // 선택 초기화
            selectedTowns.clear()
            selectedVills.clear()
            layoutTown.removeAllViews()
            layoutVill.removeAllViews()
            layoutVill.visibility = View.GONE

            selectedCity = when (checkedId) {
                R.id.rbJeju -> "제주시"
                R.id.rbSeogwipo -> "서귀포시"
                else -> ""
            }
            if (selectedCity.isNotEmpty()) loadTowns(selectedCity)
        }
    }

    private fun setupDateGuestButton() {
        btnDateGuest.setOnClickListener {
            LodgingFilterBottomSheet().show(supportFragmentManager, "lodging_filter")
        }
        updateDateGuestButtonText()
    }

    private fun setupSearchButton() {
        btnSearch.setOnClickListener {
            if (selectedCity.isEmpty()) {
                Toast.makeText(this, "시를 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 목록 화면으로 이동 (필터 모두 전달)
            val intent = Intent(this, LodgingListActivity::class.java).apply {
                putExtra("city", selectedCity)
                putExtra("town", selectedTowns.joinToString(","))
                putExtra("vill", selectedVills.joinToString(","))
                putExtra(LodgingFilterBottomSheet.EXTRA_CHECK_IN, checkInDate)
                putExtra(LodgingFilterBottomSheet.EXTRA_CHECK_OUT, checkOutDate)
                putExtra(LodgingFilterBottomSheet.EXTRA_ADULTS, adults)
                putExtra(LodgingFilterBottomSheet.EXTRA_CHILDREN, children)
            }
            startActivity(intent)
        }
    }

    /** Town 목록 로드 */
    private fun loadTowns(city: String) {
        lifecycleScope.launch {
            try {
                val towns = withContext(Dispatchers.IO) {
                    RetrofitProvider.locationApi.getTowns(city)
                }
                layoutTown.visibility = View.VISIBLE
                createCheckBoxes(layoutTown, towns, selectedTowns) { _, _ ->
                    // 선택된 town이 있으면 vill 로드
                    if (selectedTowns.isNotEmpty()) {
                        loadVillsForSelectedTowns()
                    } else {
                        layoutVill.visibility = View.GONE
                        selectedVills.clear()
                        layoutVill.removeAllViews()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@LodgingSearchActivity, "읍/면/동 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * LocationApi.getVills(city, town)은 단일 town만 받으므로
     * 선택된 town들에 대해 병렬로 API 호출 후 결과를 합쳐서 보여줌.
     */
    private fun loadVillsForSelectedTowns() {
        lifecycleScope.launch {
            try {
                val city = selectedCity
                // 병렬 호출
                val jobs = selectedTowns.map { town ->
                    async(Dispatchers.IO) { RetrofitProvider.locationApi.getVills(city, town) }
                }
                val merged = jobs.flatMap { it.await() }.toSet().toList() // 중복 제거

                layoutVill.visibility = View.VISIBLE
                createCheckBoxes(layoutVill, merged, selectedVills) { _, _ -> /* no-op */ }
            } catch (e: Exception) {
                Toast.makeText(this@LodgingSearchActivity, "리 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 체크박스 UI 생성(다중선택/해제 + 상태 유지) */
    private fun createCheckBoxes(
        container: FlexboxLayout,
        items: List<String>,
        selectedList: MutableList<String>,
        onSelect: (String, Boolean) -> Unit
    ) {
        container.removeAllViews()
        items.forEach { label ->
            val cb = CheckBox(this).apply {
                text = label
                isChecked = selectedList.contains(label)
                setOnCheckedChangeListener { _, checked ->
                    if (checked) {
                        if (!selectedList.contains(label)) selectedList.add(label)
                    } else {
                        selectedList.remove(label)
                    }
                    onSelect(label, checked)
                }
            }
            container.addView(cb)
        }
    }

    private fun updateDateGuestButtonText() {
        val nights = calculateNights(checkInDate, checkOutDate)
        val dateText = if (checkInDate.isNotEmpty() && checkOutDate.isNotEmpty())
            "$checkInDate ~ $checkOutDate • ${nights}박" else "날짜, 인원 선택"
        val guestText = "성인 $adults, 아동 $children"
        btnDateGuest.text = "$dateText\n$guestText"
    }

    private fun calculateNights(ci: String, co: String): Int {
        if (ci.isBlank() || co.isBlank()) return 0
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val s = fmt.parse(ci)?.time ?: return 0
            val e = fmt.parse(co)?.time ?: return 0
            ((e - s) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
        } catch (_: Exception) { 0 }
    }
}