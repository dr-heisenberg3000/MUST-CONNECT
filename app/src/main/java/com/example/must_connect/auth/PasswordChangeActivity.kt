package com.example.must_connect.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.example.must_connect.databinding.ActivityPasswordChangeBinding
import com.example.must_connect.models.AppUser
import com.example.must_connect.App
import com.example.must_connect.utils.ToastUtils

class PasswordChangeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasswordChangeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordChangeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        binding.btnSubmit.setOnClickListener {
            changePassword()
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

    private fun changePassword() {
        val currentUser = App.currentUser
        if (currentUser == null) {
            ToastUtils.showErrorToast(this, "User not logged in")
            return
        }

        val oldPassword = binding.etOldPassword.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (oldPassword != currentUser.password) {
            ToastUtils.showErrorToast(this, "Old password is incorrect")
            return
        }

        if (newPassword != confirmPassword) {
            ToastUtils.showErrorToast(this, "New passwords don't match")
            return
        }

        currentUser.password = newPassword

        Backendless.Data.of(AppUser::class.java).save(currentUser, object : AsyncCallback<AppUser> {
            override fun handleResponse(response: AppUser?) {
                ToastUtils.showSuccessToast(
                    this@PasswordChangeActivity,
                    "Password changed successfully"
                )
                finish()
            }

            override fun handleFault(fault: BackendlessFault) {
                ToastUtils.showErrorToast(
                    this@PasswordChangeActivity,
                    "Error: ${fault.message}"
                )
            }
        })
    }
}