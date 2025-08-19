package bitc.fullstack502.android_studio

data class LoginResponse(
    val id: Long,               // ✅ 숫자 PK
    val usersId: String,
    val name: String,
    val email: String,
    val phone: String,
    val accessToken: String?    // ✅ JWT/세션 토큰
)
