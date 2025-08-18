package bitc.fullstack502.android_studio.ui.post

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import bitc.fullstack502.android_studio.databinding.ActivityPostListBinding
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.network.dto.PagePostDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostListActivity : AppCompatActivity() {
    private lateinit var bind: ActivityPostListBinding

    private val adapter = PostListAdapter { post ->
        if (!isLoggedIn()) {
            showLoginRequiredDialog()
            return@PostListAdapter
        }
        startActivity(Intent(this, PostDetailActivity::class.java).putExtra("id", post.id))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityPostListBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.recycler.layoutManager = LinearLayoutManager(this)
        bind.recycler.adapter = adapter

        // 엔터로 검색
        bind.etQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch()
                true
            } else false
        }

        // 글쓰기 버튼
        bind.fab.setOnClickListener {
            if (!isLoggedIn()) {
                showLoginRequiredDialog()
            } else {
                startActivity(Intent(this, PostWriteActivity::class.java))
            }
        }

        bind.swipe.setOnRefreshListener { reload() }
        bind.swipe.isRefreshing = true
        reload()
    }

    override fun onResume() {
        super.onResume()
        bind.swipe.isRefreshing = true
        reload()
    }

    private fun isLoggedIn(): Boolean {
        val sp = getSharedPreferences("userInfo", MODE_PRIVATE)
        return !sp.getString("usersId", "").isNullOrBlank()
    }

    private fun showLoginRequiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("로그인이 필요합니다")
            .setMessage("해당 기능을 이용하려면 로그인 해주세요.")
            .setPositiveButton("확인", null)
            .show()
    }

    private fun reload() {
        val q = bind.etQuery.text.toString().trim()
        if (q.isEmpty()) load() else doSearch()
    }

    private fun fieldKey(): String = when (bind.spnField.selectedItemPosition) {
        0 -> "title"
        1 -> "content"
        else -> "author"
    }

    private fun doSearch() {
        val q = bind.etQuery.text.toString().trim()
        if (q.isEmpty()) { load(); return }
        ApiProvider.api.searchPosts(fieldKey(), q).enqueue(object : Callback<PagePostDto> {
            override fun onResponse(call: Call<PagePostDto>, res: Response<PagePostDto>) {
                if (isFinishing || isDestroyed) return
                bind.swipe.isRefreshing = false
                val list = res.body()?.content ?: emptyList()
                adapter.submitList(list.toList())
            }
            override fun onFailure(call: Call<PagePostDto>, t: Throwable) {
                if (isFinishing || isDestroyed) return
                bind.swipe.isRefreshing = false
                adapter.submitList(emptyList())
            }
        })
    }

    private fun load() {
        ApiProvider.api.list().enqueue(object : Callback<PagePostDto> {
            override fun onResponse(call: Call<PagePostDto>, res: Response<PagePostDto>) {
                if (isFinishing || isDestroyed) return
                bind.swipe.isRefreshing = false
                val list = res.body()?.content ?: emptyList()
                adapter.submitList(list.toList())
            }
            override fun onFailure(call: Call<PagePostDto>, t: Throwable) {
                if (isFinishing || isDestroyed) return
                bind.swipe.isRefreshing = false
                adapter.submitList(emptyList())
            }
        })
    }
}
