package com.example.orbitmvisample.utils

import android.annotation.SuppressLint
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.DisplayMetrics
import androidx.annotation.DimenRes
import com.example.orbitmvisample.AppContext


@Suppress("unused")
object Dimens {
    /**
     * @JvmStatic annotation is needed for usage the methods in DataBinding's layouts
     */

    @JvmStatic
    fun dpToPx(dp: Float): Float = displayMetrics.density * dp

    @JvmStatic
    fun dpToPx(dp: Int): Float = dpToPx(dp.toFloat())

    @JvmStatic
    fun dpToPxI(dp: Int): Int = dpToPx(dp).toInt()

    @JvmStatic
    fun pxToDp(px: Float): Float = px / displayMetrics.density

    @JvmStatic
    fun pxToDp(dp: Int): Float = pxToDp(dp.toFloat())

    @JvmStatic
    fun pxToDpI(dp: Int): Int = pxToDp(dp).toInt()

    @JvmStatic
    fun getPx(@DimenRes dimensRes: Int): Float = AppContext.getDimension(dimensRes)

    @JvmStatic
    fun getDp(@DimenRes dimensRes: Int): Float = pxToDp(getPx(dimensRes))

    val statusBarHeight by lazy {
        @SuppressLint("InternalInsetResource", "DiscouragedApi")
        val id = Resources.r.getIdentifier("status_bar_height", "dimen", "android")
        if (id > 0) Resources.r.getDimensionPixelSize(id)
        else if (VERSION.SDK_INT >= VERSION_CODES.M) dpToPxI(24) else dpToPxI(25)
    }

    val navigationBarHeight: Int
        get() {
            val name = if (Resources.r.configuration.orientation == ORIENTATION_PORTRAIT)
                "navigation_bar_height" else "navigation_bar_height_landscape"

            @SuppressLint("InternalInsetResource", "DiscouragedApi")
            val id = Resources.r.getIdentifier(name, "dimen", "android")
            return if (id > 0) Resources.r.getDimensionPixelSize(id) else 0
        }

    private val displayMetrics: DisplayMetrics
        get() = AppContext.resources.displayMetrics
}