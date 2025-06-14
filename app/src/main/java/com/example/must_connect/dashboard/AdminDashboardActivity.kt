package com.example.must_connect.dashboard

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import com.example.must_connect.R
import com.example.must_connect.databinding.ActivityAdminBinding
import com.example.must_connect.models.Post
import com.example.must_connect.posts.PostAdapter

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var adapter: PostAdapter
    private var posts = mutableListOf<Post>()
    private var userCount = 0
    private var postCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadStatistics()
        loadPosts()
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(posts, false) // No edit/delete for admin
        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = adapter
    }

    private fun loadStatistics() {
        // User count
        Backendless.Data.of("Users").getObjectCount(object : AsyncCallback<Int> {
            override fun handleResponse(count: Int?) {
                userCount = count ?: 0
                updateStatsUI()
            }
            override fun handleFault(fault: BackendlessFault) {
                binding.tvUserCount.text = "Users: Error"
            }
        })

        // Post count
        Backendless.Data.of("Posts").getObjectCount(object : AsyncCallback<Int> {
            override fun handleResponse(count: Int?) {
                postCount = count ?: 0
                updateStatsUI()
            }
            override fun handleFault(fault: BackendlessFault) {
                binding.tvPostCount.text = "Posts: Error"
            }
        })
    }

    private fun updateStatsUI() {
        binding.tvUserCount.text = "Total Users: $userCount"
        binding.tvPostCount.text = "Total Posts: $postCount"
    }

    private fun loadPosts() {
        val query = DataQueryBuilder.create()
        query.sortBy = "created DESC"
        query.pageSize = 20 // Limit to recent posts

        Backendless.Data.of(Post::class.java).find(query, object : AsyncCallback<List<Post>> {
            override fun handleResponse(foundPosts: List<Post>?) {
                posts.clear()
                foundPosts?.let { posts.addAll(it) }
                adapter.notifyDataSetChanged()
            }

            override fun handleFault(fault: BackendlessFault) {
                Toast.makeText(this@AdminDashboardActivity, "Failed to load posts: ${fault.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}