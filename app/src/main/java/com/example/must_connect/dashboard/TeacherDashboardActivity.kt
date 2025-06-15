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
import com.example.must_connect.databinding.ActivityTeacherBinding
import com.example.must_connect.models.Post
import com.example.must_connect.posts.CreatePostActivity
import com.example.must_connect.posts.PostAdapter
import com.example.must_connect.posts.PostDetailActivity
import com.example.must_connect.utils.hide
import com.example.must_connect.utils.show
import com.example.must_connect.App

class TeacherDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherBinding
    private lateinit var adapter: PostAdapter
    private var posts = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Teacher Portal"
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        setupRecyclerView()
        loadPosts()

        binding.fabCreatePost.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(posts, true, object : PostAdapter.OnItemClickListener {
            override fun onItemClick(post: Post) {
                val intent = Intent(this@TeacherDashboardActivity, PostDetailActivity::class.java)
                intent.putExtra("post_id", post.objectId)
                startActivity(intent)
            }

            override fun onEditClick(post: Post) {
                val intent = Intent(this@TeacherDashboardActivity, CreatePostActivity::class.java)
                intent.putExtra("post_id", post.objectId)
                startActivity(intent)
            }

            override fun onDeleteClick(post: Post) {
                Backendless.Data.of(Post::class.java).remove(post, object : AsyncCallback<Long> {
                    override fun handleResponse(response: Long?) {
                        loadPosts()
                        Toast.makeText(
                            this@TeacherDashboardActivity,
                            "Post deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun handleFault(fault: BackendlessFault) {
                        Toast.makeText(
                            this@TeacherDashboardActivity,
                            "Delete failed: ${fault.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        })

        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = adapter
    }

    private fun loadPosts() {
        val currentUser = App.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not found, please login again", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.progressBar.show()

        val queryBuilder = DataQueryBuilder.create().apply {
            whereClause = "authorId = '${currentUser.objectId}'"
            sortBy = listOf("created DESC")
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
                    this@TeacherDashboardActivity,
                    "Failed to load posts: ${fault.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.teacher_menu, menu)
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
        startActivity(Intent(this@TeacherDashboardActivity, LoginActivity::class.java))
        finish()
    }
}