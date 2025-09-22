package com.avatye.haru.memo.ui.custom.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import com.avatye.haru.memo.data.enum.EraserType
import com.avatye.haru.memo.data.enum.PenType

internal class DrawingCanvasView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {


    private val pathList = mutableListOf<Pair<Path, Paint>>()
    private val redoList = mutableListOf<Pair<Path, Paint>>()
    private var currentPath: Path? = null
    private var currentPaint: Paint? = null

    private var currentPenType: PenType = PenType.PENCIL
    private var currentEraserType: EraserType? = null

    private var lastX = 0f
    private var lastY = 0f
    private var penColor = Color.BLACK

    private var showEraserCursor = false
    private var eraserCursorX = 0f
    private var eraserCursorY = 0f


    var onStateChanged: (() -> Unit)? = null

    companion object {
        const val NAME: String = "DrawingCanvasView"

        // 초기값
        var penSize = 20f      //  팬 (0~100)
        var eraserSize = 150f  // 지우개 (100~200)
    }

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        pathList.forEach { (path, paint) ->
            canvas.drawPath(path, paint)
        }
        currentPath?.let { path ->
            currentPaint?.let { paint ->
                canvas.drawPath(path, paint)
            }
        }

        if (currentEraserType == EraserType.AREA && showEraserCursor) {
            val previewPaint = Paint().apply {
                style = Paint.Style.STROKE
                color = Color.GRAY
                strokeWidth = 2f
                isAntiAlias = true
            }
            canvas.drawCircle(eraserCursorX, eraserCursorY, eraserSize / 2f, previewPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (currentEraserType == EraserType.STROKE) {
                    val touchX = x
                    val touchY = y
                    val iterator = pathList.iterator()
                    while (iterator.hasNext()) {
                        val (path, _) = iterator.next()
                        val bounds = android.graphics.RectF()
                        path.computeBounds(bounds, true)

                        // 경계 박스를 넓혀서 터치 허용 범위 증가
                        bounds.inset(-penSize, -penSize)

                        if (bounds.contains(touchX, touchY)) {
                            iterator.remove()
                            onStateChanged?.invoke()
                            invalidate()
                            break
                        }
                    }
                    return true
                }

                // eraser
                if (currentEraserType == EraserType.AREA) {
                    eraserCursorX = x
                    eraserCursorY = y
                    showEraserCursor = true
                    invalidate()
                }

                currentPath = Path().apply { moveTo(x, y) }
                lastX = x
                lastY = y
                currentPaint = createPaint()
                redoList.clear()
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                if (currentEraserType == EraserType.STROKE) return true

                // eraser
                if (currentEraserType == EraserType.AREA) {
                    eraserCursorX = x
                    eraserCursorY = y
                    showEraserCursor = true
                    invalidate()
                }

                currentPath?.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
                lastX = x
                lastY = y
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                if (currentEraserType == EraserType.STROKE) return true

                // eraser
                if (currentEraserType == EraserType.AREA) {
                    showEraserCursor = false
                    invalidate()
                }

                currentPath?.let { path ->
                    path.lineTo(x, y)
                    pathList.add(Pair(path, Paint(currentPaint)))
                    currentPath = null
                    currentPaint = null
                    redoList.clear()
                    onStateChanged?.invoke()
                    invalidate()
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                // eraser
                if (currentEraserType == EraserType.STROKE) {
                    showEraserCursor = false
                    invalidate()
                }
            }
        }
        return true
    }


    private fun createPaint(): Paint {
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = if (currentEraserType != null) Color.WHITE else penColor
            strokeWidth = if (currentEraserType == EraserType.AREA) eraserSize else penSize
        }

        if (currentEraserType == null) {
            setPenEffect(paint)
        }

        return paint
    }


    fun undo() {
        if (pathList.isNotEmpty()) {
            redoList.add(pathList.removeAt(pathList.lastIndex))
            onStateChanged?.invoke()
            invalidate()
        }
    }

    fun redo() {
        if (redoList.isNotEmpty()) {
            pathList.add(redoList.removeAt(redoList.lastIndex))
            onStateChanged?.invoke()
            invalidate()
        }
    }

    fun setPenType(type: PenType) {
        currentPenType = type
        currentEraserType = null
    }

    fun setPenColor(@ColorInt color: Int) {
        penColor = color
    }


    fun setPenSize(size: Int) {
        penSize = size.toFloat().coerceAtLeast(1f)
    }

    private fun setPenEffect(paint: Paint) {
        when (currentPenType) {
            PenType.PENCIL -> {
                paint.apply {
                    maskFilter = null
                    strokeWidth = penSize
                    alpha = 255
                    pathEffect = null
                }
            }

            PenType.HIGHLIGHTER -> {
                paint.apply {
                    alpha = 80
                    strokeWidth = penSize     // 형광펜은 더 두껍고 투명
                    pathEffect = null
                }
            }

            PenType.DOTTED_LINE -> {
                paint.apply {
                    alpha = 255
                    strokeWidth = penSize.coerceAtMost(8f) // 두껍게 하면 점선 안보임
                    pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
                }
            }

            PenType.NEON -> {
                paint.apply {
                    strokeWidth = penSize
                    alpha = 200
                    setShadowLayer(10f, 0f, 0f, penColor)  // Glow 효과
                    color = penColor
                }
            }
        }
    }

    fun setEraserType(type: EraserType) {
        currentEraserType = type
    }

    fun setEraserSize(size: Int) {
        eraserSize = size.toFloat().coerceAtLeast(1f)
    }

    fun clearAll() {
        while (pathList.isNotEmpty()) {
            redoList.add(pathList.removeAt(pathList.lastIndex))
        }
        currentPath = null
        currentPaint = null
        onStateChanged?.invoke()
        invalidate()
    }


    fun getBitmap(): Bitmap? {
        if (pathList.isEmpty() && currentPath == null) return null

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap).apply {
            drawColor(Color.WHITE)
        }
        draw(canvas)
        return bitmap
    }

    fun hasContent(): Boolean = pathList.isNotEmpty()

    fun canUndo(): Boolean = pathList.isNotEmpty()

    fun canRedo(): Boolean = redoList.isNotEmpty()
}
