package bitc.fullstack502.android_studio.ui.mypage

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import bitc.fullstack502.android_studio.*
import bitc.fullstack502.android_studio.network.ApiProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FindIdPwActivity : AppCompatActivity() {

    private lateinit var tabId: TextView
    private lateinit var tabPw: TextView
    private lateinit var layoutIdFind: LinearLayout
    private lateinit var layoutPwFind: LinearLayout

    private lateinit var etEmailForId: EditText
    private lateinit var etPassForId: EditText
    private lateinit var btnFindId: Button

    private lateinit var etUserIdForPw: EditText
    private lateinit var etEmailForPw: EditText
    private lateinit var btnFindPw: Button

    // 간단 로딩뷰(없으면 자동 생성)
    private lateinit var progress: ProgressBar

    // 탭 색상(없으면 hex로 대체)
    private val colorSelected by lazy { ContextCompat.getColor(this, R.color.ink_900) /* 진한 회색 등 */ }
    private val colorUnselected by lazy { ContextCompat.getColor(this, R.color.jeju_tint) /* 연한 회색 등 */ }
    private val textOnSelected by lazy { ContextCompat.getColor(this, android.R.color.white) }
    private val textOnUnselected by lazy { ContextCompat.getColor(this, android.R.color.black) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_id_pw) // ⚠️ XML 파일명이 activity_find_id_pw.xml 인지 확인

        // 탭/레이아웃
        tabId = findViewById(R.id.tab_id)
        tabPw = findViewById(R.id.tab_pw)
        layoutIdFind = findViewById(R.id.layout_id_find)
        layoutPwFind = findViewById(R.id.layout_pw_find)

        tabId.setOnClickListener { selectTab(isIdTab = true) }
        tabPw.setOnClickListener { selectTab(isIdTab = false) }

        // 아이디 찾기 뷰
        etEmailForId = findViewById(R.id.et_email_for_id)
        etPassForId  = findViewById(R.id.et_pass_for_id)
        btnFindId = findViewById(R.id.btn_find_id)
        btnFindId.setOnClickListener { onFindId() }

        // 비밀번호 찾기 뷰
        etUserIdForPw = findViewById(R.id.et_userid_for_pw)
        etEmailForPw  = findViewById(R.id.et_email_for_pw)
        btnFindPw = findViewById(R.id.btn_find_pw)
        btnFindPw.setOnClickListener { onFindPassword() }

        // 프로그레스(레이아웃에 없으면 동적 추가)
        progress = findViewById<ProgressBar?>(R.id.progressBar) ?: ProgressBar(this).also {
            (findViewById<View>(android.R.id.content) as ViewGroup).addView(it)
            it.visibility = View.GONE
        }

        // 기본: 아이디 탭 활성
        selectTab(isIdTab = true)
    }

    private fun selectTab(isIdTab: Boolean) {
        layoutIdFind.visibility = if (isIdTab) View.VISIBLE else View.GONE
        layoutPwFind.visibility = if (isIdTab) View.GONE else View.VISIBLE

        // enabled만 바꾸면 접근성/색상 헷갈리니, 배경/텍스트 컬러까지 명시
        styleTab(tabId, selected = isIdTab)
        styleTab(tabPw, selected = !isIdTab)
    }

    private fun styleTab(tab: TextView, selected: Boolean) {
        tab.isEnabled = !selected // 선택된 탭은 클릭 비활성화
        tab.setBackgroundColor(if (selected) colorSelected else colorUnselected)
        tab.setTextColor(if (selected) textOnSelected else textOnUnselected)
    }

    // --- 아이디 찾기 ---
    private fun onFindId() {
        hideKeyboard()
        val email = etEmailForId.text.toString().trim()
        val pass  = etPassForId.text.toString()

        if (email.isBlank()) {
            toast("이메일을 입력하세요.")
            return
        }

        if (pass.isBlank()) {
            toast("비밀번호를 입력하세요.")
            return
        }

        lockUi(true)
        val req = FindIdRequest(email = email, pass = pass)
        ApiProvider.api.findUsersId(req).enqueue(object : Callback<FindIdResponse> {
            override fun onResponse(call: Call<FindIdResponse>, res: Response<FindIdResponse>) {
                lockUi(false)
                if (res.isSuccessful) {
                    val usersId = res.body()?.usersId.orEmpty()
                    if (usersId.isNotBlank())
                        showDialog("아이디 찾기 결과", "회원님의 아이디는 \"$usersId\" 입니다.")
                    else toast("아이디를 찾을 수 없습니다.")
                } else {
                    toast("아이디를 찾을 수 없습니다. (코드 ${res.code()})")
                }
            }
            override fun onFailure(call: Call<FindIdResponse>, t: Throwable) {
                lockUi(false); toast("네트워크 오류: ${t.localizedMessage}")
            }
        })
    }

    // --- 비밀번호 찾기 ---
    private fun onFindPassword() {
        hideKeyboard()
        val usersId = etUserIdForPw.text.toString().trim()
        val email   = etEmailForPw.text.toString().trim()

        if (usersId.isBlank()) {
            toast("아이디를 입력하세요.")
            return
        }
        if (email.isBlank()) {
            toast("이메일을 입력하세요.")
            return
        }

        lockUi(true)
        val req = FindPasswordRequest(usersId = usersId, email = email)
        ApiProvider.api.findUserPassword(req).enqueue(object : Callback<FindPasswordResponse> {
            override fun onResponse(call: Call<FindPasswordResponse>, res: Response<FindPasswordResponse>) {
                lockUi(false)
                if (res.isSuccessful) {
                    val pass = res.body()?.pass.orEmpty()
                    if (pass.isNotBlank())
                        showDialog("비밀번호 찾기 결과", "회원님의 비밀번호는 \"$pass\" 입니다.")
                    else toast("비밀번호를 찾을 수 없습니다.")
                } else {
                    toast("비밀번호를 찾을 수 없습니다. (코드 ${res.code()})")
                }
            }
            override fun onFailure(call: Call<FindPasswordResponse>, t: Throwable) {
                lockUi(false); toast("네트워크 오류: ${t.localizedMessage}")
            }
        })
    }

    private fun lockUi(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        btnFindId.isEnabled = !loading
        btnFindPw.isEnabled = !loading
        tabId.isEnabled = !loading && layoutPwFind.visibility == View.VISIBLE   // 다른 탭만 전환 가능
        tabPw.isEnabled = !loading && layoutIdFind.visibility == View.VISIBLE
    }

    private fun hideKeyboard() {
        currentFocus?.let { v ->
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    private fun showDialog(title: String, msg: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton("확인", null)
            .show()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
