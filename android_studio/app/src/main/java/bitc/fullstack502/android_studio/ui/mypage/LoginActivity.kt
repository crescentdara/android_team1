package bitc.fullstack502.android_studio.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.android_studio.databinding.ActivityLoginBinding
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.LoginRequest
import bitc.fullstack502.android_studio.LoginResponse
import bitc.fullstack502.android_studio.ui.MainActivity
import bitc.fullstack502.android_studio.util.AuthManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.jvm.java

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnLogin.setOnClickListener {
            val usersId = b.etUsersId.text.toString()
            val pass = b.etPass.text.toString()

            val req = LoginRequest(usersId, pass)

            ApiProvider.api.login(req).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!

                        // ✅ 로그인 정보 저장
                        AuthManager.saveLogin(
                            userPk = data.id,
                            usersId = data.usersId,
                            name = data.name,
                            email = data.email,
                            phone = data.phone,
                            accessToken = "dummy"
                        )

                        // ✅ 메인으로 이동
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "아이디/비밀번호 오류", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
