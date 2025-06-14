package com.example.must_connect.models

import java.util.Date

class Comment {
    var objectId: String? = null
    var text: String = ""
    var postId: String? = null
    var authorId: String? = null
    var authorName: String = ""
    var createdAt: Date? = null
}