package com.dinesh.mynotes.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import com.dinesh.mynotes.R
import com.google.android.material.snackbar.Snackbar

class FancySnackbarLayout {
    private var secondaryAction: Snackbar.Callback? = null

    companion object {
        var snackbar: Snackbar? = null
        var layout: Snackbar.SnackbarLayout? = null
        var snackbarTextView: TextView? = null
        var btnCopy: ImageButton? = null
        var btnShare: ImageButton? = null
    }

    fun make(view: View, message: String, duration: Int): FancySnackbarLayout {
        snackbar = Snackbar.make(view, message, duration)
        return this
    }

    @SuppressLint("ClickableViewAccessibility")
    fun makeCustomLayout(
        view: View, context: Context, message: String, duration: Int = Snackbar.LENGTH_INDEFINITE, btnCopyClickListener: View.OnClickListener? = null,
        btnShareClickListener: View.OnClickListener? = null, tvLongClickListener: View.OnLongClickListener? = null,
    ): FancySnackbarLayout {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customView = inflater.inflate(R.layout.snackbar_custom, null) as ViewGroup

        snackbarTextView = customView.findViewById<TextView>(R.id.snackbar_text)
        btnCopy = customView.findViewById<ImageButton>(R.id.snackbar_btnCopy)
        btnShare = customView.findViewById<ImageButton>(R.id.snackbar_btnShare)

        val typedValue = TypedValue()
        context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceInverse, typedValue, true)
        val color = typedValue.data
        val colorStateList = ColorStateList.valueOf(color)
        (btnCopy as ImageButton).imageTintList = colorStateList
        (btnShare as ImageButton).imageTintList = colorStateList

        snackbarTextView?.text = message
//        snackbarTextView?.setOnLongClickListener {
//            snackbar?.dismiss()
//            true
//        }


//        var x1 = 0f
//        var x2 = 0f
//
//        snackbarTextView.setOnTouchListener { v, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    x1 = event.x
//                }
//                MotionEvent.ACTION_UP -> {
//                    x2 = event.x
//
//                    val deltaX = x2 - x1
//
//                    if (deltaX > 0) {
//                        Log.i(TAG, "makeCustomLayout: Swiped from left to right")
//                        // Swiped from left to right
//                        // Perform task for left to right swipe
//                    } else {
//                        Log.e(TAG, "makeCustomLayout: Swiped from right to left")
//                        // Swiped from right to left
//                        // Perform task for right to left swipe
//                    }
//                }
//            }
//
//            true
//        }

        snackbarTextView?.setOnLongClickListener(tvLongClickListener)
        btnCopy?.setOnClickListener(btnCopyClickListener)
        btnShare?.setOnClickListener(btnShareClickListener)

        snackbar = Snackbar.make(view, "", duration)
//        snackbar?.view?.setBackgroundColor(Color.WHITE)
        snackbar?.view?.setPadding(0, 0, 0, 0)
        snackbar?.view?.layoutParams = (snackbar?.view?.layoutParams as FrameLayout.LayoutParams).apply {
            gravity = Gravity.BOTTOM
        }
        layout = snackbar?.view as Snackbar.SnackbarLayout
        layout?.addView(customView)

        return this
    }

    fun setAction(text: String, listener: View.OnClickListener): FancySnackbarLayout {
        snackbar?.setAction(text, listener)
        return this
    }

    fun setBackgroundColor(color: Int): FancySnackbarLayout {
        snackbar?.view?.setBackgroundColor(color)
        return this
    }

    fun setTextColor(color: Int): FancySnackbarLayout {
        val textView = snackbar?.view?.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView?.setTextColor(color)
        return this
    }

    fun setActionTextColor(color: Int): FancySnackbarLayout {
        snackbar?.setActionTextColor(color)
        return this
    }

    fun setMaxLines(lines: Int): FancySnackbarLayout {
        val textView = snackbar?.view?.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView?.maxLines = lines
        return this
    }

    fun setInit(btnCopyView: Int = View.VISIBLE, btnShareView: Int = View.VISIBLE, btnCopyImage: Int = R.drawable.baseline_content_copy_24, btnShareImage: Int = R.drawable.baseline_share_24): FancySnackbarLayout {
        btnCopy?.visibility = btnCopyView
        btnShare?.visibility = btnShareView
        btnCopy?.setImageResource(btnCopyImage)
        btnShare?.setImageResource(btnShareImage)
//        snackbarTextView?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        return this
    }

    fun show() {
        snackbar?.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(snackbar: Snackbar?, event: Int) {
                secondaryAction?.onDismissed(snackbar, event)
                layout?.removeAllViews()
                snackbarTextView = null
                btnCopy = null
                btnShare = null
                layout = null
                secondaryAction?.onDismissed(snackbar, event)
            }
        })
        snackbar?.show()
    }

    fun dismiss() {
        layout?.removeAllViews()
        snackbarTextView = null
        btnCopy = null
        btnShare = null
        layout = null
        snackbar?.dismiss()
    }
}
