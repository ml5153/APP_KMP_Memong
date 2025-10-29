package com.memong.aos.ui.custom.header

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.memong.aos.R
import com.memong.aos.data.enum.MainGroupMode
import com.memong.aos.data.enum.MainLayoutMode
import com.memong.aos.data.enum.MemoSortType
import com.memong.aos.databinding.ViewMainHeaderBinding

internal class MainHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding = ViewMainHeaderBinding.inflate(LayoutInflater.from(context), this, true)

    private var currentLayoutMode = MainLayoutMode.GRID
    private var currentGroupMode = MainGroupMode.DATE

    private var onAction1Click: (() -> Unit)? = null
    private var onAction2Click: (() -> Unit)? = null
    private var onAction3Click: (() -> Unit)? = null
    private var onAction4Click: (() -> Unit)? = null
    private var onBackClick: (() -> Unit)? = null
    private var onCheckboxClick: ((Boolean) -> Unit)? = null

    init {
        setupClickListeners()
    }

    fun getAction1Button(): View = binding.lyAction1
    fun getAction2Button(): View = binding.lyAction2
    fun getAction3Button(): View = binding.lyAction3
    fun getAction4Button(): View = binding.lyAction4

    private fun setupClickListeners() {
        binding.lyAction1.setOnClickListener {
            onAction1Click?.invoke()
        }

        binding.lyAction2.setOnClickListener {
            onAction2Click?.invoke()
        }

        binding.lyAction3.setOnClickListener {
            onAction3Click?.invoke()
        }

        binding.lyAction4.setOnClickListener {
            onAction4Click?.invoke()
        }

        binding.cbMainHeaderCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                onCheckboxClick?.invoke(isChecked)
            }
        }

        binding.lyBack.setOnClickListener {
            onBackClick?.invoke()
        }
    }

    fun setHeaderMode(
        memoSortType: MemoSortType,
        layoutMode: MainLayoutMode = currentLayoutMode,
        groupMode: MainGroupMode = currentGroupMode,
        value: Map<String, Any>? = null
    ) {

        currentLayoutMode = layoutMode
        currentGroupMode = groupMode

        this.post {
            when (layoutMode) {
                MainLayoutMode.GRID -> {
                    binding.lyBack.isVisible = false
                    binding.tvCenterText.isVisible = false
                    binding.lyAction1.isVisible = true
                    binding.lyAction2.isVisible = true
                    binding.lyAction3.isVisible = true
                    binding.lyAction4.isVisible = true
                    binding.lyMainHeaderCheckbox.isVisible = false
                    binding.cbMainHeaderCheckbox.isChecked = false
                    binding.btnAction1.setImageResource(R.drawable.ic_header_search)
                    binding.btnAction2.setImageResource(
                        when (groupMode) {
                            MainGroupMode.DATE -> R.drawable.ic_header_tag
                            MainGroupMode.TAG -> R.drawable.ic_header_calendar
                        }
                    )
                    binding.btnAction3.setImageResource(R.drawable.ic_header_view_mode_list)
                    binding.btnAction4.setImageResource(R.drawable.ic_header_info)
                    binding.lyAction2.alpha = if (memoSortType == MemoSortType.CUSTOM) 0.3f else 1f
                }

                MainLayoutMode.LIST -> {
                    binding.lyBack.isVisible = false
                    binding.tvCenterText.isVisible = false
                    binding.lyAction1.isVisible = true
                    binding.lyAction2.isVisible = true
                    binding.lyAction3.isVisible = true
                    binding.lyAction4.isVisible = true
                    binding.lyMainHeaderCheckbox.isVisible = false
                    binding.cbMainHeaderCheckbox.isChecked = false
                    binding.btnAction1.setImageResource(R.drawable.ic_header_search)
                    binding.btnAction2.setImageResource(
                        when (groupMode) {
                            MainGroupMode.DATE -> R.drawable.ic_header_tag
                            MainGroupMode.TAG -> R.drawable.ic_header_calendar
                        }
                    )
                    binding.btnAction3.setImageResource(R.drawable.ic_header_view_mode_grid)
                    binding.btnAction4.setImageResource(R.drawable.ic_header_info)
                    binding.lyAction2.alpha = if (memoSortType == MemoSortType.CUSTOM) 0.3f else 1f
                }

                MainLayoutMode.EDIT -> {
                    binding.lyBack.isVisible = true
                    binding.tvCenterText.isVisible = true
                    binding.lyAction1.isVisible = true
                    binding.lyAction2.isVisible = true
                    binding.lyAction3.isVisible = true
                    binding.lyAction4.isVisible = false
                    binding.lyMainHeaderCheckbox.isVisible = true
                    binding.btnAction1.setImageResource(R.drawable.ic_header_share)
                    binding.btnAction2.setImageResource(R.drawable.ic_header_paste)
                    binding.btnAction3.setImageResource(R.drawable.ic_header_trash)

                    val selectCount = value?.get("selectCount").toString().toInt()
                    val itemCount = value?.get("itemCount").toString().toInt()
                    binding.tvCenterText.text = context.getString(R.string.haru_header_selected_mode_count_text, selectCount.toString(), itemCount.toString())
                    binding.cbMainHeaderCheckbox.isChecked = selectCount == itemCount
                    binding.lyAction2.alpha = if (selectCount == 1) 1.0f else 0.3f
                }
            }
        }

    }

    fun setOnAction1ClickListener(listener: () -> Unit) {
        onAction1Click = listener
    }

    fun setOnAction2ClickListener(listener: () -> Unit) {
        onAction2Click = listener
    }

    fun setOnAction3ClickListener(listener: () -> Unit) {
        onAction3Click = listener
    }

    fun setOnAction4ClickListener(listener: () -> Unit) {
        onAction4Click = listener
    }

    fun setOnBackClickListener(listener: () -> Unit) {
        onBackClick = listener
    }

    fun setOnCheckBoxClickListener(listener: (Boolean) -> Unit) {
        onCheckboxClick = listener
    }


    fun onDestroy() {
        onBackClick = null
        onAction1Click = null
        onAction2Click = null
        onAction3Click = null
        onAction4Click = null
        onCheckboxClick = null
        this.removeAllViews()
    }

}


