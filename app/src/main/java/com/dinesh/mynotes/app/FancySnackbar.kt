package com.dinesh.mynotes.app

import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar


class FancySnackbar {

    private var snackbar: Snackbar? = null

    fun make(view: View, message: String, duration: Int): FancySnackbar {
        snackbar = Snackbar.make(view, message, duration)
        return this
    }

    fun setAction(text: String, listener: View.OnClickListener): FancySnackbar {
        snackbar?.setAction(text, listener)
        return this
    }

    fun setBackgroundColor(color: Int): FancySnackbar {
        snackbar?.view?.setBackgroundColor(color)
        return this
    }

    fun setTextColor(color: Int): FancySnackbar {
        val textView = snackbar?.view?.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView?.setTextColor(color)
        return this
    }

    fun setActionTextColor(color: Int): FancySnackbar {
        snackbar?.setActionTextColor(color)
        return this
    }

    fun setMaxLines(lines: Int): FancySnackbar {
        val textView = snackbar?.view?.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView?.maxLines = lines
        return this
    }

    fun show() {
        snackbar?.show()
    }
}

/*

        FancySnackbar()
            .make(findViewById(android.R.id.content), "This is a fancy snackbar!", Snackbar.LENGTH_INDEFINITE)
            .setBackgroundColor(Color.RED)
            .setAction("Dismiss", View.OnClickListener {
                // Do something when dismiss button is clicked
            })
            .show()


 */