package com.example.must_connect.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import com.example.must_connect.R
import com.example.must_connect.dashboard.AdminDashboardActivity
import com.example.must_connect.dashboard.StudentDashboardActivity
import com.example.must_connect.dashboard.TeacherDashboardActivity
import com.example.must_connect.databinding.ActivityLoginBinding
import com.example.must_connect.models.AppUser
import com.example.must_connect.App
import com.example.must_connect.utils.ToastUtils

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
/*
        // Initialize Backendless exactly as in your original working version
        if (!Backendless.isInitialized()) {
            Backendless.initApp(
                this,
                "F32A7A06-6136-4700-91D5-39E89D83ADB0",
                "FC53F76D-656B-41B2-900D-83B0EEBF163D"
            )
            Backendless.setUrl("https://api.backendless.com")
        }
*/
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                ToastUtils.showErrorToast(this, "Please fill all fields")
                return@setOnClickListener
            }

            loginUser(username, password)
        }
    }

    private fun loginUser(username: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.isIndeterminate = true

        // Original working query logic
        val whereClause = "username = '$username' AND password = '$password'"
        val queryBuilder = DataQueryBuilder.create().apply {
            this.whereClause = whereClause
        }

        Backendless.Data.of(AppUser::class.java).find(queryBuilder, object : AsyncCallback<List<AppUser>> {
            override fun handleResponse(users: List<AppUser>?) {
                binding.progressBar.visibility = View.GONE
                binding.progressBar.isIndeterminate = false

                if (users != null && users.isNotEmpty()) {
                    val user = users[0]
                    App.currentUser = user

                    when (user.role) {
                        "student" -> startActivity(Intent(this@LoginActivity, StudentDashboardActivity::class.java))
                        "teacher" -> startActivity(Intent(this@LoginActivity, TeacherDashboardActivity::class.java))
                        "admin" -> startActivity(Intent(this@LoginActivity, AdminDashboardActivity::class.java))
                        else -> ToastUtils.showErrorToast(this@LoginActivity, "Invalid role")
                    }
                    finish()
                } else {
                    ToastUtils.showErrorToast(this@LoginActivity, "Invalid credentials")
                }
            }

            override fun handleFault(fault: BackendlessFault) {
                binding.progressBar.visibility = View.GONE
                binding.progressBar.isIndeterminate = false
                ToastUtils.showErrorToast(this@LoginActivity, "Login failed: ${fault.message}")
            }
        })
    }
}