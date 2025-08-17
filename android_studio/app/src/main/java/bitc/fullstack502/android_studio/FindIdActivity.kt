package bitc.fullstack502.android_studio

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class FindIdActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_id)

        val nameEt = findViewById<EditText>(R.id.et_name)
        val emailEt = findViewById<EditText>(R.id.et_email)
        val findIdBtn = findViewById<Button>(R.id.btn_find_id)

        findIdBtn.setOnClickListener {
            val name = nameEt.text.toString().trim()
            val email = emailEt.text.toString().trim()

            if(name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "이름과 이메일을 모두 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            findUserIdByNameAndEmail(name, email)
        }
    }

    private fun findUserIdByNameAndEmail(name: String, email: String) {
        // TODO: 서버 API 호출로 이름+이메일에 맞는 아이디 요청

        // 테스트용 임시 예시 (name: 홍길동, email: test@example.com)
        if(name == "홍길동" && email == "test@example.com") {
            showUserIdDialog("testUser123")
        } else {
            Toast.makeText(this, "일치하는 회원이 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showUserIdDialog(userId: String) {
        AlertDialog.Builder(this)
            .setTitle("아이디 찾기 결과")
            .setMessage("회원님의 아이디는 \"$userId\" 입니다.")
            .setPositiveButton("확인") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
