package com.example.orbitmvisample.ui.alert.impl.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.example.orbitmvisample.R
import com.example.orbitmvisample.ui.alert.AlertData.AlertStyle
import com.example.orbitmvisample.utils.Dimens
import com.example.orbitmvisample.utils.Dimens.dpToPx
import com.example.orbitmvisample.utils.Dimens.dpToPxI
import com.example.orbitmvisample.utils.Resources.getColorR

/**
 * Custom floating toast view
 */
class FloatingToast(
    private val parent: ViewGroup,
    private val alertStyle: AlertStyle? = null,  // if not null - affects the use of predefined colors
    private val settings: Settings = Settings()
) {
    constructor(
        parent: View,
        alertStyle: AlertStyle? = null,
        settings: Settings = Settings(),
    ) : this(parent as ViewGroup, alertStyle, settings)

    private var layout: ViewGroup? = null
    private var view: View? = null
    private var translation: Float = 0f

    fun show(message: CharSequence?, onComplete: (() -> Unit)? = null) {
        if (view != null) return

        layout = buildLayout(parent.context)
        view = settings.customView ?: buildView(message)
        val layout = layout ?: return
        val view = view ?: return

        val lp = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            marginStart = settings.marginHorizontal
            marginEnd = settings.marginHorizontal
            gravity = settings.viewGravity
            when (gravity) {
                Gravity.TOP -> topMargin = Dimens.statusBarHeight + settings.marginVertical
                Gravity.BOTTOM -> bottomMargin =
                    Dimens.navigationBarHeight + settings.marginVertical
            }
        }

        view.isInvisible = true
        layout.addView(view, lp)
        parent.addView(layout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        view.post {
            when (settings.animationType) {
                AnimationType.TRANSLATION -> runTranslationAnimation(true, onComplete)
                else -> runFadeAnimation(true, onComplete)
            }
        }
    }

    fun hide(onComplete: (() -> Unit)? = null) {
        when (settings.animationType) {
            AnimationType.TRANSLATION -> runTranslationAnimation(false, onComplete)
            else -> runFadeAnimation(false, onComplete)
        }
    }

    fun isShowing(): Boolean {
        return layout != null && view != null && view?.isVisible == true
    }

    private fun buildLayout(context: Context?): ViewGroup? {
        context ?: return null
        return FrameLayout(context).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    private fun buildView(message: CharSequence?): View? {
        return settings.customViewBuilder?.invoke(message) ?: defViewBuilder(message)
    }

    private val defViewBuilder: (message: CharSequence?) -> View? = { message ->
        layout?.context?.let { context ->
            TextView(context).apply {
                text = message
                setTextSize(TypedValue.COMPLEX_UNIT_PX, settings.textSize)
                setTextColor(settings.textColor)
                background = buildBgShape(settings.radii, getBgColor())
                gravity = settings.textGravity
                val p = settings.contentPadding
                setPadding(p, p, p, p)
                elevation = settings.viewElevation
                setOnClickListener { hide() }
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun buildBgShape(radii: Float, bgColor: Int): Drawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = floatArrayOf(radii, radii, radii, radii, radii, radii, radii, radii)
            setColor(bgColor)
        }
    }

    private fun runTranslationAnimation(show: Boolean, onComplete: (() -> Unit)? = null) {
        val view = view ?: return
        val layout = layout ?: return
        if (show) {
            translation = when (settings.viewGravity) {
                Gravity.TOP -> view.y + view.height
                else -> view.y - layout.height
            }
            view.translationY = -translation
            view.animate()
                .setDuration(settings.animationMills)
                .translationYBy(translation)
                .withStartAction {
                    view.isVisible = true
                }
                .withEndAction(onComplete)
                .start()
        } else {
            view.animate()
                .setDuration(settings.animationMills)
                .translationYBy(-translation)
                .withEndAction {
                    onAnimationEnd()
                    onComplete?.invoke()
                }
                .start()
        }
    }

    private fun runFadeAnimation(show: Boolean, onComplete: (() -> Unit)? = null) {
        val view = view ?: return
        if (show) {
            view.alpha = 0f
            view.animate()
                .setDuration(settings.animationMills)
                .alpha(1f)
                .withStartAction {
                    view.isVisible = true
                }
                .withEndAction(onComplete)
                .start()
        } else {
            view.animate()
                .setDuration(settings.animationMills)
                .alpha(0f)
                .withEndAction {
                    onAnimationEnd()
                    onComplete?.invoke()
                }
                .start()
        }
    }

    private fun onAnimationEnd() {
        layout?.removeView(view)
        parent.removeView(layout)
        view = null
        layout = null
        translation = 0f
    }

    @Suppress("unused")
    enum class AnimationType {
        TRANSLATION,
        FADE
    }

    fun getBgColor(): Int {
        return when (alertStyle) {
            null -> settings.bgColor
            AlertStyle.ERROR -> BG_COLOR_ERROR
            else -> BG_COLOR_INFO
        }
    }

    /**
     * The toast settings
     */
    class Settings(
        val viewGravity: Int = VIEW_GRAVITY,
        val marginVertical: Int = MARGIN_VERTICAL,
        val marginHorizontal: Int = MARGIN_HORIZONTAL,
        val viewElevation: Float = VIEW_ELEVATION,
        val contentPadding: Int = CONTENT_PADDING,
        val textSize: Float = TEXT_SIZE,
        val radii: Float = RADII,
        val bgColor: Int = BG_COLOR_ERROR, // the value ignored if alertStyle is defined
        val textColor: Int = TEXT_COLOR,
        val textGravity: Int = TEXT_GRAVITY,
        val animationMills: Long = ANIMATION_MILLS,
        val animationType: AnimationType = AnimationType.TRANSLATION,
        val customViewBuilder: ((CharSequence?) -> View?)? = null,
        val customView: View? = null
    )

    companion object {
        private const val VIEW_GRAVITY = Gravity.TOP
        private val MARGIN_VERTICAL = dpToPxI(12)
        private val MARGIN_HORIZONTAL: Int = dpToPxI(16)
        private val VIEW_ELEVATION: Float = dpToPx(20)
        private val CONTENT_PADDING: Int = dpToPxI(6)
        private val TEXT_SIZE = dpToPx(15)
        private val RADII = dpToPx(12)
        private val BG_COLOR_ERROR = getColorR(R.color.neon_red)
        private val BG_COLOR_INFO = getColorR(R.color.black)
        private val TEXT_COLOR = getColorR(R.color.white)
        private const val TEXT_GRAVITY = Gravity.CENTER
        private const val ANIMATION_MILLS = 200L
    }
}