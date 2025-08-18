package bitc.fullstack502.android_studio.network

import bitc.fullstack502.android_studio.model.Lodging
import retrofit2.Call
import retrofit2.http.GET

interface LodgingApi {
    @GET("lodgings")
    fun getLodgings(): Call<List<Lodging>>
}
