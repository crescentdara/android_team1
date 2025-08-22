package bitc.fullstack502.android_studio.network

import bitc.fullstack502.android_studio.CheckIdResponse
import bitc.fullstack502.android_studio.FindIdRequest
import bitc.fullstack502.android_studio.FindIdResponse
import bitc.fullstack502.android_studio.FindPasswordRequest
import bitc.fullstack502.android_studio.FindPasswordResponse
import bitc.fullstack502.android_studio.LoginRequest
import bitc.fullstack502.android_studio.LoginResponse
import bitc.fullstack502.android_studio.SignupRequest
import bitc.fullstack502.android_studio.UpdateUserRequest
import bitc.fullstack502.android_studio.UsersResponse
import bitc.fullstack502.android_studio.model.ChatMessage
import bitc.fullstack502.android_studio.model.ConversationSummary
import bitc.fullstack502.android_studio.model.LodgingItem
import bitc.fullstack502.android_studio.model.Flight
import bitc.fullstack502.android_studio.model.BookingRequest
import bitc.fullstack502.android_studio.model.BookingResponse
import bitc.fullstack502.android_studio.network.dto.*
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
    ): Response<Unit>

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
    fun detail(
        @Path("id") id: Long,
        @Header("X-USER-ID") usersId: String? = null
    ): Call<PostDto>

    @Multipart
    @POST("/api/posts")
    fun create(
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Part image: MultipartBody.Part?,
        @Header("X-USER-ID") usersId: String? = null
    ): Call<Long>

    @Multipart
    @PUT("/api/posts/{id}")
    fun update(
        @Path("id") id: Long,
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Part image: MultipartBody.Part?,
        @Header("X-USER-ID") usersId: String? = null
    ): Call<Void>

    @POST("/api/posts/{id}/like")
    fun toggleLike(
        @Path("id") id: Long,
        @Header("X-USER-ID") usersId: String? = null
    ): Call<Long>

    @GET("/api/comments/{postId}")
    fun comments(@Path("postId") postId: Long): Call<List<CommDto>>

    @FormUrlEncoded
    @POST("/api/comments")
    fun writeComment(
        @Field("postId") postId: Long,
        @Field("parentId") parentId: Long?,
        @Field("content") content: String,
        @Header("X-USER-ID") usersId: String? = null
    ): Call<Long>

    @FormUrlEncoded
    @PUT("/api/comments/{id}")
    fun editComment(
        @Path("id") id: Long,
        @Field("content") content: String,
        @Header("X-USER-ID") usersId: String? = null
    ): Call<Void>

    @DELETE("/api/comments/{id}")
    fun deleteComment(
        @Path("id") id: Long,
        @Header("X-USER-ID") usersId: String? = null
    ): Call<Void>

    @DELETE("/api/posts/{id}")
    fun deletePost(
        @Path("id") id: Long,
        @Header("X-USER-ID") usersId: String? = null
    ): Call<Void>

    @GET("/api/posts/search")
    fun searchPosts(
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
    @GET("/api/lodging/{id}/wish")
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
    fun findUsersId(@Body request: FindIdRequest): Call<FindIdResponse>

    @POST("/api/find-password")
    fun findUserPassword(@Body request: FindPasswordRequest): Call<FindPasswordResponse>

    @GET("/api/user-info")
    fun getUserInfo(@Query("userId") userId: String): Call<SignupRequest>

    @DELETE("/api/delete-user")
    fun deleteUser(@Query("userId") userId: String): Call<Void>

    @PUT("/api/update-user")
    fun updateUser(@Body request: SignupRequest): Call<Map<String, String>>

    // 신규 V2
    @POST("/api/users/register")
    fun registerUserV2(@Body request: SignupRequest): Call<Void>

    @GET("/api/users/check-id")
    fun checkIdV2(@Query("usersId") usersId: String): Call<CheckIdResponse>

    @GET("/api/users/{usersId}")
    fun getUserInfoV2(@Path("usersId") usersId: String): Call<UsersResponse>

    @PUT("/api/users")
    fun updateUserV2(@Body request: UpdateUserRequest): Call<UsersResponse>

    @DELETE("/api/users/{usersId}")
    fun deleteUserV2(@Path("usersId") usersId: String): Call<Void>

    // ---------- 마이페이지 ----------
    @GET("/api/mypage/posts")
    suspend fun getMyPosts(@Query("userPk") userPk: Long): List<PostDto>

    @GET("/api/mypage/comments")
    suspend fun getMyComments(@Query("userPk") userPk: Long): List<CommentDto>

    @GET("/api/mypage/liked-posts")
    suspend fun getLikedPosts(@Query("userPk") userPk: Long): List<PostDto>

    @GET("/api/mypage/lodging/wishlist")
    suspend fun getLodgingWishlist(
        @Query("userId") userId: Long
    ): List<LodgingListDto>

    // ApiProvider.kt (인터페이스 일부)
//    @GET("/api/mypage/flight-bookings")
//    suspend fun getFlightBookings(@Query("userPk") userPk: Long): List<BookingResponse>

    // 사용자 예약 목록
    @GET("/api/mypage/flight/bookings")
    suspend fun getFlightBookings(@Query("userPk") userPk: Long): List<BookingResponse>

    // 예약 단건 상세
    @GET("/api/bookings/flight/{id}")
    suspend fun getFlightBooking(@Path("id") bookingId: Long): BookingResponse

    // 항공편 단건
    @GET("/api/flights/{id}")
    suspend fun getFlight(@Path("id") id: Long): Flight

    @GET("/api/mypage/flight-wishlist")
    suspend fun getFlightWishlist(@Query("userPk") userPk: Long): List<FlightWishDto>


    @GET("/api/mypage/lodging/bookings")
    suspend fun getLodgingBookings(
        @Query("userId") userId: Long
    ): List<LodgingBookingDto>

    /* ---------- 항공 검색 ---------- */
    @GET("/api/flight/search")
    fun searchFlights(
        @Query("dep") dep: String,
        @Query("arr") arr: String,
        @Query("date") date: String,        // yyyy-MM-dd
        @Query("depTime") depTime: String? = null
    ): Call<List<Flight>>

    /* ---------- 항공 예약 ---------- */
    @POST("/api/bookings/flight")
    fun createFlightBooking(@Body req: BookingRequest): Call<BookingResponse>


    /* ---------- 항공 즐겨찾기 ---------- */
    @GET("/api/flight/{flightId}/wish/status")
    fun getFlightWishStatus(
        @Path("flightId") flightId: Long,
        @Header("X-USER-ID") userId: Long
    ): Call<WishStatusDto>


    @PUT("/api/flight/{flightId}/wish")
    fun toggleFlightWish(
        @Path("flightId") flightId: Long,
        @Header("X-USER-ID") userId: Long
    ): Call<WishStatusDto>

    @POST("/api/bookings/flight/{id}/cancel")
    suspend fun cancelBooking(@Path("id") bookingId: Long): Response<Void>


}
