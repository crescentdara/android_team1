package bitc.fullstack502.android_studio.util

import android.content.Context
import android.content.SharedPreferences

object AuthManager {
    private const val SP_NAME = "userInfo"
    private const val K_USER_PK = "userPk"
    private const val K_USERS_ID = "usersId"
    private const val K_NAME = "name"
    private const val K_EMAIL = "email"
    private const val K_PHONE = "phone"
    private const val K_TOKEN = "accessToken"

    private lateinit var sp: SharedPreferences

    fun init(context: Context) {
        if (!::sp.isInitialized) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        }
    }

    fun saveLogin(
        userPk: Long,
        usersId: String,
        name: String?,
        email: String?,
        phone: String?,
        accessToken: String?
    ) {
        sp.edit().apply {
            putLong(K_USER_PK, userPk)
            putString(K_USERS_ID, usersId)
            putString(K_NAME, name)
            putString(K_EMAIL, email)
            putString(K_PHONE, phone)
            putString(K_TOKEN, accessToken)
        }.apply()
    }

    fun logout() {
        sp.edit().clear().apply()
    }

    // ✅ 토큰 없어도 로그인으로 인정
    fun isLoggedIn(): Boolean = id() > 0

    fun id(): Long = sp.getLong(K_USER_PK, 0L)
    fun usersId(): String = sp.getString(K_USERS_ID, "") ?: ""
    fun name(): String = sp.getString(K_NAME, "") ?: ""
    fun email(): String = sp.getString(K_EMAIL, "") ?: ""
    fun phone(): String = sp.getString(K_PHONE, "") ?: ""
    fun accessToken(): String? = sp.getString(K_TOKEN, null)
}
