package com.example.orbitmvisample.utils.spannable

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.TypefaceSpan

class CustomTypefaceSpan(
    family: String?,
    private val typeFace: Typeface,
    private val fontSize: Float
) : TypefaceSpan(family) {

    constructor(type: Typeface, fontSize: Float) : this("", type, fontSize)

    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, typeFace, fontSize)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, typeFace, fontSize)
    }

    private fun applyCustomTypeFace(paint: Paint, typeFace: Typeface, fontSize: Float) {
        val oldStyle = paint.typeface?.style ?: 0
        val fake = oldStyle and typeFace.style.inv()
        if (fake and Typeface.BOLD != 0) paint.isFakeBoldText = true
        if (fake and Typeface.ITALIC != 0) paint.textSkewX = -0.25f
        paint.typeface = typeFace
        paint.textSize = fontSize
    }
}