package com.avatye.haru.memo.ui.custom.header

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.avatye.haru.memo.databinding.ViewDrawHeaderBinding

internal class DrawHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding = ViewDrawHeaderBinding.inflate(LayoutInflater.from(context), this, true)

    var onBackClick: (() -> Unit)? = null
    var onUndoClick: (() -> Unit)? = null
    var onRedoClick: (() -> Unit)? = null
    var onConfirmClick: (() -> Unit)? = null

    init {


        binding.lyBack.setOnClickListener { onBackClick?.invoke() }

        binding.lyUndo.setOnClickListener {
            onUndoClick?.invoke()
        }

        binding.lyRedo.setOnClickListener {
            onRedoClick?.invoke()
        }

        binding.lyConfirm.setOnClickListener {
            onConfirmClick?.invoke()
        }
    }

    fun updateCanvasState(hasContent: Boolean, canUndo: Boolean, canRedo: Boolean) {
        val activeAlpha = 1.0f
        val inactiveAlpha = 0.3f

        binding.lyConfirm.isEnabled = hasContent
        binding.lyConfirm.isClickable = hasContent
        binding.lyConfirm.alpha = if (hasContent) activeAlpha else inactiveAlpha

        binding.lyUndo.isEnabled = canUndo
        binding.lyUndo.isClickable = canUndo
        binding.lyUndo.alpha = if (canUndo) activeAlpha else inactiveAlpha

        binding.lyRedo.isEnabled = canRedo
        binding.lyRedo.isClickable = canRedo
        binding.lyRedo.alpha = if (canRedo) activeAlpha else inactiveAlpha
    }

    fun onDestroy() {
        onBackClick = null
        onUndoClick = null
        onRedoClick = null
        onConfirmClick = null
        removeAllViews()
    }
}