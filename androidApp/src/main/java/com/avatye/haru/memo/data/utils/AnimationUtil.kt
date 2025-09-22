package com.avatye.haru.memo.data.utils

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

internal object AnimationUtil {


    private val animatorMap = mutableMapOf<View, ObjectAnimator>()

    fun scaleUp(view: View, scale: Float = 1.2f, duration: Long = 150) {
        if (!view.isAttachedToWindow || view.visibility != View.VISIBLE) return
        view.animate()
            .scaleX(scale)
            .scaleY(scale)
            .setDuration(duration)
            .start()
    }

    fun scaleDown(view: View, scale: Float = 0.88f, duration: Long = 150) {
        if (!view.isAttachedToWindow || view.visibility != View.VISIBLE) return
        if (animatorMap.containsKey(view)) return
        view.animate()
            .scaleX(scale)
            .scaleY(scale)
            .setDuration(duration)
            .start()
    }

    fun scaleReset(view: View, duration: Long = 150) {
        animatorMap.remove(view)?.cancel()
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .start()
    }

    fun startPulse(view: View, scale: Float = 1.1f, duration: Long = 3000L) {
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, scale, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, scale, 1f)
        ).apply {
            this.duration = duration
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        animatorMap[view] = animator
    }

    fun stopPulse(view: View) {
        animatorMap[view]?.apply {
            cancel()
        }
        animatorMap.remove(view)
        view.scaleX = 1f
        view.scaleY = 1f
    }
}