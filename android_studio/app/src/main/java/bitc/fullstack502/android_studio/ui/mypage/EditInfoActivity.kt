package bitc.fullstack502.android_studio.ui.mypage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.android_studio.SignupRequest
import bitc.fullstack502.android_studio.network.ApiProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import bitc.fullstack502.android_studio.R

class EditInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_info)

        val etName = findViewById<EditText>(R.id.et_name)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPhone = findViewById<EditText>(R.id.et_phone)
        val btnSave = findViewById<Button>(R.id.btn_save)

        // 기존 정보 세팅
        etName.setText(intent.getStringExtra("name") ?: "")
        etEmail.setText(intent.getStringExtra("email") ?: "")
        etPhone.setText(intent.getStringExtra("phone") ?: "")

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val phone = etPhone.text.toString()

            val sharedPref = getSharedPreferences("userInfo", MODE_PRIVATE)
            val userId = sharedPref.getString("userId", "") ?: ""
            val pass = sharedPref.getString("pass", "") ?: ""

            if (userId.isBlank() || pass.isBlank()) {
                Toast.makeText(this, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ 파라미터 순서 수정
            val request = SignupRequest(
                usersId = userId,
                email = email,
                pass = pass,
                name = name,
                phone = phone
            )

            val api = ApiProvider.api
            api.updateUser(request).enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(
                    call: Call<Map<String, String>>,
                    response: Response<Map<String, String>>
                ) {
                    if (response.isSuccessful) {
                        val resultIntent = Intent().apply {
                            putExtra("name", name)
                            putExtra("email", email)
                            putExtra("phone", phone)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    } else {
                        Toast.makeText(this@EditInfoActivity, "업데이트 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    Toast.makeText(this@EditInfoActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
