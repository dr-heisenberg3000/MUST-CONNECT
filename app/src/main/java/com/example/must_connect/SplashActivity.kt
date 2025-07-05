package com.example.must_connect

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.must_connect.auth.LoginActivity
import com.example.must_connect.dashboard.AdminDashboardActivity
import com.example.must_connect.dashboard.StudentDashboardActivity
import com.example.must_connect.dashboard.TeacherDashboardActivity
import com.example.must_connect.models.AppUser
import com.example.must_connect.utils.BackendlessUtils

class SplashActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Removed startAnimations() call since splash is now plain white
        
        // Check user session and navigate
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserSession()
        }, 2000) // 2 seconds delay
    }
    
    // Removed startAnimations() function
    
    private fun checkUserSession() {
        // Check if user is already logged in
        val currentUser = App.currentUser
        
        if (currentUser != null) {
            // User is logged in, navigate to appropriate dashboard
            navigateToDashboard(currentUser)
        } else {
            // User is not logged in, go to login screen
            navigateToLogin()
        }
    }
    
    private fun navigateToDashboard(user: AppUser) {
        val intent = when (user.role) {
            "admin" -> Intent(this, AdminDashboardActivity::class.java)
            "teacher" -> Intent(this, TeacherDashboardActivity::class.java)
            else -> Intent(this, StudentDashboardActivity::class.java)
        }
        
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}
