package bitc.fullstack502.android_studio

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.DELETE
import retrofit2.http.Query
import retrofit2.http.PUT

interface ApiService {
    @POST("api/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/signup")
    fun registerUser(@Body request: SignupRequest): Call<Void>

    @GET("api/checkId")
    fun checkId(@Query("id") id: String): Call<CheckIdResponse>

    @POST("api/find-id")
    fun findUserId(@Body request: FindIdRequest): Call<FindIdResponse>

    @POST("api/find-password")
    fun findUserPassword(@Body request: FindPasswordRequest): Call<FindPasswordResponse>  // 수정됨

    @GET("api/user-info")
    fun getUserInfo(@Query("userId") userId: String): Call<SignupRequest>  // 수정됨

    @DELETE("api/delete-user")
    fun deleteUser(@Query("userId") userId: String): Call<Void>

    @PUT("api/update-user")
    fun updateUser(@Body request: SignupRequest): Call<Map<String, String>>  // 수정됨


}

