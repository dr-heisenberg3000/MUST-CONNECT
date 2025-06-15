package com.example.must_connect.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import com.example.must_connect.R
import com.example.must_connect.auth.LoginActivity
import com.example.must_connect.databinding.ActivityAdminBinding
import com.example.must_connect.models.Post
import com.example.must_connect.posts.PostAdapter
import com.example.must_connect.utils.hide
import com.example.must_connect.utils.show
import com.example.must_connect.App
import com.example.must_connect.auth.PasswordChangeActivity

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var adapter: PostAdapter
    private var posts = mutableListOf<Post>()
    private var userCount = 0
    private var postCount = 0

    private val postItemClickListener = object : PostAdapter.OnItemClickListener {
        override fun onItemClick(post: Post) {
            Toast.makeText(this@AdminDashboardActivity, "Clicked: ${post.title}", Toast.LENGTH_SHORT).show()
        }
        override fun onEditClick(post: Post) {}
        override fun onDeleteClick(post: Post) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupRecyclerView()
        loadStatistics()
        loadPosts()
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(posts, false, postItemClickListener)
        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = adapter
    }

    private fun loadStatistics() {
        Backendless.Data.of("AppUsers").getObjectCount(object : AsyncCallback<Int> {
            override fun handleResponse(count: Int?) {
                userCount = count ?: 0
                binding.tvUserCount.text = "Total Users: $userCount"
            }
            override fun handleFault(fault: BackendlessFault) {
                binding.tvUserCount.text = "Users: Error"
            }
        })

        Backendless.Data.of("Posts").getObjectCount(object : AsyncCallback<Int> {
            override fun handleResponse(count: Int?) {
                postCount = count ?: 0
                binding.tvPostCount.text = "Total Posts: $postCount"
            }
            override fun handleFault(fault: BackendlessFault) {
                binding.tvPostCount.text = "Posts: Error"
            }
        })
    }

    private fun loadPosts() {
        binding.progressBar.show()

        val queryBuilder = DataQueryBuilder.create().apply {
            sortBy = listOf("created DESC")
            setPageSize(20)
        }

        Backendless.Data.of(Post::class.java).find(queryBuilder, object : AsyncCallback<List<Post>> {
            override fun handleResponse(foundPosts: List<Post>?) {
                binding.progressBar.hide()
                posts.clear()
                foundPosts?.let { posts.addAll(it) }
                adapter.notifyDataSetChanged()
            }

            override fun handleFault(fault: BackendlessFault) {
                binding.progressBar.hide()
                Toast.makeText(
                    this@AdminDashboardActivity,
                    "Failed to load posts: ${fault.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.admin_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_refresh -> {
                loadPosts()
                loadStatistics()
                true
            }
            R.id.menu_change_password -> {
                startActivity(Intent(this, PasswordChangeActivity::class.java))
                true
            }
            R.id.menu_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        App.currentUser = null
        startActivity(Intent(this@AdminDashboardActivity, LoginActivity::class.java))
        finish()
    }
}