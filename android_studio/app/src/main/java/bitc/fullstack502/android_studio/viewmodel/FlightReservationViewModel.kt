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

    // ✅ 예약 응답
    private val _bookingResponse = MutableLiveData<BookingResponse>()
    val bookingResponse: LiveData<BookingResponse> get() = _bookingResponse

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // ✅ 항공권 예약 (suspend + Response 사용)
    fun bookFlight(request: BookingRequest) {
        _loading.value = true
        viewModelScope.launch {
            try {
                // retrofit2.Response 로 명시해서 okhttp3.Response와 혼동 방지
                val res: retrofit2.Response<BookingResponse> =
                    Apis.reservation.createBooking(request)

                if (res.isSuccessful) {
                    // body()는 nullable → let으로 비-null 보장 후 postValue
                    res.body()?.let { booking ->
                        _bookingResponse.postValue(booking)
                    } ?: run {
                        _error.postValue("예약 응답이 비어 있습니다.")
                    }
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

    // ✅ 항공권 검색 결과
    private val _flights = MutableLiveData<List<Flight>>()
    val flights: LiveData<List<Flight>> get() = _flights

    // ✅ 항공권 검색 (dateYmd: "yyyy-MM-dd", depTime: "HH:mm" 또는 null)
    // FlightReservationViewModel.kt (searchFlights 안)
    fun searchFlights(dep: String, arr: String, dateYmd: String, depTime: String? = null) {
        val api = Apis.flightSearch
        val safeTime = depTime?.takeIf { it.isNotBlank() }

        // ✅ 이전 결과 제거해 화면을 확실히 리셋
        _flights.postValue(emptyList())

        Log.d("FLIGHT_REQ", "dep=$dep arr=$arr date=$dateYmd depTime=${safeTime ?: "null"}")

        api.searchFlights(dep.trim(), arr.trim(), dateYmd.trim(), safeTime)
            .enqueue(object : retrofit2.Callback<List<Flight>> {
                override fun onResponse(
                    call: retrofit2.Call<List<Flight>>,
                    response: retrofit2.Response<List<Flight>>
                ) {
                    if (response.isSuccessful) {
                        val list = response.body().orEmpty()
                        Log.d("FLIGHT_RES", "code=${response.code()} size=${list.size}")
                        _flights.postValue(list)
                        if (list.isEmpty()) _error.postValue("해당 조건의 항공편이 없습니다.")
                    } else {
                        _error.postValue("항공편 조회 실패: ${response.code()} ${response.message()}")
                    }
                }
                override fun onFailure(call: retrofit2.Call<List<Flight>>, t: Throwable) {
                    _error.postValue(t.message ?: "서버 통신 오류")
                }
            })
    }
}
