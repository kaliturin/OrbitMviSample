package com.example.orbitmvisample.ui.alert.impl

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.KeyEvent
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.appcompat.app.AlertDialog
import com.example.orbitmvisample.R
import com.example.orbitmvisample.ui.alert.Alert
import com.example.orbitmvisample.ui.alert.AlertData
import com.example.orbitmvisample.ui.alert.AlertListener

class DialogAlert(
    context: Context,
    private val alertData: AlertData
) : Alert {

    override val id: AlertData.Id = alertData.id
    private var alertListener: AlertListener? = null
    private val dialogBuilder: AlertDialog.Builder
    private var alertDialog: AlertDialog? = null

    @MainThread
    override fun show() {
        alertDialog = dialogBuilder.show()
        alertListener?.onOpen(this)
        onOpen(alertData)
    }

    @MainThread
    override fun hide(): Boolean {
        return alertDialog?.run {
            hide()
            alertDialog = null
            true
        } ?: false
    }

    override fun isShowing(): Boolean {
        return alertDialog?.isShowing == true
    }

    override fun alertListener(alertListener: AlertListener?): Boolean {
        this.alertListener = alertListener
        return true
    }

    init {
        val dlgData = (alertData.additional as? DialogAlertData)

        dialogBuilder = with(alertData) {
            val builder = dlgData?.themeResId?.let {
                AlertDialog.Builder(context, it)
            } ?: AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(cancellable)
                .setOnKeyListener { dlg, keyCode, _ ->
                    if (keyCode == KeyEvent.KEYCODE_BACK) dlg.dismiss()
                    true
                }
                .setOnDismissListener {
                    alertListener?.onClose(this@DialogAlert)
                    alertData.onClose?.invoke()
                }

            builder.setPositiveButton(
                positive ?: context.getString(R.string.ok)
            ) { _, _ -> onPositive?.invoke() }

            onNegative?.run {
                builder.setNegativeButton(
                    negative ?: context.getString(R.string.cancel)
                ) { _, _ -> invoke() }
            }

            onNeutral?.run {
                builder.setNeutralButton(
                    neutral ?: context.getString(R.string.later)
                ) { _, _ -> invoke() }
            }

            contentView?.let {
                builder.setView(it)
            }

            builder
        }
    }

    private fun onOpen(alertData: AlertData) {
        if (alertData.linkColor != null && alertData.message != null) {
            alertDialog?.findViewById<TextView>(android.R.id.message)?.apply {
                setLinkTextColor(alertData.linkColor)
                movementMethod = LinkMovementMethod.getInstance()
            }
        }
        alertData.onOpen?.invoke()
    }

    class DialogAlertData(val themeResId: Int = 0)
}