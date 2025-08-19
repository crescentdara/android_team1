package bitc.fullstack502.android_studio.ui.mypage

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.R
import bitc.fullstack502.android_studio.model.CommonItem
import bitc.fullstack502.android_studio.util.AuthManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseListActivity : AppCompatActivity() {

    protected lateinit var recycler: RecyclerView
    protected lateinit var progress: View
    protected lateinit var empty: View
    protected lateinit var adapter: CommonListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_list_common)

        AuthManager.init(this)

        recycler = findViewById(R.id.recycler)
        progress = findViewById(R.id.progress)
        empty = findViewById(R.id.empty)

        adapter = CommonListAdapter(mutableListOf()) { onItemClick(it) }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        recycler.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )

        load()
    }

    private fun load() {
        toggle(loading = true)
        lifecycleScope.launch {
            try {
                val data = fetchItems()
                adapter.submit(data)
                toggle(empty = data.isEmpty())
            } catch (e: Exception) {
                e.printStackTrace()
                toggle(empty = true)
            }
        }
    }

    protected fun userPk(): Long = AuthManager.id()

    protected fun toggle(loading: Boolean = false, empty: Boolean = false) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        this.empty.visibility = if (!loading && empty) View.VISIBLE else View.GONE
        recycler.visibility = if (!loading && !empty) View.VISIBLE else View.GONE
    }

    protected abstract suspend fun fetchItems(): List<CommonItem>
    protected open fun onItemClick(item: CommonItem) {}
}
