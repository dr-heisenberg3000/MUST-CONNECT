package com.example.must_connect.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.must_connect.R
import com.google.android.material.card.MaterialCardView

object ToastUtils {
    
    fun showSuccessToast(context: Context, message: String) {
        showCustomToast(context, message, R.color.colorSuccess, R.drawable.ic_check)
    }
    
    fun showErrorToast(context: Context, message: String) {
        showCustomToast(context, message, R.color.colorError, R.drawable.ic_error)
    }
    
    fun showInfoToast(context: Context, message: String) {
        showCustomToast(context, message, R.color.colorPrimary, R.drawable.ic_info)
    }
    
    private fun showCustomToast(context: Context, message: String, backgroundColor: Int, iconRes: Int) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.custom_toast, null)
        
        val cardView = layout.findViewById<MaterialCardView>(R.id.toastCard)
        val textView = layout.findViewById<TextView>(R.id.toastText)
        val iconView = layout.findViewById<View>(R.id.toastIcon)
        
        textView.text = message
        cardView.setCardBackgroundColor(context.getColor(backgroundColor))
        
        val toast = Toast(context)
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 100)
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.show()
    }
} 