package com.example.must_connect.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.example.must_connect.R
import com.example.must_connect.databinding.ActivityPasswordChangeBinding

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

        Backendless.UserService.updatePassword(oldPassword, newPassword, object : AsyncCallback<Void> {
            override fun handleResponse(response: Void?) {
                Toast.makeText(this@PasswordChangeActivity, "Password updated", Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun handleFault(fault: BackendlessFault) {
                Toast.makeText(this@PasswordChangeActivity, "Error: ${fault.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}