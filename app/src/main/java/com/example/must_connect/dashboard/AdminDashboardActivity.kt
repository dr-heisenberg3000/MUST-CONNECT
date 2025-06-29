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
import com.example.must_connect.auth.CreateUserActivity
import com.example.must_connect.auth.PasswordChangeActivity
import com.example.must_connect.databinding.ActivityAdminBinding
import com.example.must_connect.models.AppUser
import com.example.must_connect.models.Post
import com.example.must_connect.posts.PostAdapter
import com.example.must_connect.posts.PostDetailActivity
import com.example.must_connect.utils.hide
import com.example.must_connect.utils.show
import com.example.must_connect.App
import com.example.must_connect.utils.ToastUtils

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var adapter: PostAdapter
    private var posts = mutableListOf<Post>()
    private var userCount = 0
    private var postCount = 0
    private val authorMap = mutableMapOf<String, String>()

    private val postItemClickListener = object : PostAdapter.OnItemClickListener {
        override fun onItemClick(post: Post) {
            val intent = Intent(this@AdminDashboardActivity, PostDetailActivity::class.java)
            intent.putExtra("post_id", post.objectId)
            startActivity(intent)
        }

        override fun onEditClick(post: Post) {
            // Not used for admin
        }

        override fun onDeleteClick(post: Post) {
            deletePost(post)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Admin Portal"
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        setupRecyclerView()
        loadStatistics()
        loadPosts()
    }

    private fun setupRecyclerView() {
        // Set isAdmin = true
        adapter = PostAdapter(posts, true, postItemClickListener, authorMap, false, true)
        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = adapter
    }

    private fun deletePost(post: Post) {
        Backendless.Data.of(Post::class.java).remove(post, object : AsyncCallback<Long> {
            override fun handleResponse(response: Long?) {
                loadPosts()
                ToastUtils.showSuccessToast(this@AdminDashboardActivity, "Post deleted")
            }

            override fun handleFault(fault: BackendlessFault) {
                ToastUtils.showErrorToast(this@AdminDashboardActivity, "Delete failed: ${fault.message}")
            }
        })
    }

    private fun loadStatistics() {
        Backendless.Data.of(AppUser::class.java).getObjectCount(object : AsyncCallback<Int> {
            override fun handleResponse(count: Int?) {
                userCount = count ?: 0
                binding.tvUserCount.text = "Total Users: $userCount"
            }
            override fun handleFault(fault: BackendlessFault) {
                binding.tvUserCount.text = "Users: Error"
            }
        })

        Backendless.Data.of(Post::class.java).getObjectCount(object : AsyncCallback<Int> {
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
                foundPosts?.let {
                    posts.addAll(it)

                    // Collect author IDs
                    val authorIds = it.map { post -> post.authorId ?: "" }.distinct()
                    fetchAuthorNames(authorIds)
                }
            }

            override fun handleFault(fault: BackendlessFault) {
                binding.progressBar.hide()
                ToastUtils.showErrorToast(this@AdminDashboardActivity, "Failed to load posts: ${fault.message}")
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
                ToastUtils.showErrorToast(this@AdminDashboardActivity, "Failed to load authors: ${fault.message}")
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
            R.id.menu_create_user -> {
                startActivity(Intent(this, CreateUserActivity::class.java))
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