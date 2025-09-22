package com.memong.aos.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.core.content.FileProvider
import com.avatye.adcash.BannerAdSize
import com.avatye.haru.log.LogTrack
import com.memong.aos.BuildConfig
import com.memong.aos.data.enum.EraserType
import com.memong.aos.data.enum.PenType
import com.memong.aos.data.extension.start
import com.memong.aos.data.utils.EventUtil
import com.memong.aos.databinding.ActivityDrawBinding
import java.io.File
import java.io.FileOutputStream

internal class DrawActivity : BaseActivity() {

    override val NAME: String
        get() = DrawActivity::class.java.simpleName

    private lateinit var binding: ActivityDrawBinding

    companion object {
        fun start(activity: Activity, close: Boolean = false) {
            activity.start(
                intent = Intent(activity, DrawActivity::class.java),
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP,
                close = close
            )
        }

        fun createIntent(
            activity: Activity
        ): Intent {
            return Intent(activity, DrawActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }
    }


    private var selectedColor: Int = Color.BLACK


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureWindowInsets(binding.drawRootView, paddingDp = 0)

        binding.drawingCanvas.setPenColor(selectedColor)
        setHeaderView()
        setToolbar()

        // bottom Banner
        requestBannerAd(
            placementId = BuildConfig.ADCASH_BOTTOM_BANNER_PID,
            bannerAdSize = BannerAdSize.DYNAMIC,
            bannerView = binding.bottomBannerView
        )
    }


    private fun setHeaderView() {
        binding.headerDraw.apply {
            onBackClick = { finish() }
            onUndoClick = {
                binding.drawingCanvas.undo()
                updateToolbarState()
            }
            onRedoClick = {
                binding.drawingCanvas.redo()
                updateToolbarState()
            }
            onConfirmClick = {
                val bitmap: Bitmap? = binding.drawingCanvas.getBitmap()
                LogTrack.i(NAME) { "onCreate -> onCheckClick" }

                try {
                    val file = File(filesDir, "draw_${System.currentTimeMillis()}.png")
                    FileOutputStream(file).use { outputStream ->
                        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }

                    val resultIntent = Intent().apply {
                        putExtra(
                            "draw_uri",
                            FileProvider.getUriForFile(
                                this@DrawActivity,
                                "${packageName}.fileprovider",
                                file
                            )
                        )
                    }
                    EventUtil.sendEvent(this@DrawActivity, EventUtil.CATEGORY_DETAIL, EventUtil.ACTION_TOOL_DRAW)
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }


        binding.drawingCanvas.onStateChanged = {
            updateToolbarState()
        }

        // 초기 도구 상태 설정
        binding.drawingCanvas.setPenType(PenType.PENCIL)
        updateToolbarState()

    }


    private fun setToolbar() {
        binding.editorToolBar.apply {
            onPenSelected = { penType, size ->
                binding.drawingCanvas.setPenType(penType)
                binding.drawingCanvas.setPenSize(size)
                binding.drawingCanvas.setPenColor(selectedColor)
            }

            onEraserSelected = { mode ->
                binding.drawingCanvas.setEraserType(mode)

                when (mode) {
                    EraserType.AREA,
                    EraserType.STROKE -> {

                    }

                    EraserType.ALL -> {
                        binding.drawingCanvas.clearAll()
                    }

                    else -> {

                    }
                }
            }

            onEraserSizeChanged = { size ->
                binding.drawingCanvas.setEraserSize(size)
            }

            onColorPaletteClick = { color ->
                selectedColor = color
                binding.drawingCanvas.setPenColor(selectedColor)
            }
        }

    }


    private fun updateToolbarState() {
        val hasContent = binding.drawingCanvas.hasContent()
        val canUndo = binding.drawingCanvas.canUndo()
        val canRedo = binding.drawingCanvas.canRedo()

        binding.headerDraw.updateCanvasState(hasContent, canUndo, canRedo)
    }

    override fun onPause() {
        super.onPause()
        binding.bottomBannerView.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.bottomBannerView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.headerDraw.onDestroy()
        binding.bottomBannerView.onDestroy()
    }
}