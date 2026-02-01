package com.learning.companionshimejis.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager

/**
 * Handles all WindowManager operations for the overlay service. Responsible for adding, updating,
 * and removing pet views and menus.
 */
class PetWindowManager(val context: Context) {

    private val windowManager: WindowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    /**
     * Adds a view to the window manager.
     * @param view The view to add.
     * @param params The layout parameters for the view.
     */
    fun addView(view: View, params: WindowManager.LayoutParams) {
        try {
            windowManager.addView(view, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Updates the layout parameters of a view.
     * @param view The view to update.
     * @param params The new layout parameters.
     */
    fun updateViewLayout(view: View, params: WindowManager.LayoutParams) {
        try {
            windowManager.updateViewLayout(view, params)
        } catch (e: Exception) {
            // View might be already removed or invalid
        }
    }

    /**
     * Removes a view from the window manager.
     * @param view The view to remove.
     */
    fun removeView(view: View) {
        try {
            windowManager.removeView(view)
        } catch (e: Exception) {
            // View might be already removed or invalid
        }
    }

    /**
     * Creates a layout parameters object for a pet view.
     * @param size The size of the pet view.
     * @return The created layout parameters object.
     */
    fun createPetLayoutParams(size: Int): WindowManager.LayoutParams {
        val params =
                WindowManager.LayoutParams(
                        size,
                        size,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        else WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        PixelFormat.TRANSLUCENT
                )
        params.gravity = Gravity.TOP or Gravity.START
        return params
    }

    /**
     * Creates a layout parameters object for a menu view.
     * @param x The x-coordinate of the menu.
     * @param y The y-coordinate of the menu.
     * @return The created layout parameters object.
     */
    fun createMenuLayoutParams(x: Int, y: Int): WindowManager.LayoutParams {
        val params =
                WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        else WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        PixelFormat.TRANSLUCENT
                )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = x
        params.y = y
        return params
    }

    /**
     * Gets the usable bounds of the screen. It accounts for the system bars and display cutouts.
     * @return The usable bounds of the screen.
     */
    fun getUsableBounds(): android.graphics.Rect {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val insets =
                    metrics.windowInsets.getInsets(
                            WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
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
}
