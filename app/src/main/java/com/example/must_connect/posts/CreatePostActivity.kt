package com.example.must_connect.posts

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
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

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private var mediaUri: Uri? = null
    private val pollOptions = mutableListOf<String>()

    private val mediaPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let {
                mediaUri = it
                binding.tvMediaStatus.text = "1 file selected"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPostTypeSpinner()

        binding.spinnerPostType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.layoutPollOptions.visibility = if (position == 2) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnAttachMedia.setOnClickListener {
            openMediaPicker()
        }

        binding.btnAddOption.setOnClickListener {
            addPollOption()
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
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        mediaPickerLauncher.launch(intent)
    }

    private fun addPollOption() {
        val option1 = binding.etOption1.text.toString().trim()
        val option2 = binding.etOption2.text.toString().trim()

        if (option1.isNotEmpty()) pollOptions.add(option1)
        if (option2.isNotEmpty()) pollOptions.add(option2)

        binding.etOption1.text?.clear()
        binding.etOption2.text?.clear()

        Toast.makeText(this, "${pollOptions.size} options added", Toast.LENGTH_SHORT).show()
    }

    private fun createPost() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        val postType = when(binding.spinnerPostType.selectedItemPosition) {
            0 -> "notice"
            1 -> "suggestion"
            2 -> "poll"
            else -> "notice"
        }
        val allowComments = binding.switchAllowComments.isChecked

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (postType == "poll" && pollOptions.size < 2) {
            Toast.makeText(this, "Add at least 2 poll options", Toast.LENGTH_SHORT).show()
            return
        }

        if (mediaUri != null) {
            uploadMediaAndCreatePost(title, content, postType, allowComments)
        } else {
            savePost(title, content, postType, allowComments, null)
        }
    }

    private fun uploadMediaAndCreatePost(title: String, content: String, postType: String, allowComments: Boolean) {
        mediaUri?.let { uri ->
            BackendlessUtils.uploadMedia(this, uri) { mediaUrl ->
                if (mediaUrl != null) {
                    savePost(title, content, postType, allowComments, mediaUrl)
                } else {
                    Toast.makeText(this, "Failed to upload media", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun savePost(title: String, content: String, postType: String, allowComments: Boolean, mediaUrl: String?) {
        val currentUser = App.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not found, please login again", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val post = Post().apply {
            this.title = title
            this.content = content
            this.type = postType
            this.mediaUrl = mediaUrl
            this.allowComments = allowComments
            this.authorId = currentUser.objectId
            this.pollOptions = if (postType == "poll") pollOptions else listOf()
        }

        Backendless.Data.of(Post::class.java).save(post, object : AsyncCallback<Post> {
            override fun handleResponse(response: Post?) {
                Toast.makeText(this@CreatePostActivity, "Post created", Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun handleFault(fault: BackendlessFault) {
                Toast.makeText(this@CreatePostActivity, "Error: ${fault.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}