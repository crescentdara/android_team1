package bitc.fullstack502.android_studio.util

import android.content.Context

private const val SP_NAME = "userInfo"

/** 로그인 여부: usersId 가 저장되어 있으면 true */
fun Context.isLoggedIn(): Boolean =
    !getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        .getString("usersId", "").isNullOrBlank()

/** 백엔드 PK 저장 시 사용(없으면 0L 반환). 없으면 0L이므로 서버에서 거부됨 */
fun Context.userPkOrZero(): Long =
    getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        .getLong("userPk", 0L)

/** 서버 상대경로 -> 절대경로 */
fun fullUrl(path: String?): String? {
    if (path.isNullOrBlank()) return null
    if (path.startsWith("http", ignoreCase = true)) return path
    val p = if (path.startsWith("/")) path else "/$path"
    return "http://10.0.2.2:8080$p"
}
