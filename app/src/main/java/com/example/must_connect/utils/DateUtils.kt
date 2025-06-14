package com.example.must_connect.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatDateTime(date: Date?): String {
        if (date == null) return ""
        val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        return sdf.format(date)
    }

    fun formatDate(date: Date?): String {
        if (date == null) return ""
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(date)
    }
}