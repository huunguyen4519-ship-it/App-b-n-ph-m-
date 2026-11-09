package com.huunguyen.keyboard

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import kotlin.math.hypot

/**
 * Lớp phủ ánh sáng xanh: lan toả từ giữa ra, cùng hai dải ở mép trên và dưới chạy ngược chiều.
 * Không nhận chạm để phím bên dưới vẫn bấm được.
 */
class GlowView(context: Context) : View(context) {

    private val density = resources.displayMetrics.density
    private val blue = Color.parseColor("#3FA9FF")
    private val tailSegments = 8

    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = blue
        strokeCap = Paint.Cap.ROUND
    }
    private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = blue
        strokeCap = Paint.Cap.ROUND
        maskFilter = BlurMaskFilter(12 * density, BlurMaskFilter.Blur.NORMAL)
    }

    private var radiateRadius = -1f
    private var radiateAlpha = 1f

    // Tiến trình hai dải: 0..2 (mỗi dải chạy 2 lượt), -1 là không chạy
    private var topProgress = -1f
    private var bottomProgress = -1f

    private var radiateAnim: ValueAnimator? = null
    private var stripAnim: ValueAnimator? = null

    init {
        isClickable = false
        isFocusable = false
    }

    override fun onTouchEvent(event: android.view.MotionEvent?): Boolean = false

    fun play() {
        post {
            cancel()
            startRadiate()
            startStrips()
        }
    }

    fun cancel() {
        radiateAnim?.cancel()
        stripAnim?.cancel()
        radiateAnim = null
        stripAnim = null
        radiateRadius = -1f
        topProgress = -1f
        bottomProgress = -1f
        invalidate()
    }

    private fun startRadiate() {
        val maxR = hypot(width / 2f, height / 2f) + 12 * density
        radiateAnim = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1400
            interpolator = DecelerateInterpolator(2f)
            addUpdateListener {
                val v = it.animatedValue as Float
                radiateRadius = v * maxR
                radiateAlpha = 1f - v * 0.4f
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    radiateRadius = -1f
                    invalidate()
                }
            })
            start()
        }
    }

    private fun startStrips() {
        stripAnim = ValueAnimator.ofFloat(0f, 2f).apply {
            duration = 1000
            interpolator = LinearInterpolator()
            addUpdateListener {
                val t = it.animatedValue as Float
                topProgress = t
                bottomProgress = t
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    topProgress = -1f
                    bottomProgress = -1f
                    invalidate()
                }
            })
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawRadiate(canvas)
        drawStrips(canvas)
    }

    private fun drawRadiate(canvas: Canvas) {
        if (radiateRadius <= 0f) return
        val cx = width / 2f
        val cy = height / 2f
        val step = 10 * density
        for (i in 0 until tailSegments) {
            val r = radiateRadius - i * step
            if (r <= 0f) continue
            val fade = (1f - i.toFloat() / tailSegments) * radiateAlpha
            setStroke(haloPaint, 18 * density, fade * 0.35f)
            canvas.drawCircle(cx, cy, r, haloPaint)
            setStroke(corePaint, 3 * density, fade)
            canvas.drawCircle(cx, cy, r, corePaint)
        }
    }

    private fun drawStrips(canvas: Canvas) {
        val pad = 4 * density
        if (topProgress >= 0f) {
            drawStrip(canvas, headFraction(topProgress), pad, leftToRight = true)
        }
        if (bottomProgress >= 0f) {
            drawStrip(canvas, 1f - headFraction(bottomProgress), height - pad, leftToRight = false)
        }
    }

    // Tiến trình 0..2 chia thành hai lượt, mỗi lượt trả về 0..1
    private fun headFraction(t: Float): Float = t - kotlin.math.floor(t)

    private fun drawStrip(canvas: Canvas, head: Float, y: Float, leftToRight: Boolean) {
        val w = width.toFloat()
        val tailLen = 0.25f * w
        val headX = head * w
        val dir = if (leftToRight) -1f else 1f
        val segs = 8
        for (i in 0 until segs) {
            val x2 = headX + dir * i * (tailLen / segs)
            val x1 = headX + dir * (i + 1) * (tailLen / segs)
            val fade = 1f - i.toFloat() / segs
            setStroke(haloPaint, 12 * density, fade * 0.4f)
            canvas.drawLine(minOf(x1, x2), y, maxOf(x1, x2), y, haloPaint)
            setStroke(corePaint, 2.5f * density, fade)
            canvas.drawLine(minOf(x1, x2), y, maxOf(x1, x2), y, corePaint)
        }
    }

    private fun setStroke(paint: Paint, width: Float, alpha: Float) {
        paint.strokeWidth = width
        paint.alpha = (alpha.coerceIn(0f, 1f) * 255).toInt()
    }
}
