package com.memong.aos.ui.custom.header

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.memong.aos.data.extension.showIme
import com.memong.aos.databinding.ViewSearchTagHeaderBinding

internal class SearchTagHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding: ViewSearchTagHeaderBinding =
        ViewSearchTagHeaderBinding.inflate(LayoutInflater.from(context), this, true)

    private var textChangedListener: ((String) -> Unit)? = null
    private var clearClickListener: (() -> Unit)? = null

    private val blueColor = Color.parseColor("#0076ED")
    private val blackColor = ContextCompat.getColor(context, android.R.color.black)

    private var isDeleteMode = false

    init {
        // EditText 텍스트 변경 리스너
        binding.tvTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isDeleteMode) {
                    val text = s?.toString().orEmpty()
                    showClearButton(text.isNotEmpty())
                    textChangedListener?.invoke(text)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Clear 버튼 클릭 처리
        binding.lyClear.setOnClickListener {
            clearClickListener?.invoke()
//            binding.tvTitle.text?.clear()
        }

        // 초기 상태
        binding.tvTitle.hint = "새로운 태그 첨부"
        showClearButton(false)
        showCheckbox(false)
        showDeleteButton(false)
    }

    fun setDeleteMode(enabled: Boolean, selectedCount: Int = 0) {
        isDeleteMode = enabled

        if (enabled) {
            binding.tvTitle.apply {
                isEnabled = false
                setText("${selectedCount}개")
                setTextColor(blueColor)
            }
            showClearButton(false)
            showCheckbox(true)
            showDeleteButton(true)

            // 전체 선택 체크박스 초기화 (초기엔 false)
            binding.cbMainHeaderCheckbox.setOnCheckedChangeListener(null)
            binding.cbMainHeaderCheckbox.isChecked = false
            binding.cbMainHeaderCheckbox.setOnCheckedChangeListener { _, isChecked ->
                checkboxChangedListener?.invoke(isChecked)
            }

        } else {
            binding.tvTitle.apply {
                isEnabled = true
                setText("")
                setTextColor(blackColor)
                hint = "새로운 태그 첨부"
            }
            showClearButton(false)
            showCheckbox(false)
            showDeleteButton(false)
        }
    }

    fun updateSelectedCount(count: Int) {
        if (isDeleteMode) {
            binding.tvTitle.setText("${count}개")
        }
    }

    fun setHint(hint: String) {
        binding.tvTitle.hint = hint
    }

    fun setEditText(text: String) {
        val current = binding.tvTitle.text?.toString() ?: ""
        if (current == text) return  // 동일한 텍스트면 무시

        binding.tvTitle.setText(text)
        binding.tvTitle.setSelection(text.length)
    }

    fun clearText() {
        binding.tvTitle.text?.clear()
    }

    fun showClearButton(show: Boolean) {
        if (isDeleteMode) {
            binding.lyClear.visibility = View.GONE
        } else {
            binding.lyClear.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    fun showCheckbox(show: Boolean) {
        binding.cbMainHeaderCheckbox.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun showDeleteButton(show: Boolean) {
        binding.lyDelete.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun setOnTextChangedListener(listener: (String) -> Unit) {
        this.textChangedListener = listener
    }

    fun setOnClearClickListener(listener: () -> Unit) {
        this.clearClickListener = listener
    }

    fun getInputText(): String = binding.tvTitle.text.toString()

    fun setOnBackClickListener(listener: () -> Unit) {
        binding.lyBack.setOnClickListener { listener() }
    }

    fun setOnDeleteClickListener(listener: () -> Unit) {
        binding.lyDelete.setOnClickListener { listener() }
    }

    fun setOnCheckboxToggleListener(listener: (Boolean) -> Unit) {
        binding.cbMainHeaderCheckbox.setOnCheckedChangeListener { _, isChecked ->
            listener(isChecked)
        }
    }

    fun setEditTextEnabled(enabled: Boolean) {
        binding.tvTitle.isEnabled = enabled
        binding.tvTitle.isFocusable = enabled
        binding.tvTitle.isFocusableInTouchMode = enabled
    }

    fun setEditTextColor(color: Int) {
        binding.tvTitle.setTextColor(color)
    }

    fun setHeaderCheckbox(checked: Boolean) {
        binding.cbMainHeaderCheckbox.setOnCheckedChangeListener(null)
        binding.cbMainHeaderCheckbox.isChecked = checked
        binding.cbMainHeaderCheckbox.setOnCheckedChangeListener { _, isChecked ->
            checkboxChangedListener?.invoke(isChecked)
        }
    }

    fun setOnEditTextFocusListener(listener: () -> Unit) {
        binding.tvTitle.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) listener.invoke()
        }
    }

    fun clearTextFocus() {
        binding.tvTitle.clearFocus()
    }

    fun setOnEditorActionListener(listener: () -> Unit) {
        binding.tvTitle.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                listener()
                true
            } else false
        }
    }

    private var checkboxChangedListener: ((Boolean) -> Unit)? = null
    fun setOnHeaderCheckboxChanged(listener: (Boolean) -> Unit) {
        this.checkboxChangedListener = listener
    }


    fun focusAndShowKeyboard() {
        val et = binding.tvTitle

        et.isEnabled = true
        et.isFocusable = true
        et.isFocusableInTouchMode = true

        // 아직 attach 안됐으면 attach 후에 재시도
        if (!et.isAttachedToWindow) {
            et.post { focusAndShowKeyboard() }
            return
        }

        et.requestFocus()
        et.setSelection(et.text?.length ?: 0)
        et.showSoftInputOnFocus = true
        et.post {
            et.showIme()
        }
    }
}

