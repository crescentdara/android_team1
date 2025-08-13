package bitc.fullstack502.android_studio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.ui.ChatListActivity

class IdInputActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MY_ID = "extra_my_id"
        const val EXTRA_PARTNER_ID = "extra_partner_id"
        const val EXTRA_ROOM_ID = "extra_room_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_id_input)

        val etMyId: EditText = findViewById(R.id.etMyId)
        val etPartnerId: EditText = findViewById(R.id.etPartnerId)
        val etRoomId: EditText = findViewById(R.id.etRoomId)
        val btnGo: Button = findViewById(R.id.btnGo)

        // 테스트 편하게 기본값 세팅
        etMyId.setText("android1")
        etPartnerId.setText("android2")
        etRoomId.setText("testroom")

//        btnGo.setOnClickListener {
//            val myId = etMyId.text.toString().trim()
//            val partnerId = etPartnerId.text.toString().trim()
//            val roomId = etRoomId.text.toString().trim().ifEmpty { "testroom" }
//
//            if (myId.isEmpty() || partnerId.isEmpty()) {
//                etMyId.error = if (myId.isEmpty()) "필수" else null
//                etPartnerId.error = if (partnerId.isEmpty()) "필수" else null
//                return@setOnClickListener
//            }
//
//            // 내 ID 로컬 저장
//            getSharedPreferences("chat", MODE_PRIVATE)
//                .edit()
//                .putString("myId", myId)
//                .apply()
//
//            // MainActivity로 전달
//            val i = Intent(this, ChatRoomActivity::class.java).apply {
//                putExtra(IdInputActivity.EXTRA_MY_ID, myId)
//                putExtra(IdInputActivity.EXTRA_PARTNER_ID, partnerId)
//                putExtra(IdInputActivity.EXTRA_ROOM_ID, roomId)
//            }
//            startActivity(i)
//        }
//    }


        btnGo.setOnClickListener {
            val myId = etMyId.text.toString().trim().ifEmpty { "android1" }

            // 내 ID만 필수
            if (myId.isEmpty()) {
                etMyId.error = "필수"
                return@setOnClickListener
            }

            // 로컬 저장(선택)
            getSharedPreferences("chat", MODE_PRIVATE)
                .edit()
                .putString("myId", myId)
                .apply()

            // ✅ 채팅 '목록'으로 이동
            val i = Intent(this, ChatListActivity::class.java).apply {
                putExtra(IdInputActivity.EXTRA_MY_ID, myId)
            }
            startActivity(i)
        }
    }
}