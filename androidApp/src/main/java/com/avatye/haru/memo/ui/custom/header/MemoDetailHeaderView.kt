package com.avatye.haru.memo.ui.custom.header

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.avatye.haru.memo.R
import com.avatye.haru.memo.data.enum.MemoMode
import com.avatye.haru.memo.data.utils.PreferenceUtil
import com.avatye.haru.memo.data.utils.PreferenceUtil.KEY_SIMPLIFY
import com.avatye.haru.memo.databinding.ViewMemoDetailHeaderBinding

internal class MemoDetailHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private val binding = ViewMemoDetailHeaderBinding.inflate(LayoutInflater.from(context), this, true)
    private var isLocked: Boolean = false
    private var isImageSelectedMode = false
    private var isImageSelectBtnVisible = false

    var onBackClick: (() -> Unit)? = null
    var onAction1Click: (() -> Unit)? = null
    var onAction2Click: (() -> Unit)? = null
    var onAction3Click: (() -> Unit)? = null
    var onAction4Click: (() -> Unit)? = null
    var onSaveClick: (() -> Unit)? = null
    var onImageSelectedClick: (() -> Unit)? = null
    var onImageSelectedDeleteClick: (() -> Unit)? = null
    var onExitImageSelectMode: (() -> Unit)? = null

    init {
        binding.lyBack.setOnClickListener {
            if (isImageSelectedMode) {
                isImageSelectedMode = false
                binding.lySave.isVisible = true
                binding.lyImageSelected.isVisible = isImageSelectBtnVisible
                binding.lyImageSelectedDelete.isVisible = false
                onExitImageSelectMode?.invoke()
            } else {
                onBackClick?.invoke()
            }
        }
        binding.lyAction1.setOnClickListener { onAction1Click?.invoke() }
        binding.lyAction2.setOnClickListener { onAction2Click?.invoke() }
        binding.lyAction3.setOnClickListener { onAction3Click?.invoke() }
        binding.lyAction4.setOnClickListener { onAction4Click?.invoke() }
        binding.lySave.setOnClickListener { onSaveClick?.invoke() }
        binding.lyImageSelected.setOnClickListener {
            isImageSelectedMode = true
            binding.lySave.isVisible = false
            binding.lyImageSelected.isVisible = false
            binding.lyImageSelectedDelete.isVisible = true
            onImageSelectedClick?.invoke()
        }
        binding.lyImageSelectedDelete.setOnClickListener {
            isImageSelectedMode = false
            binding.lySave.isVisible = true
            binding.lyImageSelected.isVisible = isImageSelectBtnVisible
            binding.lyImageSelectedDelete.isVisible = false
            onImageSelectedDeleteClick?.invoke()
        }

    }

    fun setHeaderMode(mode: MemoMode) {
        when (mode) {
            MemoMode.CREATE_MEMO, MemoMode.MODIFY_MEMO -> {
                binding.tvTitle.isVisible = true
                binding.tvTitle.text = if (mode == MemoMode.CREATE_MEMO)
                    context.getString(R.string.haru_header_create_mode_title)
                else
                    context.getString(R.string.haru_header_modify_mode_title)

                binding.lyLock.isVisible = false
                binding.lyAction1.isVisible = false
                binding.lyAction2.isVisible = false
                binding.lyAction3.isVisible = false
                binding.lyAction4.isVisible = false

                isImageSelectedMode = false
                binding.lySave.isVisible = true

                binding.lyImageSelected.isVisible = isImageSelectBtnVisible && !isImageSelectedMode
                binding.lyImageSelectedDelete.isVisible = false
            }

            MemoMode.READ_MEMO -> {
                binding.tvTitle.text = ""
                binding.lyLock.isVisible = isLocked
                binding.lyAction1.isVisible = true
                binding.lyAction2.isVisible = true
                binding.lyAction3.isVisible = true
                binding.lyAction4.isVisible = false
                isImageSelectedMode = false
                binding.lySave.isVisible = false
                binding.lyImageSelected.isVisible = false
                binding.lyImageSelectedDelete.isVisible = false

                binding.btnAction1.setImageResource(R.drawable.ic_important_selector)
                binding.btnAction2.setImageResource(R.drawable.ic_header_search)
                binding.btnAction3.setImageResource(R.drawable.ic_header_info)

                if (isLocked) {
                    binding.lyAction1.isVisible = false
                    if (PreferenceUtil.get(KEY_SIMPLIFY, false)) {
                        binding.btnLock.setImageResource(R.drawable.ic_item_unlock)
                    } else {
                        binding.btnLock.setImageResource(R.drawable.ic_item_lock)
                    }
                }
            }

            MemoMode.NONE -> {

            }
        }
    }

    fun setLockState(isLocked: Boolean) {
        this.isLocked = isLocked
    }

    fun setImportantState(selected: Boolean) {
        binding.btnAction1.isSelected = selected
    }

    fun setImageSelectBtnVisible(visible: Boolean) {
        isImageSelectBtnVisible = visible
        if (!isImageSelectedMode) {
            binding.lyImageSelected.isVisible = isImageSelectBtnVisible
        }
    }


    override fun setBackgroundColor(color: Int) {
        binding.root.setBackgroundColor(color)
    }

    fun onDestroy() {
        onBackClick = null
        onAction1Click = null
        onAction2Click = null
        onAction3Click = null
        onAction4Click = null
        onSaveClick = null
        onImageSelectedClick = null
        onImageSelectedDeleteClick = null
        this.removeAllViews()
    }

}
