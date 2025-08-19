package bitc.fullstack502.android_studio.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import bitc.fullstack502.android_studio.model.BookingRequest
import bitc.fullstack502.android_studio.model.BookingResponse
import bitc.fullstack502.android_studio.model.Flight
import bitc.fullstack502.android_studio.network.ApiProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FlightReservationViewModel : ViewModel() {

    // ================= 예약 =================
    private val _bookingResponse = MutableLiveData<BookingResponse>()
    val bookingResponse: LiveData<BookingResponse> get() = _bookingResponse

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // ===== 예약 =====
    fun bookFlight(request: BookingRequest) {
        _loading.postValue(true)
        ApiProvider.api.createFlightBooking(request)
            .enqueue(object : Callback<BookingResponse> {
                override fun onResponse(
                    call: Call<BookingResponse>,
                    response: Response<BookingResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        // 🔧 nullable 안전 처리: let 사용
                        body?.let { _bookingResponse.postValue(it) }
                            ?: _error.postValue("예약 응답이 비어 있습니다.")
                    } else {
                        val errBody = try { response.errorBody()?.string() } catch (_: Exception) { null }
                        _error.postValue(
                            "예약 실패: ${response.code()} ${response.message()}" +
                                    (if (errBody.isNullOrBlank()) "" else " - $errBody")
                        )
                    }
                    _loading.postValue(false)
                }

                override fun onFailure(call: Call<BookingResponse>, t: Throwable) {
                    _error.postValue(t.message ?: "예약 중 네트워크 오류")
                    _loading.postValue(false)
                }
            })
    }


    // ============== 항공편 검색(분리 저장) ==============
    private val _outFlights = MutableLiveData<List<Flight>>()
    val outFlights: LiveData<List<Flight>> get() = _outFlights

    private val _inFlights = MutableLiveData<List<Flight>>()
    val inFlights: LiveData<List<Flight>> get() = _inFlights

    // 기존 Activity 호환용 별칭(가는편을 flights로 노출)
    val flights: LiveData<List<Flight>> get() = _outFlights

    @Volatile private var lastReqOut: Long = 0L
    @Volatile private var lastReqIn: Long = 0L

    /** 가는편 검색 */
    fun searchFlights(dep: String, arr: String, dateYmd: String, depTime: String? = null) {
        val api = ApiProvider.api
        val safeTime = depTime?.takeIf { it.isNotBlank() }
        val reqId = System.nanoTime()
        lastReqOut = reqId

        _outFlights.postValue(emptyList())
        _loading.postValue(true)

        Log.d("FLIGHT_REQ(OUT)", "dep=$dep arr=$arr date=$dateYmd depTime=${safeTime ?: "null"}")

        api.searchFlights(dep.trim(), arr.trim(), dateYmd.trim(), safeTime)
            .enqueue(object : retrofit2.Callback<List<Flight>> {
                override fun onResponse(
                    call: retrofit2.Call<List<Flight>>,
                    response: retrofit2.Response<List<Flight>>
                ) {
                    if (lastReqOut != reqId) { _loading.postValue(false); return }

                    if (response.isSuccessful) {
                        val raw = response.body().orEmpty()
                        val cleaned = cleanFlights(raw, dateYmd)
                        Log.d("FLIGHT_RES(OUT)", "code=${response.code()} raw=${raw.size} cleaned=${cleaned.size}")
                        _outFlights.postValue(cleaned)
                        if (cleaned.isEmpty()) _error.postValue("해당 조건의 항공편이 없습니다.")
                    } else {
                        _error.postValue("항공편 조회 실패: ${response.code()} ${response.message()}")
                    }
                    _loading.postValue(false)
                }

                override fun onFailure(call: retrofit2.Call<List<Flight>>, t: Throwable) {
                    if (lastReqOut == reqId) {
                        _error.postValue(t.message ?: "서버 통신 오류")
                        _loading.postValue(false)
                    }
                }
            })
    }

    /** 오는편 검색 (왕복) */
    fun searchInboundFlights(dep: String, arr: String, dateYmd: String, depTime: String? = null) {
        val api = ApiProvider.api
        val safeTime = depTime?.takeIf { it.isNotBlank() }
        val reqId = System.nanoTime()
        lastReqIn = reqId

        _inFlights.postValue(emptyList())
        _loading.postValue(true)

        Log.d("FLIGHT_REQ(IN)", "dep=$dep arr=$arr date=$dateYmd depTime=${safeTime ?: "null"}")

        api.searchFlights(dep.trim(), arr.trim(), dateYmd.trim(), safeTime)
            .enqueue(object : retrofit2.Callback<List<Flight>> {
                override fun onResponse(
                    call: retrofit2.Call<List<Flight>>,
                    response: retrofit2.Response<List<Flight>>
                ) {
                    if (lastReqIn != reqId) { _loading.postValue(false); return }

                    if (response.isSuccessful) {
                        val raw = response.body().orEmpty()
                        val cleaned = cleanFlights(raw, dateYmd)
                        Log.d("FLIGHT_RES(IN)", "code=${response.code()} raw=${raw.size} cleaned=${cleaned.size}")
                        _inFlights.postValue(cleaned)
                    } else {
                        _error.postValue("항공편(오는편) 조회 실패: ${response.code()} ${response.message()}")
                    }
                    _loading.postValue(false)
                }

                override fun onFailure(call: retrofit2.Call<List<Flight>>, t: Throwable) {
                    if (lastReqIn == reqId) {
                        _error.postValue(t.message ?: "서버 통신 오류")
                        _loading.postValue(false)
                    }
                }
            })
    }

    /* ================== 정리 파이프라인 ================== */
    private fun dayOfWeekIndexKst(dateYmd: String): Int {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.KOREA).apply {
                timeZone = java.util.TimeZone.getTimeZone("Asia/Seoul")
            }
            val d = sdf.parse(dateYmd) ?: return 0
            val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Seoul"), java.util.Locale.KOREA)
            cal.time = d
            val dow = cal.get(java.util.Calendar.DAY_OF_WEEK)
            if (dow == java.util.Calendar.SUNDAY) 7 else dow - 1
        } catch (_: Exception) { 0 }
    }

    private fun matchesSelectedDay(days: String?, dateYmd: String): Boolean {
        val raw = (days ?: "").trim()
        if (raw.isEmpty()) return true

        val idx = dayOfWeekIndexKst(dateYmd)
        if (idx == 0) return true

        val num = idx.toString()
        val kor = "월화수목금토일"[idx - 1].toString()
        val engTokens = listOf("mon","tue","wed","thu","fri","sat","sun")
        val eng = engTokens[idx - 1]

        val norm = raw.replace(" ", "")
            .replace("/", ",")
            .replace("|", ",")
            .replace(";", ",")
            .lowercase(java.util.Locale.ROOT)

        if (norm.any { it in '1'..'7' } && norm.contains(num)) return true
        if (norm.any { it in "월화수목금토일" } && norm.contains(kor)) return true
        if (engTokens.any { norm.contains(it) } && norm.contains(eng)) return true

        return true
    }

    private fun uniqueKey(f: Flight): String =
        "${f.flNo.trim().uppercase()}|${f.dep.trim()}|${f.arr.trim()}|${hhmm(f.depTime)}"

    private fun hhmm(src: String?): String {
        val t = (src ?: "").trim()
        if (t.isEmpty()) return ""
        return when {
            t.length >= 5 && t[2] == ':' -> t.substring(0, 5)
            t.contains('T') -> t.substringAfter('T').take(5)
            else -> t.take(5)
        }
    }

    private fun cleanFlights(raw: List<Flight>, dateYmd: String): List<Flight> {
        val filtered = raw.asSequence()
            .filter { matchesSelectedDay(it.days, dateYmd) }
            .map { it.copy(depTime = hhmm(it.depTime), arrTime = hhmm(it.arrTime)) }
            .distinctBy { uniqueKey(it) }
            .sortedBy { it.depTime }
            .toList()

        if (filtered.isEmpty() && raw.isNotEmpty()) {
            return raw.asSequence()
                .map { it.copy(depTime = hhmm(it.depTime), arrTime = hhmm(it.arrTime)) }
                .distinctBy { uniqueKey(it) }
                .sortedBy { it.depTime }
                .toList()
        }
        return filtered
    }
}
