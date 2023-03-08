package com.example.orbitmvisample.ui.lifecycle

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class ScreenTouchInterceptionObserver(
    private val context: Context,
    private val onTouchListener: View.OnTouchListener
) : DefaultLifecycleObserver {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    private var activityView: View? = null

    override fun onCreate(owner: LifecycleOwner) {
        windowManager ?: return

        val layoutParams = WindowManager.LayoutParams(
            0, 0, 0, 0,
            WindowManager.LayoutParams.TYPE_APPLICATION,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )

        @SuppressLint("ClickableViewAccessibility")
        activityView = View(context).apply {
            setOnTouchListener(onTouchListener)
        }

        windowManager.addView(activityView, layoutParams)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        windowManager?.removeViewImmediate(activityView)
    }
}