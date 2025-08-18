package bitc.fullstack502.android_studio

import retrofit2.Call
import retrofit2.http.*

interface UserApi {

    @POST("api/register")
    fun registerUser(@Body request: SignupRequest): Call<Void>

    @GET("api/checkId")
    fun checkId(@Query("id") id: String): Call<CheckIdResponse>

    @POST("api/find-id")
    fun findUserId(@Body request: FindIdRequest): Call<FindIdResponse>

    @POST("api/find-password")
    fun findUserPassword(@Body request: FindPasswordRequest): Call<FindPasswordResponse>

    @GET("api/user-info")
    fun getUserInfo(@Query("userId") userId: String): Call<SignupRequest>

    @DELETE("api/delete-user")
    fun deleteUser(@Query("userId") userId: String): Call<Void>

    @PUT("api/update-user")
    fun updateUser(@Body request: SignupRequest): Call<Map<String, String>>

    @POST("api/login")  // 서버 API에 맞게 경로 수정하세요
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}
