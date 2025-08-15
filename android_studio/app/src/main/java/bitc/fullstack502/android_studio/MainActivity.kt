package bitc.fullstack502.android_studio

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.android_studio.ui.lodging.LodgingSearchActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 바로 숙박 검색 화면으로 이동
        val intent = Intent(this, LodgingSearchActivity::class.java)
        startActivity(intent)
        
        // 현재 Activity 종료 (뒤로가기 시 앱 종료)
        finish()
    }
}