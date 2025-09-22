package com.memong.aos.ui.custom.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.RadioButton
import com.memong.aos.data.enum.MemoSortType
import com.memong.aos.data.utils.PreferenceUtil
import com.memong.aos.databinding.DialogMemoSortingCustomBinding

internal class MemoSortingCustomDialog(context: Context) : Dialog(context) {

    private val binding = DialogMemoSortingCustomBinding.inflate(LayoutInflater.from(context))

    companion object {
        const val NAME = "MemoSortingCustomDialog"
    }


    init {
        setContentView(binding.root)
        setDefault()
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    fun setButtonListener(
        onLatestCreate: (() -> Unit)? = null,
        onOldestCreate: (() -> Unit)? = null,
        onLatestUpdate: (() -> Unit)? = null,
        onOldestUpdate: (() -> Unit)? = null,
        onCustom: (() -> Unit)? = null,
    ) {

        binding.lyLatestCreate.setOnClickListener {
            PreferenceUtil.set(key = PreferenceUtil.KEY_MEMO_SORTING_CURRENT_TYPE, value = MemoSortType.LATEST_CREATE.name)
            selectOnly(target = binding.rbLatestCreate)
            onLatestCreate?.invoke()
        }
        binding.lyOldestCreate.setOnClickListener {
            PreferenceUtil.set(key = PreferenceUtil.KEY_MEMO_SORTING_CURRENT_TYPE, value = MemoSortType.OLDEST_CREATE.name)
            selectOnly(target = binding.rbOldestCreate)
            onOldestCreate?.invoke()
        }
        binding.lyLatestUpdate.setOnClickListener {
            PreferenceUtil.set(key = PreferenceUtil.KEY_MEMO_SORTING_CURRENT_TYPE, value = MemoSortType.LATEST_UPDATE.name)
            selectOnly(target = binding.rbLatestUpdate)
            onLatestUpdate?.invoke()
        }
        binding.lyOldestUpdate.setOnClickListener {
            PreferenceUtil.set(key = PreferenceUtil.KEY_MEMO_SORTING_CURRENT_TYPE, value = MemoSortType.OLDEST_UPDATE.name)
            selectOnly(target = binding.rbOldestUpdate)
            onOldestUpdate?.invoke()
        }
        binding.lyCustom.setOnClickListener {
            PreferenceUtil.set(key = PreferenceUtil.KEY_MEMO_SORTING_CURRENT_TYPE, value = MemoSortType.CUSTOM.name)
            selectOnly(target = binding.rbCustom)
            onCustom?.invoke()
        }
    }

    private fun setDefault() {
        val currentSortType = PreferenceUtil.get(key = PreferenceUtil.KEY_MEMO_SORTING_CURRENT_TYPE, defaultValue = MemoSortType.LATEST_CREATE.name)
        when (currentSortType) {
            MemoSortType.LATEST_CREATE.name -> selectOnly(target = binding.rbLatestCreate)
            MemoSortType.OLDEST_CREATE.name -> selectOnly(target = binding.rbOldestCreate)
            MemoSortType.LATEST_UPDATE.name -> selectOnly(target = binding.rbLatestUpdate)
            MemoSortType.OLDEST_UPDATE.name -> selectOnly(target = binding.rbOldestUpdate)
            MemoSortType.CUSTOM.name -> selectOnly(target = binding.rbCustom)
        }
    }


    private fun selectOnly(target: RadioButton) {
        val buttons = listOf(
            binding.rbLatestCreate,
            binding.rbOldestCreate,
            binding.rbLatestUpdate,
            binding.rbOldestUpdate,
            binding.rbCustom
        )
        buttons.forEach { it.isChecked = (it == target) }
    }


    fun onDestroy() {
        this@MemoSortingCustomDialog.dismiss()
    }

}