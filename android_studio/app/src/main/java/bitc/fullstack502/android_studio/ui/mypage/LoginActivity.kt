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

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        // ✅ 회원가입 이동
        b.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // ✅ 아이디/비번 찾기 이동 (통합 화면 쓰는 경우)
        b.tvFindInfo.setOnClickListener {
            startActivity(Intent(this, FindIdPwActivity::class.java))
            // 만약 화면을 따로 쓰면 다음처럼 분기해서 사용:
            // startActivity(Intent(this, FindIdActivity::class.java))
            // startActivity(Intent(this, FindPwActivity::class.java))
        }

        b.btnLogin.setOnClickListener {
            val usersId = b.etUsersId.text.toString()
            val pass = b.etPass.text.toString()

            val req = LoginRequest(usersId, pass)

            ApiProvider.api.login(req).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!
                        AuthManager.saveLogin(
                            userPk = data.id,
                            usersId = data.usersId,
                            name = data.name,
                            email = data.email,
                            phone = data.phone,
                            accessToken = "dummy"
                        )
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
