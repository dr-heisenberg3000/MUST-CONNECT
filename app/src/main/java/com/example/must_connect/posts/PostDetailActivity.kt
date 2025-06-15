package com.example.must_connect.posts

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import com.example.must_connect.databinding.ActivityPostDetailBinding
import com.example.must_connect.models.Comment
import com.example.must_connect.models.Post
import com.google.android.material.snackbar.Snackbar
import com.example.must_connect.App

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var post: Post
    private lateinit var commentAdapter: CommentAdapter
    private var comments = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val postId = intent.getStringExtra("post_id") ?: run {
            Toast.makeText(this, "Post not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupCommentsRecycler()
        loadPost(postId)

        binding.btnSubmitComment.setOnClickListener {
            submitComment()
        }
    }

    private fun loadPost(postId: String) {
        Backendless.Data.of(Post::class.java).findById(postId, object : AsyncCallback<Post?> {
            override fun handleResponse(foundPost: Post?) {
                foundPost?.let {
                    post = it
                    displayPost()
                    loadComments()
                } ?: run {
                    Toast.makeText(this@PostDetailActivity, "Post not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun handleFault(fault: BackendlessFault) {
                Toast.makeText(this@PostDetailActivity, "Failed to load post: ${fault.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun displayPost() {
        binding.tvTitle.text = post.title
        binding.tvContent.text = post.content
        binding.layoutComments.visibility = if (post.allowComments) View.VISIBLE else View.GONE
    }

    private fun setupCommentsRecycler() {
        commentAdapter = CommentAdapter(comments)
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = commentAdapter
    }

    private fun loadComments() {
        val postId = post.objectId ?: return

        val whereClause = "postId = '$postId'"
        val query = DataQueryBuilder.create().apply {
            this.whereClause = whereClause
            this.sortBy = listOf("created ASC")
        }

        Backendless.Data.of(Comment::class.java).find(query, object : AsyncCallback<List<Comment>?> {
            override fun handleResponse(foundComments: List<Comment>?) {
                comments.clear()
                foundComments?.let { comments.addAll(it) }
                commentAdapter.notifyDataSetChanged()
            }

            override fun handleFault(fault: BackendlessFault) {
                Snackbar.make(binding.root, "Failed to load comments: ${fault.message}", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    private fun submitComment() {
        val commentText = binding.etComment.text.toString().trim()
        if (commentText.isEmpty()) return

        val currentUser = App.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not found, please login again", Toast.LENGTH_SHORT).show()
            return
        }

        val comment = Comment().apply {
            this.text = commentText
            this.postId = post.objectId
            this.authorId = currentUser.objectId
            this.authorName = currentUser.fullName ?: "Anonymous"
        }

        Backendless.Data.of(Comment::class.java).save(comment, object : AsyncCallback<Comment?> {
            override fun handleResponse(response: Comment?) {
                response?.let {
                    binding.etComment.text?.clear()
                    comments.add(it)
                    commentAdapter.notifyItemInserted(comments.size - 1)
                }
            }

            override fun handleFault(fault: BackendlessFault) {
                Snackbar.make(binding.root, "Failed to post comment: ${fault.message}", Snackbar.LENGTH_SHORT).show()
            }
        })
    }
}