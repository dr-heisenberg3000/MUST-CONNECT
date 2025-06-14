package com.example.must_connect.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.backendless.Backendless
import com.backendless.persistence.DataQueryBuilder
import com.example.must_connect.R
import com.example.must_connect.auth.PasswordChangeActivity
import com.example.must_connect.databinding.ActivityStudentBinding
import com.example.must_connect.models.Post
import com.example.must_connect.posts.PostAdapter
import com.example.must_connect.posts.PostDetailActivity

class StudentDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentBinding
    private lateinit var adapter: PostAdapter
    private var posts = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadPosts()

        binding.swipeRefresh.setOnRefreshListener {
            loadPosts()
        }
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(posts, false) { post ->
            // Open post detail when clicked
            val intent = Intent(this, PostDetailActivity::class.java)
            intent.putExtra("post_id", post.objectId)
            startActivity(intent)
        }
        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = adapter
    }

    private fun loadPosts() {
        binding.progressBar.visibility = View.VISIBLE

        val query = DataQueryBuilder.create()
        query.sortBy = "created DESC"

        Backendless.Data.of(Post::class.java).find(query, object : AsyncCallback<List<Post>> {
            override fun handleResponse(foundPosts: List<Post>?) {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                posts.clear()
                foundPosts?.let { posts.addAll(it) }
                adapter.notifyDataSetChanged()
            }

            override fun handleFault(fault: BackendlessFault) {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(this@StudentDashboardActivity, "Failed to load posts: ${fault.message}", Toast.LENGTH_SHORT).show()
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
            else -> super.onOptionsItemSelected(item)
        }
    }
}