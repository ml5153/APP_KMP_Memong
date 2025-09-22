package com.memong.aos.ui.custom.progress

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.memong.aos.R
import com.memong.aos.databinding.CustomProgressViewBinding

internal class CustomProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding: CustomProgressViewBinding =
        CustomProgressViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        val rotation = AnimationUtils.loadAnimation(context, R.anim.rotate_infinite)
        binding.imgSpinner.startAnimation(rotation)

        // 터치 + 키 이벤트 소비를 위한 설정
        isClickable = true
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()   // 여기 중요: 뷰가 실제로 포커스를 가져야 Back Key 이벤트를 받음
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            // ProgressView 위에서는 Back 키 소비
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}