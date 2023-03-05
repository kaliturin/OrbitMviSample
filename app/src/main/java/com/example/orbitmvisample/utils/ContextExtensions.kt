package com.example.orbitmvisample.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentManager
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.google.android.material.color.MaterialColors

fun Context?.getColorR(@ColorRes colorRes: Int, defColor: Int = 0): Int {
    return if (this == null || colorRes == 0) defColor
    else ContextCompat.getColor(this, colorRes)
}

fun Context?.getColorA(@AttrRes attrRes: Int, defColor: Int = 0): Int {
    return if (this == null) defColor
    else MaterialColors.getColor(this, attrRes, defColor)
}

fun Context?.getDrawableR(@DrawableRes drawableRes: Int, mutate: Boolean = false): Drawable? {
    return if (this == null) null
    else try {
        AppCompatResources.getDrawable(this, drawableRes)
    } catch (_: Resources.NotFoundException) {
        // couldn't get the drawable - try to get is as a vector (tested on KITKAT)
        try {
            VectorDrawableCompat.create(resources, drawableRes, theme)
        } catch (_: Exception) {
            null
        }
    }?.let { if (mutate) it.mutate() else it }
}

fun Context?.getDrawableA(@AttrRes attr: Int): Drawable? =
    getDrawableR(getResourceId(attr), false)

fun Context?.getDrawableTintedR(@DrawableRes drawableRes: Int, @ColorInt tint: Int?): Drawable? {
    return getDrawableR(drawableRes, true)?.apply {
        tint?.let { DrawableCompat.setTint(this, it) }
    }
}

fun Context?.getDrawableTintedA(@DrawableRes drawableRes: Int, @AttrRes tintAttr: Int?): Drawable? {
    return if (tintAttr == null) getDrawableR(drawableRes)
    else getDrawableTintedR(drawableRes, getColorA(tintAttr))
}

fun Context?.getResourceId(@AttrRes attrRes: Int?): Int =
    if (this == null) 0 else getResourceId(this.theme, attrRes)

private fun getResourceId(theme: Resources.Theme, @AttrRes attrRes: Int?): Int {
    if (attrRes == null) return 0
    return TypedValue().run {
        theme.resolveAttribute(attrRes, this, true)
        resourceId
    }
}

fun Context?.getDimension(@DimenRes dimenRes: Int): Float =
    if (this == null) 0f else resources.getDimension(dimenRes)

fun Context?.getDimensionI(@DimenRes dimenRes: Int): Int =
    if (this == null) 0 else getDimension(dimenRes).toInt()

fun Context?.getTypeface(@FontRes fontRes: Int): Typeface? =
    if (this == null) null else ResourcesCompat.getFont(this, fontRes)

fun Context?.getInteger(@IntegerRes intRes: Int): Int =
    if (this == null) 0 else resources.getInteger(intRes)

fun Context?.getFloat(@DimenRes dimensRes: Int): Float {
    return if (this == null) 0f
    else TypedValue().run {
        resources.getValue(dimensRes, this, true)
        float
    }
}

fun Context?.getBoolean(@BoolRes boolRes: Int): Boolean =
    if (this == null) false else resources.getBoolean(boolRes)

fun Context?.getStringArray(@ArrayRes res: Int): Array<String> =
    if (this == null) emptyArray() else resources.getStringArray(res)

fun Context?.findFragmentManager(): FragmentManager? {
    return when (this) {
        is AppCompatActivity -> supportFragmentManager
        is android.view.ContextThemeWrapper -> baseContext.findFragmentManager()
        is androidx.appcompat.view.ContextThemeWrapper -> baseContext.findFragmentManager()
        else -> null
    }
}

