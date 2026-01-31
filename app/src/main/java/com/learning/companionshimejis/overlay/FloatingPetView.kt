package com.learning.companionshimejis.overlay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.learning.companionshimejis.data.model.EmoteType

class FloatingPetView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
        View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private var spriteSheet: Bitmap? = null
    private val srcRect = Rect()
    private val dstRect = Rect()
    private var currentEmote: EmoteType = EmoteType.NONE

    fun updateFrame(bitmap: Bitmap, frameX: Int, frameY: Int, frameWidth: Int, frameHeight: Int) {
        this.spriteSheet = bitmap
        srcRect.set(frameX, frameY, frameX + frameWidth, frameY + frameHeight)
        invalidate()
    }

    fun setEmote(emote: EmoteType) {
        this.currentEmote = emote
        invalidate()
    }

    // For legacy/static support (optional)
    fun setPetImage(bitmap: Bitmap) {
        this.spriteSheet = bitmap
        srcRect.set(0, 0, bitmap.width, bitmap.height)
        invalidate()
    }

    fun updateAlpha(alpha: Float) {
        this.alpha = alpha
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        spriteSheet?.let {
            dstRect.set(0, 0, width, height)
            canvas.drawBitmap(it, srcRect, dstRect, paint)
        }

        drawEmote(canvas)
    }

    private fun drawEmote(canvas: Canvas) {
        if (currentEmote == EmoteType.NONE) return

        val bubbleSize = width * 0.4f
        val bubbleX = width * 0.7f
        // Lower the bubble to prevent clipping at the top of the view
        val bubbleY = (bubbleSize / 2f) + (height * 0.05f)

        // Draw Bubble Background
        paint.color = android.graphics.Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawCircle(bubbleX, bubbleY, bubbleSize / 2, paint)

        // Draw Shadow/Border
        paint.color = android.graphics.Color.GRAY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawCircle(bubbleX, bubbleY, bubbleSize / 2, paint)

        // Draw specific symbols based on emote
        paint.color = android.graphics.Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = bubbleSize * 0.6f
        paint.style = Paint.Style.FILL

        val symbol =
                when (currentEmote) {
                    EmoteType.HAPPY -> "❤️"
                    EmoteType.SURPRISED -> "!"
                    EmoteType.THINKING -> "?"
                    EmoteType.SLEEPY -> "zZ"
                    EmoteType.ANGRY -> "#"
                    else -> ""
                }

        // Vertically center text in bubble
        val textY = bubbleY - ((paint.descent() + paint.ascent()) / 2)
        canvas.drawText(symbol, bubbleX, textY, paint)
    }
}
