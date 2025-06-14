package com.example.must_connect.posts

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.example.must_connect.R
import com.example.must_connect.databinding.ActivityPostDetailBinding
import com.example.must_connect.models.Comment
import com.example.must_connect.models.Post
import com.google.android.material.snackbar.Snackbar

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
            finish()
            return
        }

        loadPost(postId)
        setupCommentsRecycler()

        binding.btnSubmitComment.setOnClickListener {
            submitComment()
        }
    }

    private fun loadPost(postId: String) {
        Backendless.Data.of(Post::class.java).findById(postId, object : AsyncCallback<Post> {
            override fun handleResponse(foundPost: Post) {
                post = foundPost
                displayPost()
                loadComments()
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

        if (!post.mediaUrl.isNullOrEmpty()) {
            // Load media using Glide or similar
        }

        // Show/hide comment section based on post type
        binding.layoutComments.visibility = if (post.type == "suggestion") View.VISIBLE else View.GONE
    }

    private fun setupCommentsRecycler() {
        commentAdapter = CommentAdapter(comments)
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = commentAdapter
    }

    private fun loadComments() {
        val whereClause = "postId = '$post.objectId'"
        val query = DataQueryBuilder.create()
        query.whereClause = whereClause
        query.sortBy = "created ASC"

        Backendless.Data.of(Comment::class.java).find(query, object : AsyncCallback<List<Comment>> {
            override fun handleResponse(foundComments: List<Comment>?) {
                comments.clear()
                foundComments?.let { comments.addAll(it) }
                commentAdapter.notifyDataSetChanged()
            }

            override fun handleFault(fault: BackendlessFault) {
                Snackbar.make(binding.root, "Failed to load comments", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    private fun submitComment() {
        val commentText = binding.etComment.text.toString().trim()
        if (commentText.isEmpty()) return

        val comment = Comment().apply {
            this.text = commentText
            this.postId = post.objectId
            this.authorId = Backendless.UserService.CurrentUser().objectId
            this.authorName = Backendless.UserService.CurrentUser().getProperty("name") as? String ?: "Anonymous"
        }

        Backendless.Data.of(Comment::class.java).save(comment, object : AsyncCallback<Comment> {
            override fun handleResponse(response: Comment) {
                binding.etComment.text.clear()
                comments.add(response)
                commentAdapter.notifyItemInserted(comments.size - 1)
            }

            override fun handleFault(fault: BackendlessFault) {
                Snackbar.make(binding.root, "Failed to post comment", Snackbar.LENGTH_SHORT).show()
            }
        })
    }
}