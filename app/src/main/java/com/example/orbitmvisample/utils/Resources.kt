package com.example.orbitmvisample.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.*
import androidx.lifecycle.LifecycleOwner
import com.example.orbitmvisample.AppContext

@SuppressLint("StaticFieldLeak")
@Suppress("unused")
object Resources {
    /**
     * @JvmStatic annotation is needed for usage the methods in DataBinding's layouts
     */

    val context = AppContext

    val c = context

    val r: android.content.res.Resources
        get() = resources

    val resources: android.content.res.Resources
        get() = context.resources

    val isNetworkAvailable: Boolean
        get() = NetworkAccessibilityObserver.isNetworkAvailable

    @JvmStatic
    fun getString(@StringRes stringRes: Int?): String? =
        if (stringRes != null) context.getString(stringRes) else null

    @JvmStatic
    fun getString(@StringRes stringRes: Int, vararg formatArgs: Any?) =
        context.resources.getString(stringRes, formatArgs)

    @JvmStatic
    fun getText(@StringRes textRes: Int) = context.getText(textRes)

    @JvmStatic
    @JvmOverloads
    fun getColorR(@ColorRes colorRes: Int, defColor: Int = 0) =
        context.getColorR(colorRes, defColor)

    @JvmStatic
    @JvmOverloads
    fun getColorA(context: Context?, @AttrRes attrRes: Int, defColor: Int = 0) =
        context.getColorA(attrRes, defColor)

    @JvmStatic
    @JvmOverloads
    fun getDrawableR(
        @DrawableRes drawableRes: Int, mutate: Boolean = false
    ) = getDrawableR(context, drawableRes, mutate)

    @JvmStatic
    @JvmOverloads
    fun getDrawableR(
        context: Context?, @DrawableRes drawableRes: Int, mutate: Boolean = false
    ) = context.getDrawableR(drawableRes, mutate)

    @JvmStatic
    fun getDrawableA(context: Context?, @AttrRes attr: Int) =
        context.getDrawableR(getResourceId(attr), false)

    @JvmStatic
    fun getDrawableTintedR(
        context: Context?, @DrawableRes drawableRes: Int, @ColorInt tint: Int?
    ) = context.getDrawableTintedR(drawableRes, tint)

    @JvmStatic
    fun getDrawableTintedA(
        context: Context?, @DrawableRes drawableRes: Int, @AttrRes tintAttr: Int?
    ) = context.getDrawableTintedA(drawableRes, tintAttr)

    @JvmStatic
    fun getResourceId(@AttrRes attrRes: Int?) = context.getResourceId(attrRes)

    @JvmStatic
    fun getDimension(@DimenRes dimenRes: Int) = context.getDimension(dimenRes)

    @JvmStatic
    fun getDimensionI(@DimenRes dimenRes: Int) = context.getDimensionI(dimenRes)

    @JvmStatic
    fun getTypeface(@FontRes fontRes: Int) = context.getTypeface(fontRes)

    @JvmStatic
    fun getInteger(@IntegerRes intRes: Int) = context.getInteger(intRes)

    @JvmStatic
    fun getFloat(@DimenRes dimensRes: Int) = context.getFloat(dimensRes)

    @JvmStatic
    fun getBoolean(@BoolRes boolRes: Int) = context.getBoolean(boolRes)

    @JvmStatic
    fun getStringArray(@ArrayRes res: Int) = context.getStringArray(res)

    fun findFragmentManager(context: Context?) =
        context.findFragmentManager()

    fun findLifecycleOwner(owner: Any?): LifecycleOwner? {
        return when (owner) {
            is LifecycleOwner -> owner
            is Context -> {
                var context = owner
                while (context != null) {
                    if (context is LifecycleOwner) return context
                    context = (context as? ContextWrapper)?.baseContext
                }
                null
            }
            else -> null
        }
    }
}