package bitc.fullstack502.android_studio.ui.mypage

import android.R.attr.password
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.android_studio.LoginRequest
import bitc.fullstack502.android_studio.LoginResponse
import bitc.fullstack502.android_studio.network.ApiProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import bitc.fullstack502.android_studio.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsersId = findViewById<EditText>(R.id.et_users_id)
        val etPass = findViewById<EditText>(R.id.et_pass)
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
            val usersId = etUsersId.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (usersId.isBlank() || pass.isBlank()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(usersId, pass)
            ApiProvider.api.login(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        if (user != null) {
                            Toast.makeText(this@LoginActivity, "${user.name}님 환영합니다!", Toast.LENGTH_SHORT).show()

                            // SharedPreferences에 로그인 정보 저장
                            val sharedPref = getSharedPreferences("userInfo", MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("usersId", user.usersId)
                                putString("name", user.name)
                                putString("email", user.email)
                                putString("phone", user.phone)
                                apply()
                            }

                            // 다음 화면으로 이동
                            val intent = Intent(this@LoginActivity, MyPageActivity::class.java)
                            intent.putExtra("usersId", user.usersId)
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
