package bitc.fullstack502.android_studio.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bitc.fullstack502.android_studio.*
import bitc.fullstack502.android_studio.network.ApiProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.ui.mypage.LoginActivity
import bitc.fullstack502.android_studio.util.AuthManager
import kotlin.jvm.java

class MyPageActivity : AppCompatActivity() {

    private lateinit var tvUsersId: TextView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView

    private lateinit var tvGreetingMy: TextView


    private var usersId: String = ""

    private val editInfoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val newName = data?.getStringExtra("name") ?: ""
            val newEmail = data?.getStringExtra("email") ?: ""
            val newPhone = data?.getStringExtra("phone") ?: ""

            // 로컬 반영
            tvName.text = "이름: $newName"
            tvEmail.text = "Email: $newEmail"
            tvPhone.text = "전화번호: $newPhone"

            val sp = getSharedPreferences("userInfo", MODE_PRIVATE)
            with(sp.edit()) {
                putString("name", newName)
                putString("email", newEmail)
                putString("phone", newPhone)
                apply()
            }

            // (선택) 서버 재동기화가 필요하면 아래 유지 / 필요없으면 제거 가능
            val updated = UpdateUserRequest(
                usersId = usersId,
                name = newName,
                email = newEmail,
                phone = newPhone
            )
            ApiProvider.api.updateUserV2(updated).enqueue(object : Callback<UsersResponse> {
                override fun onResponse(
                    call: Call<UsersResponse>,
                    response: Response<UsersResponse>
                ) {
                    if (!response.isSuccessful) {
                        Toast.makeText(
                            this@MyPageActivity,
                            "수정 실패 (${response.code()})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<UsersResponse>, t: Throwable) {
                    Toast.makeText(this@MyPageActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
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

        tvUsersId = findViewById(R.id.tv_user_id)
        tvName = findViewById(R.id.tv_name)
        tvEmail = findViewById(R.id.tv_email)
        tvPhone = findViewById(R.id.tv_phone)

        // usersId 우선도: intent → SharedPreferences
        usersId = intent.getStringExtra("usersId") ?: getSharedPreferences("userInfo", MODE_PRIVATE)
            .getString("usersId", "") ?: ""

        if (usersId.isBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        tvUsersId.text = "아이디: $usersId"
        loadUserInfo(usersId)

        findViewById<Button>(R.id.btn_edit_info).setOnClickListener {
            val sp = getSharedPreferences("userInfo", MODE_PRIVATE)
            val intent = Intent(this, EditInfoActivity::class.java).apply {
                putExtra("name", sp.getString("name", ""))
                putExtra("email", sp.getString("email", ""))
                putExtra("phone", sp.getString("phone", ""))
            }
            editInfoLauncher.launch(intent)
        }

        findViewById<Button>(R.id.btn_logout).setOnClickListener {
            val sp = getSharedPreferences("userInfo", MODE_PRIVATE)
            sp.edit().clear().apply()
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.btn_delete_account).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("회원 탈퇴")
                .setMessage("정말 탈퇴하시겠습니까? 탈퇴 시 모든 데이터가 삭제됩니다.")
                .setPositiveButton("확인") { d, _ ->
                    d.dismiss()
                    deleteUser()
                }
                .setNegativeButton("취소") { d, _ -> d.dismiss() }
                .show()
        }

        findViewById<View>(R.id.row_my_posts).setOnClickListener {
            startActivity(Intent(this, MyPostsActivity::class.java))
        }
        findViewById<View>(R.id.row_my_comments).setOnClickListener {
            startActivity(Intent(this, MyCommentsActivity::class.java))
        }
        findViewById<View>(R.id.row_liked_posts).setOnClickListener {
            startActivity(Intent(this, LikedPostsActivity::class.java))
        }
        findViewById<View>(R.id.row_flight_wishlist).setOnClickListener {
            startActivity(Intent(this, FlightWishlistActivity::class.java))
        }
        findViewById<View>(R.id.row_lodging_wishlist).setOnClickListener {
            startActivity(Intent(this, LodgingWishlistActivity::class.java))
        }
        findViewById<View>(R.id.row_flight_bookings).setOnClickListener {
            startActivity(Intent(this, FlightBookingsActivity::class.java))
        }
        findViewById<View>(R.id.row_lodging_bookings).setOnClickListener {
            startActivity(Intent(this, LodgingBookingsActivity::class.java))
        }

        tvGreetingMy = findViewById(R.id.tv_greeting_my)

    }

    private fun loadUserInfo(userId: String) {
        ApiProvider.api.getUserInfoV2(userId).enqueue(object : Callback<UsersResponse> {
            override fun onResponse(call: Call<UsersResponse>, response: Response<UsersResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        // ✅ 여기서 greeting_my 적용
                        tvGreetingMy.text = getString(R.string.greeting_my, it.name)

                        tvName.text = "이름: ${it.name}"
                        tvEmail.text = "Email: ${it.email}"
                        tvPhone.text = "전화번호: ${it.phone}"

                        val sp = getSharedPreferences("userInfo", MODE_PRIVATE)
                        with(sp.edit()) {
                            putString("name", it.name)
                            putString("email", it.email)
                            putString("phone", it.phone)
                            apply()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<UsersResponse>, t: Throwable) {
                Toast.makeText(this@MyPageActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteUser() {
        // ✅ V2 엔드포인트 사용
        ApiProvider.api.deleteUserV2(usersId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MyPageActivity, "회원 탈퇴 완료", Toast.LENGTH_SHORT).show()
                    val sp = getSharedPreferences("userInfo", MODE_PRIVATE)
                    sp.edit().clear().apply()
                    startActivity(Intent(this@MyPageActivity, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this@MyPageActivity,
                        "탈퇴 실패 (${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@MyPageActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
