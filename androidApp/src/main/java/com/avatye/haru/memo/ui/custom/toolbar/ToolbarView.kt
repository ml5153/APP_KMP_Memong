package com.avatye.haru.memo.ui.custom.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.Toast
import com.avatye.haru.memo.R
import com.avatye.haru.memo.databinding.ViewToolbarBinding

internal class ToolbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val binding = ViewToolbarBinding.inflate(LayoutInflater.from(context), this, true)

    private var onDrawingCLick: (() -> Unit)? = null
    private var onCheckBoxClick: (() -> Unit)? = null
    private var onBulletClick: (() -> Unit)? = null
    private var onGalleryClick: (() -> Unit)? = null
    private var onCameraClick: (() -> Unit)? = null
    private var onMemoColor: (() -> Unit)? = null

    private var isLocked: Boolean = false

    init {
        binding.lyDrawing.setOnClickListener {
            onDrawingCLick?.invoke()
        }
        binding.lyCheckbox.setOnClickListener {
            onCheckBoxClick?.invoke()
        }
        binding.lyBullet.setOnClickListener {
            onBulletClick?.invoke()
        }
        binding.lyGallery.setOnClickListener {
            onGalleryClick?.invoke()
        }
        binding.lyCamera.setOnClickListener {
            onCameraClick?.invoke()
        }

        binding.lyMemoColor.setOnClickListener {
            if (isLocked) {
                Toast.makeText(context, context.getString(R.string.haru_toast_secret_memo_cannot_change_background_color), Toast.LENGTH_SHORT).show()
            } else {
                onMemoColor?.invoke()
            }
        }
    }

    fun setLocked(isLocked: Boolean) {
        this@ToolbarView.isLocked = isLocked
        binding.lyMemoColor.alpha = if (isLocked) 0.3f else 1f
    }

    fun setOnDrawingClickListener(listener: () -> Unit) {
        onDrawingCLick = listener
    }

    fun setOnCheckBoxClickListener(listener: () -> Unit) {
        onCheckBoxClick = listener
    }

    fun setOnBulletClickListener(listener: () -> Unit) {
        onBulletClick = listener
    }

    fun setOnGalleryClickListener(listener: () -> Unit) {
        onGalleryClick = listener
    }

    fun setOnCameraClickListener(listener: () -> Unit) {
        onCameraClick = listener
    }

    fun setOnMemoColorClickListener(listener: () -> Unit) {
        onMemoColor = listener
    }

    fun onDestroy() {
        onDrawingCLick = null
        onCheckBoxClick = null
        onBulletClick = null
        onGalleryClick = null
        onCameraClick = null
        onMemoColor = null
    }


}
