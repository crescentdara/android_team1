package bitc.fullstack502.android_studio.ui.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.model.CommonItem
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.util.fullUrl
import coil.load
import kotlinx.coroutines.launch

class BookingListAdapter(
    private val items: MutableList<CommonItem>
) : RecyclerView.Adapter<BookingListAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val iv: ImageView = v.findViewById(R.id.ivThumb)
        val tvTitle: TextView = v.findViewById(R.id.tvTitle)
        val tvSub: TextView = v.findViewById(R.id.tvSub)
        val arrow: ImageView = v.findViewById(R.id.ivArrow) // 원래 꺾쇠 자리
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_item_common, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvSub.text = item.subtitle

        val url = fullUrl(item.imageUrl)
        if (url.isNullOrEmpty()) {
            holder.iv.visibility = View.GONE
        } else {
            holder.iv.visibility = View.VISIBLE
            holder.iv.load(url)
        }

        // 원래 꺾쇠 숨기고 버튼으로 대체
        holder.arrow.visibility = View.GONE

        // 버튼이 이미 있으면 재사용
        var btnCancel = holder.itemView.findViewWithTag<Button>("btnCancel_${holder.adapterPosition}")
        if (btnCancel == null) {
            btnCancel = Button(holder.itemView.context).apply {
                tag = "btnCancel_${holder.adapterPosition}"
                text = "취소"
                textSize = 11f
                isAllCaps = false

                // Material 기본 minWidth/minHeight 제거
                minWidth = 0
                minHeight = 0
                minimumWidth = 0
                minimumHeight = 0

                // 패딩도 직접 지정
                setPadding(24, 8, 24, 8)

                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))

                // ivArrow 자리(오른쪽 끝)에 배치
                val params = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.marginEnd = 16
                params.gravity = android.view.Gravity.END or android.view.Gravity.CENTER_VERTICAL
                layoutParams = params
            }

            val root = holder.itemView as ViewGroup
            root.addView(btnCancel)
        }

        if (item.subtitle.contains("CANCEL")) {
            btnCancel.text = "취소완료"
            btnCancel.isEnabled = false
            btnCancel.setOnClickListener(null)
        } else {
            btnCancel.text = "취소"
            btnCancel.isEnabled = true
            btnCancel.setOnClickListener {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("예매 취소")
                    .setMessage("예매를 취소하시겠습니까?")
                    .setPositiveButton("확인") { _, _ ->
                        (holder.itemView.context as? LifecycleOwner)?.lifecycleScope?.launch {
                            try {
                                val res = ApiProvider.api.cancelBooking(item.id)
                                if (res.isSuccessful) {
                                    Toast.makeText(holder.itemView.context, "취소 완료", Toast.LENGTH_SHORT).show()
                                    val newSub = item.subtitle.replaceAfterLast("•", " CANCEL")
                                    items[position] = item.copy(subtitle = newSub, clickable = false)
                                    notifyItemChanged(position)
                                } else {
                                    Toast.makeText(holder.itemView.context, "취소 실패", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(holder.itemView.context, "네트워크 오류", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("닫기", null)
                    .show()
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun submit(newItems: List<CommonItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
