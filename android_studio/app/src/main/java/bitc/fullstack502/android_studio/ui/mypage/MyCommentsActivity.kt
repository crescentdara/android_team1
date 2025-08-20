package bitc.fullstack502.android_studio.ui.mypage

import android.content.Intent
import android.os.Bundle
import bitc.fullstack502.android_studio.model.CommonItem
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.ui.post.PostDetailActivity

class MyCommentsActivity : BaseListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "내가 쓴 댓글"
    }

    override suspend fun fetchItems(): List<CommonItem> {
        val comments = ApiProvider.api.getMyComments(userPk())
        return comments.map { c ->
            CommonItem(
                id = c.postId,                              // 게시글 상세 이동을 위해 postId 사용
                title = c.postTitle ?: "댓글이 달린 글",     // 글 제목
                subtitle = c.content ?: "",                 // 내가 쓴 댓글 내용
                imageUrl = c.postImgUrl                     // 🔥 서버에서 내려온 URL 그대로 사용
            )
        }
    }


    override fun onItemClick(item: CommonItem) {
        startActivity(
            Intent(this, PostDetailActivity::class.java)
                .putExtra("postId", item.id)
        )
    }
}
