package bitc.fullstack502.android_studio.ui.lodging

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.network.dto.AvailabilityDto
import bitc.fullstack502.android_studio.network.dto.LodgingDetailDto
import bitc.fullstack502.android_studio.network.dto.LodgingWishStatusDto
import com.bumptech.glide.Glide
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*

class LodgingDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var naverMap: NaverMap? = null
    private var marker: Marker? = null

    private var lodgingId: Long = 0L
    private var lat = 0.0
    private var lon = 0.0
    private var name = ""

    private lateinit var imgCover: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvAddr: TextView
    private lateinit var tvPhone: TextView
    private lateinit var rgRoom: RadioGroup
    private lateinit var rbSingle: RadioButton
    private lateinit var rbDeluxe: RadioButton
    private lateinit var rbSuite: RadioButton
    private lateinit var tvSelection: TextView
    private lateinit var tvTotalPrice: TextView
    private lateinit var btnReserve: Button
    private lateinit var btnWish: ImageButton
    private lateinit var tvWishCount: TextView

    private var wished = false
    private val testUserId = 1L
    private val priceWon = 100_000
    private var selectedNights = 0

    private var checkIn: String = ""
    private var checkOut: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lodging_detail)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        imgCover = findViewById(R.id.imgCover)
        tvName = findViewById(R.id.tvName)
        tvAddr = findViewById(R.id.tvAddr)
        tvPhone = findViewById(R.id.tvPhone)
        rgRoom = findViewById(R.id.rgRoom)
        rbSingle = findViewById(R.id.rbSingle)
        rbDeluxe = findViewById(R.id.rbDeluxe)
        rbSuite = findViewById(R.id.rbSuite)
        tvSelection = findViewById(R.id.tvSelection)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        btnReserve = findViewById(R.id.btnReserve)
        btnWish = findViewById(R.id.btnWish)
        tvWishCount = findViewById(R.id.tvWishCount)

        lodgingId = intent.getLongExtra("lodgingId", 0L).let { if (it == 0L) 1570L else it }
        checkIn = intent.getStringExtra("checkIn") ?: ""
        checkOut = intent.getStringExtra("checkOut") ?: ""

        // 숙박일수 계산
        selectedNights = try {
            val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val start = fmt.parse(checkIn)?.time ?: 0
            val end = fmt.parse(checkOut)?.time ?: 0
            ((end - start) / (24 * 60 * 60 * 1000)).toInt()
        } catch (e: Exception) {
            0
        }

        setReserveEnabled(false)
        fetchDetail()

        rgRoom.setOnCheckedChangeListener { _, _ -> updateSelectionAndTotal() }

        btnReserve.setOnClickListener {
            // 예약 로직 (결제화면 이동 등)
            val intent = Intent(this, LodgingPaymentActivity::class.java).apply {
                putExtra("lodgingId", lodgingId)
                putExtra("checkIn", checkIn)
                putExtra("checkOut", checkOut)
                putExtra("roomType", when (rgRoom.checkedRadioButtonId) {
                    R.id.rbSingle -> "싱글"
                    R.id.rbDeluxe -> "디럭스"
                    R.id.rbSuite -> "스위트"
                    else -> ""
                })
                putExtra("totalPrice", priceWon * selectedNights)
            }
            startActivity(intent)
        }
        btnWish.setOnClickListener { toggleWish() }
    }

    override fun onMapReady(nMap: NaverMap) {
        naverMap = nMap
        nMap.uiSettings.apply {
            isZoomControlEnabled = true
            isLocationButtonEnabled = false
            isCompassEnabled = false
            isScaleBarEnabled = true
        }
        nMap.moveCamera(CameraUpdate.scrollTo(LatLng(37.5666102, 126.9783881)))
        updateMapMarkerIfReady()
    }

    private fun fetchDetail() {
        ApiProvider.api.getDetail(lodgingId).enqueue(object : Callback<LodgingDetailDto> {
            override fun onResponse(c: Call<LodgingDetailDto>, r: Response<LodgingDetailDto>) {
                if (!r.isSuccessful) {
                    Log.e("Detail", "HTTP ${r.code()} ${r.errorBody()?.string()}")
                    return
                }
                val d = r.body() ?: return
                name = d.name.orEmpty()
                lat = d.lat ?: 0.0
                lon = d.lon ?: 0.0
                tvName.text = name
                tvAddr.text = listOfNotNull(d.addrRd, d.addrJb).joinToString(" / ")
                tvPhone.text = d.phone ?: ""
                if (!d.img.isNullOrBlank()) {
                    Glide.with(this@LodgingDetailActivity).load(d.img).into(imgCover)
                }
                updateMapMarkerIfReady()
                refreshWish()
                checkAvailability(checkIn, checkOut)
            }
            override fun onFailure(c: Call<LodgingDetailDto>, t: Throwable) { }
        })
    }

    private fun updateMapMarkerIfReady() {
        val nMap = naverMap ?: return
        val pos = LatLng(lat, lon)
        nMap.moveCamera(CameraUpdate.scrollTo(pos))
        if (marker == null) marker = Marker()
        marker?.apply { position = pos; captionText = name; map = nMap }
    }

    private fun refreshWish() {
        ApiProvider.api.wishStatus(lodgingId, testUserId)
            .enqueue(object : Callback<LodgingWishStatusDto> {
                override fun onResponse(call: Call<LodgingWishStatusDto>, response: Response<LodgingWishStatusDto>) {
                    response.body()?.let { s ->
                        wished = s.wished
                        tvWishCount.text = s.wishCount.toString()
                        updateWishIcon()
                    }
                }
                override fun onFailure(call: Call<LodgingWishStatusDto>, t: Throwable) { }
            })
    }

    private fun toggleWish() {
        ApiProvider.api.wishToggle(lodgingId, testUserId)
            .enqueue(object : Callback<LodgingWishStatusDto> {
                override fun onResponse(call: Call<LodgingWishStatusDto>, response: Response<LodgingWishStatusDto>) {
                    response.body()?.let { s ->
                        wished = s.wished
                        tvWishCount.text = s.wishCount.toString()
                        updateWishIcon()
                    }
                }
                override fun onFailure(call: Call<LodgingWishStatusDto>, t: Throwable) { }
            })
    }

    private fun updateWishIcon() {
        btnWish.setImageResource(if (wished) R.drawable.ic_star_24 else R.drawable.ic_star_border_24)
    }

    private fun checkAvailability(ci: String, co: String) {
        ApiProvider.api.getAvailability(lodgingId, ci, co, null)
            .enqueue(object : Callback<AvailabilityDto> {
                override fun onResponse(c: Call<AvailabilityDto>, r: Response<AvailabilityDto>) {
                    val a = r.body() ?: return
                    val enable = a.availableRooms > 0
                    rbSingle.isEnabled = enable
                    rbDeluxe.isEnabled = enable
                    rbSuite.isEnabled = enable
                    if (!enable) {
                        rgRoom.clearCheck()
                        tvSelection.text = "선택한 옵션: (만실)"
                        Toast.makeText(this@LodgingDetailActivity, a.reason ?: "만실", Toast.LENGTH_SHORT).show()
                    } else updateSelectionAndTotal()
                }
                override fun onFailure(c: Call<AvailabilityDto>, t: Throwable) { }
            })
    }

    private fun updateSelectionAndTotal() {
        val room = when (rgRoom.checkedRadioButtonId) {
            R.id.rbSingle -> "싱글"
            R.id.rbDeluxe -> "디럭스"
            R.id.rbSuite -> "스위트"
            else -> "-"
        }
        val priceText = if (room == "-") "" else " / 1박: ₩%,d".format(priceWon)
        val nightsText = if (selectedNights > 0) " • ${selectedNights}박" else ""
        tvSelection.text = "선택한 옵션: $room$priceText$nightsText"
        val total = priceWon * selectedNights
        tvTotalPrice.text = "총 결제금액: " + NumberFormat.getCurrencyInstance(Locale.KOREA).format(total)

        setReserveEnabled(room != "-")
    }

    private fun setReserveEnabled(enabled: Boolean) {
        btnReserve.isEnabled = enabled
        btnReserve.alpha = if (enabled) 1f else 0.5f
    }

    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { mapView.onPause(); super.onPause() }
    override fun onStop() { mapView.onStop(); super.onStop() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
    override fun onDestroy() { mapView.onDestroy(); super.onDestroy() }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
