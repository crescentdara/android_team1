package bitc.fullstack502.android_studio.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.android_studio.LoginRequest
import bitc.fullstack502.android_studio.LoginResponse
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.network.ApiProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsersId = findViewById<EditText>(R.id.et_users_id)
        val etPass    = findViewById<EditText>(R.id.et_pass)
        val btnLogin  = findViewById<Button>(R.id.btn_login)
        val tvError   = findViewById<TextView>(R.id.tv_error) // 레이아웃에 존재해야 함

        // 회원가입
        val tvSignup = findViewById<TextView>(R.id.tv_signup)
        tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // 아이디/비밀번호 찾기
        val tvFindInfo = findViewById<TextView>(R.id.tv_find_info)
        tvFindInfo.setOnClickListener {
            startActivity(Intent(this, FindIdPwActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val usersId = etUsersId.text.toString().trim()
            val pass    = etPass.text.toString().trim()

            // 클릭할 때마다 에러 숨김
            tvError.visibility = View.GONE
            tvError.text = ""

            if (usersId.isBlank() || pass.isBlank()) {
                tvError.text = "아이디와 비밀번호를 입력해주세요."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            val request = LoginRequest(usersId, pass)
            ApiProvider.api.login(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        if (user != null) {
                            Toast.makeText(
                                this@LoginActivity,
                                "${user.name}님 환영합니다!",
                                Toast.LENGTH_SHORT
                            ).show()

                            // 로그인 정보 저장
                            val sp = getSharedPreferences("userInfo", MODE_PRIVATE)
                            with(sp.edit()) {
                                putString("usersId", user.usersId)
                                putString("name", user.name)
                                putString("email", user.email)
                                putString("phone", user.phone)
                                apply()
                            }

                            // 마이페이지로 이동
                            startActivity(
                                Intent(this@LoginActivity, MyPageActivity::class.java)
                                    .putExtra("usersId", user.usersId)
                            )
                            finish()
                        } else {
                            // 성공인데 바디 없음 → 실패로 간주
                            tvError.text = "아이디 혹은 비밀번호가 일치하지 않습니다."
                            tvError.visibility = View.VISIBLE
                        }
                    } else {
                        // 401/403 등
                        tvError.text = "아이디 혹은 비밀번호가 일치하지 않습니다."
                        tvError.visibility = View.VISIBLE
                        Log.e("Login", "code=${response.code()}, error=${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(
                        this@LoginActivity,
                        "서버 연결 실패: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("Login", "onFailure: ${t.message}")
                }
            })
        }
    }
}
