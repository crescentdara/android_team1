package bitc.fullstack502.android_studio.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.databinding.ItemFlightTicketBinding
import bitc.fullstack502.android_studio.model.Flight
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.network.dto.WishStatusDto
import bitc.fullstack502.android_studio.util.AuthManager
import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class FlightAdapter(
    private val flights: MutableList<Flight> = mutableListOf(),
    // (item, position, price)
    private val onSelect: ((Flight, Int, Int) -> Unit)? = null,
    private val priceOf: (Flight) -> Int = { 98_700 }
) : RecyclerView.Adapter<FlightAdapter.FlightViewHolder>() {

    private var selectedPos: Int = RecyclerView.NO_POSITION

    // 즐겨찾기 상태 캐시: flightId -> wished
    private val wishMap = mutableMapOf<Long, Boolean>()
    private val requesting = mutableSetOf<Long>() // 중복 요청 방지

    inner class FlightViewHolder(val binding: ItemFlightTicketBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Flight, selected: Boolean) = with(binding) {
            // --- 상단 요약 ---
            tvAirline.text  = (item.airline ?: "").trim()
            tvFlightNo.text = (item.flNo    ?: "").trim()
            tvDep.text      = safeTime(item.depTime)
            tvArr.text      = safeTime(item.arrTime)
            tvDuration.text = calcDuration(item.depTime, item.arrTime)
            tvDuration.visibility = if (tvDuration.text.isNullOrBlank()) View.GONE else View.VISIBLE

            val price = priceOf(item)
            tvPrice.text    = formatWon(price)
            tvFareName.text = "이코노미"

            // 선택 상태 UI
            applySelectedState(cardRoot, selected)
            panelDetails.visibility = if (selected) View.VISIBLE else View.GONE

            // ------- 즐겨찾기(별) 초기 상태 -------
            val fid = item.id
            val uid = AuthManager.id()
            // 캐시에 있으면 바로 반영, 없고 로그인 되어 있으면 서버에서 상태 조회
            updateStar(wishMap[fid ?: -1L] ?: false)
            if (uid > 0L && fid != null && !wishMap.containsKey(fid) && !requesting.contains(fid)) {
                requesting.add(fid)
                ApiProvider.api.getFlightWishStatus(fid, uid).enqueue(object : Callback<WishStatusDto> {
                    override fun onResponse(call: Call<WishStatusDto>, res: Response<WishStatusDto>) {
                        requesting.remove(fid)
                        if (res.isSuccessful) {
                            res.body()?.let { st ->
                                wishMap[fid] = st.wished
                                // 현재 바인딩된 항목이 같은 flight인지 확인 후 갱신
                                if (adapterPosition != RecyclerView.NO_POSITION &&
                                    flights.getOrNull(adapterPosition)?.id == fid) {
                                    updateStar(st.wished)
                                } else {
                                    val idx = flights.indexOfFirst { it.id == fid }
                                    if (idx != -1) notifyItemChanged(idx)
                                }
                            }
                        }
                    }
                    override fun onFailure(call: Call<WishStatusDto>, t: Throwable) {
                        requesting.remove(fid)
                    }
                })
            }

            // ------- 카드 탭: 선택 토글 + 모달 트리거(onSelect 호출) -------
            root.setOnClickListener {
                val old = selectedPos
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                selectedPos = pos
                if (old != RecyclerView.NO_POSITION) notifyItemChanged(old)
                notifyItemChanged(selectedPos)

                val p = priceOf(item)
                onSelect?.invoke(item, pos, p)
            }

            // ------- 별 클릭: 로그인 체크 → 서버 토글 → UI 반영 -------
            btnWish.setOnClickListener {
                val context = it.context
                val userId = AuthManager.id()
                val flightId = item.id
                if (userId <= 0L) {
                    Toast.makeText(context, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (flightId == null) {
                    Toast.makeText(context, "항공편 정보 오류입니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val current = wishMap[flightId] ?: false

                // 낙관적 업데이트
                wishMap[flightId] = !current
                updateStar(!current)

                ApiProvider.api.toggleFlightWish(flightId, userId).enqueue(object : Callback<WishStatusDto> {
                    override fun onResponse(call: Call<WishStatusDto>, res: Response<WishStatusDto>) {
                        if (res.isSuccessful) {
                            res.body()?.let { st ->
                                wishMap[flightId] = st.wished
                                updateStar(st.wished)
                            }
                        } else {
                            // 롤백
                            wishMap[flightId] = current
                            updateStar(current)
                            Toast.makeText(context, "즐겨찾기 저장 실패(${res.code()})", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<WishStatusDto>, t: Throwable) {
                        // 롤백
                        wishMap[flightId] = current
                        updateStar(current)
                        Toast.makeText(context, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        private fun calcDuration(dep: String?, arr: String?): String {
            val d = safeTime(dep)
            val a = safeTime(arr)
            if (d.length != 5 || a.length != 5) return ""

            fun toMin(t: String): Int {
                val h = t.substring(0, 2).toIntOrNull() ?: return -1
                val m = t.substring(3, 5).toIntOrNull() ?: return -1
                return h * 60 + m
            }

            val dm = toMin(d)
            val am = toMin(a)
            if (dm < 0 || am < 0) return ""

            val diff = if (am >= dm) am - dm else (am + 24 * 60) - dm
            val hh = diff / 60
            val mm = diff % 60
            return when {
                hh > 0 && mm > 0 -> "${hh}시간 ${mm}분"
                hh > 0           -> "${hh}시간"
                else             -> "${mm}분"
            }
        }

        private fun applySelectedState(card: MaterialCardView, selected: Boolean) {
            val c = card.context
            val sel = c.getColor(R.color.jeju_primary)
            val def = c.getColor(R.color.divider)
            val ink = c.getColor(R.color.ink_900)

            fun dp(c: Context, v: Float) =
                (v * c.resources.displayMetrics.density).toInt()

            if (selected) {
                card.strokeColor = sel
                card.strokeWidth = dp(c, 2f)
                binding.tvPrice.setTextColor(sel)
            } else {
                card.strokeColor = def
                card.strokeWidth = dp(c, 1f)
                binding.tvPrice.setTextColor(ink)
            }
        }

        private fun updateStar(wished: Boolean) {
            binding.btnWish.setImageResource(
                if (wished) R.drawable.ic_star_24 else R.drawable.ic_star_border_24
            )
            binding.btnWish.alpha = if (wished) 1.0f else 0.85f
            binding.btnWish.contentDescription = if (wished) "즐겨찾기 해제" else "즐겨찾기 추가"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val binding = ItemFlightTicketBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FlightViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        holder.bind(flights[position], position == selectedPos)
    }

    override fun getItemCount(): Int = flights.size

    fun update(newItems: List<Flight>) {
        flights.clear()
        flights.addAll(newItems)
        selectedPos = RecyclerView.NO_POSITION
        wishMap.clear()
        notifyDataSetChanged()
    }

    fun getSelected(): Flight? =
        if (selectedPos in flights.indices) flights[selectedPos] else null

    // ===== helpers =====
    private fun safeTime(src: String?): String {
        val t = (src ?: "").trim()
        if (t.isEmpty()) return ""
        return when {
            t.length >= 5 && t[2] == ':' -> t.substring(0, 5)
            'T' in t                     -> t.substringAfter('T').take(5)
            else                         -> t.take(5)
        }
    }

    private fun formatWon(price: Int): String {
        val f = NumberFormat.getInstance(Locale.KOREA)
        return "₩" + f.format(price)
    }
}
