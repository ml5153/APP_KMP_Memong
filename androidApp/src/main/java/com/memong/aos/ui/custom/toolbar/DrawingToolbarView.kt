package com.memong.aos.ui.custom.toolbar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.avatye.haru.log.LogTrack
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.memong.aos.R
import com.memong.aos.data.enum.EraserType
import com.memong.aos.data.enum.PenType
import com.memong.aos.data.utils.Util
import com.memong.aos.databinding.ViewDrawingToolbarBinding
import com.memong.aos.ui.custom.bottomsheet.EraserToolBottomSheetDialog
import java.util.Locale

internal class DrawingToolbarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    companion object {
        const val NAME: String = "DrawingToolbarView"
    }

    private var binding: ViewDrawingToolbarBinding = ViewDrawingToolbarBinding.inflate(LayoutInflater.from(context), this, true)

    var onPenSelected: ((PenType, Int) -> Unit)? = null
    var onEraserSelected: ((EraserType) -> Unit)? = null
    var onEraserSizeChanged: ((Int) -> Unit)? = null
    var onColorPaletteClick: ((Int) -> Unit)? = null


    private var currentPenType = PenType.PENCIL

    private var currentSelectedColor: Int = Color.BLACK

    private val penSizeMap = mutableMapOf<PenType, Int>().apply {
        PenType.entries.forEach { put(it, 20) }
    }

    private val penThumbTextView = TextView(context).apply {
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        textSize = 12f
        setTextColor(ContextCompat.getColor(context, android.R.color.white))
        background = ContextCompat.getDrawable(context, R.drawable.ripple_rectangle_all_radius_12dp)
    }


    init {
        initPenTool()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initPenTool() {
        with(binding) {
            // Pen
            lyPencil.setOnClickListener { selectPen(PenType.PENCIL) }
            lyHighlighter.setOnClickListener { selectPen(PenType.HIGHLIGHTER) }
            lyDottedLine.setOnClickListener { selectPen(PenType.DOTTED_LINE) }
            lyNeon.setOnClickListener { selectPen(PenType.NEON) }

            // Eraser
            lyEraser.setOnClickListener {
                updateToolSelectionUI(isEraser = true)

                val defaultEraserType = EraserType.AREA // 초기값
                val eraserDialog = EraserToolBottomSheetDialog(initialType = defaultEraserType).apply {
                    onSelected = { mode ->
                        onEraserSelected?.invoke(mode)
                    }
                    onSizeChanged = { size ->
                        onEraserSizeChanged?.invoke(size)
                    }
                }

                onEraserSelected?.invoke(defaultEraserType)
                (context as? FragmentActivity)?.supportFragmentManager?.let {
                    eraserDialog.show(it, "EraserToolDialog")
                }
            }

            // Palette
            lyPalette.setOnClickListener {
                showColorPicker(selectedColor = currentSelectedColor)
            }


            penSizeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    penSizeMap[currentPenType] = progress
                    onPenSelected?.invoke(currentPenType, progress)
                    updateSeekBarThumbWithText(penSizeSeekbar, progress)
                }

                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }

        // 초기 선택
        selectPen(PenType.PENCIL)
        addView(penThumbTextView)
    }


    private fun selectPen(type: PenType) {
        currentPenType = type
        binding.lyPenToolContainer.isVisible = true

        // UI 업데이트
        updateToolSelectionUI(selectedPenType = type)

        // 각 펜에 맞는 굵기 설정
        binding.penSizeSeekbar.progress = penSizeMap[type] ?: 20

        // 리스너 호출
        onPenSelected?.invoke(type, binding.penSizeSeekbar.progress)


    }

    private fun updateToolSelectionUI(selectedPenType: PenType? = null, isEraser: Boolean = false) {
        val selectedBg = ContextCompat.getDrawable(context, R.drawable.shape_bg_circle_transparent)
        val defaultBg = ContextCompat.getDrawable(context, R.drawable.ripple_circle_normal_toolbar)

        binding.lyPencil.background = if (selectedPenType == PenType.PENCIL) selectedBg else defaultBg
        binding.lyHighlighter.background = if (selectedPenType == PenType.HIGHLIGHTER) selectedBg else defaultBg
        binding.lyDottedLine.background = if (selectedPenType == PenType.DOTTED_LINE) selectedBg else defaultBg
        binding.lyNeon.background = if (selectedPenType == PenType.NEON) selectedBg else defaultBg
        binding.lyEraser.background = if (isEraser) selectedBg else defaultBg
    }

    private fun updateSeekBarThumbWithText(seekBar: SeekBar, value: Int) {
        val context = seekBar.context
        val baseThumb = ContextCompat.getDrawable(context, R.drawable.shape_seekbar_thumb) ?: return

        val textView = TextView(context).apply {
            text = String.format(Locale.getDefault(), "%d", value)
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 12f
            setPadding(0, 0, 0, 0)
        }

        val spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
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
            val thumbSize = Util.dpToPx(context, 30) // Thumb 기준 크기
            val left = (thumbSize - textView.measuredWidth) / 2
            val top = (thumbSize - textView.measuredHeight) / 2
            val right = thumbSize - left - textView.measuredWidth
            val bottom = thumbSize - top - textView.measuredHeight
            layerDrawable.setLayerInset(1, left, top, right, bottom)
        }

        seekBar.thumb = layerDrawable
    }


    private fun showColorPicker(selectedColor: Int) {
        LogTrack.i { "$NAME -> showColorPicker -> selectedColor: $selectedColor" }

        context?.let { ctx ->
            ColorPickerDialogBuilder
                .with(context)
                .setTitle(ctx.getString(R.string.haru_drawing_memo_pick_color))
                .initialColor(selectedColor) // 초기 선택 색상
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER) // WHEEL_TYPE
                .setColorEditTextColor(R.color.bg_yellow)
                .showLightnessSlider(true) // 밝기 슬라이더 표시 여부
                .showAlphaSlider(false) // 투명도(알파) 슬라이더 표시 여부
                .showColorEdit(true)
                .density(30) // 팔레트의 밀도 (색상 개수)
                .setPositiveButton(ctx.getString(R.string.haru_common_layout_confirm)) { dialog, color, _ ->
                    currentSelectedColor = color
                    binding.btnPaletteColor.imageTintList = ColorStateList.valueOf(color)
                    onColorPaletteClick?.invoke(color)
                }

                .setNegativeButton(ctx.getString(R.string.haru_common_layout_cancel)) { dialog, which ->
                    // 취소 시 동작
                }
                .build()
                .show()
        }
    }


}
