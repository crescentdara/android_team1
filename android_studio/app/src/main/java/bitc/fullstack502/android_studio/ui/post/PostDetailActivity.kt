package bitc.fullstack502.android_studio.ui.post

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.databinding.ActivityPostDetailBinding
import bitc.fullstack502.android_studio.network.RetrofitClient
import bitc.fullstack502.android_studio.network.dto.CommDto
import bitc.fullstack502.android_studio.network.dto.PostDto
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostDetailActivity : AppCompatActivity() {
    private lateinit var b: ActivityPostDetailBinding

    private val TEST_USER = "testuser"
    private var replyTarget: CommDto? = null
    private var id: Long = 0
    private lateinit var cAdapter: CommentAdapter
    private var isLiked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(b.root)

        id = intent.getLongExtra("id", 0L)

        // 댓글 어댑터 연결
        cAdapter = CommentAdapter { comm -> showCommentUserMenu(comm) }
        b.rvComments.layoutManager = LinearLayoutManager(this)
        b.rvComments.adapter = cAdapter

        // 좋아요 버튼
        b.btnHeart.setOnClickListener { toggleLikeAndIcon() }

        // 수정 / 삭제
        b.btnEdit.setOnClickListener {
            startActivity(Intent(this, PostWriteActivity::class.java).putExtra("editId", id))
        }
        b.btnDelete.setOnClickListener { confirmDeletePost() }

        // 댓글 등록
        b.btnSend.setOnClickListener {
            writeComment(replyTarget?.id, b.etComment.text.toString())
        }

        // 글 작성자 클릭 시 1:1 채팅 메뉴
        b.tvMeta.setOnClickListener { showPostAuthorMenu() }

        loadDetail()
        loadComments()
    }

    private fun loadDetail() {
        RetrofitClient.api.detail(id).enqueue(object : Callback<PostDto> {
            override fun onResponse(call: Call<PostDto>, response: Response<PostDto>) {
                val p = response.body() ?: return

                b.tvTitle.text = p.title
                b.tvMeta.text = "♥ ${p.likeCount} · 조회 ${p.lookCount} · by ${p.author}"
                b.tvContent.text = p.content

                // 이미지 로드
                if (!p.imgUrl.isNullOrEmpty()) {
                    Glide.with(b.img).load("http://10.0.2.2:8080${p.imgUrl}").into(b.img)
                }

                // 좋아요 상태
                b.tvLikeCount.text = "${p.likeCount}"
                isLiked = p.liked ?: false
                b.btnHeart.setImageResource(
                    if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
                )

                // 작성자 여부에 따른 버튼 표시
                val mine = (p.author == TEST_USER)
                b.btnEdit.visibility = if (mine) View.VISIBLE else View.GONE
                b.btnDelete.visibility = if (mine) View.VISIBLE else View.GONE
            }

            override fun onFailure(call: Call<PostDto>, t: Throwable) {}
        })
    }

    private fun toggleLikeAndIcon() {
        b.btnHeart.isEnabled = false
        RetrofitClient.api.toggleLike(id).enqueue(object : Callback<Long> {
            override fun onResponse(call: Call<Long>, response: Response<Long>) {
                val cnt = response.body() ?: return
                isLiked = !isLiked
                b.btnHeart.setImageResource(
                    if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
                )
                b.tvLikeCount.text = "$cnt"
                b.btnHeart.isEnabled = true
            }

            override fun onFailure(call: Call<Long>, t: Throwable) {
                b.btnHeart.isEnabled = true
            }
        })
    }

    private fun loadComments() {
        RetrofitClient.api.comments(id).enqueue(object : Callback<List<CommDto>> {
            override fun onResponse(call: Call<List<CommDto>>, response: Response<List<CommDto>>) {
                cAdapter.submitList(response.body() ?: emptyList())
            }
            override fun onFailure(call: Call<List<CommDto>>, t: Throwable) {}
        })
    }

    private fun writeComment(parentId: Long?, text: String) {
        if (text.isBlank()) return
        RetrofitClient.api.writeComment(id, parentId, text).enqueue(object : Callback<Long> {
            override fun onResponse(call: Call<Long>, response: Response<Long>) {
                b.etComment.text = null
                replyTarget = null
                b.etComment.hint = "댓글쓰기"
                loadComments()
            }
            override fun onFailure(call: Call<Long>, t: Throwable) {}
        })
    }

    private fun confirmDeletePost() {
        AlertDialog.Builder(this)
            .setMessage("게시글을 삭제할까요?")
            .setPositiveButton("삭제") { _, _ -> deletePost() }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun deletePost() {
        RetrofitClient.api.deletePost(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) { finish() }
            override fun onFailure(call: Call<Void>, t: Throwable) {}
        })
    }

    private fun showCommentUserMenu(c: CommDto) {
        val sheet = BottomSheetDialog(this)
        val options = mutableListOf("1:1 채팅", "답글 쓰기")
        val isMine = c.author == TEST_USER
        if (isMine) options += listOf("댓글 수정", "댓글 삭제")

        val list = ListView(this).apply {
            adapter = ArrayAdapter(
                this@PostDetailActivity,
                android.R.layout.simple_list_item_1,
                options
            )
            setOnItemClickListener { _, _, pos, _ ->
                when (options[pos]) {
                    "1:1 채팅" -> {
                        Toast.makeText(this@PostDetailActivity, "채팅 기능은 추후 구현 예정", Toast.LENGTH_SHORT).show()
                    }
                    "답글 쓰기" -> {
                        replyTarget = c
                        b.etComment.hint = "↳ ${c.author}에게 답글"
                        sheet.dismiss()
                    }
                    "댓글 수정" -> showEditCommentDialog(c)
                    "댓글 삭제" -> confirmDeleteComment(c.id)
                }
            }
        }
        sheet.setContentView(list)
        sheet.show()
    }

    private fun showEditCommentDialog(c: CommDto) {
        val input = EditText(this).apply {
            setText(c.content)
            setSelection(text.length)
        }
        AlertDialog.Builder(this)
            .setTitle("댓글 수정")
            .setView(input)
            .setPositiveButton("저장") { _, _ ->
                RetrofitClient.api.editComment(c.id, input.text.toString())
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) { loadComments() }
                        override fun onFailure(call: Call<Void>, t: Throwable) {}
                    })
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun confirmDeleteComment(commentId: Long) {
        AlertDialog.Builder(this)
            .setMessage("댓글을 삭제할까요?")
            .setPositiveButton("삭제") { _, _ ->
                RetrofitClient.api.deleteComment(commentId)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) { loadComments() }
                        override fun onFailure(call: Call<Void>, t: Throwable) {}
                    })
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showPostAuthorMenu() {
        val list = arrayOf("1:1 채팅")
        AlertDialog.Builder(this)
            .setItems(list) { _, which ->
                if (which == 0) {
                    Toast.makeText(this, "채팅 기능은 추후 구현 예정", Toast.LENGTH_SHORT).show()
                }
            }.show()
    }
}
