//package bitc.fullstack502.android_studio.viewmodel
//
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import bitc.fullstack502.android_studio.model.BookingRequest
//import bitc.fullstack502.android_studio.model.BookingResponse
//import bitc.fullstack502.android_studio.model.Flight
//import bitc.fullstack502.android_studio.net.Apis
//import kotlinx.coroutines.launch
//
//class FlightReservationViewModel : ViewModel() {
//
//    // ================= 예약 =================
//    private val _bookingResponse = MutableLiveData<BookingResponse>()
//    val bookingResponse: LiveData<BookingResponse> get() = _bookingResponse
//
//    private val _loading = MutableLiveData<Boolean>()
//    val loading: LiveData<Boolean> get() = _loading
//
//    private val _error = MutableLiveData<String>()
//
//
//    val error: LiveData<String> get() = _error
//
//    fun bookFlight(request: BookingRequest) {
//        _loading.value = true
//        viewModelScope.launch {
//            try {
//                val res: retrofit2.Response<BookingResponse> =
//                    Apis.reservation.createBooking(request)
//
//                if (res.isSuccessful) {
//                    res.body()?.let { _bookingResponse.postValue(it) }
//                        ?: _error.postValue("예약 응답이 비어 있습니다.")
//                } else {
//                    val err = runCatching { res.errorBody()?.string() }.getOrNull()
//                    _error.postValue("예약 실패: ${res.code()} ${res.message()}${err?.let { " - $it" } ?: ""}")
//                }
//            } catch (e: Exception) {
//                _error.postValue(e.message ?: "예약 중 오류가 발생했습니다.")
//            } finally {
//                _loading.postValue(false)
//            }
//        }
//    }
//
//    // ============== 항공편 검색(분리 저장) ==============
//    // 가는편(Out) 리스트
//    private val _outFlights = MutableLiveData<List<Flight>>()
//    val outFlights: LiveData<List<Flight>> get() = _outFlights
//
//    // 오는편(In) 리스트
//    private val _inFlights = MutableLiveData<List<Flight>>()
//    val inFlights: LiveData<List<Flight>> get() = _inFlights
//
//    // ✅ 기존 Activity 호환용 별칭(가는편을 flights로 노출)
//    val flights: LiveData<List<Flight>> get() = _outFlights
//
//    // 요청 토큰(뒤늦은 응답 무시용)
//    @Volatile private var lastReqOut: Long = 0L
//    @Volatile private var lastReqIn: Long = 0L
//
//    /**
//     * 가는편 검색 (UI 기존 코드 그대로 사용 가능)
//     * @param dateYmd "yyyy-MM-dd"
//     * @param depTime "HH:mm" or null
//     */
//    fun searchFlights(dep: String, arr: String, dateYmd: String, depTime: String? = null) {
//        val api = Apis.flightSearch
//        val safeTime = depTime?.takeIf { it.isNotBlank() }
//        val reqId = System.nanoTime()
//        lastReqOut = reqId
//
//        // 새 검색 시작 시 이전 결과 초기화(깜빡임 방지하려면 지워도 됨)
//        _outFlights.postValue(emptyList())
//
//        Log.d("FLIGHT_REQ(OUT)", "dep=$dep arr=$arr date=$dateYmd depTime=${safeTime ?: "null"}")
//
//        api.searchFlights(dep.trim(), arr.trim(), dateYmd.trim(), safeTime)
//            .enqueue(object : retrofit2.Callback<List<Flight>> {
//                override fun onResponse(
//                    call: retrofit2.Call<List<Flight>>,
//                    response: retrofit2.Response<List<Flight>>
//                ) {
//                    if (lastReqOut != reqId) return // ✅ 오래된 응답 무시
//                    if (response.isSuccessful) {
//                        val list = response.body().orEmpty()
//                        Log.d("FLIGHT_RES(OUT)", "code=${response.code()} size=${list.size}")
//                        _outFlights.postValue(list)
//                        if (list.isEmpty()) _error.postValue("해당 조건의 항공편이 없습니다.")
//                    } else {
//                        _error.postValue("항공편 조회 실패: ${response.code()} ${response.message()}")
//                    }
//                }
//
//                override fun onFailure(call: retrofit2.Call<List<Flight>>, t: Throwable) {
//                    if (lastReqOut != reqId) return
//                    _error.postValue(t.message ?: "서버 통신 오류")
//                }
//            })
//    }
//
//    /**
//     * 오는편 검색 (왕복일 때 사용)
//     * Activity에서 inDate로 호출: viewModel.searchInboundFlights(arr, dep, inDateYmd)
//     */
//    fun searchInboundFlights(dep: String, arr: String, dateYmd: String, depTime: String? = null) {
//        val api = Apis.flightSearch
//        val safeTime = depTime?.takeIf { it.isNotBlank() }
//        val reqId = System.nanoTime()
//        lastReqIn = reqId
//
//        _inFlights.postValue(emptyList())
//
//        Log.d("FLIGHT_REQ(IN)", "dep=$dep arr=$arr date=$dateYmd depTime=${safeTime ?: "null"}")
//
//        api.searchFlights(dep.trim(), arr.trim(), dateYmd.trim(), safeTime)
//            .enqueue(object : retrofit2.Callback<List<Flight>> {
//                override fun onResponse(
//                    call: retrofit2.Call<List<Flight>>,
//                    response: retrofit2.Response<List<Flight>>
//                ) {
//                    if (lastReqIn != reqId) return // ✅ 오래된 응답 무시
//                    if (response.isSuccessful) {
//                        val list = response.body().orEmpty()
//                        Log.d("FLIGHT_RES(IN)", "code=${response.code()} size=${list.size}")
//                        _inFlights.postValue(list)
//                    } else {
//                        _error.postValue("항공편(오는편) 조회 실패: ${response.code()} ${response.message()}")
//                    }
//                }
//
//                override fun onFailure(call: retrofit2.Call<List<Flight>>, t: Throwable) {
//                    if (lastReqIn != reqId) return
//                    _error.postValue(t.message ?: "서버 통신 오류")
//                }
//            })
//    }
//}


package bitc.fullstack502.android_studio.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bitc.fullstack502.android_studio.model.BookingRequest
import bitc.fullstack502.android_studio.model.BookingResponse
import bitc.fullstack502.android_studio.model.Flight
import bitc.fullstack502.android_studio.net.Apis
import kotlinx.coroutines.launch

class FlightReservationViewModel : ViewModel() {

    // ================= 예약 =================
    private val _bookingResponse = MutableLiveData<BookingResponse>()
    val bookingResponse: LiveData<BookingResponse> get() = _bookingResponse

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun bookFlight(request: BookingRequest) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val res: retrofit2.Response<BookingResponse> =
                    Apis.reservation.createBooking(request)

                if (res.isSuccessful) {
                    res.body()?.let { _bookingResponse.postValue(it) }
                        ?: _error.postValue("예약 응답이 비어 있습니다.")
                } else {
                    val err = runCatching { res.errorBody()?.string() }.getOrNull()
                    _error.postValue("예약 실패: ${res.code()} ${res.message()}${err?.let { " - $it" } ?: ""}")
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "예약 중 오류가 발생했습니다.")
            } finally {
                _loading.postValue(false)
            }
        }
    }

    // ============== 항공편 검색(분리 저장) ==============
    // 가는편(Out) 리스트
    private val _outFlights = MutableLiveData<List<Flight>>()
    val outFlights: LiveData<List<Flight>> get() = _outFlights

    // 오는편(In) 리스트
    private val _inFlights = MutableLiveData<List<Flight>>()
    val inFlights: LiveData<List<Flight>> get() = _inFlights

    // ✅ 기존 Activity 호환용 별칭(가는편을 flights로 노출)
    val flights: LiveData<List<Flight>> get() = _outFlights

    // 요청 토큰(뒤늦은 응답 무시용)
    @Volatile private var lastReqOut: Long = 0L
    @Volatile private var lastReqIn: Long = 0L

    /**
     * 가는편 검색 (UI 기존 코드 그대로 사용 가능)
     * @param dateYmd "yyyy-MM-dd"
     * @param depTime "HH:mm" or null
     */
    fun searchFlights(dep: String, arr: String, dateYmd: String, depTime: String? = null) {
        val api = Apis.flightSearch
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
                    if (lastReqOut != reqId) { _loading.postValue(false); return } // 오래된 응답 무시

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

    /**
     * 오는편 검색 (왕복일 때 사용)
     * Activity에서 inDate로 호출: viewModel.searchInboundFlights(arr, dep, inDateYmd)
     */
    fun searchInboundFlights(dep: String, arr: String, dateYmd: String, depTime: String? = null) {
        val api = Apis.flightSearch
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
    // KST 기준: 1=월 ... 7=일
    private fun dayOfWeekIndexKst(dateYmd: String): Int {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.KOREA).apply {
                timeZone = java.util.TimeZone.getTimeZone("Asia/Seoul")
            }
            val d = sdf.parse(dateYmd) ?: return 0
            val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Seoul"), java.util.Locale.KOREA)
            cal.time = d
            val dow = cal.get(java.util.Calendar.DAY_OF_WEEK) // SUN=1..SAT=7
            if (dow == java.util.Calendar.SUNDAY) 7 else dow - 1 // 월=1..일=7
        } catch (_: Exception) { 0 }
    }

    // 서버 days 포맷 자동 매칭: "1234567" / "월화수목금토일" / "Mon,Tue,..."
    private fun matchesSelectedDay(days: String?, dateYmd: String): Boolean {
        val raw = (days ?: "").trim()
        if (raw.isEmpty()) return true // 서버가 빈 값이면 필터 미적용

        val idx = dayOfWeekIndexKst(dateYmd) // 1=월..7=일
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

        // 숫자형
        if (norm.any { it in '1'..'7' } && norm.contains(num)) return true
        // 한글형
        if (norm.any { it in "월화수목금토일" } && norm.contains(kor)) return true
        // 영문형
        if (engTokens.any { norm.contains(it) } && norm.contains(eng)) return true

        // 포맷을 확신 못하면 과필터 방지 위해 통과
        return true
    }

    // 중복 판단 키: 편명+출발/도착+출발시각(HH:mm)
    private fun uniqueKey(f: Flight): String =
        "${f.flNo.trim().uppercase()}|${f.dep.trim()}|${f.arr.trim()}|${hhmm(f.depTime)}"

    // "2025-08-18T06:30:00" / "06:30:00" / "06:30" -> "06:30"
    private fun hhmm(src: String?): String {
        val t = (src ?: "").trim()
        if (t.isEmpty()) return ""
        return when {
            t.length >= 5 && t[2] == ':' -> t.substring(0, 5)
            t.contains('T') -> t.substringAfter('T').take(5)
            else -> t.take(5)
        }
    }

    // 요일 필터 → 시간 정규화 → 중복 제거 → 정렬 (+빈 결과면 폴백)
    private fun cleanFlights(raw: List<Flight>, dateYmd: String): List<Flight> {
        val filtered = raw.asSequence()
            .filter { matchesSelectedDay(it.days, dateYmd) }
            .map { it.copy(depTime = hhmm(it.depTime), arrTime = hhmm(it.arrTime)) }
            .distinctBy { uniqueKey(it) }
            .sortedBy { it.depTime }
            .toList()

        // 과필터로 전부 사라지면: 중복만 제거해서 그대로 노출 (안전 폴백)
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
