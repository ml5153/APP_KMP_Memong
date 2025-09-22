package com.memong.aos.ui.custom.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import com.memong.aos.databinding.DialogMemoCustomRestoreBinding

class MemoCustomRestoreDialog(
    context: Context,
    private val onPhoneBackupClick: () -> Unit,
    private val onGoogleDriveClick: () -> Unit,
    private val onHowToClick: () -> Unit
) : Dialog(context) {

    private val binding = DialogMemoCustomRestoreBinding.inflate(LayoutInflater.from(context))


    init {
        setContentView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(true)

        binding.btnPhoneBackup.setOnClickListener { onPhoneBackupClick() }
        binding.btnGoogleDrive.setOnClickListener { onGoogleDriveClick() }
        binding.btnHowTo.setOnClickListener { onHowToClick() }
    }

    fun setTitleText(text: String) {
        binding.tvTitle.text = text
    }

    fun setBottomSubText(text: String) {
        binding.tvBottomSub.text = text
    }

}
