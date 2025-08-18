package bitc.fullstack502.android_studio

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.android_studio.adapter.LodgingAdapter
import bitc.fullstack502.android_studio.model.Lodging
import bitc.fullstack502.android_studio.network.LodgingApi
import bitc.fullstack502.android_studio.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LodgingListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LodgingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lodging_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchLodgings()
    }

    private fun fetchLodgings() {
        val api = RetrofitClient.lodgingApiService
        api.getLodgings().enqueue(object : Callback<List<Lodging>> {
            override fun onResponse(call: Call<List<Lodging>>, response: Response<List<Lodging>>) {
                if (response.isSuccessful && response.body() != null) {
                    adapter = LodgingAdapter(this@LodgingListActivity, response.body()!!)
                    recyclerView.adapter = adapter
                } else {
                    Toast.makeText(this@LodgingListActivity, "서버 응답 오류", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Lodging>>, t: Throwable) {
                Toast.makeText(this@LodgingListActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                Log.e("LodgingListActivity", "API 호출 실패", t)
            }
        })
    }

}
