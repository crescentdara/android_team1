package bitc.fullstack502.android_studio.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bitc.fullstack502.android_studio.data.PostListItem
import bitc.fullstack502.android_studio.data.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PostUiState(
    val loading: Boolean = false,
    val items: List<PostListItem> = emptyList(),
    val error: String? = null
)

class PostViewModel(
    private val repo: PostRepository = PostRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(PostUiState())
    val ui: StateFlow<PostUiState> = _ui

    fun load(q: String = "") {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            try {
                val page = repo.getPosts(q, page = 0, size = 20)
                _ui.value = PostUiState(loading = false, items = page.content)
            } catch (e: Exception) {
                _ui.value = PostUiState(loading = false, error = e.message)
            }
        }
    }

    fun create(userId: Long, title: String, content: String?) {
        viewModelScope.launch {
            try {
                repo.createPost(userId, title, content)
                load() // 새로고침
            } catch (e: Exception) {
                // 에러 처리 가능
            }
        }
    }
}
