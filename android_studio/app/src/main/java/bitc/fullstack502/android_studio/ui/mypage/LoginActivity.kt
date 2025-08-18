package bitc.fullstack502.android_studio.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.android_studio.LoginRequest
import bitc.fullstack502.android_studio.LoginResponse
import bitc.fullstack502.android_studio.RetrofitClient
import bitc.fullstack502.android_studio.ui.mypage.ui.mypage.FindIdPwActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUserId = findViewById<EditText>(R.id.et_user_id)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)

        // 회원가입 텍스트뷰
        val tvSignup = findViewById<TextView>(R.id.tv_signup)
        tvSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // 아이디/비밀번호 찾기 텍스트뷰 클릭시 find_id_pw 액티비티로 이동
        val tvFindInfo = findViewById<TextView>(R.id.tv_find_info)
        tvFindInfo.setOnClickListener {
            val intent = Intent(this, FindIdPwActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val userId = etUserId.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (userId.isBlank() || password.isBlank()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(userId, password)
            RetrofitClient.apiService.login(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        if (user != null) {
                            Toast.makeText(this@LoginActivity, "${user.name}님 환영합니다!", Toast.LENGTH_SHORT).show()

                            // SharedPreferences에 로그인 정보 저장
                            val sharedPref = getSharedPreferences("userInfo", MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("userId", user.userId)
                                putString("pass", password)
                                putString("name", user.name)
                                putString("email", user.email)
                                putString("phone", user.phone)
                                apply()
                            }

                            // 다음 화면으로 이동
                            val intent = Intent(this@LoginActivity, MyPage::class.java)
                            intent.putExtra("userId", user.userId)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "서버 응답 오류: 데이터 없음", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "로그인 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        Log.e("Login", "errorBody: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "서버 연결 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Login", "onFailure: ${t.message}")
                }
            })
        }
    }
}
