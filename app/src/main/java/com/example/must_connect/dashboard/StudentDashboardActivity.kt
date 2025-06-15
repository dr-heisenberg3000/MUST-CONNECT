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
import com.example.must_connect.auth.PasswordChangeActivity
import com.example.must_connect.databinding.ActivityStudentBinding
import com.example.must_connect.models.Post
import com.example.must_connect.posts.PostAdapter
import com.example.must_connect.posts.PostDetailActivity
import com.example.must_connect.utils.hide
import com.example.must_connect.utils.show
import com.example.must_connect.App

class StudentDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentBinding
    private lateinit var adapter: PostAdapter
    private var posts = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupRecyclerView()
        loadPosts()

        binding.swipeRefresh.setOnRefreshListener {
            loadPosts()
        }
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(posts, false, object : PostAdapter.OnItemClickListener {
            override fun onItemClick(post: Post) {
                val intent = Intent(this@StudentDashboardActivity, PostDetailActivity::class.java)
                intent.putExtra("post_id", post.objectId)
                startActivity(intent)
            }

            override fun onEditClick(post: Post) {}
            override fun onDeleteClick(post: Post) {}
        })

        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = adapter
    }

    private fun loadPosts() {
        binding.progressBar.show()

        val queryBuilder = DataQueryBuilder.create().apply {
            sortBy = listOf("created DESC")
        }

        Backendless.Data.of(Post::class.java).find(queryBuilder, object : AsyncCallback<List<Post>> {
            override fun handleResponse(foundPosts: List<Post>?) {
                binding.progressBar.hide()
                binding.swipeRefresh.isRefreshing = false
                posts.clear()
                foundPosts?.let { posts.addAll(it) }
                adapter.notifyDataSetChanged()
            }

            override fun handleFault(fault: BackendlessFault) {
                binding.progressBar.hide()
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(
                    this@StudentDashboardActivity,
                    "Failed to load posts: ${fault.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.student_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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
        startActivity(Intent(this@StudentDashboardActivity, LoginActivity::class.java))
        finish()
    }
}