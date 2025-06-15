package com.example.must_connect.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.example.must_connect.databinding.ActivityPasswordChangeBinding
import com.example.must_connect.App
import com.example.must_connect.models.AppUser

class PasswordChangeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasswordChangeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordChangeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmit.setOnClickListener {
            changePassword()
        }
    }

    private fun changePassword() {
        val oldPassword = binding.etOldPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (newPassword != confirmPassword) {
            binding.tilConfirmPassword.error = "Passwords don't match"
            return
        }

        val currentUser = App.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Verify old password matches
        if (currentUser.password != oldPassword) {
            Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_SHORT).show()
            return
        }

        // Update password locally
        currentUser.password = newPassword

        // Save to backend
        Backendless.Data.of(AppUser::class.java).save(currentUser, object : AsyncCallback<AppUser> {
            override fun handleResponse(response: AppUser?) {
                Toast.makeText(
                    this@PasswordChangeActivity,
                    "Password updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }

            override fun handleFault(fault: BackendlessFault) {
                Toast.makeText(
                    this@PasswordChangeActivity,
                    "Error: ${fault.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}