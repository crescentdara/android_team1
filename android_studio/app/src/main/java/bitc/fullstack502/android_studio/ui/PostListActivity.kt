package bitc.fullstack502.android_studio.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import bitc.fullstack502.android_studio.ui.FlowObserver
import bitc.fullstack502.android_studio.databinding.ActivityPostListBinding

class PostListActivity : ComponentActivity() {

    private lateinit var b: ActivityPostListBinding
    private val vm: PostViewModel by viewModels()
    private val adapter = PostAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPostListBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.rv.layoutManager = LinearLayoutManager(this)
        b.rv.adapter = adapter

        b.btnCreate.setOnClickListener {
            val title = b.etTitle.text?.toString()?.ifBlank { null } ?: "테스트 글"
            vm.create(userId = 1L, title = title, content = "안드로이드에서 생성")
        }

        lifecycle.addObserver(FlowObserver(this) {
            vm.ui.collect { state ->
                if (state.items.isNotEmpty()) adapter.submit(state.items)
            }
        })

        vm.load()
    }
}
