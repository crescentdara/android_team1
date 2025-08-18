package bitc.fullstack502.android_studio.ui.post

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import bitc.fullstack502.android_studio.databinding.ActivityPostWriteBinding
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.network.dto.PostDto
import com.bumptech.glide.Glide
import com.yalantis.ucrop.UCrop
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.UUID

class PostWriteActivity : AppCompatActivity() {

    private lateinit var b: ActivityPostWriteBinding

    // 크롭된 업로드 파일
    private var croppedFile: File? = null

    private var editId: Long? = null
    private var existingImageUrl: String? = null

    // ✅ 로그인 사용자 헤더 값
    private fun usersIdHeader(): String {
        val sp = getSharedPreferences("userInfo", MODE_PRIVATE)
        return sp.getString("usersId", "") ?: ""
    }

    // 1) 갤러리 선택
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        val dest = Uri.fromFile(File(cacheDir, "crop_${UUID.randomUUID()}.jpg"))

        val uCropIntent = UCrop.of(uri, dest)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(1080, 1080)
            .getIntent(this)

        cropLauncher.launch(uCropIntent)
    }

    // 2) 크롭 결과
    private val cropLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val out = UCrop.getOutput(result.data!!) ?: return@registerForActivityResult
            val f = File(cacheDir, "final_${UUID.randomUUID()}.jpg")
            contentResolver.openInputStream(out)?.use { input ->
                f.outputStream().use { input.copyTo(it) }
            }
            croppedFile = f
            b.imgPreview.setImageBitmap(BitmapFactory.decodeFile(f.absolutePath))
            updateSubmitEnabled()
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            // 필요 시 안내
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPostWriteBinding.inflate(layoutInflater)
        setContentView(b.root)

        // 🔒 로그인 가드
        if (!isLoggedIn()) {
            AlertDialog.Builder(this)
                .setTitle("로그인이 필요합니다")
                .setMessage("글쓰기를 사용하려면 로그인 해주세요.")
                .setPositiveButton("확인") { _, _ -> finish() }
                .show()
            return
        }

        // 수정 모드 여부
        val id = intent.getLongExtra("editId", 0L)
        editId = if (id > 0) id else null
        if (editId != null) {
            b.btnSubmit.text = "수정"
            loadForEdit(editId!!)
        } else {
            b.btnSubmit.text = "등록"
        }

        b.btnImage.setOnClickListener { pickImage.launch("image/*") }
        b.btnSubmit.setOnClickListener { submit() }

        listOf(b.etTitle, b.etContent).forEach { it.addTextChangedListener { updateSubmitEnabled() } }
        updateSubmitEnabled()
    }

    private fun isLoggedIn(): Boolean {
        val sp = getSharedPreferences("userInfo", MODE_PRIVATE)
        return !sp.getString("usersId", "").isNullOrBlank()
    }

    private fun loadForEdit(id: Long) {
        // ✅ 헤더 추가
        ApiProvider.api.detail(id, usersIdHeader()).enqueue(object : Callback<PostDto> {
            override fun onResponse(call: Call<PostDto>, response: Response<PostDto>) {
                val p = response.body() ?: return
                b.etTitle.setText(p.title)
                b.etContent.setText(p.content) // 서버에서 String 보장
                existingImageUrl = p.imgUrl
                if (!existingImageUrl.isNullOrBlank()) {
                    Glide.with(b.imgPreview)
                        .load("http://10.0.2.2:8080$existingImageUrl")
                        .into(b.imgPreview)
                }
                updateSubmitEnabled()
            }
            override fun onFailure(call: Call<PostDto>, t: Throwable) { }
        })
    }

    /** 클래스 스코프 함수 */
    private fun updateSubmitEnabled() {
        val hasImage = (croppedFile != null) || (!existingImageUrl.isNullOrBlank())
        val ok = hasImage &&
                b.etTitle.text.isNullOrBlank().not() &&
                b.etContent.text.isNullOrBlank().not()
        b.btnSubmit.isEnabled = ok
    }

    private fun submit() {
        val titleRb = b.etTitle.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val contentRb = b.etContent.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val part: MultipartBody.Part? = croppedFile?.let {
            MultipartBody.Part.createFormData(
                name = "image",
                filename = it.name,
                body = it.asRequestBody("image/*".toMediaTypeOrNull())
            )
        }

        val userHeader = usersIdHeader() // ✅ 한 번만 읽어 사용

        if (editId == null) {
            // ✅ 헤더 추가
            ApiProvider.api.create(titleRb, contentRb, part, userHeader)
                .enqueue(object : Callback<Long> {
                    override fun onResponse(call: Call<Long>, response: Response<Long>) { finish() }
                    override fun onFailure(call: Call<Long>, t: Throwable) {}
                })
        } else {
            // ✅ 헤더 추가
            ApiProvider.api.update(editId!!, titleRb, contentRb, part, userHeader)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) { finish() }
                    override fun onFailure(call: Call<Void>, t: Throwable) {}
                })
        }
    }
}
