package com.learning.companionshimejis.overlay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class FloatingPetView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
        View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private var spriteSheet: Bitmap? = null
    private val srcRect = Rect()
    private val dstRect = Rect()

    fun updateFrame(bitmap: Bitmap, frameX: Int, frameY: Int, frameWidth: Int, frameHeight: Int) {
        this.spriteSheet = bitmap

        // Source on Sprite Sheet
        srcRect.set(frameX, frameY, frameX + frameWidth, frameY + frameHeight)

        // Trigger redraw
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
    }
}
