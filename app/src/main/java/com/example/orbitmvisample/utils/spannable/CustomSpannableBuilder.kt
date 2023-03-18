package com.example.orbitmvisample.utils.spannable

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.text.getSpans
import com.example.orbitmvisample.utils.Resources
import com.example.orbitmvisample.utils.getColorA
import com.example.orbitmvisample.utils.multiLet
import com.example.orbitmvisample.utils.spannable.CustomImageSpan.Companion.ALIGN_CENTER
import kotlin.reflect.KClass

/**
 * Probably in common cases you will be using [android.text.SpannableStringBuilder] with extensions
 * from androidx.core.text.SpannableStringBuilder.kt
 * But they don't always support the all spans and their convenient customisations yet.
 */
class CustomSpannableBuilder(
    private val context: Context? = Resources.context,
    private val spanningText: CharSequence? = null,
    @ColorInt private val foregroundSpanColor: Int? = null,
    @AttrRes private val foregroundSpanColorAttr: Int? = null,
    private val styleSpanTextStyle: Int? = null,
    private val typefaceSpanFont: Typeface? = null,
    private val typefaceSpanFontSize: Float? = null,
    @DrawableRes private val imageSpanDrawableRes: Int? = null,
    private val imageSpanDrawable: Drawable? = null,
    private val imageSpanFlags: Int? = ALIGN_CENTER, // ALIGN_BOTTOM/ALIGN_BASELINE/ALIGN_CENTER
    private val imageSpanDrawableMutate: Boolean = false,
    private val imageSpanTag: String = "",
    private val clickSpanLinkUnderline: Boolean? = null,
    @ColorInt private val clickSpanLinkColor: Int? = null,
    @ColorInt private val clickSpanBgColor: Int? = null,
    @AttrRes private val clickSpanLinkColorAttr: Int? = null,
    @AttrRes private val clickSpanBgColorAttr: Int? = null,
    private val clickSpanListener: (((View)) -> Unit)? = null
) {

    private fun getForegroundColorSpan(): ForegroundColorSpan? {
        val colorFromAttr = foregroundSpanColorAttr?.let { context?.getColorA(it) }
        return (colorFromAttr ?: foregroundSpanColor)
            ?.let { ForegroundColorSpan(it) }
    }

    private fun getStyleSpan(): StyleSpan? {
        return styleSpanTextStyle?.let { StyleSpan(it) }
    }

    private fun getCustomTypefaceSpan(): CustomTypefaceSpan? {
        return multiLet(
            typefaceSpanFont, typefaceSpanFontSize
        ) { typefaceSpanFont, typefaceSpanFontSize ->
            CustomTypefaceSpan(typefaceSpanFont, typefaceSpanFontSize)
        }
    }

    private fun getCustomImageSpan(): CustomImageSpan? {
        val drawable = imageSpanDrawable?.let {
            if (imageSpanDrawableMutate) it.mutate() else it
        } ?: imageSpanDrawableRes?.let {
            Resources.getDrawableR(context, it, imageSpanDrawableMutate)
        }
        return drawable?.let {
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
            CustomImageSpan(it, imageSpanFlags ?: 0, imageSpanTag)
        }
    }

    private fun getCustomClickableSpan(): CustomClickableSpan? {
        val clickSpanLinkColor = clickSpanLinkColorAttr?.let { context?.getColorA(it) }
        val clickSpanBgColor = clickSpanBgColorAttr?.let { context?.getColorA(it) }
        return clickSpanListener?.let {
            CustomClickableSpan(
                clickSpanLinkUnderline,
                clickSpanLinkColor ?: this.clickSpanLinkColor,
                clickSpanBgColor ?: this.clickSpanBgColor,
                it
            )
        }
    }

    private fun findSpans(): List<CharacterStyle> {
        return mutableListOf<CharacterStyle?>(
            getForegroundColorSpan(),
            getStyleSpan(),
            getCustomTypefaceSpan(),
            getCustomImageSpan(),
            getCustomClickableSpan()
        ).filterNotNull()
    }

    fun build(text: CharSequence? = spanningText): SpannableString {
        val string = SpannableString(text ?: "")
        return addSpans(string, findSpans())
    }

    companion object {
        fun <T : Spannable> addSpans(spannable: T, vararg spans: Any): T {
            return spannable.apply {
                spans.forEach {
                    setSpan(it, 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        fun <T : Spannable> removeSpans(spannable: T, vararg spanClass: KClass<*>): T {
            return spannable.apply {
                spanClass.forEach { clazz ->
                    for (span in spannable.getSpans<Any>()) {
                        if (clazz.isInstance(span)) removeSpan(span)
                    }
                }
            }
        }
    }
}