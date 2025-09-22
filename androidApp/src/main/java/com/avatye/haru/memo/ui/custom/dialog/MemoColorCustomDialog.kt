package com.avatye.haru.memo.ui.custom.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.avatye.haru.memo.R
import com.avatye.haru.memo.data.extension.toForeground
import com.avatye.haru.memo.databinding.DialogMemoColorCustomBinding

internal class MemoColorCustomDialog(
    context: Context,
    private var selectedColor: Int
) : Dialog(context) {

    private val binding = DialogMemoColorCustomBinding.inflate(LayoutInflater.from(context))
    private val colorViewMap: Map<View, Int> by lazy {
        mapOf(
            binding.colorWhite to R.color.bg_white,
            binding.colorYellow to R.color.bg_yellow,
            binding.colorGreen to R.color.bg_green,
            binding.colorBlue to R.color.bg_blue,
            binding.colorOrange to R.color.bg_orange,
            binding.colorPink to R.color.bg_pink
        )
    }

    private val originalColor: Int = selectedColor

    private var onPreview: ((Int) -> Unit)? = null
    private var onConfirm: ((Int) -> Unit)? = null
    private var onCancel: ((Int) -> Unit)? = null

    companion object {
        const val NAME = "MemoColorCustomDialog"
    }

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val selectedDrawable = ContextCompat.getDrawable(context, R.drawable.shape_color_button_selected)

        colorViewMap.forEach { (view, resId) ->
            val colorInt = ContextCompat.getColor(context, resId)
            if(colorInt == selectedColor) {
                view.toForeground(selectedDrawable)
            }


            view.isSelected = (colorInt == selectedColor)
            view.setOnClickListener {
                view.toForeground(null)
                clearSelection()
                view.toForeground(selectedDrawable)
                view.isSelected = true
                selectedColor = colorInt
                onPreview?.invoke(colorInt)
            }
        }

        binding.btnConfirm.setOnClickListener {
            onConfirm?.invoke(selectedColor)
        }

        binding.btnCancel.setOnClickListener {
            clearSelection()

            colorViewMap.forEach { (view, resId) ->
                val colorInt = ContextCompat.getColor(context, resId)
                if (colorInt == originalColor) {
                    view.isSelected = true
                }
            }

            onCancel?.invoke(originalColor)
            dismiss()
        }
    }

    private fun clearSelection() {
        colorViewMap.forEach { (view, resId) ->
            view.isSelected = false
            view.toForeground(null)
        }
    }

    fun setButton(
        onPreview: ((Int) -> Unit)? = null,
        onConfirm: ((Int) -> Unit)? = null,
        onCancel: ((Int) -> Unit)? = null
    ) {
        this.onPreview = onPreview
        this.onConfirm = onConfirm
        this.onCancel = onCancel
    }

    fun onDestroy() {
        colorViewMap.keys.forEach { it.setOnClickListener(null) }
        binding.btnCancel.setOnClickListener(null)
        binding.btnConfirm.setOnClickListener(null)
        dismiss()
    }
}