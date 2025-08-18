package bitc.fullstack502.android_studio.ui.lodging

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.model.LodgingItem
import bitc.fullstack502.android_studio.network.RetrofitProvider
import bitc.fullstack502.android_studio.ui.LodgingFilterBottomSheet
import coil.load
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

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
        findViewById<TextView>(R.id.tvLocation).text =
            "위치: ${listOfNotNull(city, town, vill).joinToString(" ") { it.ifBlank { "-" } }}"
        findViewById<TextView>(R.id.tvDates).text =
            if (checkIn.isNotBlank() && checkOut.isNotBlank())
                "날짜: $checkIn ~ $checkOut" else "날짜: -"
        findViewById<TextView>(R.id.tvGuests).text =
            "인원: 성인 $adults, 아동 $children"

        // 3) RecyclerView & 상태 뷰
        rv = findViewById(R.id.rvLodgings)
        rv.layoutManager = LinearLayoutManager(this)
        progress = findViewById(R.id.progressBar)
        empty = findViewById(R.id.placeholderEmpty)

        // 4) API 호출
        loadLodgings(city, town, vill, checkIn, checkOut, adults, children)
    }

    private fun loadLodgings(
        city: String?, town: String?, vill: String?,
        checkIn: String?, checkOut: String?,
        adults: Int, children: Int
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
                } else {
                    rv.adapter = LodgingAdapter(items) { selectedItem ->
                        // ✅ 클릭 시 상세 화면 이동
                        val intent = Intent(this@LodgingListActivity, LodgingDetailActivity::class.java).apply {
                            putExtra("lodgingId", selectedItem.id) // LodgingItem.id 필드 사용
                        }
                        startActivity(intent)
                    }
                    rv.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                progress.visibility = View.GONE
                empty.visibility = View.VISIBLE
                rv.visibility = View.GONE
                Toast.makeText(
                    this@LodgingListActivity,
                    "숙소 불러오기 실패: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private class LodgingAdapter(
        private val items: List<LodgingItem>,
        private val onItemClick: (LodgingItem) -> Unit
    ) : RecyclerView.Adapter<LodgingAdapter.VH>() {

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val iv: ImageView = view.findViewById(R.id.ivLodgingThumb)
            val tvName: TextView = view.findViewById(R.id.tvLodgingName)
            val tvAddr: TextView = view.findViewById(R.id.tvLodgingAddr)
            val tvPrice: TextView = view.findViewById(R.id.tvLodgingPrice)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_lodging, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.tvName.text = item.name
            holder.tvAddr.text = listOfNotNull(item.city, item.town).joinToString(" ")

            // 가격 표시
            if (item.price > 0L) {
                val won = NumberFormat.getNumberInstance(Locale.KOREA).format(item.price)
                holder.tvPrice.text = "${won}원"
            } else {
                holder.tvPrice.text = "가격 정보 없음"
            }

            // 이미지
            if (item.img.isNullOrBlank()) {
                holder.iv.setImageResource(R.drawable.ic_launcher_foreground)
            } else {
                holder.iv.load(item.img)
            }

            // ✅ 클릭 이벤트
            holder.itemView.setOnClickListener {
                onItemClick(item)
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
