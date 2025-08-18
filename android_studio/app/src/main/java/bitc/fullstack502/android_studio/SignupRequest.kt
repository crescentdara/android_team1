package bitc.fullstack502.android_studio

data class SignupRequest(
    val usersId: String,
    val email: String,
    val pass: String,
    val name: String,
    val phone: String
)