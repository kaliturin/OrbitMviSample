package com.example.orbitmvisample.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.*
import androidx.lifecycle.LifecycleOwner
import com.example.orbitmvisample.AppContext

@Suppress("unused")
object Resources {
    /**
     * @JvmStatic annotation is needed for usage the methods in DataBinding's layouts
     */

    @JvmStatic
    @JvmOverloads
    fun getColorR(@ColorRes colorRes: Int, defColor: Int = 0) =
        AppContext.getColorR(colorRes, defColor)

    @JvmStatic
    @JvmOverloads
    fun getColorA(context: Context?, @AttrRes attrRes: Int, defColor: Int = 0) =
        context.getColorA(attrRes, defColor)

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
    fun getResourceId(@AttrRes attrRes: Int?) = AppContext.getResourceId(attrRes)

    @JvmStatic
    fun getDimension(@DimenRes dimenRes: Int) = AppContext.getDimension(dimenRes)

    @JvmStatic
    fun getDimensionI(@DimenRes dimenRes: Int) = AppContext.getDimensionI(dimenRes)

    @JvmStatic
    fun getTypeface(@FontRes fontRes: Int) = AppContext.getTypeface(fontRes)

    @JvmStatic
    fun getInteger(@IntegerRes intRes: Int) = AppContext.getInteger(intRes)

    @JvmStatic
    fun getFloat(@DimenRes dimensRes: Int) = AppContext.getFloat(dimensRes)

    @JvmStatic
    fun getBoolean(@BoolRes boolRes: Int) = AppContext.getBoolean(boolRes)

    @JvmStatic
    fun getStringArray(@ArrayRes res: Int) = AppContext.getStringArray(res)

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