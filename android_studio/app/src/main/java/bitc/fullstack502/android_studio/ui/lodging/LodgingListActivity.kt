package bitc.fullstack502.android_studio.ui.lodging

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.model.LodgingItem
import bitc.fullstack502.android_studio.network.RetrofitProvider
import bitc.fullstack502.android_studio.ui.LodgingFilterBottomSheet
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LodgingListActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var progress: View
    private lateinit var empty: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lodging_list)

        // 1) 전달된 검색 조건
        val checkIn = intent.getStringExtra(LodgingFilterBottomSheet.EXTRA_CHECK_IN).orEmpty()
        val checkOut = intent.getStringExtra(LodgingFilterBottomSheet.EXTRA_CHECK_OUT).orEmpty()
        val adults = intent.getIntExtra(LodgingFilterBottomSheet.EXTRA_ADULTS, 2)
        val children = intent.getIntExtra(LodgingFilterBottomSheet.EXTRA_CHILDREN, 0)
        val city = intent.getStringExtra("city")
        val town = intent.getStringExtra("town")
        val vill = intent.getStringExtra("vill")
        val keyword = intent.getStringExtra("keyword").orEmpty()

        // 2) 상단 요약 표시
        findViewById<TextView>(R.id.tvKeyword).text = "키워드: ${if (keyword.isBlank()) "-" else keyword}"
        findViewById<TextView>(R.id.tvLocation).text =
            "위치: ${listOfNotNull(city, town, vill).joinToString(" ") { it.ifBlank { "-" } }}"
        findViewById<TextView>(R.id.tvDates).text =
            if (checkIn.isNotBlank() && checkOut.isNotBlank()) "날짜: $checkIn ~ $checkOut" else "날짜: -"
        findViewById<TextView>(R.id.tvGuests).text = "인원: 성인 $adults, 아동 $children"

        // 3) RecyclerView & 상태 뷰
        rv = findViewById(R.id.rvLodgings)
        rv.layoutManager = LinearLayoutManager(this)
        progress = findViewById(R.id.progressBar)
        empty = findViewById(R.id.placeholderEmpty)

        // 4) API 호출
        loadLodgings(city, town, vill, checkIn, checkOut, adults, children)
    }

    private fun loadLodgings(
        city: String?,
        town: String?,
        vill: String?,
        checkIn: String?,
        checkOut: String?,
        adults: Int,
        children: Int
    ) {
        progress.visibility = View.VISIBLE
        empty.visibility = View.GONE
        rv.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val items: List<LodgingItem> = withContext(Dispatchers.IO) {
                    RetrofitProvider.lodgingApi.getLodgings(
                        city = city?.ifBlank { null },
                        town = town?.ifBlank { null },
                        vill = vill?.ifBlank { null },
                        checkIn = checkIn?.ifBlank { null },
                        checkOut = checkOut?.ifBlank { null },
                        adults = adults,
                        children = children
                    )
                }

                progress.visibility = View.GONE
                if (items.isEmpty()) {
                    empty.visibility = View.VISIBLE
                    rv.visibility = View.GONE
                } else {
                    rv.adapter = LodgingAdapter(items)
                    rv.visibility = View.VISIBLE
                    empty.visibility = View.GONE
                }
            } catch (e: Exception) {
                progress.visibility = View.GONE
                empty.visibility = View.VISIBLE
                rv.visibility = View.GONE
                Toast.makeText(this@LodgingListActivity, "숙소 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 심플 어댑터: 이름 / 가격 / 평점만 텍스트로 표시 (레이아웃 없이 코드로 구성) */
    private class LodgingAdapter(
        private val items: List<LodgingItem>
    ) : RecyclerView.Adapter<LodgingVH>() {

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): LodgingVH {
            val tv = android.widget.TextView(parent.context).apply {
                setPadding(24, 32, 24, 32)
                textSize = 16f
            }
            return LodgingVH(tv)
        }

        override fun onBindViewHolder(holder: LodgingVH, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size
    }

    private class LodgingVH(private val tv: android.widget.TextView) :
        RecyclerView.ViewHolder(tv) {

        fun bind(item: LodgingItem) {
            val rating = item.rating?.let { " | ★ ${"%.1f".format(it)}" } ?: ""
            tv.text = "${item.name} - ${item.price}원$rating"
        }
    }
}