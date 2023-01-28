package com.dinesh.mynotes.app

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

fun AppCompatActivity.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_INDEFINITE){
        FancySnackbar()
            .make(findViewById(android.R.id.content), message, duration)
            .setBackgroundColor(Color.WHITE)
            .setActionTextColor(Color.BLUE)
            .setTextColor(Color.BLACK)
            .setAction("Dismiss") { }
            .show()
}


fun AppCompatActivity.showErrorSnackbar(message: String, duration: Int = Snackbar.LENGTH_INDEFINITE){
        FancySnackbar()
            .make(findViewById(android.R.id.content), message, duration)
            .setBackgroundColor(Color.RED)
            .setActionTextColor(Color.LTGRAY)
            .setTextColor(Color.WHITE)
            .setAction("Dismiss") { }
            .show()
}