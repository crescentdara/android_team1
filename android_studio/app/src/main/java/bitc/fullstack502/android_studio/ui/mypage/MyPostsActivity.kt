package bitc.fullstack502.android_studio.ui.mypage

import android.os.Bundle
import bitc.fullstack502.android_studio.model.CommonItem
import bitc.fullstack502.android_studio.network.ApiProvider
import bitc.fullstack502.android_studio.network.dto.*

class MyPostsActivity : BaseListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); title = "내가 쓴 글"
    }
    override suspend fun fetchItems(): List<CommonItem> {
        val list = ApiProvider.api.getMyPosts(userPk())
        return list.map { CommonItem(it.id, it.title, it.content.orEmpty(), it.imgUrl) }
    }
}