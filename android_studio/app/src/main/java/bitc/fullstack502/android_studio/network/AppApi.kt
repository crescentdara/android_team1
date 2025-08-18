package bitc.fullstack502.android_studio.network

import bitc.fullstack502.android_studio.CheckIdResponse
import bitc.fullstack502.android_studio.FindIdRequest
import bitc.fullstack502.android_studio.FindIdResponse
import bitc.fullstack502.android_studio.FindPasswordRequest
import bitc.fullstack502.android_studio.FindPasswordResponse
import bitc.fullstack502.android_studio.LoginRequest
import bitc.fullstack502.android_studio.LoginResponse
import bitc.fullstack502.android_studio.SignupRequest
import bitc.fullstack502.android_studio.model.ChatMessage
import bitc.fullstack502.android_studio.model.ConversationSummary
import bitc.fullstack502.android_studio.model.LodgingItem
import bitc.fullstack502.android_studio.network.dto.*
import bitc.fullstack502.android_studio.network.dto.AvailabilityDto
import bitc.fullstack502.android_studio.network.dto.NaverLocalResp
import bitc.fullstack502.android_studio.network.dto.PagePostDto
import bitc.fullstack502.android_studio.network.dto.PostDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

/** 모든 엔드포인트 통합 인터페이스 */
interface AppApi {

    // ---------- Chat ----------
    @GET("/api/chat/conversations")
    suspend fun conversations(@Query("userId") userId: String): List<ConversationSummary>

    @GET("/api/chat/history")
    suspend fun history(
        @Query("roomId") roomId: String,
        @Query("size") size: Int,
        @Query("beforeId") beforeId: Long?,
        @Query("me") me: String,
        @Query("other") other: String
    ): List<ChatMessage>

    @PUT("/api/chat/read")
    suspend fun markRead(
        @Query("roomId") roomId: String,
        @Query("userId") userId: String
    ): Response<Unit>   // body 없는 200

    // ---------- Naver Local (서버 프록시) ----------
    @GET("/api/naver/local/nearby")
    fun nearby(
        @Query("query") query: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radius") radius: Int = 1000,
        @Query("size") size: Int = 30
    ): Call<NaverLocalResp>

    // ---------- 게시글 / 댓글 ----------
    @GET("/api/posts")
    fun list(@Query("page") page: Int = 0, @Query("size") size: Int = 20): Call<PagePostDto>

    @GET("/api/posts/{id}")
    fun detail(@Path("id") id: Long): Call<PostDto>

    @Multipart
    @POST("/api/posts")
    fun create(
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Part image: MultipartBody.Part?
    ): Call<Long>

    @Multipart
    @PUT("/api/posts/{id}")
    fun update(
        @Path("id") id: Long,
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Part image: MultipartBody.Part?
    ): Call<Void>

    @POST("/api/posts/{id}/like")
    fun toggleLike(@Path("id") id: Long): Call<Long>

    @GET("/api/comments/{postId}")
    fun comments(@Path("postId") postId: Long): Call<List<CommDto>>

    @FormUrlEncoded
    @POST("/api/comments")
    fun writeComment(
        @Field("postId") postId: Long,
        @Field("parentId") parentId: Long?,
        @Field("content") content: String
    ): Call<Long>

    @FormUrlEncoded
    @PUT("/api/comments/{id}")
    fun editComment(@Path("id") id: Long, @Field("content") content: String): Call<Void>

    @DELETE("/api/comments/{id}")
    fun deleteComment(@Path("id") id: Long): Call<Void>

    @DELETE("/api/posts/{id}")
    fun deletePost(@Path("id") id: Long): Call<Void>

    @GET("/api/posts/search")
    fun search(
        @Query("field") field: String,
        @Query("q") q: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Call<PagePostDto>

    // ---------- 지역 ----------
    @GET("/api/locations/cities")
    suspend fun getCities(): List<String>

    @GET("/api/locations/towns")
    suspend fun getTowns(@Query("city") city: String): List<String>

    @GET("/api/locations/vills")
    suspend fun getVills(@Query("city") city: String, @Query("town") town: String): List<String>

    // ---------- 숙소 목록/예약 ----------
    @GET("/api/lodgings")
    suspend fun getLodgings(
        @Query("city") city: String?,
        @Query("town") town: String?,
        @Query("vill") vill: String?,
        @Query("checkIn") checkIn: String?,
        @Query("checkOut") checkOut: String?,
        @Query("adults") adults: Int?,
        @Query("children") children: Int?
    ): List<LodgingItem>

    @POST("/api/lodging/book")
    fun createBooking(@Body booking: LodgingBookingDto): Call<Void>

    // ---------- 숙소 상세/재고/결제사전요청 ----------
    @GET("/api/lodging/{id}/detail")
    fun getDetail(@Path("id") id: Long): Call<LodgingDetailDto>

    @GET("/api/lodging/{id}/availability")
    fun getAvailability(
        @Path("id") id: Long,
        @Query("checkIn") checkIn: String,
        @Query("checkOut") checkOut: String,
        @Query("guests") guests: Int? = null
    ): Call<AvailabilityDto>

    @POST("/api/lodging/{id}/prepay")
    fun prepay(@Path("id") id: Long, @Body body: Map<String, Any>): Call<Map<String, Any>>

    // ---------- 숙소 찜 ----------
    @GET("/api/lodging/{id}/wish/status")
    fun wishStatus(@Path("id") lodgingId: Long, @Query("userId") userId: Long): Call<LodgingWishStatusDto>

    @POST("/api/lodging/{id}/wish/toggle")
    fun wishToggle(@Path("id") lodgingId: Long, @Query("userId") userId: Long): Call<LodgingWishStatusDto>

    // ---------- 회원/인증 ----------
    @POST("/api/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/api/signup")
    fun registerUser(@Body request: SignupRequest): Call<Void>

    @GET("/api/checkId")
    fun checkId(@Query("id") id: String): Call<CheckIdResponse>

    @POST("/api/find-id")
    fun findUserId(@Body request: FindIdRequest): Call<FindIdResponse>

    @POST("/api/find-password")
    fun findUserPassword(@Body request: FindPasswordRequest): Call<FindPasswordResponse>

    @GET("/api/user-info")
    fun getUserInfo(@Query("userId") userId: String): Call<SignupRequest>

    @DELETE("/api/delete-user")
    fun deleteUser(@Query("userId") userId: String): Call<Void>

    @PUT("/api/update-user")
    fun updateUser(@Body request: SignupRequest): Call<Map<String, String>>
}
