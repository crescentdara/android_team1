package bitc.fullstack502.android_studio.data

class PostRepository(private val api: PostApi = RetrofitProvider.postApi) {
    suspend fun getPosts(q: String = "", page: Int = 0, size: Int = 10) =
        api.list(q, page, size)

    suspend fun createPost(userId: Long, title: String, content: String?) =
        api.create(PostCreate(userId, title, content, null))
}
