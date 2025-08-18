// EditInfoActivity.kt
package bitc.fullstack502.android_studio.ui.mypage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.android_studio.*
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

            val sp = getSharedPreferences("userInfo", MODE_PRIVATE)
            val usersId = sp.getString("usersId", "") ?: ""

            if (usersId.isBlank()) {
                Toast.makeText(this, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = UpdateUserRequest(
                usersId = usersId,
                name = name,
                email = email,
                phone = phone
            )

            // ✅ V2 엔드포인트 사용
            ApiProvider.api.updateUserV2(request).enqueue(object : Callback<UsersResponse> {
                override fun onResponse(call: Call<UsersResponse>, response: Response<UsersResponse>) {
                    if (response.isSuccessful) {
                        val resultIntent = Intent().apply {
                            putExtra("name", name)
                            putExtra("email", email)
                            putExtra("phone", phone)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    } else {
                        Toast.makeText(this@EditInfoActivity, "업데이트 실패 (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<UsersResponse>, t: Throwable) {
                    Toast.makeText(this@EditInfoActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
