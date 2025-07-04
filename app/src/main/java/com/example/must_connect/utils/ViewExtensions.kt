package com.example.must_connect.utils

import android.view.View
import androidx.fragment.app.Fragment

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun Fragment.showToast(message: String) {
    ToastUtils.showInfoToast(requireContext(), message)
}