package com.example.orbitmvisample.utils.spannable

import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View

class CustomClickableSpan(
    private val underlineLink: Boolean? = null,
    private val linkColor: Int? = null,
    private val bgColor: Int? = null,
    private val callback: (View) -> Unit
) : ClickableSpan() {

    override fun onClick(widget: View) {
        callback.invoke(widget)
    }

    override fun updateDrawState(ds: TextPaint) {
        bgColor?.let { ds.bgColor = it }
        ds.color = linkColor ?: ds.linkColor
        underlineLink?.let { ds.isUnderlineText = it }
    }
}