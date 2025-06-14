package com.example.must_connect.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.backendless.Backendless
import com.backendless.persistence.DataQueryBuilder
import com.example.must_connect.R
import com.example.must_connect.auth.PasswordChangeActivity
import com.example.must_connect.databinding.ActivityTeacherBinding
import com.example.must_connect.models.Post
import com.example.must_connect.posts.CreatePostActivity
import com.example.must_connect.posts.PostAdapter
import android.widget.Toast

class TeacherDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherBinding
    private lateinit var adapter: PostAdapter
    private var posts = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadPosts()

        binding.fabCreatePost.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(posts, true) // true for teacher
        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = adapter
    }

    private fun loadPosts() {
        binding.progressBar.visibility = View.VISIBLE

        val currentUserId = Backendless.UserService.CurrentUser().objectId
        val query = DataQueryBuilder.create()
        query.whereClause = "createdBy = '$currentUserId'"
        query.sortBy = "created DESC"

        Backendless.Data.of(Post::class.java).find(query, object : AsyncCallback<List<Post>> {
            override fun handleResponse(foundPosts: List<Post>?) {
                binding.progressBar.visibility = View.GONE
                posts.clear()
                foundPosts?.let { posts.addAll(it) }
                adapter.notifyDataSetChanged()
            }

            override fun handleFault(fault: BackendlessFault) {
                binding.progressBar.visibility = View.GONE
                showToast("Failed to load posts: ${fault.message}")
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.teacher_menu, menu)

        // Show admin menu only for admin users
        val isAdmin = Backendless.UserService.CurrentUser().getProperty("role") == "admin"
        menu?.findItem(R.id.menu_admin)?.isVisible = isAdmin

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_change_password -> {
                startActivity(Intent(this, PasswordChangeActivity::class.java))
                true
            }
            R.id.menu_admin -> {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}