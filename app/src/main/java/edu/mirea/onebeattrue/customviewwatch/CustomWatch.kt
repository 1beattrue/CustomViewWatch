package edu.mirea.onebeattrue.customviewwatch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

class CustomWatch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var hasNumbers: Boolean = false
    private var hasSeconds: Boolean = false
    private var backgroundColor: Int = Color.WHITE
    private var mainColor: Int = Color.BLACK

    private var hour: Int = 0
    private var minute: Int = 0
    private var second: Int = 0

    private val paint = Paint()

    private val handler = Handler(Looper.getMainLooper())

    init {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.CustomWatch,
            defStyleAttr,
            defStyleRes
        )

        hasNumbers = typedArray.getBoolean(R.styleable.CustomWatch_hasNumbers, false)
        hasSeconds = typedArray.getBoolean(R.styleable.CustomWatch_hasSeconds, false)
        backgroundColor = typedArray.getColor(R.styleable.CustomWatch_backgroundColor, Color.WHITE)
        mainColor = typedArray.getColor(R.styleable.CustomWatch_mainColor, Color.BLACK)

        val (h, m, s) = getCurrentTime()
        hour = h
        minute = m
        second = s

        typedArray.recycle()
    }

    private val clockRunnable = object : Runnable {
        override fun run() {
            val (h, m, s) = getCurrentTime()
            hour = h
            minute = m
            second = s

            invalidate()

            handler.postDelayed(this, 1000)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler.post(clockRunnable)
    }

    private val backgroundRect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.color = mainColor
        backgroundRect.set(0f, 0f, width.toFloat(), height.toFloat())
        val roundedCorner = minOf(width, height) / 8f
        canvas.drawRoundRect(
            backgroundRect,
            roundedCorner,
            roundedCorner,
            paint
        )

        paint.color = backgroundColor
        val cx = width / 2f
        val cy = height / 2f
        val dialRadius = (minOf(width, height) / 2 * 0.9).toFloat()
        canvas.drawCircle(
            cx,
            cy,
            dialRadius,
            paint
        )

        if (hasNumbers) {
            drawNumbers(dialRadius, cx, cy, canvas)
        }

        drawHourHand(dialRadius, cx, cy, canvas)
        drawMinuteHand(dialRadius, cx, cy, canvas)

        if (hasSeconds) {
            drawSecondHand(dialRadius, cx, cy, canvas)
        }
    }

    private fun drawSecondHand(
        dialRadius: Float,
        cx: Float,
        cy: Float,
        canvas: Canvas
    ) {
        canvas.save()

        val secondRotation = second * 6f
        canvas.rotate(secondRotation, cx, cy)

        paint.color = mainColor
        paint.strokeWidth = minOf(width, height) / 60f
        canvas.drawLine(cx, cy, cx, cy - dialRadius * 0.8f, paint)

        canvas.restore()
    }


    private fun drawMinuteHand(
        dialRadius: Float,
        cx: Float,
        cy: Float,
        canvas: Canvas
    ) {
        canvas.save()

        val minuteRotation = minute * 6f + second / 10f
        canvas.rotate(minuteRotation, cx, cy)

        paint.color = mainColor
        paint.strokeWidth = minOf(width, height) / 30f
        canvas.drawLine(cx, cy, cx, cy - dialRadius * 0.6f, paint)

        canvas.restore()
    }


    private fun drawHourHand(
        dialRadius: Float,
        cx: Float,
        cy: Float,
        canvas: Canvas
    ) {
        canvas.save()

        val hourRotation = (hour % 12) * 30f + minute / 2f
        canvas.rotate(hourRotation, cx, cy)

        paint.color = mainColor
        paint.strokeWidth = minOf(width, height) / 20f
        canvas.drawLine(cx, cy, cx, cy - dialRadius * 0.4f, paint)

        canvas.restore()
    }

    private fun drawNumbers(
        dialRadius: Float,
        cx: Float,
        cy: Float,
        canvas: Canvas
    ) {
        paint.color = mainColor
        val textSize = dialRadius / 5
        paint.textSize = textSize
        paint.textAlign = Paint.Align.CENTER
        for (number in 1..12) {
            val angle = Math.PI * (number - 3) / 6
            val x = (cx + cos(angle) * (dialRadius - textSize)).toFloat()
            val y = (cy + sin(angle) * (dialRadius - textSize)).toFloat()

            val textHeight = paint.descent() - paint.ascent()
            val textOffset = (textHeight / 2) - paint.descent()
            canvas.drawText(number.toString(), x, y + textOffset, paint)
        }
    }

    companion object {
        private fun getCurrentTime(): Triple<Int, Int, Int> {
            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR)
            val minute = cal.get(Calendar.MINUTE)
            val second = cal.get(Calendar.SECOND)
            return Triple(hour, minute, second)
        }
    }
}

