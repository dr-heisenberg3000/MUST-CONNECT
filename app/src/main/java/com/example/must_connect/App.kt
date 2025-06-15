package com.example.must_connect

import android.app.Application
import android.util.Log
import com.backendless.Backendless

class App : Application() {
    companion object {
        var currentUser: com.example.must_connect.models.AppUser? = null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("Backendless", "Initializing Backendless...")
        Backendless.initApp(
            this,
            "F32A7A06-6136-4700-91D5-39E89D83ADB0",
            "FC53F76D-656B-41B2-900D-83B0EEBF163D"
        )
        Backendless.setUrl("https://eu-api.backendless.com")
        Log.d("Backendless", "Initialization complete")
    }
}