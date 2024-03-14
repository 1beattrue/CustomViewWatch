package edu.mirea.onebeattrue.customviewwatch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin
import kotlin.properties.Delegates

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

        typedArray.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        postInvalidateOnAnimation()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val size = when {
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY -> minOf(
                widthSize,
                heightSize
            )

            widthMode == MeasureSpec.EXACTLY -> widthSize
            heightMode == MeasureSpec.EXACTLY -> heightSize

            else -> minOf(widthSize, heightSize)
        }

        setMeasuredDimension(size, size)
    }

    private val backgroundRect = RectF()
    private var side by Delegates.notNull<Float>()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val currentTime = getCurrentTime()
        hour = currentTime.first
        minute = currentTime.second
        second = currentTime.third

        side = minOf(width, height).toFloat()
        val cx = side / 2
        val cy = side / 2

        paint.color = mainColor
        backgroundRect.set(0f, 0f, side, side)
        val roundedCorner = side / 8
        canvas.drawRoundRect(
            backgroundRect,
            roundedCorner,
            roundedCorner,
            paint
        )

        paint.color = backgroundColor
        val dialRadius = (side / 2 * 0.9f)
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
            postInvalidateOnAnimation()
        } else {
            postDelayed({ invalidate() }, (60 - second) * 1000L)
        }
    }

    private fun drawSecondHand(
        dialRadius: Float,
        cx: Float,
        cy: Float,
        canvas: Canvas
    ) {
        canvas.save()

        val secondRotation = getSecondRotation()
        canvas.rotate(secondRotation, cx, cy)

        paint.color = mainColor
        paint.strokeWidth = side / 60
        canvas.drawLine(cx, cy, cx, cy - dialRadius * 0.8f, paint)

        canvas.restore()
    }

    private fun getSecondRotation(): Float = second * 6f

    private fun drawMinuteHand(
        dialRadius: Float,
        cx: Float,
        cy: Float,
        canvas: Canvas
    ) {
        canvas.save()

        val minuteRotation = getMinuteRotation()
        canvas.rotate(minuteRotation, cx, cy)

        paint.color = mainColor
        paint.strokeWidth = side / 30f
        canvas.drawLine(cx, cy, cx, cy - dialRadius * 0.6f, paint)

        canvas.restore()
    }

    private fun getMinuteRotation(): Float = minute * 6f + second / 10f

    private fun drawHourHand(
        dialRadius: Float,
        cx: Float,
        cy: Float,
        canvas: Canvas
    ) {
        canvas.save()

        val hourRotation = getHourRotation()
        canvas.rotate(hourRotation, cx, cy)

        paint.color = mainColor
        paint.strokeWidth = side / 20f
        canvas.drawLine(cx, cy, cx, cy - dialRadius * 0.4f, paint)

        canvas.restore()
    }

    private fun getHourRotation(): Float = (hour % 12) * 30f + minute / 2f

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
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR)
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)
            return Triple(hour, minute, second)
        }
    }
}