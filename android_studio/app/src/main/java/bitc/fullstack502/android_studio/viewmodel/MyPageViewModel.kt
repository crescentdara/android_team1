package bitc.fullstack502.android_studio.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.network.dto.FlightWishDto
import kotlinx.coroutines.launch

class MyPageViewModel : ViewModel() {

    private val _flightWishlist = MutableLiveData<List<FlightWishDto>>()
    val flightWishlist: LiveData<List<FlightWishDto>> get() = _flightWishlist

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun loadFlightWishlist(userPk: Long) {
        viewModelScope.launch {
            try {
                val list = ApiProvider.api.getFlightWishlist(userPk)
                _flightWishlist.postValue(list)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "위시리스트 조회 실패")
            }
        }
    }
}
