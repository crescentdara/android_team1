package bitc.fullstack502.android_studio.ui.mypage

import android.os.Bundle
import bitc.fullstack502.android_studio.model.CommonItem
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.network.dto.*

class MyCommentsActivity : BaseListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); title = "내가 쓴 댓글"
    }
    override suspend fun fetchItems(): List<CommonItem> {
        val list = ApiProvider.api.getMyComments(userPk())
        return list.map {
            CommonItem(
                id = it.id,
                title = it.content.orEmpty(),
                subtitle = "원글: ${it.postTitle.orEmpty()}",
                imageUrl = null
            )
        }
    }
}