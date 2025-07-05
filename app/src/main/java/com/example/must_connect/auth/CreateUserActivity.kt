package com.example.must_connect.auth

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.example.must_connect.databinding.ActivityCreateUserBinding
import com.example.must_connect.models.AppUser
import com.example.must_connect.utils.ToastUtils

class CreateUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRoleSpinner()
        setupSubmitButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRoleSpinner() {
        val roles = listOf("Student", "Teacher")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        binding.spinnerRole.setAdapter(adapter)
        binding.spinnerRole.setText("", false) // Start with empty text to show hint
    }

    private fun setupSubmitButton() {
        binding.btnCreate.setOnClickListener {
            createUser()
        }
    }

    private fun createUser() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val selectedRole = binding.spinnerRole.text.toString()

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || fullName.isEmpty() || email.isEmpty() || selectedRole.isEmpty()) {
            ToastUtils.showErrorToast(this, "Please fill all fields")
            return
        }

        if (password != confirmPassword) {
            ToastUtils.showErrorToast(this, "Passwords don't match")
            return
        }

        if (password.length < 6) {
            ToastUtils.showErrorToast(this, "Password must be at least 6 characters")
            return
        }

        // Basic email validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ToastUtils.showErrorToast(this, "Please enter a valid email address")
            return
        }

        val role = when (selectedRole) {
            "Student" -> "student"
            "Teacher" -> "teacher"
            else -> "student"
        }

        val user = AppUser().apply {
            this.username = username
            this.password = password
            this.fullName = fullName
            this.email = email
            this.role = role
        }

        Backendless.Data.of(AppUser::class.java).save(user, object : AsyncCallback<AppUser> {
            override fun handleResponse(response: AppUser?) {
                ToastUtils.showSuccessToast(this@CreateUserActivity, "User created successfully")
                finish()
            }

            override fun handleFault(fault: BackendlessFault) {
                ToastUtils.showErrorToast(this@CreateUserActivity, "Error: ${fault.message}")
            }
        })
    }
}