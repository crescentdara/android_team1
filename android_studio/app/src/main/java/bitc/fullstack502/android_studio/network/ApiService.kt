package bitc.fullstack502.android_studio.network

import bitc.fullstack502.android_studio.network.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET("/api/posts")
    fun list(@Query("page") page:Int = 0, @Query("size") size:Int = 20): Call<PagePostDto>

    @GET("/api/posts/{id}")
    fun detail(@Path("id") id:Long): Call<PostDto>

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
    fun toggleLike(@Path("id") id:Long): Call<Long>

    @GET("/api/comments/{postId}")
    fun comments(@Path("postId") postId:Long): Call<List<CommDto>>

    @FormUrlEncoded
    @POST("/api/comments")
    fun writeComment(
        @Field("postId") postId:Long,
        @Field("parentId") parentId:Long?,
        @Field("content") content:String
    ): Call<Long>

    @FormUrlEncoded
    @PUT("/api/comments/{id}")
    fun editComment(@Path("id") id:Long, @Field("content") content:String): Call<Void>

    @DELETE("/api/comments/{id}")
    fun deleteComment(@Path("id") id:Long): Call<Void>

    @DELETE("/api/posts/{id}")
    fun deletePost(@Path("id") id: Long): Call<Void>

    @GET("/api/posts/search")
    fun search(
        @Query("field") field: String,
        @Query("q") q: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Call<PagePostDto>

}
