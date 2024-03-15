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

    private var shape: Shape = Shape.ROUND
    private var hasNumbers: Boolean = false
    private var hasSeconds: Boolean = false
    private var dialColor: Int = Color.WHITE
    private var mainColor: Int = Color.BLACK
    private var secondHandColor: Int = Color.RED

    private var hour: Int = 0
    private var minute: Int = 0
    private var second: Int = 0

    private val paint = Paint()

    init {
        context.obtainStyledAttributes(
            attrs,
            R.styleable.CustomWatch,
            defStyleAttr,
            defStyleRes
        ).apply {
            try {
                shape = when (getInt(R.styleable.CustomWatch_shape, KEY_ROUND_SHAPE)) {
                    KEY_ROUND_SHAPE -> Shape.ROUND
                    KEY_SQUARE_SHAPE -> Shape.SQUARE
                    else -> Shape.ROUND
                }
                hasNumbers = getBoolean(R.styleable.CustomWatch_hasNumbers, false)
                hasSeconds = getBoolean(R.styleable.CustomWatch_hasSeconds, false)
                dialColor = getColor(R.styleable.CustomWatch_dialColor, Color.WHITE)
                mainColor = getColor(R.styleable.CustomWatch_mainColor, Color.BLACK)
                secondHandColor = getColor(R.styleable.CustomWatch_secondHandColor, Color.RED)
            } finally {
                recycle()
            }
        }
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

    private val rectF = RectF()
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

        drawBackground(cx, cy, canvas)

        val dialRadius = (side / 2 * 0.9f)
        val centerRadius = dialRadius / 20
        drawDial(dialRadius, centerRadius, cx, cy, canvas)

        if (hasNumbers) {
            drawNumbers(dialRadius, cx, cy, canvas)
        }

        drawHourHand(dialRadius, centerRadius, cx, cy, canvas)
        drawMinuteHand(dialRadius, centerRadius, cx, cy, canvas)

        if (hasSeconds) {
            drawSecondHand(dialRadius, centerRadius, cx, cy, canvas)
            postInvalidateOnAnimation()
        } else {
            postDelayed({ invalidate() }, (60 - second) * 1000L)
        }
    }

    private fun drawDial(
        dialRadius: Float,
        centerRadius: Float,
        cx: Float,
        cy: Float,
        canvas: Canvas
    ) {
        paint.reset()
        paint.color = dialColor
        canvas.drawCircle(cx, cy, dialRadius, paint)

        paint.color = mainColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dialRadius / 30
        canvas.drawCircle(cx, cy, centerRadius, paint)
    }

    private fun drawBackground(
        cx: Float,
        cy: Float,
        canvas: Canvas
    ) {
        paint.reset()
        paint.color = mainColor
        when (shape) {
            Shape.ROUND -> {
                canvas.drawCircle(
                    cx,
                    cy,
                    side / 2,
                    paint
                )
            }

            Shape.SQUARE -> {
                rectF.set(0f, 0f, side, side)
                val roundedCorner = side / 8
                canvas.drawRoundRect(
                    rectF,
                    roundedCorner,
                    roundedCorner,
                    paint
                )
            }
        }
    }

    private fun drawSecondHand(
        dialRadius: Float,
        centerRadius: Float,
        cx: Float,
        cy: Float,
        canvas: Canvas
    ) {
        paint.reset()
        canvas.save()

        val secondRotation = getSecondRotation()
        canvas.rotate(secondRotation, cx, cy)

        paint.color = secondHandColor
        rectF.set(
            cx + centerRadius / 4,
            cy + 2 * centerRadius,
            cx - centerRadius / 4,
            cy - dialRadius * 0.9f
        )
        val roundedCorner = side / 32
        canvas.drawRoundRect(
            rectF,
            roundedCorner,
            roundedCorner,
            paint
        )

        canvas.restore()
    }

    private fun getSecondRotation(): Float = second * 6f

    private fun drawMinuteHand(
        dialRadius: Float,
        centerRadius: Float,
        cx: Float,
        cy: Float,
        canvas: Canvas
    ) {
        paint.reset()
        canvas.save()

        val minuteRotation = getMinuteRotation()
        canvas.rotate(minuteRotation, cx, cy)

        paint.color = mainColor
        rectF.set(
            cx + centerRadius * 0.5f,
            cy - centerRadius,
            cx - centerRadius * 0.5f,
            cy - dialRadius * 0.7f
        )
        val roundedCorner = side / 32
        canvas.drawRoundRect(
            rectF,
            roundedCorner,
            roundedCorner,
            paint
        )

        canvas.restore()
    }

    private fun getMinuteRotation(): Float = minute * 6f + second / 10f

    private fun drawHourHand(
        dialRadius: Float,
        centerRadius: Float,
        cx: Float,
        cy: Float,
        canvas: Canvas
    ) {
        paint.reset()
        canvas.save()

        val hourRotation = getHourRotation()
        canvas.rotate(hourRotation, cx, cy)

        paint.color = mainColor
        rectF.set(
            cx + centerRadius * 0.7f,
            cy - centerRadius,
            cx - centerRadius * 0.7f,
            cy - dialRadius * 0.5f
        )
        val roundedCorner = side / 32
        canvas.drawRoundRect(
            rectF,
            roundedCorner,
            roundedCorner,
            paint
        )

        canvas.restore()
    }

    private fun getHourRotation(): Float = (hour % 12) * 30f + minute / 2f

    private fun drawNumbers(
        dialRadius: Float,
        cx: Float,
        cy: Float,
        canvas: Canvas
    ) {
        paint.reset()
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

        enum class Shape {
            ROUND, SQUARE
        }

        private const val KEY_ROUND_SHAPE = 0
        private const val KEY_SQUARE_SHAPE = 1
    }
}