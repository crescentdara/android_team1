package bitc.fullstack502.android_studio.ui.mypage

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import bitc.fullstack502.android_studio.CheckIdResponse
import bitc.fullstack502.android_studio.SignupRequest
import bitc.fullstack502.android_studio.network.ApiProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import bitc.fullstack502.android_studio.R

class SignupActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etId: EditText
    private lateinit var etPassword: EditText
    private lateinit var etPasswordConfirm: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnIdCheck: Button
    private lateinit var tvIdCheck: TextView
    private lateinit var tvPwCheck: TextView

    private var isIdChecked = false
    private var isIdAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        etName = findViewById(R.id.et_name)
        etId = findViewById(R.id.et_id)
        etPassword = findViewById(R.id.et_password)
        etPasswordConfirm = findViewById(R.id.et_password_confirm)
        etEmail = findViewById(R.id.et_email)
        etPhone = findViewById(R.id.et_phone)
        btnRegister = findViewById(R.id.btn_register)
        btnIdCheck = findViewById(R.id.btn_id_check)
        tvIdCheck = findViewById(R.id.tv_id_check)
        tvPwCheck = findViewById(R.id.tv_pw_check)

        // 비밀번호 확인 체크
        etPasswordConfirm.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pw = etPassword.text.toString()
                val pwConfirm = s.toString()

                when {
                    pw.length < 6 -> {
                        tvPwCheck.text = "비밀번호는 최소 6자 이상이어야 합니다."
                        tvPwCheck.setTextColor(ContextCompat.getColor(this@SignupActivity, android.R.color.holo_red_dark))
                    }
                    pw != pwConfirm -> {
                        tvPwCheck.text = "비밀번호가 일치하지 않습니다."
                        tvPwCheck.setTextColor(ContextCompat.getColor(this@SignupActivity, android.R.color.holo_red_dark))
                    }
                    else -> {
                        tvPwCheck.text = "비밀번호가 일치합니다."
                        tvPwCheck.setTextColor(ContextCompat.getColor(this@SignupActivity, android.R.color.holo_green_dark))
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 아이디 중복 체크 버튼 클릭
        btnIdCheck.setOnClickListener {
            val inputId = etId.text.toString()
            if (inputId.isBlank()) {
                tvIdCheck.text = "아이디를 입력해주세요."
                tvIdCheck.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                isIdAvailable = false
                isIdChecked = false
            } else {
                ApiProvider.api.checkId(inputId).enqueue(object : Callback<CheckIdResponse> {
                    override fun onResponse(call: Call<CheckIdResponse>, response: Response<CheckIdResponse>) {
                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null && body.available) {
                                tvIdCheck.text = "사용 가능한 아이디입니다."
                                tvIdCheck.setTextColor(ContextCompat.getColor(this@SignupActivity, android.R.color.holo_green_dark))
                                isIdAvailable = true
                            } else {
                                tvIdCheck.text = "이미 사용 중인 아이디입니다."
                                tvIdCheck.setTextColor(ContextCompat.getColor(this@SignupActivity, android.R.color.holo_red_dark))
                                isIdAvailable = false
                            }
                            isIdChecked = true
                        } else {
                            tvIdCheck.text = "서버 오류가 발생했습니다."
                            tvIdCheck.setTextColor(ContextCompat.getColor(this@SignupActivity, android.R.color.holo_red_dark))
                            isIdChecked = false
                            isIdAvailable = false
                        }
                    }

                    override fun onFailure(call: Call<CheckIdResponse>, t: Throwable) {
                        tvIdCheck.text = "서버 연결 실패: ${t.message}"
                        tvIdCheck.setTextColor(ContextCompat.getColor(this@SignupActivity, android.R.color.holo_red_dark))
                        isIdChecked = false
                        isIdAvailable = false
                    }
                })
            }
        }

        // 전화번호 자동 하이픈 입력 처리
        etPhone.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return

                isFormatting = true

                s?.let {
                    val digits = it.toString().replace("-", "")
                    val formatted = StringBuilder()

                    for (i in digits.indices) {
                        formatted.append(digits[i])
                        if (i == 2 || i == 6) {
                            if (i != digits.length - 1) {
                                formatted.append("-")
                            }
                        }
                    }

                    val newText = formatted.toString()
                    if (newText != it.toString()) {
                        etPhone.setText(newText)
                        etPhone.setSelection(newText.length)
                    }
                }

                isFormatting = false
            }
        })

        // 회원가입 버튼 클릭
        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val id = etId.text.toString()
            val password = etPassword.text.toString()
            val passwordConfirm = etPasswordConfirm.text.toString()
            val email = etEmail.text.toString()
            val phone = etPhone.text.toString()

            if (name.isBlank() || id.isBlank() || password.isBlank() || email.isBlank()) {
                Toast.makeText(this, "모든 필수 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isIdChecked || !isIdAvailable) {
                Toast.makeText(this, "아이디 중복확인을 해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != passwordConfirm) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val signupRequest = SignupRequest(
                name = name,
                usersId = id,
                pass = password,
                email = email,
                phone = phone
            )

            ApiProvider.api.registerUser(signupRequest).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@SignupActivity, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@SignupActivity, "회원가입 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@SignupActivity, "서버 연결 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
