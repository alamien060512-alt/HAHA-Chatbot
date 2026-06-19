package com.imhungry.looiai.robot

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class RobotFaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paintHead = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E1E2A")
        style = Paint.Style.FILL
    }
    private val paintHeadBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6C63FF")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val paintEyeOuter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0D0D14")
        style = Paint.Style.FILL
    }
    private val paintEyeIris = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#A8B4FF")
        style = Paint.Style.FILL
    }
    private val paintEyePupil = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0D0D14")
        style = Paint.Style.FILL
    }
    private val paintEyeShine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val paintEyeBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6C63FF")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val paintMouthBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0D0D14")
        style = Paint.Style.FILL
    }
    private val paintMouthSeg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6C63FF")
        style = Paint.Style.FILL
    }
    private val paintAntenna = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6C63FF")
        style = Paint.Style.FILL
    }
    private val paintAntennaBall = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#A8B4FF")
        style = Paint.Style.FILL
    }
    private val paintCheek = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#A8B4FF")
        alpha = 80
        style = Paint.Style.FILL
    }

    var blinkProgress: Float = 1f
        set(value) { field = value; invalidate() }

    var eyeOffsetX: Float = 0f
        set(value) { field = value; invalidate() }

    var eyeOffsetY: Float = 0f
        set(value) { field = value; invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val s = min(w, h)
        val cx = w / 2f
        val cy = h / 2f

        val scale = s / 200f

        fun sp(x: Float) = cx + (x - 100f) * scale
        fun tp(y: Float) = cy + (y - 100f) * scale
        fun sc(v: Float) = v * scale

        // Head
        val headRect = RectF(sp(30f), tp(30f), sp(170f), tp(170f))
        canvas.drawRoundRect(headRect, sc(20f), sc(20f), paintHead)
        canvas.drawRoundRect(headRect, sc(20f), sc(20f), paintHeadBorder)

        // Ears
        val earLeft = RectF(sp(12f), tp(80f), sp(30f), tp(110f))
        canvas.drawRoundRect(earLeft, sc(4f), sc(4f), paintHead)
        canvas.drawRoundRect(earLeft, sc(4f), sc(4f), paintEyeBorder)
        val earRight = RectF(sp(170f), tp(80f), sp(188f), tp(110f))
        canvas.drawRoundRect(earRight, sc(4f), sc(4f), paintHead)
        canvas.drawRoundRect(earRight, sc(4f), sc(4f), paintEyeBorder)

        // Antenna
        canvas.drawRect(sp(97f), tp(14f), sp(103f), tp(30f), paintAntenna)
        canvas.drawCircle(sp(100f), tp(8f), sc(6f), paintAntennaBall)

        // Eyes
        val eyeR = sc(22f)
        val irisR = sc(12f)
        val pupilR = sc(5f)
        val shineR = sc(2.5f)

        fun drawEye(ex: Float, ey: Float) {
            val ex2 = sp(ex) + eyeOffsetX * sc(4f)
            val ey2 = tp(ey) + eyeOffsetY * sc(4f)
            canvas.save()
            canvas.scale(1f, blinkProgress, ex2, ey2)
            canvas.drawCircle(ex2, ey2, eyeR, paintEyeOuter)
            canvas.drawCircle(ex2, ey2, eyeR, paintEyeBorder)
            canvas.drawCircle(ex2, ey2, irisR, paintEyeIris)
            canvas.drawCircle(ex2, ey2, pupilR, paintEyePupil)
            canvas.drawCircle(ex2 + sc(4f), ey2 - sc(4f), shineR, paintEyeShine)
            canvas.restore()
        }

        drawEye(55f, 85f)
        drawEye(145f, 85f)

        // Mouth panel
        val mouthRect = RectF(sp(60f), tp(118f), sp(140f), tp(158f))
        canvas.drawRoundRect(mouthRect, sc(10f), sc(10f), paintMouthBg)

        // Mouth segments (smile)
        val segW = sc(13f)
        val segH = sc(10f)
        val segY = tp(138f)
        val segPositions = listOf(66f, 80f, 94f, 108f, 122f)
        segPositions.forEachIndexed { i, x ->
            val rect = RectF(sp(x), segY, sp(x) + segW, segY + segH)
            val boost = if (i == 0 || i == 4) sc(3f) else 0f
            val seg = RectF(rect.left, rect.top + boost, rect.right, rect.bottom)
            canvas.drawRoundRect(seg, sc(2f), sc(2f), paintMouthSeg)
        }

        // Cheeks
        canvas.drawCircle(sp(38f), tp(122f), sc(9f), paintCheek)
        canvas.drawCircle(sp(162f), tp(122f), sc(9f), paintCheek)
    }
}
