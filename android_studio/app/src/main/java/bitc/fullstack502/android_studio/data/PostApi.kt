package bitc.fullstack502.android_studio.data

import retrofit2.http.*

data class IdResponse(val id: Long)
data class LikeResponse(val liked: Boolean)

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean
)

data class PostListItem(
    val id: Long,
    val title: String,
    val authorName: String?,
    val lookCount: Long,
    val createdAt: String
)

data class PostCreate(val userId: Long, val title: String, val content: String?, val img: String?)
data class PostUpdate(val title: String, val content: String?, val img: String?)

interface PostApi {
    @GET("/api/posts")
    suspend fun list(
        @Query("q") q: String = "",
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PageResponse<PostListItem>

    @POST("/api/posts")
    suspend fun create(@Body body: PostCreate): IdResponse
}
