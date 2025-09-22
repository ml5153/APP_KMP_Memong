package com.avatye.haru.memo.data.extension

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager


inline fun <reified T : Parcelable> Activity.extraParcel(key: String): T? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    intent.getParcelableExtra(key, T::class.java)
} else {
    intent.getParcelableExtra(key) as? T
}


fun Activity.extraString(key: String): String? = intent?.extras?.getString(key)


fun Activity.extraInt(key: String): Int? = intent?.extras?.getInt(key)


fun Activity.extraLong(key: String): Long? = intent?.extras?.getLong(key)


fun Activity.extraBoolean(key: String): Boolean? = intent?.extras?.getBoolean(key)


fun Activity.extraFloat(key: String): Float? = intent?.extras?.getFloat(key)

fun Activity.hideKeyboard() {
    val view = currentFocus ?: window.decorView.findFocusableView() ?: View(this)
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

private fun View.findFocusableView(): View? {
    if (isFocusable && windowToken != null) return this
    if (this is ViewGroup) {
        for (i in 0 until childCount) {
            val result = getChildAt(i).findFocusableView()
            if (result != null) return result
        }
    }
    return null
}


val Activity?.isAlive: Boolean
    get() = !(this?.isFinishing ?: true)


fun Activity.start(
    intent: Intent,
    flags: Int? = null,
    transition: Pair<Int, Int>? = null,
    close: Boolean = false
) {
    flags?.let { intent.addFlags(it) }

    val options = transition?.let {
        ActivityOptions.makeCustomAnimation(this, it.first, it.second).toBundle()
    }

    startActivity(intent, options)

    if (close) {
        finish()
    }
}

fun Activity.startResult(
    intent: Intent,
    requestCode: Int,
    flags: Int? = null,
    transition: Pair<Int, Int>? = null
) {
    flags?.let { intent.addFlags(it) }

    val options = transition?.let {
        ActivityOptions.makeCustomAnimation(this, it.first, it.second).toBundle()
    }

    startActivityForResult(intent, requestCode, options)
}


fun Activity.scrollFocusedViewIntoView(view: View) {
    Handler(Looper.getMainLooper()).postDelayed({
        view.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }, 100)
}