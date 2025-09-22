package com.memong.aos.ui.custom.header

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.memong.aos.data.extension.disableCursorAndFocus
import com.memong.aos.data.extension.hideIme
import com.memong.aos.data.extension.showIme
import com.memong.aos.databinding.ViewMemoDetailSearchHeaderBinding

internal class MemoDetailSearchHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    val binding = ViewMemoDetailSearchHeaderBinding.inflate(LayoutInflater.from(context), this, true)

    var onBackClick: (() -> Unit)? = null
    var onSearchClick: ((String) -> Unit)? = null
    var onClearClick: (() -> Unit)? = null
    var onMoveUpClick: (() -> Unit)? = null
    var onMoveDownClick: (() -> Unit)? = null

    private var hasResults: Boolean = false

    init {
        binding.lyBack.setOnClickListener { onBackClick?.invoke() }

        binding.lyClear.setOnClickListener {
            binding.etSearch.setText("")
            setControlsVisible(false)
            setIndicator("0 / 0")
            onClearClick?.invoke()
        }

        binding.lyMoveUp.setOnClickListener {
            if (hasResults) onMoveUpClick?.invoke()
        }

        binding.lyMoveDown.setOnClickListener {
            if (hasResults) onMoveDownClick?.invoke()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val keyword = s?.toString().orEmpty()
                val hasInput = keyword.isNotBlank()
                setControlsVisible(hasInput)
                if (!hasInput) {
                    setIndicator("0 / 0")
                    hasResults = false
                }
                onSearchClick?.invoke(keyword)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 초기 상태
        setControlsVisible(false)
        setIndicator("0 / 0")
    }

    private fun setControlsVisible(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        binding.lyClear.visibility = visibility
        binding.tvIndicator.visibility = visibility
        binding.lyMoveUp.visibility = visibility
        binding.lyMoveDown.visibility = visibility
        binding.divider.visibility = visibility
    }

    fun setIndicator(text: String) {
        binding.tvIndicator.text = text
    }

    fun setHasResults(resultAvailable: Boolean) {
        hasResults = resultAvailable
    }

    fun clearSearch() {
        binding.etSearch.setText("")
        setControlsVisible(false)
        setIndicator("0 / 0")
        hasResults = false
    }

    fun getKeyword(): String {
        return binding.etSearch.text?.toString().orEmpty()
    }


    fun focusAndShowKeyboard(selectAll: Boolean = true) {
        binding.etSearch.isFocusableInTouchMode = true
        binding.etSearch.requestFocus()
        if (selectAll) {
            binding.etSearch.selectAll()
        } else {
            binding.etSearch.setSelection(binding.etSearch.text?.length ?: 0)
        }
        binding.etSearch.showIme()
    }

    fun clearFocusAndHideKeyboard() {
        binding.etSearch.clearFocus()
        binding.etSearch.hideIme()
    }

    fun onDestroy() {
        onBackClick = null
        onSearchClick = null
        onClearClick = null
        onMoveUpClick = null
        onMoveDownClick = null
        binding.etSearch.disableCursorAndFocus()
        removeAllViews()
    }
}
