package com.dinesh.mynotes.app

import android.graphics.Color
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

fun AppCompatActivity.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_INDEFINITE){
    val backgroundColorTypedValue = TypedValue()
    theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceInverse, backgroundColorTypedValue, true)
    val backgroundColor = backgroundColorTypedValue.data

    val actionTextColorTypedValue = TypedValue()
    theme.resolveAttribute(com.google.android.material.R.attr.colorPrimaryInverse, actionTextColorTypedValue, true)
    val actionTextColor = actionTextColorTypedValue.data

    val textColorTypedValue = TypedValue()
    theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceInverse, textColorTypedValue, true)
    val textColor = textColorTypedValue.data

        FancySnackbar()
            .make(findViewById(android.R.id.content), message, duration)
            .setBackgroundColor(backgroundColor)
            .setActionTextColor(actionTextColor)
            .setTextColor(textColor)
            .setAction("Dismiss") { }
            .show()
}


fun AppCompatActivity.showErrorSnackbar(message: String, duration: Int = Snackbar.LENGTH_INDEFINITE){
    val backgroundColorTypedValue = TypedValue()
    theme.resolveAttribute(com.google.android.material.R.attr.colorError, backgroundColorTypedValue, true)
    val backgroundColor = backgroundColorTypedValue.data

    val actionTextColorTypedValue = TypedValue()
    theme.resolveAttribute(com.google.android.material.R.attr.colorErrorContainer, actionTextColorTypedValue, true)
    val actionTextColor = actionTextColorTypedValue.data

    val textColorTypedValue = TypedValue()
    theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceInverse, textColorTypedValue, true)
    val textColor = textColorTypedValue.data

        FancySnackbar()
            .make(findViewById(android.R.id.content), message, duration)
            .setBackgroundColor(backgroundColor)
            .setActionTextColor(actionTextColor)
            .setTextColor(textColor)
            .setAction("Dismiss") { }
            .show()
}
