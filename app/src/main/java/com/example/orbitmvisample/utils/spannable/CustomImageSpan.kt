package com.example.orbitmvisample.utils.spannable

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.style.ImageSpan

/**
 * DynamicDrawableSpan.ALIGN_CENTER supporting ImageSpan that has actually been implemented since SDK 29.
 * With tagging support.
 */
open class CustomImageSpan(drawable: Drawable, verticalAlignment: Int, var tag: String = "") :
    ImageSpan(drawable, verticalAlignment) {

    override fun draw(
        canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int,
        y: Int, bottom: Int, paint: Paint
    ) {
        if (verticalAlignment == ALIGN_CENTER) {
            canvas.save()
            val paintFM = paint.fontMetricsInt
            val fontHeight = paintFM.descent - paintFM.ascent
            val centerY = y + paintFM.descent - fontHeight / 2
            val transY = centerY - (drawable.bounds.bottom - drawable.bounds.top) / 2
            canvas.translate(x, transY.toFloat())
            drawable.draw(canvas)
            canvas.restore()
        } else super.draw(canvas, text, start, end, x, top, y, bottom, paint)
    }

    override fun getSize(
        paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?
    ): Int {
        return if (verticalAlignment == ALIGN_CENTER) {
            val rect = drawable.bounds
            // update the text line height
            fm?.let {
                val paintFM = paint.fontMetricsInt
                val fontHeight = paintFM.descent - paintFM.ascent
                val drHeight = rect.bottom - rect.top
                val centerY = paintFM.ascent + fontHeight / 2
                it.ascent = centerY - drHeight / 2
                it.top = it.ascent
                it.bottom = centerY + drHeight / 2
                it.descent = it.bottom
            }
            rect.right
        } else super.getSize(paint, text, start, end, fm)
    }

    companion object {
        /**
         * Alies for [android.text.style.DynamicDrawableSpan.ALIGN_CENTER] that is available only since SDK 29
         */
        const val ALIGN_CENTER = 2

        /**
         * Searches [CustomImageSpan] by a tag in a spannable string
         */
        inline fun <reified T : CustomImageSpan> findImageSpan(
            spannable: Spannable?, tag: CharSequence
        ): ImageSpan? {
            return spannable?.getSpans(0, spannable.length, T::class.java)
                ?.find { it.tag == tag }
        }
    }
}