package com.example.must_connect.models

import com.backendless.BackendlessUser

const val ROLE_STUDENT = "student"
const val ROLE_TEACHER = "teacher"
const val ROLE_ADMIN = "admin"

class User : BackendlessUser() {
    var role: String? = null
    var rollNumber: String? = null
    var fullName: String? = null
}