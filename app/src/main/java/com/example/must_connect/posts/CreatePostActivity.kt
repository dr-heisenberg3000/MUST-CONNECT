package com.example.must_connect.posts

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.example.must_connect.R
import com.example.must_connect.databinding.ActivityCreatePostBinding
import com.example.must_connect.models.Post
import com.example.must_connect.utils.BackendlessUtils

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private var mediaUri: Uri? = null
    private val PICK_MEDIA_REQUEST = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPostTypeSpinner()

        binding.btnAttachMedia.setOnClickListener {
            openMediaPicker()
        }

        binding.btnSubmit.setOnClickListener {
            createPost()
        }
    }

    private fun setupPostTypeSpinner() {
        val postTypes = listOf("Notice", "Suggestion", "Poll")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, postTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPostType.adapter = adapter
    }

    private fun openMediaPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_MEDIA_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_MEDIA_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let {
                mediaUri = it
                binding.tvMediaStatus.text = "1 file selected"
            }
        }
    }

    private fun createPost() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        val postType = when(binding.spinnerPostType.selectedItemPosition) {
            0 -> "notice"
            1 -> "suggestion"
            else -> "poll"
        }

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (mediaUri != null) {
            uploadMediaAndCreatePost(title, content, postType)
        } else {
            savePost(title, content, postType, null)
        }
    }

    private fun uploadMediaAndCreatePost(title: String, content: String, postType: String) {
        mediaUri?.let { uri ->
            BackendlessUtils.uploadMedia(this, uri) { mediaUrl ->
                if (mediaUrl != null) {
                    savePost(title, content, postType, mediaUrl)
                } else {
                    Toast.makeText(this, "Failed to upload media", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun savePost(title: String, content: String, postType: String, mediaUrl: String?) {
        val post = Post().apply {
            this.title = title
            this.content = content
            this.type = postType
            this.mediaUrl = mediaUrl
            this.allowComments = postType == "suggestion"
            this.createdBy = Backendless.UserService.CurrentUser().objectId
        }

        Backendless.Data.of(Post::class.java).save(post, object : AsyncCallback<Post> {
            override fun handleResponse(response: Post) {
                Toast.makeText(this@CreatePostActivity, "Post created", Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun handleFault(fault: BackendlessFault) {
                Toast.makeText(this@CreatePostActivity, "Error: ${fault.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}