package com.example.must_connect.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import com.example.must_connect.models.AppUser
import com.example.must_connect.models.Post
import com.example.must_connect.posts.CreatePostActivity
import com.example.must_connect.posts.PostAdapter
import com.example.must_connect.posts.PostDetailActivity
import com.example.must_connect.utils.hide
import com.example.must_connect.utils.show
import com.example.must_connect.App
import com.example.must_connect.utils.ToastUtils

class StudentDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentBinding
    private lateinit var adapter: PostAdapter
    private var posts = mutableListOf<Post>()
    private val authorMap = mutableMapOf<String, String>() // Store author names

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
        }, authorMap)  // Pass author map to adapter

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
                foundPosts?.let {
                    posts.addAll(it)

                    // Collect author IDs
                    val authorIds = it.map { post -> post.authorId ?: "" }.distinct()
                    fetchAuthorNames(authorIds)
                }
            }

            override fun handleFault(fault: BackendlessFault) {
                binding.progressBar.hide()
                binding.swipeRefresh.isRefreshing = false
                ToastUtils.showErrorToast(this@StudentDashboardActivity, "Failed to load posts: ${fault.message}")
            }
        })
    }

    private fun fetchAuthorNames(authorIds: List<String>) {
        if (authorIds.isEmpty()) return

        val whereClause = "objectId IN (${authorIds.joinToString(",") { "'$it'" }})"
        val queryBuilder = DataQueryBuilder.create().apply {
            this.whereClause = whereClause
        }

        Backendless.Data.of(AppUser::class.java).find(queryBuilder, object : AsyncCallback<List<AppUser>> {
            override fun handleResponse(users: List<AppUser>?) {
                users?.forEach { user ->
                    user.objectId?.let { id ->
                        // Use fullName for teachers/admins
                        authorMap[id] = user.fullName ?: user.username
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun handleFault(fault: BackendlessFault) {
                ToastUtils.showErrorToast(this@StudentDashboardActivity, "Failed to load authors: ${fault.message}")
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.student_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_refresh -> {
                loadPosts()
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
        startActivity(Intent(this@StudentDashboardActivity, LoginActivity::class.java))
        finish()
    }
}