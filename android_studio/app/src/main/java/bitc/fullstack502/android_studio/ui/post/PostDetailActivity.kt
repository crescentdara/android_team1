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
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.network.dto.CommDto
import bitc.fullstack502.android_studio.network.dto.PostDto
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import bitc.fullstack502.android_studio.ui.ChatRoomActivity
import kotlin.jvm.java

class PostDetailActivity : AppCompatActivity() {
    private lateinit var b: ActivityPostDetailBinding
    private var id: Long = 0
    private lateinit var cAdapter: CommentAdapter
    private var isLiked = false
    private var myUsersId = ""
    private var replyTarget: CommDto? = null
    private var postAuthor: String = ""   // ✅ 글 작성자 보관

    // ====== 로그인 유틸 ======
    private fun usersIdHeader(): String {
        val sp = getSharedPreferences("userInfo", MODE_PRIVATE)
        return sp.getString("usersId", "") ?: ""
    }

    private fun requireLoginOrFinish(): Boolean {
        val id = usersIdHeader()
        if (id.isBlank()) {
            AlertDialog.Builder(this)
                .setTitle("로그인이 필요합니다")
                .setMessage("해당 기능을 이용하려면 로그인 해주세요.")
                .setPositiveButton("확인") { _, _ -> finish() }
                .show()
            return false
        }
        return true
    }

    // ====== 채팅 유틸 ======
    private fun roomIdOf(a: String, b: String): String = if (a <= b) "$a|$b" else "$b|$a"
    private fun openChatWith(other: String) {
        if (other.isBlank() || other == myUsersId) return
        val intent = Intent(this, ChatRoomActivity::class.java).apply {
            putExtra("me", myUsersId)
            putExtra("other", other)
            putExtra("roomId", roomIdOf(myUsersId, other)) // 서버/앱 공통 규칙
        }
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(b.root)

        if (!requireLoginOrFinish()) return
        myUsersId = usersIdHeader()

        // ✅ "id" 없으면 "postId"로 보조 처리
        id = intent.getLongExtra("id", 0L)
        if (id == 0L) {
            id = intent.getLongExtra("postId", 0L)
        }

        // 댓글 어댑터
        cAdapter = CommentAdapter { comm -> showCommentUserMenu(comm) }
        b.rvComments.layoutManager = LinearLayoutManager(this)
        b.rvComments.adapter = cAdapter

        // 이하 동일 ...


        // 좋아요
        b.btnHeart.setOnClickListener { toggleLikeAndIcon() }

        // 수정/삭제
        b.btnEdit.setOnClickListener {
            startActivity(Intent(this, PostWriteActivity::class.java).putExtra("editId", id))
        }
        b.btnDelete.setOnClickListener { confirmDeletePost() }

        // 댓글 등록
        b.btnSend.setOnClickListener {
            writeComment(replyTarget?.id, b.etComment.text.toString())
        }

        // 상단 메타(작성자) 클릭 → 1:1 채팅
        b.tvMeta.setOnClickListener { showPostAuthorMenu() }

        loadDetail()
        loadComments()
    }

    // ====== 게시글 상세 ======
    private fun loadDetail() {
        ApiProvider.api.detail(id, myUsersId).enqueue(object : Callback<PostDto> {
            override fun onResponse(call: Call<PostDto>, res: Response<PostDto>) {
                val p = res.body() ?: return
                b.tvTitle.text = p.title
                b.tvMeta.text = "♥ ${p.likeCount} · 조회 ${p.lookCount} · by ${p.author}"
                b.tvContent.text = p.content
                postAuthor = p.author                       // ✅ 저장

                val raw = p.imgUrl
                val url = when {
                    raw.isNullOrBlank() -> null
                    raw.startsWith("http", ignoreCase = true) -> raw
                    else -> "http://10.0.2.2:8080$raw"
                }

                b.img.setImageDrawable(null)
                b.img.clearColorFilter()

                if (url == null) {
                    b.img.setImageResource(R.drawable.ic_launcher_foreground)
                } else {
                    Glide.with(b.img)
                        .load(url)
                        .centerCrop()
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(b.img)
                }

                isLiked = p.liked == true
                b.btnHeart.setImageResource(
                    if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
                )
                b.tvLikeCount.text = "${p.likeCount}"

                val mine = (p.author == myUsersId)
                b.btnEdit.visibility = if (mine) View.VISIBLE else View.GONE
                b.btnDelete.visibility = if (mine) View.VISIBLE else View.GONE
            }

            override fun onFailure(call: Call<PostDto>, t: Throwable) {}
        })
    }

    // ====== 좋아요 토글 ======
    private fun toggleLikeAndIcon() {
        b.btnHeart.isEnabled = false
        ApiProvider.api.toggleLike(id, myUsersId).enqueue(object : Callback<Long> {
            override fun onResponse(call: Call<Long>, res: Response<Long>) {
                val cnt = res.body() ?: return
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

    // ====== 댓글 목록 ======
    private fun loadComments() {
        ApiProvider.api.comments(id).enqueue(object : Callback<List<CommDto>> {
            override fun onResponse(call: Call<List<CommDto>>, res: Response<List<CommDto>>) {
                val flat = res.body() ?: emptyList()
                cAdapter.submitList(arrangeComments(flat)) // ✅ 부모 아래에 붙도록 정렬
            }

            override fun onFailure(call: Call<List<CommDto>>, t: Throwable) {}
        })
    }

    /** ✅ 부모-자식 구조로 평탄화(부모 → 자식들 순서로) */
    private fun arrangeComments(src: List<CommDto>): List<CommDto> {
        // 입력 순서(이미 생성일 ASC)를 유지하려고 LinkedHashMap 사용
        val bucket = LinkedHashMap<Long?, MutableList<CommDto>>()
        for (c in src) bucket.getOrPut(c.parentId) { mutableListOf() }.add(c)

        val out = mutableListOf<CommDto>()
        fun dfs(p: CommDto) {
            out += p
            bucket[p.id]?.forEach { dfs(it) }
        }
        bucket[null]?.forEach { dfs(it) }   // 최상위부터 DFS
        return out
    }

    // ====== 댓글 작성/수정/삭제 ======
    private fun writeComment(parentId: Long?, text: String) {
        if (text.isBlank()) return
        ApiProvider.api.writeComment(id, parentId, text, myUsersId)
            .enqueue(object : Callback<Long> {
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
        ApiProvider.api.deletePost(id, myUsersId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                finish()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {}
        })
    }

    private fun showCommentUserMenu(c: CommDto) {
        val sheet = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val options = mutableListOf("1:1 채팅", "답글 쓰기")
        val isMine = (c.author == myUsersId)
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
                        openChatWith(c.author); sheet.dismiss()
                    } // ✅ 바로 연결
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
        sheet.setContentView(list); sheet.show()
    }

    private fun showEditCommentDialog(c: CommDto) {
        val input = EditText(this).apply { setText(c.content); setSelection(text.length) }
        AlertDialog.Builder(this)
            .setTitle("댓글 수정")
            .setView(input)
            .setPositiveButton("저장") { _, _ ->
                ApiProvider.api.editComment(c.id, input.text.toString(), myUsersId)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            loadComments()
                        }

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
                ApiProvider.api.deleteComment(commentId, myUsersId)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            loadComments()
                        }

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
                if (which == 0) openChatWith(postAuthor) // ✅ 바로 연결
            }.show()
    }
}
