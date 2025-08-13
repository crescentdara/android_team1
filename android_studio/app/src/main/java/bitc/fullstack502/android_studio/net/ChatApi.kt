package bitc.fullstack502.android_studio.net

import bitc.fullstack502.android_studio.model.ChatMessage
import bitc.fullstack502.android_studio.model.ConversationSummary
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface ChatApi {
    @GET("/api/chat/conversations")
    suspend fun conversations(@Query("userId") userId: String): List<ConversationSummary>

    @GET("/api/chat/history")
    suspend fun history(
        @Query("roomId") roomId: String,
        @Query("size") size: Int = 50
    ): List<ChatMessage>

    @PUT("/api/chat/read")
    suspend fun markRead(
        @Query("roomId") roomId: String,
        @Query("userId") userId: String
    ): Response<Unit>   // ✅ 바디 없는 응답 허용
}