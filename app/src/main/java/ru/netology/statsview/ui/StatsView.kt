package ru.netology.statsview.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import ru.netology.statsview.R
import ru.netology.statsview.util.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var oval = RectF(0F, 0F, 0F, 0F)
    private var lineWidth = AndroidUtils.dp(context, 5F).toFloat()
    private var fontSize = AndroidUtils.dp(context, 40F).toFloat()
    private var colors = emptyList<Int>()
    private var typeAnimation = 0
    private var progress = 0F
    private var valueAnimator: ValueAnimator? = null

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
            val resId = getResourceId(R.styleable.StatsView_colors, 0)
            colors = resources.getIntArray(resId).toList()
            typeAnimation = getInt(R.styleable.StatsView_typeAnimation, 0)
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            update()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius, center.y - radius,
            center.x + radius, center.y + radius,
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }

        when(typeAnimation) {
            0 -> {
                var startFrom = -90F
                for ((index, datum) in data.withIndex()) {
                    var angle = 360F * (datum / (data.max() * 4))
                    paint.color = colors.getOrNull(index) ?: randomColor()
                    canvas.drawArc(oval, startFrom + (progress * 360F), angle * progress, false, paint)
                    startFrom += angle

                    if (startFrom == 270F) {
                        paint.color = colors.getOrNull(0) ?: randomColor()
                        angle = 70F
                        canvas.drawArc(oval, startFrom + (progress * 360F), angle * progress, false, paint)
                    }
                }
            }
            1 -> {
                var startFrom = -90F
                for ((index, datum) in data.withIndex()) {
                    var angle = 360F * (datum / (data.max() * 4))
                    paint.color = colors.getOrNull(index) ?: randomColor()
                    canvas.drawArc(oval, startFrom, progress * 360F - (startFrom + 90F), false, paint)
                    startFrom += angle

                    if (startFrom == 270F) {
                        paint.color = colors.getOrNull(0) ?: randomColor()
                        angle = 70F
                        canvas.drawArc(oval, startFrom, angle, false, paint)
                    }

                    if ((startFrom + 90F) > progress * 360F) return
                }
            }
            2 -> {
                var startFrom = -90F
                for ((index, datum) in data.withIndex()) {
                    val angle = 360F * (datum / (data.max() * 4))
                    paint.color = colors.getOrNull(index) ?: randomColor()
                    canvas.drawArc(oval, startFrom, angle / 2 * progress, false, paint)
                    canvas.drawArc(oval, startFrom, -angle / 2 * progress, false, paint)
                    startFrom += angle
                }
            }
        }

        if (data.max() != data.min()) {
            var numberOne = 0F
            var numberTwo = 0F
            var numberThree = 0F
            var numberFour = 0F

            for ((index, datum) in data.withIndex()) {
                when (index) {
                    0 -> numberOne = datum / (data.max() * 4)
                    1 -> numberTwo = datum / (data.max() * 4)
                    2 -> numberThree = datum / (data.max() * 4)
                    3 -> numberFour = datum / (data.max() * 4)
                }
            }

            val percentages = numberOne + numberTwo + numberThree + numberFour
            canvas.drawText(
                "%.2f%%".format(percentages * 100),
                center.x,
                center.y + textPaint.textSize / 4,
                textPaint,
            )
        } else {
            val quantityNumber = data.count()

            canvas.drawText(
                "%.2f%%".format(((data.first() / (data.first() * 4) * quantityNumber)) * 100),
                center.x,
                center.y + textPaint.textSize / 4,
                textPaint,
            )
        }
    }

    private fun update() {
        valueAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }
        progress = 0F

        valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                invalidate()
            }
            duration = 5000
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }
    }

    private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}