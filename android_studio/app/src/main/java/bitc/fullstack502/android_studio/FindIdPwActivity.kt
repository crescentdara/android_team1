package bitc.fullstack502.android_studio

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import bitc.fullstack502.android_studio.network.RetrofitClient

class FindIdPwActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_id_pw)

        val tabId = findViewById<TextView>(R.id.tab_id)
        val tabPw = findViewById<TextView>(R.id.tab_pw)

        val layoutIdFind = findViewById<LinearLayout>(R.id.layout_id_find)
        val layoutPwFind = findViewById<LinearLayout>(R.id.layout_pw_find)

        tabId.setOnClickListener {
            layoutIdFind.visibility = LinearLayout.VISIBLE
            layoutPwFind.visibility = LinearLayout.GONE
            tabId.isEnabled = false
            tabPw.isEnabled = true
        }

        tabPw.setOnClickListener {
            layoutIdFind.visibility = LinearLayout.GONE
            layoutPwFind.visibility = LinearLayout.VISIBLE
            tabId.isEnabled = true
            tabPw.isEnabled = false
        }

        // 아이디 찾기
        val etEmailForId = findViewById<EditText>(R.id.et_email_for_id)
        val etPassForId = findViewById<EditText>(R.id.et_pass_for_id)

        findViewById<Button>(R.id.btn_find_id).setOnClickListener {
            val email = etEmailForId.text.toString()
            val password = etPassForId.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = FindIdRequest(email, password)

            RetrofitClient.userApiService.findUserId(request).enqueue(object : Callback<FindIdResponse> {
                override fun onResponse(call: Call<FindIdResponse>, response: Response<FindIdResponse>) {
                    if (response.isSuccessful) {
                        val userId = response.body()?.userId
                        if (userId != null) {
                            Toast.makeText(this@FindIdPwActivity, "당신의 아이디는: $userId", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@FindIdPwActivity, "아이디를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("FindIdPwActivity", "userId is null in successful response")
                        }
                    } else {
                        val errorMsg = response.errorBody()?.string()
                        Toast.makeText(this@FindIdPwActivity, "아이디를 찾을 수 없습니다. 에러: $errorMsg", Toast.LENGTH_SHORT).show()
                        Log.e("FindIdPwActivity", "Failed findUserId response: $errorMsg")
                    }
                }

                override fun onFailure(call: Call<FindIdResponse>, t: Throwable) {
                    Toast.makeText(this@FindIdPwActivity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("FindIdPwActivity", "findUserId onFailure: ${t.message}", t)
                }
            })
        }

        // 비밀번호 찾기
        val etUserIdForPw = findViewById<EditText>(R.id.et_userid_for_pw)
        val etEmailForPw = findViewById<EditText>(R.id.et_email_for_pw)

        findViewById<Button>(R.id.btn_find_pw).setOnClickListener {
            val userId = etUserIdForPw.text.toString()
            val email = etEmailForPw.text.toString()

            if (userId.isBlank() || email.isBlank()) {
                Toast.makeText(this, "아이디와 이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = FindPasswordRequest(userId, email)

            RetrofitClient.userApiService.findUserPassword(request).enqueue(object : Callback<FindPasswordResponse> {
                override fun onResponse(call: Call<FindPasswordResponse>, response: Response<FindPasswordResponse>) {
                    if (response.isSuccessful) {
                        val password = response.body()?.password
                        if (password != null) {
                            Toast.makeText(this@FindIdPwActivity, "비밀번호: $password", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@FindIdPwActivity, "비밀번호를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("FindIdPwActivity", "password is null in successful response")
                        }
                    } else {
                        val errorMsg = response.errorBody()?.string()
                        Toast.makeText(this@FindIdPwActivity, "비밀번호를 찾을 수 없습니다. 에러: $errorMsg", Toast.LENGTH_SHORT).show()
                        Log.e("FindIdPwActivity", "Failed findUserPassword response: $errorMsg")
                    }
                }

                override fun onFailure(call: Call<FindPasswordResponse>, t: Throwable) {
                    Toast.makeText(this@FindIdPwActivity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("FindIdPwActivity", "findUserPassword onFailure: ${t.message}", t)
                }
            })
        }
    }
}
