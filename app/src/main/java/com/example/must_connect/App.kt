package com.example.must_connect

import android.app.Application
import com.backendless.Backendless

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        Backendless.setUrl("https://api.backendless.com")
        Backendless.initApp(
            this,
            "YOUR_APP_ID", // Replace with actual ID
            "YOUR_API_KEY" // Replace with actual key
        )
    }
}