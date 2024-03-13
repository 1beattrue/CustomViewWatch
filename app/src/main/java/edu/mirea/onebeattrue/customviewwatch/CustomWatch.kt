package edu.mirea.onebeattrue.customviewwatch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
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

    private val paint = Paint()
    private val path = Path()

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
    }

}