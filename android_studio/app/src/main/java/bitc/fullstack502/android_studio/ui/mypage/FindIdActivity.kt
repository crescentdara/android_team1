package bitc.fullstack502.android_studio.ui.mypage

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.android_studio.FindIdRequest
import bitc.fullstack502.android_studio.FindIdResponse
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.network.ApiProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FindIdActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var btnFind: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_id)

        // ⚠️ XML에 아래 id가 있어야 합니다: et_email, et_pass, btn_find_id
        etEmail = findViewById(R.id.et_email)
        etPass  = findViewById(R.id.et_pass)
        btnFind = findViewById(R.id.btn_find_id)

        btnFind.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass  = etPass.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 모두 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            findUserIdByEmailAndPass(email, pass)
        }
    }

    private fun findUserIdByEmailAndPass(email: String, pass: String) {
        // Retrofit 호출
        val api = ApiProvider.api
        val req = FindIdRequest(email = email, pass = pass)

        api.findUsersId(req).enqueue(object : Callback<FindIdResponse> {
            override fun onResponse(
                call: Call<FindIdResponse>,
                response: Response<FindIdResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.usersId.isNotBlank()) {
                        showUserIdDialog(body.usersId)
                    } else {
                        Toast.makeText(this@FindIdActivity, "일치하는 회원이 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    when (response.code()) {
                        401, 404 -> Toast.makeText(this@FindIdActivity, "이메일 또는 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                        else ->  Toast.makeText(this@FindIdActivity, "요청 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onFailure(call: Call<FindIdResponse>, t: Throwable) {
                Toast.makeText(this@FindIdActivity, "네트워크 오류: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showUserIdDialog(usersId: String) {
        AlertDialog.Builder(this)
            .setTitle("아이디 찾기 결과")
            .setMessage("회원님의 아이디는 \"$usersId\" 입니다.")
            .setPositiveButton("확인") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
