package com.example.myapplication

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat

/**
 * 翻牌游戏界面（View）：负责绘制卡牌网格、播放翻牌动画并上报点击事件。
 */
class CardGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnCardClickListener {
        fun onCardClick(index: Int)
    }

    var model: GameModel? = null
    var onCardClickListener: OnCardClickListener? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cardBounds = RectF()

    private var lastStates: List<GameModel.CardState>? = null
    private val animIndices = mutableListOf<Int>()
    private var animProgress = 1f
    private var animFlipUp = false
    private var animator: ValueAnimator? = null
    private var downIndex = -1

    init {
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isFakeBoldText = true
    }

    fun bindModel(model: GameModel) {
        this.model = model
        lastStates = null
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.UNSPECIFIED -> dp(320f)
            else -> MeasureSpec.getSize(widthMeasureSpec)
        }
        val gap = GAP_RATIO * width
        val cardWidth = (width - (COLS + 1) * gap) / COLS
        val height = (ROWS * cardWidth * CARD_RATIO + (ROWS + 1) * gap).toInt()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        val model = model ?: return
        detectStateChanges(model)
        val width = width.toFloat()
        val gap = GAP_RATIO * width
        val cardWidth = (width - (COLS + 1) * gap) / COLS
        val cardHeight = cardWidth * CARD_RATIO

        for (index in model.cards.indices) {
            val col = index % COLS
            val row = index / COLS
            val left = gap + col * (cardWidth + gap)
            val top = gap + row * (cardHeight + gap)
            cardBounds.set(left, top, left + cardWidth, top + cardHeight)
            drawCard(canvas, model.cards[index], index, cardBounds, cardWidth)
        }
    }

    private fun drawCard(
        canvas: Canvas,
        card: GameModel.Card,
        index: Int,
        bounds: RectF,
        cardWidth: Float
    ) {
        val scaleX = if (index in animIndices) {
            if (animFlipUp) animProgress else 1f - animProgress
        } else {
            1f
        }
        canvas.save()
        canvas.scale(scaleX, 1f, bounds.centerX(), bounds.centerY())
        when (card.state) {
            GameModel.CardState.FACE_DOWN -> drawCardBack(canvas, bounds, cardWidth)
            GameModel.CardState.FACE_UP -> drawCardFace(canvas, bounds, cardWidth, card.value)
            GameModel.CardState.MATCHED -> drawMatchedCard(canvas, bounds, cardWidth, card.value)
        }
        canvas.restore()
    }

    private fun drawCardBack(canvas: Canvas, bounds: RectF, cardWidth: Float) {
        val radius = bounds.width() * CORNER_RATIO
        val backTop = ContextCompat.getColor(context, R.color.card_back_top)
        val backBottom = ContextCompat.getColor(context, R.color.card_back_bottom)
        val borderColor = ContextCompat.getColor(context, R.color.card_back_border)

        paint.shader = LinearGradient(
            bounds.left, bounds.top, bounds.right, bounds.bottom,
            backTop, backBottom, Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(bounds, radius, radius, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = cardWidth * 0.03f
        paint.color = borderColor
        canvas.drawRoundRect(bounds, radius, radius, paint)

        val inner = RectF(bounds)
        val inset = bounds.width() * 0.10f
        inner.inset(inset, inset)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = cardWidth * 0.02f
        paint.color = borderColor
        canvas.drawRoundRect(inner, radius, radius, paint)

        canvas.save()
        canvas.rotate(45f, bounds.centerX(), bounds.centerY())
        val diamond = RectF(bounds)
        diamond.inset(bounds.width() * 0.30f, bounds.width() * 0.30f)
        paint.style = Paint.Style.FILL
        paint.color = borderColor
        canvas.drawRoundRect(diamond, radius * 0.6f, radius * 0.6f, paint)
        canvas.restore()
    }

    private fun drawCardFace(
        canvas: Canvas,
        bounds: RectF,
        cardWidth: Float,
        value: Int
    ) {
        val radius = bounds.width() * CORNER_RATIO
        paint.style = Paint.Style.FILL
        paint.color = ContextCompat.getColor(context, R.color.card_face)
        canvas.drawRoundRect(bounds, radius, radius, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = cardWidth * 0.04f
        paint.color = ContextCompat.getColor(context, R.color.card_face_border)
        canvas.drawRoundRect(bounds, radius, radius, paint)

        textPaint.color = ContextCompat.getColor(context, R.color.card_number)
        textPaint.textSize = cardWidth * 0.42f
        drawCenteredText(canvas, value.toString(), bounds)
    }

    private fun drawMatchedCard(
        canvas: Canvas,
        bounds: RectF,
        cardWidth: Float,
        value: Int
    ) {
        val radius = bounds.width() * CORNER_RATIO
        paint.style = Paint.Style.FILL
        paint.color = ContextCompat.getColor(context, R.color.card_matched_bg)
        canvas.drawRoundRect(bounds, radius, radius, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = cardWidth * 0.04f
        paint.color = ContextCompat.getColor(context, R.color.card_matched_border)
        canvas.drawRoundRect(bounds, radius, radius, paint)

        textPaint.color = ContextCompat.getColor(context, R.color.card_matched_number)
        textPaint.textSize = cardWidth * 0.30f
        drawCenteredText(canvas, value.toString(), bounds)
    }

    private fun drawCenteredText(canvas: Canvas, text: String, bounds: RectF) {
        val baseline = bounds.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(text, bounds.centerX(), baseline, textPaint)
    }

    private fun detectStateChanges(model: GameModel) {
        val current = model.cards.map { it.state }
        val previous = lastStates
        lastStates = current
        if (previous == null) return
        val changed = current.indices.filter { current[it] != previous[it] }
        if (changed.isEmpty()) return
        animIndices.clear()
        animIndices.addAll(changed)
        animFlipUp = current[changed[0]] == GameModel.CardState.FACE_UP
        startFlipAnimation()
    }

    private fun startFlipAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = FLIP_DURATION_MS
            interpolator = DecelerateInterpolator()
            addUpdateListener { va ->
                animProgress = va.animatedFraction
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    animProgress = 1f
                    animIndices.clear()
                    invalidate()
                }
            })
        }
        animator?.start()
    }

    private fun hitTest(x: Float, y: Float): Int {
        val width = width.toFloat()
        if (width <= 0f) return -1
        val gap = GAP_RATIO * width
        val cardWidth = (width - (COLS + 1) * gap) / COLS
        val cardHeight = cardWidth * CARD_RATIO
        val col = ((x - gap) / (cardWidth + gap)).toInt()
        val row = ((y - gap) / (cardHeight + gap)).toInt()
        if (col !in 0 until COLS || row !in 0 until ROWS) return -1
        val left = gap + col * (cardWidth + gap)
        val top = gap + row * (cardHeight + gap)
        return if (x >= left && x <= left + cardWidth && y >= top && y <= top + cardHeight) {
            row * COLS + col
        } else {
            -1
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downIndex = hitTest(event.x, event.y)
                return true
            }
            MotionEvent.ACTION_UP -> {
                val index = hitTest(event.x, event.y)
                if (index != -1 && index == downIndex) {
                    performClick()
                    onCardClickListener?.onCardClick(index)
                }
                downIndex = -1
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun dp(value: Float): Int =
        (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val COLS = 4
        private const val ROWS = 2
        private const val GAP_RATIO = 0.06f
        private const val CARD_RATIO = 4f / 3f
        private const val CORNER_RATIO = 0.10f
        private const val FLIP_DURATION_MS = 260L
    }
}
