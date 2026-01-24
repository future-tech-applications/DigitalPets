package com.learning.companionshimejis.system

import android.os.Build
import android.view.WindowManager

/** ## System Part (Bounds Calculation) ## */
fun getUsableBounds1(windowManager: WindowManager): android.graphics.Rect {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val metrics = windowManager.currentWindowMetrics
        val insets =
            metrics.windowInsets.getInsetsIgnoringVisibility(
                android.view.WindowInsets.Type.systemBars()
            )

        android.graphics.Rect(
            insets.left,
            insets.top,
            metrics.bounds.width() - insets.right,
            metrics.bounds.height() - insets.bottom
        )
    } else {
        val metrics = android.util.DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        android.graphics.Rect(0, 0, metrics.widthPixels, metrics.heightPixels)
    }
}