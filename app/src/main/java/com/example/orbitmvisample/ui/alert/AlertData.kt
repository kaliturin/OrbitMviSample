package com.example.orbitmvisample.ui.alert

import android.view.View

open class AlertData(
    val message: CharSequence? = null,
    val title: CharSequence? = null,
    val positive: CharSequence? = null,
    val negative: CharSequence? = null,
    val neutral: CharSequence? = null,
    val onPositive: (() -> Unit)? = null,
    val onNegative: (() -> Unit)? = null,
    val onNeutral: (() -> Unit)? = null,
    val onOpen: (() -> Unit)? = null,
    val onClose: (() -> Unit)? = null,
    val durationMills: Long = 0,
    val cancellable: Boolean = false,
    val linkColor: Int? = null,
    val contentView: View? = null,
    val alertBuilder: AlertBuilder? = null,
    val additional: Any? = null
) {
    val id = Id(message, title)

    override fun equals(other: Any?): Boolean = if (other is AlertData) id == other.id else false
    override fun hashCode(): Int = id.hashCode()
    override fun toString(): String = id.toString()

    data class Id(
        private val message: CharSequence? = null,
        private val title: CharSequence? = null
    )
}