package bitc.fullstack502.android_studio.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bitc.fullstack502.android_studio.RetrofitClient
import bitc.fullstack502.android_studio.SignupRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPage : AppCompatActivity() {

    private lateinit var tvUserId: TextView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView

    private var userId: String = ""

    private val editInfoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val newName = data?.getStringExtra("name") ?: ""
            val newEmail = data?.getStringExtra("email") ?: ""
            val newPhone = data?.getStringExtra("phone") ?: ""

            tvName.text = "이름: $newName"
            tvEmail.text = "Email: $newEmail"
            tvPhone.text = "전화번호: $newPhone"

            val sharedPref = getSharedPreferences("userInfo", MODE_PRIVATE)
            val pass = sharedPref.getString("pass", "") ?: ""

            // 업데이트된 정보 저장
            with(sharedPref.edit()) {
                putString("name", newName)
                putString("email", newEmail)
                putString("phone", newPhone)
                apply()
            }

            // ✅ 서버에 정보 업데이트 요청
            val updatedUser = SignupRequest(
                usersId = userId,
                name = newName,
                email = newEmail,
                phone = newPhone,
                pass = pass
            )

            val api = RetrofitClient.apiService
            api.updateUser(request = updatedUser).enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MyPage, "회원 정보가 성공적으로 수정되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MyPage, "서버 오류로 정보 수정에 실패했습니다. (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    Toast.makeText(this@MyPage, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    t.printStackTrace()
                }
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        val rootView = findViewById<View>(R.id.main)
        rootView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        tvUserId = findViewById(R.id.tv_user_id)
        tvName = findViewById(R.id.tv_name)
        tvEmail = findViewById(R.id.tv_email)
        tvPhone = findViewById(R.id.tv_phone)

        val appLogoTextView = findViewById<TextView>(R.id.tv_app_logo)
        appLogoTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        userId = intent.getStringExtra("userId") ?: ""
        if (userId.isBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        tvUserId.text = "아이디: $userId"

        loadUserInfoFromServer(userId)

        val btnEditInfo = findViewById<Button>(R.id.btn_edit_info)
        btnEditInfo.setOnClickListener {
            val sharedPref = getSharedPreferences("userInfo", MODE_PRIVATE)
            val name = sharedPref.getString("name", "")
            val email = sharedPref.getString("email", "")
            val phone = sharedPref.getString("phone", "")

            val intent = Intent(this, EditInfoActivity::class.java).apply {
                putExtra("name", name)
                putExtra("email", email)
                putExtra("phone", phone)
            }
            editInfoLauncher.launch(intent)
        }

        val btnLogout = findViewById<Button>(R.id.btn_logout)
        btnLogout.setOnClickListener {
            val sharedPref = getSharedPreferences("userInfo", MODE_PRIVATE)
            with(sharedPref.edit()) {
                clear()
                apply()
            }
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        val btnDeleteAccount = findViewById<Button>(R.id.btn_delete_account)
        btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("회원 탈퇴")
                .setMessage("정말 탈퇴하시겠습니까? 탈퇴 시 모든 데이터가 삭제됩니다.")
                .setPositiveButton("확인") { dialog, _ ->
                    dialog.dismiss()
                    deleteUserAccount()
                }
                .setNegativeButton("취소") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun loadUserInfoFromServer(userId: String) {
        val api = RetrofitClient.apiService
        api.getUserInfo(userId).enqueue(object : Callback<SignupRequest> {
            override fun onResponse(call: Call<SignupRequest>, response: Response<SignupRequest>) {
                if (response.isSuccessful) {
                    val userInfo = response.body()
                    userInfo?.let {
                        tvName.text = "이름: ${it.name}"
                        tvEmail.text = "Email: ${it.email}"
                        tvPhone.text = "전화번호: ${it.phone}"

                        val sharedPref = getSharedPreferences("userInfo", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("name", it.name)
                            putString("email", it.email)
                            putString("phone", it.phone)
                            putString("pass", it.pass)  // pass도 저장
                            apply()
                        }
                    }
                } else {
                    Toast.makeText(this@MyPage, "사용자 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SignupRequest>, t: Throwable) {
                Toast.makeText(this@MyPage, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteUserAccount() {
        val api = RetrofitClient.apiService
        api.deleteUser(userId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MyPage, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    val sharedPref = getSharedPreferences("userInfo", MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        clear()
                        apply()
                    }
                    val intent = Intent(this@MyPage, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@MyPage, "회원 탈퇴에 실패했습니다. 서버 에러: ${response.code()}", Toast.LENGTH_SHORT).show()
                    println("회원 탈퇴 실패 서버 응답: $errorBody")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@MyPage, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                t.printStackTrace()
            }
        })
    }
}
