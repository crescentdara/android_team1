package bitc.fullstack502.android_studio

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import bitc.fullstack502.android_studio.network.RetrofitClient

class FindIdActivity : AppCompatActivity() {

    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var findIdBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_id)

        emailEt = findViewById(R.id.et_email)
        findIdBtn = findViewById(R.id.btn_find_id)

        findIdBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 모두 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            findUserIdByEmailAndPassword(email, password)
        }
    }

    private fun findUserIdByEmailAndPassword(email: String, password: String) {
        val api = RetrofitClient.userApiService  // RetrofitClient에서 UserApi 인스턴스 가져오기

        val request = FindIdRequest(email, password)

        api.findUserId(request).enqueue(object : Callback<FindIdResponse> {
            override fun onResponse(call: Call<FindIdResponse>, response: Response<FindIdResponse>) {
                if (response.isSuccessful) {
                    val userId = response.body()?.userId
                    if (!userId.isNullOrEmpty()) {
                        showUserIdDialog(userId)
                    } else {
                        Toast.makeText(this@FindIdActivity, "일치하는 회원이 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@FindIdActivity, "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FindIdResponse>, t: Throwable) {
                Toast.makeText(this@FindIdActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showUserIdDialog(userId: String) {
        AlertDialog.Builder(this)
            .setTitle("아이디 찾기 결과")
            .setMessage("회원님의 아이디는 \"$userId\" 입니다.")
            .setPositiveButton("확인") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}