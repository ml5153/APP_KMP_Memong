package com.avatye.haru.memo.ui.custom.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import com.avatye.haru.memo.R
import com.avatye.haru.memo.data.utils.GoogleDriveUtil
import com.avatye.haru.memo.databinding.DialogMemoPopupCustomBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

internal class MemoCustomPopupDialog(context: Context) : Dialog(context) {

    private val binding: DialogMemoPopupCustomBinding =
        DialogMemoPopupCustomBinding.inflate(LayoutInflater.from(context))

    companion object {
        const val NAME = "MemogCustomPopupDialog"
    }

    init {
        setContentView(binding.root)
        setCancelable(false)
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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

    /**
     * 이미지 URL + 클릭 시 행동 지정
     */
    fun setImage(url: String, onClick: (() -> Unit)? = null) {
        Glide.with(binding.ivMessageImage.context)
            .load(GoogleDriveUtil.toDirectUrl(url))
            .transform(RoundedCorners(12.dpToPx())) // or CenterInside()
            .placeholder(R.drawable.ic_main_popup_loading_image)
            .error(R.drawable.ic_main_popup_error_image)
            .into(binding.ivMessageImage)

        binding.ivMessageImage.setOnClickListener {
            onClick?.invoke()
        }
    }

    fun Int.dpToPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()


    fun onDestroy() {
        binding.btnConfirm.setOnClickListener(null)
        binding.ivMessageImage.setOnClickListener(null)
        this@MemoCustomPopupDialog.dismiss()
    }
}
