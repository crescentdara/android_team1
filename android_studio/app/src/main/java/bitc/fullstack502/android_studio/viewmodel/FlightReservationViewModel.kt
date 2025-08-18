package bitc.fullstack502.android_studio.viewmodel

import androidx.lifecycle.*
import bitc.fullstack502.android_studio.model.BookingRequest
import bitc.fullstack502.android_studio.model.BookingResponse
import bitc.fullstack502.android_studio.model.Flight
import bitc.fullstack502.android_studio.net.FlightSearchAPI
import bitc.fullstack502.android_studio.net.RetrofitClient
import bitc.fullstack502.android_studio.net.RetrofitFlightReservationClient
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FlightReservationViewModel : ViewModel() {

    // ✅ 예약 응답 관련 LiveData
    private val _bookingResponse = MutableLiveData<BookingResponse>()
    val bookingResponse: LiveData<BookingResponse> get() = _bookingResponse

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // ✅ 항공권 예약
    fun bookFlight(request: BookingRequest) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val api = RetrofitFlightReservationClient.instance
                val response = api.createBooking(request) // suspend 함수라면 정상 동작
                _bookingResponse.postValue(response)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "예약 중 오류가 발생했습니다.")
            } finally {
                _loading.postValue(false)
            }
        }
    }

    // ✅ 항공권 검색 결과 LiveData
    private val _flights = MutableLiveData<List<Flight>>()
    val flights: LiveData<List<Flight>> get() = _flights

    // ✅ 항공권 검색
    fun searchFlights(dep: String, arr: String, depTime: String, passenger: String) {
        val api = RetrofitClient.instance.create(FlightSearchAPI::class.java)
        api.searchFlights(dep, arr, depTime).enqueue(object : Callback<List<Flight>> {
            override fun onResponse(
                call: Call<List<Flight>>,
                response: Response<List<Flight>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    _flights.postValue(response.body())
                } else {
                    _error.postValue("항공편 조회 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Flight>>, t: Throwable) {
                _error.postValue(t.message ?: "서버 통신 오류")
            }
        })
    }
}
