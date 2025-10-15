package com.memong.aos.ui.custom.bottomsheet

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.memong.aos.R
import com.memong.aos.data.enum.EraserType
import com.memong.aos.data.utils.Util
import com.memong.aos.databinding.DialogBottomSheetEraserToolBinding
import java.util.Locale

internal class EraserToolBottomSheetDialog(
    private val initialType: EraserType
) : BottomSheetDialogFragment() {

    var onSelected: ((EraserType) -> Unit)? = null
    var onSizeChanged: ((Int) -> Unit)? = null

    private var eraserSize = 80

    private lateinit var binding: DialogBottomSheetEraserToolBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogBottomSheetEraserToolBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.background = null // 배경 제거
            bottomSheet?.setBackgroundResource(R.drawable.shape_bg_rounded_top_white) // 둥근 배경 적용
        }
        return dialog
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {

            updateSelectionUI(selectedMode = initialType)

            tvEraseArea.setOnClickListener {
                updateSelectionUI(EraserType.AREA)
                onSelected?.invoke(EraserType.AREA)
            }
            tvEraseStroke.setOnClickListener {
                updateSelectionUI(EraserType.STROKE)
                onSelected?.invoke(EraserType.STROKE)
            }
            tvEraseAll.setOnClickListener {
                updateSelectionUI(EraserType.ALL)
                onSelected?.invoke(EraserType.ALL)
                dismiss()
            }

            eraserSizeSeekbar.progress = eraserSize
            updateSeekBarThumbWithText(eraserSizeSeekbar, eraserSize)
            eraserSizeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    eraserSize = progress
                    updateSeekBarThumbWithText(eraserSizeSeekbar, progress)
                    onSizeChanged?.invoke(progress)
                }

                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }
    }


    private fun updateSelectionUI(selectedMode: EraserType) {
        val ctx = context ?: return
        with(binding) {
            // 기본 색상과 선택 색상 정의
            val selectedColor = ContextCompat.getColor(ctx, R.color.haru_primary_orange)
            val defaultColor = ContextCompat.getColor(ctx, R.color.haru_black)

            tvEraseArea.setTextColor(if (selectedMode == EraserType.AREA) selectedColor else defaultColor)
            tvEraseStroke.setTextColor(if (selectedMode == EraserType.STROKE) selectedColor else defaultColor)
            tvEraseAll.setTextColor(if (selectedMode == EraserType.ALL) selectedColor else defaultColor)

            eraserSizeSeekbar.isVisible = selectedMode == EraserType.AREA
        }
    }


    private fun updateSeekBarThumbWithText(seekBar: SeekBar, value: Int) {
        val context = seekBar.context
        val baseThumb = ContextCompat.getDrawable(context, R.drawable.shape_seekbar_thumb) ?: return

        val textView = TextView(context).apply {
            text = String.format(Locale.getDefault(), "%d", value)
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 12f
        }

        val spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        textView.measure(spec, spec)
        textView.layout(0, 0, textView.measuredWidth, textView.measuredHeight)

        val bitmap = Bitmap.createBitmap(
            textView.measuredWidth,
            textView.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        textView.draw(canvas)

        val textDrawable = BitmapDrawable(context.resources, bitmap)
        val layerDrawable = LayerDrawable(arrayOf(baseThumb, textDrawable))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            layerDrawable.setLayerGravity(1, Gravity.CENTER)
            layerDrawable.setLayerSize(1, textView.measuredWidth, textView.measuredHeight)
        } else {
            val thumbSize = Util.dpToPx(context, 30)
            val left = (thumbSize - textView.measuredWidth) / 2
            val top = (thumbSize - textView.measuredHeight) / 2
            val right = thumbSize - left - textView.measuredWidth
            val bottom = thumbSize - top - textView.measuredHeight
            layerDrawable.setLayerInset(1, left, top, right, bottom)
        }

        seekBar.thumb = layerDrawable
    }

}

