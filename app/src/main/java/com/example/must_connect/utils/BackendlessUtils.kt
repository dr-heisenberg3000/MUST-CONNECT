package com.example.must_connect.utils

import android.content.Context
import android.net.Uri
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.files.BackendlessFile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.must_connect.models.Post

object BackendlessUtils {

    fun uploadMedia(context: Context, uri: Uri, callback: (String?) -> Unit) {
        val inputStream = context.contentResolver.openInputStream(uri)
        // Generate unique filename with timestamp
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "media_$timestamp"
        val file = File(context.cacheDir, fileName)
        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        Backendless.Files.upload(
            file,
            "media",
            true,
            object : AsyncCallback<BackendlessFile?> {
                override fun handleResponse(response: BackendlessFile?) {
                    callback(response?.fileURL)
                }

                override fun handleFault(fault: BackendlessFault) {
                    callback(null)
                }
            })
    }

    fun savePost(post: Post, callback: (Boolean) -> Unit) {
        Backendless.Data.of(Post::class.java).save(post, object : AsyncCallback<Post> {
            override fun handleResponse(response: Post) {
                callback(true)
            }

            override fun handleFault(fault: BackendlessFault) {
                callback(false)
            }
        })
    }
}