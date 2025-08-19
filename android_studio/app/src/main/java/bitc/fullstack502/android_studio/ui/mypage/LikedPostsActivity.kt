package bitc.fullstack502.android_studio.ui.mypage

import android.os.Bundle
import bitc.fullstack502.android_studio.model.CommonItem
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.network.dto.*

class LikedPostsActivity : BaseListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); title = "좋아요 한 게시글"
    }
    override suspend fun fetchItems(): List<CommonItem> {
        val list = ApiProvider.api.getLikedPosts(userPk())
        return list.map { CommonItem(it.id, it.title, it.content.orEmpty(), it.imgUrl) }
    }
}