package com.avatye.haru.memo.ui.custom.header

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.avatye.haru.memo.data.utils.Util
import com.avatye.haru.memo.R
import com.avatye.haru.memo.databinding.ViewTrashHeaderBinding

internal class TrashHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding: ViewTrashHeaderBinding =
        ViewTrashHeaderBinding.inflate(LayoutInflater.from(context), this, true)

    enum class TrashHeaderMode {
        NORMAL, EDIT, DETAIL
    }

    private var mode: TrashHeaderMode = TrashHeaderMode.NORMAL

    init {
        setupClickListeners()
    }

    fun setMode(newMode: TrashHeaderMode) {
        mode = newMode

        when (newMode) {
            TrashHeaderMode.NORMAL -> {
                binding.tvTitle.apply {
                    text = context.getString(R.string.haru_trash_title)
                    setTextColor(Color.BLACK)
                    visibility = View.VISIBLE
                }

                binding.tvSelect.visibility = View.VISIBLE
                binding.tvSelect.setText("선택")
                binding.tvClearAll.visibility = View.VISIBLE
                binding.tvClearAll.setText("전체비우기")
                binding.cbSelectAll.visibility = View.GONE
            }

            TrashHeaderMode.EDIT -> {
                setTitleCount(0)
                binding.tvTitle.visibility = View.VISIBLE
                binding.tvSelect.visibility = View.VISIBLE
                binding.tvSelect.setText("선택 복원")
                binding.tvClearAll.visibility = View.VISIBLE
                binding.tvClearAll.setText("선택 영구삭제")
                binding.cbSelectAll.visibility = View.VISIBLE
            }

            TrashHeaderMode.DETAIL -> {
                binding.tvTitle.visibility = View.GONE
                binding.tvSelect.visibility = View.VISIBLE
                binding.tvSelect.setText("복원")
                binding.tvClearAll.visibility = View.VISIBLE
                binding.tvClearAll.setText("영구삭제")
                binding.cbSelectAll.visibility = View.GONE
            }
        }

        // 공통 marginEnd 조정
        (binding.tvClearAll.layoutParams as? ViewGroup.MarginLayoutParams)?.let {
            it.marginEnd = Util.dpToPx(context, if (newMode == TrashHeaderMode.EDIT) 12 else 0)
            binding.tvClearAll.layoutParams = it
        }
    }


    fun setTitleCount(count: Int) {
        if (mode != TrashHeaderMode.EDIT) return

        binding.tvTitle.apply {
            text = context.getString(R.string.haru_trash_selected_count, count)
            setTextColor(ContextCompat.getColor(context, R.color.haru_primary_orange))
        }
    }

    fun setButtonsVisibility(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        binding.tvSelect.visibility = visibility
        binding.tvClearAll.visibility = visibility
    }

    // click listeners
    private var onBackClick: (() -> Unit)? = null
    private var onSelectClick: (() -> Unit)? = null
    private var onClearAllClick: (() -> Unit)? = null
    private var onCheckChanged: ((Boolean) -> Unit)? = null

    private fun setupClickListeners() {
        binding.lyBack.setOnClickListener { onBackClick?.invoke() }
        binding.tvSelect.setOnClickListener { onSelectClick?.invoke() }
        binding.tvClearAll.setOnClickListener { onClearAllClick?.invoke() }
        binding.cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            onCheckChanged?.invoke(isChecked)
        }
    }

    fun setOnBackClickListener(block: () -> Unit) {
        onBackClick = block
    }

    fun setOnSelectClickListener(block: () -> Unit) {
        onSelectClick = block
    }

    fun setOnClearAllClickListener(block: () -> Unit) {
        onClearAllClick = block
    }

    fun setOnCheckAllChangedListener(block: (Boolean) -> Unit) {
        onCheckChanged = block
    }

    fun setCheckAllState(checked: Boolean) {
        binding.cbSelectAll.setOnCheckedChangeListener(null)
        binding.cbSelectAll.isChecked = checked
        binding.cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            onCheckChanged?.invoke(isChecked)
        }
    }

    fun onDestroy() {
        binding.cbSelectAll.setOnCheckedChangeListener(null)
    }

}
