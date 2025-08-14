package bitc.fullstack502.android_studio.ui.post

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import bitc.fullstack502.android_studio.databinding.ActivityPostListBinding
import bitc.fullstack502.android_studio.network.RetrofitClient
import bitc.fullstack502.android_studio.network.dto.PagePostDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostListActivity : AppCompatActivity() {
    private lateinit var bind: ActivityPostListBinding
    private val adapter = PostListAdapter { post ->
        startActivity(Intent(this, PostDetailActivity::class.java).putExtra("id", post.id))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityPostListBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.recycler.layoutManager = LinearLayoutManager(this)
        bind.recycler.adapter = adapter

        // 검색 버튼 제거 → 엔터로 검색
        bind.etQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch()
                true
            } else {
                false
            }
        }

        bind.fab.setOnClickListener {
            startActivity(Intent(this, PostWriteActivity::class.java))
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

    private fun reload() {
        val q = bind.etQuery.text.toString().trim()
        if (q.isEmpty()) {
            load()
        } else {
            doSearch()
        }
    }

    private fun fieldKey(): String {
        return when (bind.spnField.selectedItemPosition) {
            0 -> "title"
            1 -> "content"
            else -> "author"
        }
    }

    private fun doSearch() {
        val q = bind.etQuery.text.toString().trim()
        if (q.isEmpty()) { load(); return }
        RetrofitClient.api.search(fieldKey(), q).enqueue(object : Callback<PagePostDto> {
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
        RetrofitClient.api.list().enqueue(object : Callback<PagePostDto> {
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

