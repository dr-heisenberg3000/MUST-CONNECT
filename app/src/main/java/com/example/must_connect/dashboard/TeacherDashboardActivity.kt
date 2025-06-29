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
import com.example.must_connect.databinding.ActivityTeacherBinding
import com.example.must_connect.models.AppUser
import com.example.must_connect.models.Post
import com.example.must_connect.posts.CreatePostActivity
import com.example.must_connect.posts.PostAdapter
import com.example.must_connect.posts.PostDetailActivity
import com.example.must_connect.utils.hide
import com.example.must_connect.utils.show
import com.example.must_connect.App
import com.example.must_connect.utils.ToastUtils

class TeacherDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherBinding
    private lateinit var adapter: PostAdapter
    private var posts = mutableListOf<Post>()
    private val authorMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Teacher Portal"
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        setupRecyclerView()
        loadPosts()
        binding.fabCreatePost.setOnClickListener {
            startActivityForResult(Intent(this, CreatePostActivity::class.java), 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadPosts()
        }
    }

    override fun onResume() {
        super.onResume()
        loadPosts()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.teacher_menu, menu)
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

    private fun setupRecyclerView() {
        val currentUserId = App.currentUser?.objectId ?: ""

        adapter = PostAdapter(posts, true, object : PostAdapter.OnItemClickListener {
            override fun onItemClick(post: Post) {
                val intent = Intent(this@TeacherDashboardActivity, PostDetailActivity::class.java)
                intent.putExtra("post_id", post.objectId)
                startActivity(intent)
            }

            override fun onEditClick(post: Post) {
                if (post.authorId == currentUserId) {
                    val intent = Intent(this@TeacherDashboardActivity, CreatePostActivity::class.java)
                    intent.putExtra("post_id", post.objectId)
                    startActivityForResult(intent, 1)
                } else {
                    ToastUtils.showErrorToast(this@TeacherDashboardActivity, "You can only edit your own posts")
                }
            }

            override fun onDeleteClick(post: Post) {
                if (post.authorId == currentUserId) {
                    deletePost(post)
                } else {
                    ToastUtils.showErrorToast(this@TeacherDashboardActivity, "You can only delete your own posts")
                }
            }
        }, authorMap)

        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = adapter
    }

    private fun deletePost(post: Post) {
        Backendless.Data.of(Post::class.java).remove(post, object : AsyncCallback<Long> {
            override fun handleResponse(response: Long?) {
                loadPosts()
                ToastUtils.showSuccessToast(this@TeacherDashboardActivity, "Post deleted")
            }

            override fun handleFault(fault: BackendlessFault) {
                ToastUtils.showErrorToast(this@TeacherDashboardActivity, "Delete failed: ${fault.message}")
            }
        })
    }

    private fun loadPosts() {
        binding.progressBar.show()

        val queryBuilder = DataQueryBuilder.create().apply {
            sortBy = listOf("created DESC")
        }

        Backendless.Data.of(Post::class.java).find(queryBuilder, object : AsyncCallback<List<Post>> {
            override fun handleResponse(foundPosts: List<Post>?) {
                binding.progressBar.hide()
                posts.clear()
                foundPosts?.let {
                    posts.addAll(it)
                    val authorIds = it.map { post -> post.authorId ?: "" }.distinct()
                    fetchAuthorNames(authorIds)
                }
            }

            override fun handleFault(fault: BackendlessFault) {
                binding.progressBar.hide()
                ToastUtils.showErrorToast(this@TeacherDashboardActivity, "Failed to load posts: ${fault.message}")
            }
        })
    }

    private fun fetchAuthorNames(authorIds: List<String>) {
        if (authorIds.isEmpty()) return

        val whereClause = "objectId IN (${authorIds.joinToString(",") { "'$it'" }})"
        val queryBuilder = DataQueryBuilder.create().apply { this.whereClause = whereClause }

        Backendless.Data.of(AppUser::class.java).find(queryBuilder, object : AsyncCallback<List<AppUser>> {
            override fun handleResponse(users: List<AppUser>?) {
                users?.forEach { user ->
                    user.objectId?.let { id ->
                        authorMap[id] = user.fullName ?: user.username
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun handleFault(fault: BackendlessFault) {
                ToastUtils.showErrorToast(this@TeacherDashboardActivity, "Failed to load authors: ${fault.message}")
            }
        })
    }

    private fun logout() {
        App.currentUser = null
        startActivity(Intent(this@TeacherDashboardActivity, LoginActivity::class.java))
        finish()
    }
}