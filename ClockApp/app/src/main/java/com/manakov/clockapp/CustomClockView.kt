package com.manakov.clockapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList
import kotlin.math.cos
import kotlin.math.sin

const val STROKE_WIDTH = 23f

class CustomClockView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attr, defStyle) {

    companion object {
        const val logTag = "CustomClockView"
    }

    private var clockPaint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
        style = Paint.Style.STROKE
        this.strokeWidth = STROKE_WIDTH
    }

    private var radius: Float = 0f
    private var center = Point()

    private var hourTicks = ArrayList<DoublePoint>(12)

    private var hourArrow = Arrow()
    private var minuteArrow = Arrow()
    private var secondArrow = Arrow()

    private var seconds = AtomicInteger(0)
    private var minutes = AtomicInteger(0)
    private var hours = AtomicInteger(0)

    private var rollThread =
        Thread {
            while (true) {
                val date = Calendar.getInstance()
                seconds.set(date.time.seconds)
                minutes.set(date.time.minutes)
                hours.set(date.time.hours)
                invalidate()
                TimeUnit.MILLISECONDS.sleep(16)
            }
        }

    init {
        for (i in 0..11) {
            hourTicks.add(DoublePoint(Point(), Point()))
        }

        context.theme.obtainStyledAttributes(
            attr,
            R.styleable.CustomClockView,
            0, 0
        ).apply {
            try {
                secondArrow.apply {
                    paint.color = getColor(R.styleable.CustomClockView_secondArrowColor, Color.BLACK)
                    length = getFloat(R.styleable.CustomClockView_secondArrowLength, 1f)
                }
                minuteArrow.apply {
                    paint.color = getColor(R.styleable.CustomClockView_minuteArrowColor, Color.BLACK)
                    length = getFloat(R.styleable.CustomClockView_minuteArrowLength, 1f)
                }
                hourArrow.apply {
                    paint.color = getColor(R.styleable.CustomClockView_hourArrowColor, Color.BLACK)
                    length = getFloat(R.styleable.CustomClockView_hourArrowLength, 1f)
                }
            } finally {
                recycle()
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        when {
            height >= width -> radius = width / 2f
            width > height -> radius = height / 2f
        }
        center.x = width / 2f
        center.y = height / 2f

        radius -= clockPaint.strokeWidth

        var angleOffset = Math.PI * 0
        for (dPoint in hourTicks) {
            dPoint.a.x = ((radius) * cos(angleOffset)).toFloat() + center.x
            dPoint.a.y = ((radius) * sin(angleOffset)).toFloat() + center.y

            dPoint.b.x = ((radius * 5 / 6) * cos(angleOffset)).toFloat() + center.x
            dPoint.b.y = ((radius * 5 / 6) * sin(angleOffset)).toFloat() + center.y
            angleOffset += Math.PI / 6
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawCircle(center.x, center.y, radius, clockPaint)
        for (dPoint in hourTicks) {
            canvas.drawLine(
                dPoint.a.x, dPoint.a.y, dPoint.b.x, dPoint.b.y, clockPaint
            )
        }

        secondArrow.frontPoint.x =
            ((radius * secondArrow.length) * cos(
                (15.0 - seconds.get().toDouble()) * Math.PI / 30.0
            )).toFloat() + center.x
        secondArrow.frontPoint.y =
            ((radius * secondArrow.length) * sin(
                (15.0 - seconds.get().toDouble()) * Math.PI / 30.0
            )).toFloat() * (-1) + center.y

        secondArrow.backPoint.x =
            ((radius * secondArrow.length / 2) * cos(
                (45.0 - seconds.get().toDouble()) * Math.PI / 30.0
            )).toFloat() + center.x
        secondArrow.backPoint.y =
            ((radius * secondArrow.length / 2) * sin(
                (45.0 - seconds.get().toDouble()) * Math.PI / 30.0
            )).toFloat() * (-1) + center.y

        canvas.drawLine(
            secondArrow.backPoint.x,
            secondArrow.backPoint.y,
            secondArrow.frontPoint.x,
            secondArrow.frontPoint.y,
            secondArrow.paint
        )

        minuteArrow.frontPoint.x =
            ((radius * minuteArrow.length) * cos(
                (15.0 - (minutes.get().toDouble() + (seconds.get() / 60.0))) * Math.PI / 30.0
            )).toFloat() + center.x
        minuteArrow.frontPoint.y =
            ((radius * minuteArrow.length) * sin(
                (15.0 - (minutes.get().toDouble() + (seconds.get() / 60.0))) * Math.PI / 30.0
            )).toFloat() * (-1) + center.y

        minuteArrow.backPoint.x =
            ((radius * minuteArrow.length / 2) * cos(
                (45.0 - (minutes.get().toDouble() + (seconds.get() / 60.0))) * Math.PI / 30.0
            )).toFloat() + center.x
        minuteArrow.backPoint.y =
            ((radius * minuteArrow.length / 2) * sin(
                (45.0 - (minutes.get().toDouble() + (seconds.get() / 60.0))) * Math.PI / 30.0
            )).toFloat() * (-1) + center.y

        canvas.drawLine(
            minuteArrow.backPoint.x,
            minuteArrow.backPoint.y,
            minuteArrow.frontPoint.x,
            minuteArrow.frontPoint.y,
            minuteArrow.paint
        )

        hourArrow.frontPoint.x =
            ((radius * hourArrow.length) * cos(
                (15.0 - (hours.get().toDouble() + minutes.get() / 60.0) * 5) * Math.PI / 30.0
            )).toFloat() + center.x
        hourArrow.frontPoint.y =
            ((radius * hourArrow.length) * sin(
                (15.0 - (hours.get().toDouble() + minutes.get() / 60.0) * 5) * Math.PI / 30.0
            )).toFloat() * (-1) + center.y

        hourArrow.backPoint.x =
            ((radius * hourArrow.length / 2) * cos(
                (45.0 - (hours.get().toDouble() + minutes.get() / 60.0) * 5) * Math.PI / 30.0
            )).toFloat() + center.x
        hourArrow.backPoint.y =
            ((radius * hourArrow.length / 2) * sin(
                (45.0 - (hours.get().toDouble() + minutes.get() / 60.0) * 5) * Math.PI / 30.0
            )).toFloat() * (-1) + center.y

        canvas.drawLine(
            hourArrow.backPoint.x,
            hourArrow.backPoint.y,
            hourArrow.frontPoint.x,
            hourArrow.frontPoint.y,
            hourArrow.paint
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        rollThread.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        rollThread.join()
    }

    class Point(
        var x: Float = 0f,
        var y: Float = 0f,
    )

    class DoublePoint(
        var a: Point,
        var b: Point
    )

    class Arrow {
        var length = 0.5f
            set(value) {
                field = when {
                    value < 0f -> 0f
                    value > 0.75f -> 0.75f
                    else -> value
                }
            }

        var frontPoint: Point = Point()
        var backPoint: Point = Point()

        var paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            style = Paint.Style.STROKE
            this.strokeWidth = STROKE_WIDTH / 2
        }
    }
}

