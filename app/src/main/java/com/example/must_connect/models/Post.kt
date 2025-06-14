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
    var createdBy: String? = null // User ID
    var createdAt: Date? = null
    var mediaUrl: String? = null
    var allowComments: Boolean = false
    var pollOptions: List<String> = listOf()
    var pollVotes: Map<String, Int> = mapOf()
}