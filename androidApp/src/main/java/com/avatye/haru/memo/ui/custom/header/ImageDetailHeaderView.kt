package com.avatye.haru.memo.ui.custom.header

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.avatye.haru.memo.databinding.ViewImageDetailHeaderBinding

internal class ImageDetailHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding = ViewImageDetailHeaderBinding.inflate(LayoutInflater.from(context), this, true)
    var onBackClick: (() -> Unit)? = null
    var onDownLoadClick: (() -> Unit)? = null
    var onShareClick: (() -> Unit)? = null
    var onDeleteClick: (() -> Unit)? = null

    init {
        binding.lyClose.setOnClickListener { onBackClick?.invoke() }
        binding.lyDownload.setOnClickListener { onDownLoadClick?.invoke() }
        binding.lyShare.setOnClickListener { onShareClick?.invoke() }
        binding.lyDelete.setOnClickListener { onDeleteClick?.invoke() }
    }

    fun setIndicatorText(text: String) {
        binding.tvImageIndicator.text = text
    }


    fun onDestroy() {
        onBackClick = null
        onDownLoadClick = null
        onShareClick = null
        onDeleteClick = null
        this.removeAllViews()
    }
}
