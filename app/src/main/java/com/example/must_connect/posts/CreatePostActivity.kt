package com.example.must_connect.posts

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.example.must_connect.R
import com.example.must_connect.databinding.ActivityCreatePostBinding
import com.example.must_connect.models.Post
import com.example.must_connect.utils.BackendlessUtils
import com.example.must_connect.App
import com.example.must_connect.utils.hide
import com.example.must_connect.utils.show
import com.example.must_connect.utils.ToastUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private var mediaUri: Uri? = null
    private val pollOptions = mutableListOf<String>()
    private var editingPostId: String? = null
    private var existingMediaUrl: String? = null
    private var isMediaRemoved = false
    private var optionCount = 2
    private val gson = Gson()

    private val mediaPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let {
                mediaUri = it
                binding.tvMediaStatus.text = "1 file selected"
                binding.btnRemoveMedia.visibility = View.VISIBLE
                isMediaRemoved = false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editingPostId = intent.getStringExtra("post_id")
        if (editingPostId != null) {
            loadExistingPost(editingPostId!!)
        }

        setupPostTypeSpinner()

        binding.btnAttachMedia.setOnClickListener { openMediaPicker() }
        binding.btnAddOption.setOnClickListener { addPollOption() }

        binding.btnRemoveMedia.setOnClickListener {
            mediaUri = null
            existingMediaUrl = null
            binding.tvMediaStatus.text = getString(R.string.no_media_selected)
            binding.btnRemoveMedia.visibility = View.GONE
            isMediaRemoved = true
        }

        binding.btnSubmit.setOnClickListener {
            if (editingPostId != null) updatePost() else createPost()
        }
    }

    private fun loadExistingPost(postId: String) {
        binding.progressBar.show()

        Backendless.Data.of(Post::class.java).findById(postId, object : AsyncCallback<Post?> {
            override fun handleResponse(post: Post?) {
                binding.progressBar.hide()
                post?.let {
                    binding.etTitle.setText(it.title)
                    binding.etContent.setText(it.content)
                    existingMediaUrl = it.mediaUrl
                    isMediaRemoved = false

                    if (!it.mediaUrl.isNullOrEmpty()) {
                        binding.tvMediaStatus.text = "Media attached"
                        binding.btnRemoveMedia.visibility = View.VISIBLE
                    }

                    // Update spinner selection based on post type
                    val typeText = when (it.type) {
                        "notice" -> "Notice"
                        "suggestion" -> "Suggestion"
                        "poll" -> "Poll"
                        else -> "Notice"
                    }
                    binding.spinnerPostType.setText(typeText, false)

                    if (it.type == "poll") {
                        it.pollOptions?.let { jsonOptions ->
                            try {
                                val options = gson.fromJson(jsonOptions, Array<String>::class.java).toList()
                                pollOptions.addAll(options)

                                if (options.size > 0) binding.etOption1.setText(options[0])
                                if (options.size > 1) binding.etOption2.setText(options[1])

                                for (i in 2 until options.size) {
                                    addPollOptionWithText(options[i])
                                }
                            } catch (e: Exception) {
                                ToastUtils.showErrorToast(this@CreatePostActivity, "Failed to load poll options")
                            }
                        }
                    }
                }
            }

            override fun handleFault(fault: BackendlessFault) {
                binding.progressBar.hide()
                ToastUtils.showErrorToast(this@CreatePostActivity, "Failed to load post: ${fault.message}")
            }
        })
    }

    private fun addPollOptionWithText(optionText: String) {
        optionCount++

        val newOptionLayout = layoutInflater.inflate(
            R.layout.item_poll_option,
            binding.containerAdditionalOptions,
            false
        ) as TextInputLayout

        newOptionLayout.hint = "Option $optionCount"
        val editText = newOptionLayout.findViewById<TextInputEditText>(R.id.etOption)
        editText.setText(optionText)
        editText.contentDescription = "Poll option $optionCount"

        binding.containerAdditionalOptions.addView(newOptionLayout)
    }

    private fun setupPostTypeSpinner() {
        val postTypes = listOf("Notice", "Suggestion", "Poll")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, postTypes)
        binding.spinnerPostType.setAdapter(adapter)
        binding.spinnerPostType.setText("", false) // Start with empty text to show hint
        
        binding.spinnerPostType.setOnItemClickListener { _, _, position, _ ->
            binding.cardPollOptions.visibility = if (position == 2) View.VISIBLE else View.GONE
        }
    }

    private fun openMediaPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
        mediaPickerLauncher.launch(intent)
    }

    private fun addPollOption() {
        optionCount++

        val newOptionLayout = layoutInflater.inflate(
            R.layout.item_poll_option,
            binding.containerAdditionalOptions,
            false
        ) as TextInputLayout

        newOptionLayout.hint = "Option $optionCount"
        val editText = newOptionLayout.findViewById<TextInputEditText>(R.id.etOption)
        editText.contentDescription = "Poll option $optionCount"

        binding.containerAdditionalOptions.addView(newOptionLayout)
    }

    private fun createPost() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        val selectedType = binding.spinnerPostType.text.toString()
        val postType = when(selectedType) {
            "Notice" -> "notice"
            "Suggestion" -> "suggestion"
            "Poll" -> "poll"
            else -> "notice"
        }

        val allowComments = (postType == "suggestion")

        if (title.isEmpty() || content.isEmpty()) {
            ToastUtils.showErrorToast(this, "Please fill all fields")
            return
        }

        pollOptions.clear()
        if (postType == "poll") {
            pollOptions.add(binding.etOption1.text.toString().trim())
            pollOptions.add(binding.etOption2.text.toString().trim())

            for (i in 0 until binding.containerAdditionalOptions.childCount) {
                val layout = binding.containerAdditionalOptions.getChildAt(i) as TextInputLayout
                val editText = layout.findViewById<TextInputEditText>(R.id.etOption)
                editText?.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let {
                    pollOptions.add(it)
                }
            }

            if (pollOptions.size < 2) {
                ToastUtils.showErrorToast(this, "Add at least 2 poll options")
                return
            }
        }

        if (mediaUri != null) {
            uploadMediaAndCreatePost(title, content, postType, allowComments)
        } else {
            val mediaUrl = if (editingPostId != null && !isMediaRemoved && existingMediaUrl != null) existingMediaUrl else null
            savePost(title, content, postType, allowComments, mediaUrl)
        }
    }

    private fun updatePost() = createPost()

    private fun uploadMediaAndCreatePost(title: String, content: String, postType: String, allowComments: Boolean) {
        mediaUri?.let { uri ->
            BackendlessUtils.uploadMedia(this, uri) { mediaUrl ->
                if (mediaUrl != null) {
                    savePost(title, content, postType, allowComments, mediaUrl)
                } else {
                    ToastUtils.showErrorToast(this, "Failed to upload media")
                }
            }
        }
    }

    private fun savePost(title: String, content: String, postType: String, allowComments: Boolean, mediaUrl: String?) {
        val currentUser = App.currentUser ?: run {
            ToastUtils.showErrorToast(this, "User not found, please login again")
            finish()
            return
        }

        val pollOptionsJson = if (postType == "poll" && pollOptions.isNotEmpty()) {
            gson.toJson(pollOptions)
        } else null

        val pollVotesJson = if (postType == "poll") {
            val votesMap = mutableMapOf<String, Int>().apply {
                pollOptions.forEach { option -> put(option, 0) }
            }
            gson.toJson(votesMap)
        } else "{}"

        val votedUserIdsJson = if (postType == "poll") "[]" else "[]"

        val post = if (editingPostId != null) {
            Backendless.Data.of(Post::class.java).findById(editingPostId, object : AsyncCallback<Post?> {
                override fun handleResponse(existingPost: Post?) {
                    existingPost?.let {
                        val updatedPost = Post().apply {
                            objectId = editingPostId
                            this.title = title
                            this.content = content
                            this.type = postType
                            this.mediaUrl = mediaUrl
                            this.allowComments = allowComments
                            this.authorId = currentUser.objectId
                            this.pollOptions = pollOptionsJson
                            this.pollVotes = existingPost.pollVotes
                            this.votedUserIds = existingPost.votedUserIds
                        }
                        saveToBackend(updatedPost)
                    }
                }

                override fun handleFault(fault: BackendlessFault) {
                    ToastUtils.showErrorToast(this@CreatePostActivity, "Error loading post: ${fault.message}")
                }
            })
            return
        } else {
            Post().apply {
                this.title = title
                this.content = content
                this.type = postType
                this.mediaUrl = mediaUrl
                this.allowComments = allowComments
                this.authorId = currentUser.objectId
                this.pollOptions = pollOptionsJson
                this.pollVotes = pollVotesJson
                this.votedUserIds = votedUserIdsJson
            }
        }

        saveToBackend(post)
    }

    private fun saveToBackend(post: Post) {
        Backendless.Data.of(Post::class.java).save(post, object : AsyncCallback<Post> {
            override fun handleResponse(response: Post?) {
                ToastUtils.showSuccessToast(
                    this@CreatePostActivity,
                    if (editingPostId != null) "Post updated" else "Post created"
                )
                setResult(RESULT_OK)
                finish()
            }

            override fun handleFault(fault: BackendlessFault) {
                ToastUtils.showErrorToast(this@CreatePostActivity, "Error: ${fault.message}")
            }
        })
    }
}