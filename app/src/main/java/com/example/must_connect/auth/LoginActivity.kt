package com.example.must_connect.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.example.must_connect.R
import com.example.must_connect.dashboard.AdminDashboardActivity
import com.example.must_connect.dashboard.StudentDashboardActivity
import com.example.must_connect.dashboard.TeacherDashboardActivity
import com.example.must_connect.databinding.ActivityLoginBinding
import com.example.must_connect.models.ROLE_ADMIN
import com.example.must_connect.models.ROLE_STUDENT
import com.example.must_connect.models.ROLE_TEACHER
import com.example.must_connect.models.User
import weborb.client.ant.wdm.View

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(username, password)
        }
    }

    private fun loginUser(username: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE

        Backendless.UserService.login(username, password, object : AsyncCallback<BackendlessUser> {
            override fun handleResponse(user: BackendlessUser?) {
                val currentUser = user as User
                binding.progressBar.visibility = View.GONE

                when (currentUser.role) {
                    ROLE_STUDENT -> startActivity(Intent(this@LoginActivity, StudentDashboardActivity::class.java))
                    ROLE_TEACHER, ROLE_ADMIN -> {
                        if (username == "adminuser") {
                            startActivity(Intent(this@LoginActivity, AdminDashboardActivity::class.java))
                        } else {
                            startActivity(Intent(this@LoginActivity, TeacherDashboardActivity::class.java))
                        }
                    }
                    else -> Toast.makeText(this@LoginActivity, "Invalid role", Toast.LENGTH_SHORT).show()
                }
                finish()
            }

            override fun handleFault(fault: BackendlessFault) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@LoginActivity, "Login failed: ${fault.message}", Toast.LENGTH_SHORT).show()
            }
        }, true)
    }
}