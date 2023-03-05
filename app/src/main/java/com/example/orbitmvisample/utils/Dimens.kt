package com.example.orbitmvisample.utils

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

    private val displayMetrics: DisplayMetrics
        get() = AppContext.resources.displayMetrics
}