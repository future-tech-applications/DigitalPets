package com.learning.companionshimejis.overlay

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes

class FloatingPetView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
        FrameLayout(context, attrs) {

    private val imageView: ImageView = ImageView(context)

    init {
        addView(imageView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun setPetImage(@DrawableRes resId: Int) {
        imageView.setImageResource(resId)
    }

    fun updateAlpha(alpha: Float) {
        imageView.alpha = alpha
    }

    // TODO: Expose setSize(sizePx: Int) for customizable sizes

}