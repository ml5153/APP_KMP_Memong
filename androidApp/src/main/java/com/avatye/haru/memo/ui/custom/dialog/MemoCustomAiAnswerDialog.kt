package com.avatye.haru.memo.ui.custom.dialog

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.avatye.haru.memo.data.utils.Util
import com.avatye.haru.memo.databinding.DialogMemoAiAnswerCustomBinding
import com.avatye.haru.memo.databinding.DialogMemoCustomBinding

enum class DialogMode {
    ANSWER,  // AI 답변 모드
    SUMMARY  // AI 요약 모드
}

internal class MemoCustomAiAnswerDialog(
    context: Context,
    private val mode: DialogMode
) : Dialog(context) {

    private val binding: DialogMemoAiAnswerCustomBinding =
        DialogMemoAiAnswerCustomBinding.inflate(LayoutInflater.from(context))

    companion object {
        const val NAME = "MemoCustomAiAnswerDialog"
    }

    init {
        setContentView(binding.root)
        setCancelable(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // 공통 클립보드 복사 처리
        binding.ivClipboard.setOnClickListener {
            val textToCopy = binding.tvMessage.text?.toString() ?: ""
            if (textToCopy.isNotEmpty()) {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(
                    if (mode == DialogMode.ANSWER) "AI Answer" else "AI Summary",
                    textToCopy
                )
                clipboard.setPrimaryClip(clip)
            } else {
                Util.toastShort(context, "복사할 내용이 없습니다 😢")
            }
        }
    }

    fun setTitleSpannable(normal: String, highlight: String, highlightColor: Int) {
        val full = normal + highlight
        val spannable = SpannableString(full).apply {
            setSpan(
                ForegroundColorSpan(Color.BLACK),
                0, normal.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(highlightColor),
                normal.length, full.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        binding.tvTitle.text = spannable
        binding.tvTitle.visibility = if (spannable.isNotEmpty()) View.VISIBLE else View.GONE
    }

    fun setTitle(title: String) {
        binding.tvTitle.text = title
        binding.tvTitle.visibility = if (title.isNotEmpty()) View.VISIBLE else View.GONE
    }

    fun setMessage(message: CharSequence) {
        binding.tvMessage.text = message
        binding.tvMessage.visibility = if (message.isNotEmpty()) View.VISIBLE else View.GONE
    }

    fun setBottomSubMessage(show: Boolean) {
        if (show) {
            binding.tvBottomSub.text = "* 이미지 메모는 첫번째 이미지만 요약됩니다."
            binding.tvBottomSub.visibility = View.VISIBLE
        } else {
            binding.tvBottomSub.visibility = View.GONE
        }
    }

    /**
     * 버튼 세팅
     * - ANSWER 모드 → 취소/확인 버튼 2개
     * - SUMMARY 모드 → 확인 버튼 하나만 (폭 전체 차지)
     */
    fun setButton(
        cancelText: String,
        confirmText: String,
        onCancel: (() -> Unit)? = null,
        onConfirm: (() -> Unit)? = null
    ) {
        if (mode == DialogMode.ANSWER) {
            // 버튼 2개
            binding.btnCancel.visibility = View.VISIBLE
            binding.btnConfirm.visibility = View.VISIBLE

            // 각각 weight=1 로 꽉 차게
            (binding.btnCancel.layoutParams as LinearLayout.LayoutParams).apply {
                width = 0
                weight = 1f
            }.also { binding.btnCancel.layoutParams = it }

            (binding.btnConfirm.layoutParams as LinearLayout.LayoutParams).apply {
                width = 0
                weight = 1f
            }.also { binding.btnConfirm.layoutParams = it }

            binding.btnCancel.text = cancelText
            binding.btnConfirm.text = confirmText

            binding.btnCancel.setOnClickListener {
                binding.btnCancel.isEnabled = false
                onCancel?.invoke()
            }

            binding.btnConfirm.setOnClickListener {
                binding.btnConfirm.isEnabled = false
                onConfirm?.invoke()
            }

        } else {
            // 버튼 1개 (취소 버튼만 전체 폭 차지)
            binding.btnConfirm.visibility = View.GONE
            binding.dividerView.visibility = View.GONE
            binding.btnCancel.visibility = View.VISIBLE
            (binding.btnCancel.layoutParams as LinearLayout.LayoutParams).apply {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                weight = 0f
            }.also { binding.btnCancel.layoutParams = it }

            binding.btnCancel.text = cancelText
            binding.btnCancel.setOnClickListener {
                binding.btnCancel.isEnabled = false
                onCancel?.invoke()
            }
        }
    }

    fun onDestroy() {
        binding.btnCancel.setOnClickListener(null)
        binding.btnConfirm.setOnClickListener(null)
        binding.ivClipboard.setOnClickListener(null)
        this.dismiss()
    }
}