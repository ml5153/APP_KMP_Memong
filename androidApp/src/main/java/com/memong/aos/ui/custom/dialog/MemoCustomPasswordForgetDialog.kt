package com.memong.aos.ui.custom.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.memong.aos.databinding.DialogMemoCustomForgetPasswordBinding

internal class MemoCustomPasswordForgetDialog(context: Context) : Dialog(context) {
    private val binding: DialogMemoCustomForgetPasswordBinding =
        DialogMemoCustomForgetPasswordBinding.inflate(LayoutInflater.from(context))

    companion object {
        const val NAME = "MemogCustomPasswordForgetDialog"
    }

    init {
        setContentView(binding.root)
        setCancelable(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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

    fun setMessage(message: String) {
        binding.tvMessage.text = message
        binding.tvMessage.visibility = if (message.isNotEmpty()) View.VISIBLE else View.GONE
    }

    fun setSubMessage(sub: String) {
        binding.tvMessageSub.text = sub
        binding.tvMessageSub.visibility = if (sub.isNotEmpty()) View.VISIBLE else View.GONE
    }

    fun setBottomSubMessage(sub: String) {
        binding.tvBottomSub.text = sub
        binding.tvBottomSub.visibility = if (sub.isNotEmpty()) View.VISIBLE else View.GONE
    }

    fun setButton(
        confirmText: String,
        onConfirm: (() -> Unit)? = null
    ) {
        binding.btnConfirm.text = confirmText

        binding.btnConfirm.setOnClickListener {
            onConfirm?.invoke()
        }
    }

    fun onDestroy() {
        binding.btnConfirm.setOnClickListener(null)
        this@MemoCustomPasswordForgetDialog.dismiss()
    }
}