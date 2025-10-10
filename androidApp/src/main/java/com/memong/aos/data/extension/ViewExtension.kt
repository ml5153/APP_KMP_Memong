package com.memong.aos.data.extension

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.memong.aos.R

fun EditText.exitCursor() {
    isCursorVisible = false
    clearFocus()
}

fun EditText.enterCursor() {
    isCursorVisible = true
    requestFocus()
}

fun EditText.disableCursorAndFocus() {
    isCursorVisible = false
    isFocusable = false
    isFocusableInTouchMode = false
}

fun EditText.enableCursorAndFocus() {
    isCursorVisible = true
    isFocusable = true
    isFocusableInTouchMode = true
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}


fun Int.toHex(): String = String.format("#%06X", 0xFFFFFF and this)

fun View.toForeground(drawable: Drawable?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.foreground = drawable
    } else {
        this.background = drawable ?: ContextCompat.getDrawable(context, R.drawable.shape_color_button_background)
    }

}


fun View.showIme() {
    post {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowInsetsController?.show(WindowInsets.Type.ime())
        } else {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}

fun View.hideIme() {
    post {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowInsetsController?.hide(WindowInsets.Type.ime())
        } else {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
    }
}






