package com.example.must_connect.posts

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import com.bumptech.glide.Glide
import com.example.must_connect.databinding.ActivityPostDetailBinding
import com.example.must_connect.models.AppUser
import com.example.must_connect.models.Comment
import com.example.must_connect.models.Post
import com.google.android.material.snackbar.Snackbar
import com.example.must_connect.App
import com.example.must_connect.R
import com.example.must_connect.utils.DateUtils
import com.example.must_connect.utils.ToastUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var post: Post
    private lateinit var commentAdapter: CommentAdapter
    private var comments = mutableListOf<Comment>()
    private val gson = Gson()
    private var radioGroup: RadioGroup? = null
    private val authorMap = mutableMapOf<String, String>() // Store author names

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        val postId = intent.getStringExtra("post_id")
        if (postId != null) {
            loadPost(postId)
        } else {
            ToastUtils.showErrorToast(this, "Post not found")
            finish()
        }

        binding.btnSubmitComment.setOnClickListener {
            submitComment()
        }

        binding.btnVote.setOnClickListener {
            submitVote()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun loadPost(postId: String) {
        Backendless.Data.of(Post::class.java).findById(postId, object : AsyncCallback<Post?> {
            override fun handleResponse(response: Post?) {
                if (response != null) {
                    post = response
                    displayPost()
                    loadComments()
                } else {
                    ToastUtils.showErrorToast(this@PostDetailActivity, "Post not found")
                    finish()
                }
            }

            override fun handleFault(fault: BackendlessFault) {
                ToastUtils.showErrorToast(this@PostDetailActivity, "Failed to load post: ${fault.message}")
                finish()
            }
        })
    }

    private fun displayPost() {
        binding.tvTitle.text = post.title
        binding.tvContent.text = post.content
        binding.tvTime.text = post.createdAt?.let { DateUtils.formatDateTime(it) } ?: ""
        binding.tvType.text = post.type.capitalize()

        // Load author name
        loadAuthorName()

        if (!post.mediaUrl.isNullOrEmpty()) {
            binding.ivMedia.visibility = View.VISIBLE
            Glide.with(this)
                .load(post.mediaUrl)
                .placeholder(R.drawable.placeholder)
                .into(binding.ivMedia)
        } else {
            binding.ivMedia.visibility = View.GONE
        }

        when (post.type) {
            "notice" -> {
                binding.layoutComments.visibility = View.GONE
                binding.layoutPoll.visibility = View.GONE
            }
            "suggestion" -> {
                binding.layoutComments.visibility = View.VISIBLE
                binding.layoutPoll.visibility = View.GONE
            }
            "poll" -> {
                binding.layoutComments.visibility = View.GONE
                binding.layoutPoll.visibility = View.VISIBLE
                setupPollOptions()
            }
        }
    }

    private fun loadAuthorName() {
        val authorId = post.authorId
        if (authorId != null) {
            // Check if we already have the author name
            if (authorMap.containsKey(authorId)) {
                binding.tvAuthor.text = "Posted by ${authorMap[authorId]}"
            } else {
                // Fetch author information from Backendless
                Backendless.Data.of(AppUser::class.java).findById(authorId, object : AsyncCallback<AppUser?> {
                    override fun handleResponse(user: AppUser?) {
                        user?.let {
                            val authorName = it.fullName ?: it.username
                            authorMap[authorId] = authorName
                            binding.tvAuthor.text = "Posted by $authorName"
                        } ?: run {
                            binding.tvAuthor.text = "Posted by Unknown"
                        }
                    }

                    override fun handleFault(fault: BackendlessFault) {
                        binding.tvAuthor.text = "Posted by Unknown"
                    }
                })
            }
        } else {
            binding.tvAuthor.text = "Posted by Unknown"
        }
    }

    private fun setupPollOptions() {
        val container = binding.pollOptionsContainer
        container.removeAllViews()

        val options = try {
            if (!post.pollOptions.isNullOrEmpty()) {
                gson.fromJson(post.pollOptions, Array<String>::class.java).toList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        if (options.isEmpty()) {
            binding.layoutPoll.visibility = View.GONE
            ToastUtils.showErrorToast(this, "No poll options available")
            return
        }

        val votesMap: MutableMap<String, Int> = try {
            if (post.pollVotes.isNotEmpty()) {
                gson.fromJson(post.pollVotes, object : TypeToken<Map<String, Int>>() {}.type) ?: mutableMapOf()
            } else mutableMapOf()
        } catch (e: Exception) {
            mutableMapOf()
        }

        val votedUserIds: MutableList<String> = try {
            if (post.votedUserIds.isNotEmpty()) {
                gson.fromJson(post.votedUserIds, object : TypeToken<List<String>>() {}.type) ?: mutableListOf()
            } else mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }

        val currentUserId = App.currentUser?.objectId ?: ""
        val hasVoted = currentUserId in votedUserIds
        val totalVotes = votesMap.values.sum()
        val showResults = hasVoted || App.currentUser?.role in listOf("teacher", "admin")

        if (showResults) {
            options.forEach { option ->
                val votes = votesMap[option] ?: 0
                val percentage = if (totalVotes > 0) (votes.toDouble() * 100 / totalVotes).toInt() else 0

                val optionLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(0, 8, 0, 16) }
                }

                val tvOption = TextView(this).apply {
                    text = "$option ($votes votes, $percentage%)"
                    textSize = 16f
                    setTypeface(null, Typeface.BOLD)
                }
                optionLayout.addView(tvOption)

                val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        resources.getDimensionPixelSize(R.dimen.progress_bar_height)
                    )
                    max = 100
                    progress = percentage
                    progressDrawable = ContextCompat.getDrawable(this@PostDetailActivity, R.drawable.progress_bar_poll)
                }
                optionLayout.addView(progressBar)
                container.addView(optionLayout)
            }

            if (hasVoted) {
                binding.tvVoteStatus.visibility = View.VISIBLE
                binding.tvVoteStatus.text = getString(R.string.already_voted)
            }
            binding.btnVote.visibility = View.GONE
        } else {
            radioGroup = RadioGroup(this).apply {
                orientation = RadioGroup.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            options.forEach { option ->
                RadioButton(this).apply {
                    text = option
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(0, 8, 0, 8) }
                    radioGroup?.addView(this)
                }
            }
            container.addView(radioGroup)
            binding.btnVote.visibility = View.VISIBLE
            binding.tvVoteStatus.visibility = View.GONE
        }
    }

    private fun submitVote() {
        if (App.currentUser?.objectId == null) {
            ToastUtils.showErrorToast(this, "Please login to vote")
            return
        }

        val votedUserIds: MutableList<String> = try {
            if (post.votedUserIds.isNotEmpty()) {
                gson.fromJson(post.votedUserIds, object : TypeToken<List<String>>() {}.type) ?: mutableListOf()
            } else mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }

        val currentUserId = App.currentUser?.objectId ?: ""
        if (currentUserId in votedUserIds) {
            ToastUtils.showErrorToast(this, "You already voted")
            return
        }

        val selectedId = radioGroup?.checkedRadioButtonId ?: -1
        if (selectedId == -1) {
            ToastUtils.showErrorToast(this, "Please select an option")
            return
        }

        val selectedRadioButton = radioGroup?.findViewById<RadioButton>(selectedId)
        val selectedOption = selectedRadioButton?.text?.toString() ?: return

        val votesMap: MutableMap<String, Int> = try {
            if (post.pollVotes.isNotEmpty()) {
                gson.fromJson(post.pollVotes, object : TypeToken<Map<String, Int>>() {}.type) ?: mutableMapOf()
            } else mutableMapOf()
        } catch (e: Exception) {
            mutableMapOf()
        }

        votesMap[selectedOption] = (votesMap[selectedOption] ?: 0) + 1
        votedUserIds.add(currentUserId)

        post.pollVotes = gson.toJson(votesMap)
        post.votedUserIds = gson.toJson(votedUserIds)

        Backendless.Data.of(Post::class.java).save(post, object : AsyncCallback<Post> {
            override fun handleResponse(response: Post?) {
                ToastUtils.showSuccessToast(this@PostDetailActivity, "Vote submitted!")
                setupPollOptions() // Refresh the poll display
            }

            override fun handleFault(fault: BackendlessFault) {
                ToastUtils.showErrorToast(this@PostDetailActivity, "Failed to submit vote: ${fault.message}")
            }
        })
    }

    private fun loadComments() {
        if (post.type != "suggestion") return

        val whereClause = "postId = '${post.objectId}'"
        val queryBuilder = com.backendless.persistence.DataQueryBuilder.create().apply {
            this.whereClause = whereClause
            this.sortBy = listOf("created DESC")
        }

        Backendless.Data.of(Comment::class.java).find(queryBuilder, object : AsyncCallback<List<Comment>> {
            override fun handleResponse(comments: List<Comment>?) {
                comments?.let {
                    val adapter = CommentAdapter(
                        it,
                        showDeleteButton = App.currentUser?.role in listOf("teacher", "admin"),
                        onDeleteClickListener = { comment -> deleteComment(comment) }
                    )
                    binding.rvComments.layoutManager = LinearLayoutManager(this@PostDetailActivity)
                    binding.rvComments.adapter = adapter
                }
            }

            override fun handleFault(fault: BackendlessFault) {
                // Handle error silently for comments
            }
        })
    }

    private fun deleteComment(comment: Comment) {
        Backendless.Data.of(Comment::class.java).remove(comment, object : AsyncCallback<Long> {
            override fun handleResponse(response: Long?) {
                ToastUtils.showSuccessToast(this@PostDetailActivity, "Comment deleted")
                loadComments() // Reload comments
            }

            override fun handleFault(fault: BackendlessFault) {
                ToastUtils.showErrorToast(this@PostDetailActivity, "Failed to delete comment: ${fault.message}")
            }
        })
    }

    private fun submitComment() {
        if (App.currentUser?.role == "teacher") {
            ToastUtils.showErrorToast(this, "Teachers cannot comment")
            return
        }

        val commentText = binding.etComment.text.toString().trim()
        if (commentText.isEmpty()) {
            ToastUtils.showErrorToast(this, "Please enter a comment")
            return
        }

        val currentUser = App.currentUser
        if (currentUser == null) {
            ToastUtils.showErrorToast(this, "User not found, please login again")
            return
        }

        val comment = Comment().apply {
            this.postId = post.objectId
            this.text = commentText
            this.authorId = currentUser.objectId
            this.authorName = currentUser.username
        }

        Backendless.Data.of(Comment::class.java).save(comment, object : AsyncCallback<Comment> {
            override fun handleResponse(response: Comment?) {
                binding.etComment.text?.clear()
                loadComments() // Reload comments
            }

            override fun handleFault(fault: BackendlessFault) {
                ToastUtils.showErrorToast(this@PostDetailActivity, "Failed to submit comment: ${fault.message}")
            }
        })
    }
}