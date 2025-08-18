package bitc.fullstack502.android_studio.ui.post

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import bitc.fullstack502.android_studio.databinding.ActivityPostWriteBinding
import com.yalantis.ucrop.UCrop
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.UUID
import androidx.core.widget.addTextChangedListener
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.network.dto.PostDto
import com.bumptech.glide.Glide


class PostWriteActivity : AppCompatActivity() {

    private lateinit var b: ActivityPostWriteBinding

    // 크롭된 실제 업로드 파일
    // 클래스 필드
    private var croppedFile: File? = null

    private var editId: Long? = null                 // 수정 대상 id
    private var existingImageUrl: String? = null     // 수정 모드에서 서버에 이미 있는 이미지


    // 1) 갤러리에서 이미지 선택
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        val dest = Uri.fromFile(File(cacheDir, "crop_${UUID.randomUUID()}.jpg"))

        // UCrop 인텐트 생성 (1:1)
        val uCropIntent = UCrop.of(uri, dest)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(1080, 1080)
            .getIntent(this)

        cropLauncher.launch(uCropIntent)
    }

    // 2) 크롭 결과 받기
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
            val e = UCrop.getError(result.data!!)
            e?.printStackTrace()
            // 필요하면 Toast로 안내
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPostWriteBinding.inflate(layoutInflater)
        setContentView(b.root)

        // 수정 모드 판별
        val id = intent.getLongExtra("editId", 0L)
        editId = if (id > 0) id else null
        if (editId != null) {
            b.btnSubmit.text = "수정"
            loadForEdit(editId!!)   // ← 기존 글 불러와서 채우기
        } else {
            b.btnSubmit.text = "등록"
        }

        b.btnImage.setOnClickListener { pickImage.launch("image/*") }
        b.btnSubmit.setOnClickListener { submit() }

        listOf(b.etTitle, b.etContent).forEach { it.addTextChangedListener { updateSubmitEnabled() } }
    }

    private fun loadForEdit(id: Long) {
        ApiProvider.api.detail(id).enqueue(object: Callback<PostDto> {
            override fun onResponse(call: Call<PostDto>, response: Response<PostDto>) {
                val p = response.body() ?: return
                b.etTitle.setText(p.title)
                b.etContent.setText(p.content ?: "")
                existingImageUrl = p.imgUrl
                // 기존 이미지 미리보기
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




    private fun updateSubmitEnabled() {
        val hasImage = (croppedFile != null) || (!existingImageUrl.isNullOrBlank())
        val ok = hasImage && b.etTitle.text.isNullOrBlank().not() && b.etContent.text.isNullOrBlank().not()
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

        if (editId == null) {
            // 신규 작성
            ApiProvider.api.create(titleRb, contentRb, part)
                .enqueue(object : Callback<Long> {
                    override fun onResponse(call: Call<Long>, response: Response<Long>) { finish() }
                    override fun onFailure(call: Call<Long>, t: Throwable) {}
                })
        } else {
            // 수정: 이미지 안 바꾸면 part=null → 서버에서 기존 이미지 유지
            ApiProvider.api.update(editId!!, titleRb, contentRb, part)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) { finish() }
                    override fun onFailure(call: Call<Void>, t: Throwable) {}
                })
        }
    }



}
