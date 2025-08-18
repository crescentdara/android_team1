package bitc.fullstack502.android_studio

/** PUT /api/users 요청 바디 (비밀번호 제외) */
data class UpdateUserRequest(
    val usersId: String,
    val name: String,
    val email: String,
    val phone: String
)
