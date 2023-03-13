package com.example.orbitmvisample.ui.alert

import android.view.View
import androidx.annotation.StringRes
import com.example.orbitmvisample.utils.Resources

/**
 * Alert message data
 */
data class AlertData(
    private val message: CharSequence? = null,      // alert's message
    @StringRes private val messageRes: Int? = null, // alert's message res
    private val title: CharSequence? = null,        // alert's title
    @StringRes private val titleRes: Int? = null,   // alert's title res
    val positive: CharSequence? = null,     // alert's positive button title
    val negative: CharSequence? = null,     // alert's negative button title
    val neutral: CharSequence? = null,      // alert's neutral button title
    val onPositive: (() -> Unit)? = null,   // on alert's positive button tap listener
    val onNegative: (() -> Unit)? = null,   // on alert's negative button tap listener
    val onNeutral: (() -> Unit)? = null,    // on alert's neutral button tap listener
    val onOpen: (() -> Unit)? = null,       // alert on opening event callback
    val onClose: (() -> Unit)? = null,      // alert on closing event callback
    val durationMills: Long = 0,            // showing alert duration
    val repeatingMills: Long = 0L,          // alert repeating timeout
    val cancellable: Boolean = false,       // is alert cancellable
    val linkColor: Int? = null,             // alert content's text link color
    val contentView: View? = null,          // alert content view
    val showOnlyIfParentIsResumed: Boolean = true,    // show an alert only if it's parent is resumed
    val alertBuilder: AlertBuilder? = null, // alert builder
    val alertStyle: AlertStyle? = null,     // alert style type
    val specificSettings: Any? = null       // alert specific settings
) {
    fun getMessage() = message ?: Resources.getString(messageRes)
    fun getTitle() = title ?: Resources.getString(titleRes)

    // an unique alert id allows to distinguish alerts by its main content - the message and title
    val id = Id(getMessage().toString(), getTitle().toString())

    override fun equals(other: Any?): Boolean = if (other is AlertData) id == other.id else false
    override fun hashCode(): Int = id.hashCode()
    override fun toString(): String = id.toString()

    data class Id(
        private val message: String? = null,
        private val title: String? = null
    )

    @Suppress("unused")
    enum class AlertStyle {
        ERROR,
        WARN,
        QUEST,
        INFO
    }
}