package ru.mkirilkin.doodlekong.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import ru.mkirilkin.doodlekong.data.remote.websocket.models.messages.DrawData
import ru.mkirilkin.doodlekong.util.Constants
import java.util.*
import kotlin.math.abs

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var roomName: String? = null

    var isUserDrawing = false
        set(value) {
            isEnabled = value
            field = value
        }

    var isDrawing = false

    private var viewWidth: Int? = null
    private var viewHeight: Int? = null
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private var curX: Float? = null
    private var curY: Float? = null
    private var smoothness = 5
    private var startedTouch = false

    private var paint = Paint(Paint.DITHER_FLAG).apply {
        isDither = true
        isAntiAlias = true
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = Constants.DEFAULT_PAINT_THICKNESS
    }

    private var path = Path()
    private var paths = Stack<PathData>()
    private var pathDataChangedListener: ((Stack<PathData>) -> Unit)? = null

    private var onDrawListener: ((DrawData) -> Unit)? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap!!)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val initialColor = paint.color
        val initialThickness = paint.strokeWidth
        for (pathData in paths) {
            paint.apply {
                color = pathData.color
                strokeWidth = pathData.thickness
            }
            canvas?.drawPath(pathData.path, paint)
        }
        paint.apply {
            color = initialColor
            strokeWidth = initialThickness
        }
        canvas?.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled) return false
        val newX = event?.x
        val newY = event?.y
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> startedTouch(newX ?: return false, newY ?: return false)
            MotionEvent.ACTION_MOVE -> moveTouch(newX ?: return false, newY ?: return false)
            MotionEvent.ACTION_UP -> releasedTouch()
        }
        return true
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        path.reset()
        invalidate()
    }

    fun setPathDataChangedListener(listener: (Stack<PathData>) -> Unit) {
        pathDataChangedListener = listener
    }

    fun setThickness(thickness: Float) {
        paint.strokeWidth = thickness
    }

    fun setColor(color: Int) {
        paint.color = color
    }

    fun clear() {
        canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY)
        paths.clear()
    }

    fun setOnDrawListener(listener: (DrawData) -> Unit) {
        onDrawListener = listener
    }

    fun startedTouchExternally(drawData: DrawData) {
        parseDrawData(drawData).apply {
            paint.color = color
            paint.strokeWidth = thickness
            path.reset()
            path.moveTo(fromX, fromY)
            invalidate()
            startedTouch = true
        }
    }

    fun movedTouchExternally(drawData: DrawData) {
        parseDrawData(drawData).apply {
            val dx = abs(toX - fromX)
            val dy = abs(toY - fromY)
            if (!startedTouch) {
                startedTouchExternally(drawData)
            }
            if (dx >= smoothness || dy >= smoothness) {
                path.quadTo(fromX, fromY, (fromX + toX) / 2f, (fromY + toY) / 2f)
                invalidate()
            }
        }
    }

    fun releaseTouchExternally(drawData: DrawData) {
        parseDrawData(drawData).apply {
            path.lineTo(fromX, fromY)
            canvas?.drawPath(path, paint)
            paths.push(PathData(path, paint.color, paint.strokeWidth))
            pathDataChangedListener?.let { change ->
                change(paths)
            }
            path = Path()
            invalidate()
            startedTouch = false
        }
    }

    fun undo() {
        if (paths.isNotEmpty()) {
            paths.pop()
            pathDataChangedListener?.let { change ->
                change(paths)
            }
            invalidate()
        }
    }

    fun finishOffDrawing() {
        isDrawing = false
        path.lineTo(curX ?: return, curY ?: return)
        canvas?.drawPath(path, paint)
        paths.push(PathData(path, paint.color, paint.strokeWidth))
        pathDataChangedListener?.let { change ->
            change(paths)
        }
        path = Path()
        invalidate()
    }

    private fun startedTouch(x: Float, y: Float) {
        path.reset()
        path.moveTo(x, y)
        curX = x
        curY = y
        onDrawListener?.let { draw ->
            val drawData = createDrawData(x, y, x, y, MotionEvent.ACTION_DOWN)
            draw(drawData)
        }
        invalidate()
    }

    private fun moveTouch(toX: Float, toY: Float) {
        val dx = abs(toX - (curX ?: return))
        val dy = abs(toY - (curY ?: return))
        if (dx >= smoothness || dy >= smoothness) {
            isDrawing = true
            path.quadTo(curX!!, curY!!, (curX!! + toX) / 2f, (curY!! + toY) / 2f)
            onDrawListener?.let { draw ->
                val drawData = createDrawData(curX!!, curY!!, toX, toY, MotionEvent.ACTION_MOVE)
                draw(drawData)
            }
            curX = toX
            curY = toY
            invalidate()
        }
    }

    private fun releasedTouch() {
        isDrawing = false
        path.lineTo((curX ?: return), (curY ?: return))
        paths.push(PathData(path, paint.color, paint.strokeWidth))
        pathDataChangedListener?.let { change ->
            change(paths)
        }
        onDrawListener?.let { draw ->
            val drawData = createDrawData(curX!!, curY!!, curX!!, curY!!, MotionEvent.ACTION_UP)
            draw(drawData)
        }
        path = Path()
        invalidate()
    }

    private fun createDrawData(
        fromX: Float,
        fromY: Float,
        toX: Float,
        toY: Float,
        motionEvent: Int
    ): DrawData {
        return DrawData(
            roomName = roomName
                ?: throw IllegalStateException("Must set the roomName in drawing view"),
            color = paint.color,
            thickness = paint.strokeWidth,
            fromX = fromX / viewWidth!!,
            fromY = fromY / viewHeight!!,
            toX = toX / viewWidth!!,
            toY = toY / viewHeight!!,
            motionEvent = motionEvent
        )
    }

    private fun parseDrawData(drawData: DrawData): DrawData {
        return drawData.copy(
            fromX = drawData.fromX * viewWidth!!,
            fromY = drawData.fromY * viewHeight!!,
            toX = drawData.toX * viewWidth!!,
            toY = drawData.toY * viewHeight!!,
        )
    }

    data class PathData(val path: Path, val color: Int, val thickness: Float)
}
