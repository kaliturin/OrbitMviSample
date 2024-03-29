package com.example.orbitmvisample.ui.helpers.logger

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.View
import com.example.orbitmvisample.R
import com.example.orbitmvisample.databinding.LogListItemBinding
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem

class LogListItem(
    val logMessage: LogMessage
) : BindableItem<LogListItemBinding>() {

    override fun initializeViewBinding(view: View) = LogListItemBinding.bind(view)
    override fun getLayout() = R.layout.log_list_item
    override fun isSameAs(other: Item<*>) = other is LogListItem && logMessage == other.logMessage
    override fun hasSameContentAs(other: Item<*>) =
        other is LogListItem && logMessage == other.logMessage

    @SuppressLint("SetTextI18n")
    override fun bind(viewBinding: LogListItemBinding, position: Int) {
        viewBinding.nameTextView.apply {
            text = "${res().first}: ${logMessage.message}"
            setTextColor(res().second)
        }
    }

    private fun res() = mapRes[logMessage.priority] ?: defRes

    companion object {
        private val mapRes = mapOf(
            Log.VERBOSE to Pair("V", Color.GRAY),
            Log.DEBUG to Pair("D", Color.BLACK),
            Log.INFO to Pair("I", Color.BLUE),
            Log.WARN to Pair("W", Color.MAGENTA),
            Log.ERROR to Pair("E", Color.RED),
            Log.ASSERT to Pair("A", Color.RED)
        )
        val defRes = Pair("", Color.BLACK)
    }
}
