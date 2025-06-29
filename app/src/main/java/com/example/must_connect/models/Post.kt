package com.example.must_connect.models

import java.util.Date

const val POST_NOTICE = "notice"
const val POST_SUGGESTION = "suggestion"
const val POST_POLL = "poll"

class Post {
    var objectId: String? = null
    var title: String = ""
    var content: String = ""
    var type: String = POST_NOTICE
    var authorId: String? = null
    var createdAt: Date? = null
    var mediaUrl: String? = null
    var allowComments: Boolean = false
    var pollOptions: String? = null  // JSON string of options

    // Fixed fields with proper initialization
    var pollVotes: String = "{}" // JSON string of {option: count}
    var votedUserIds: String = "[]" // JSON string of [userId]
}