package com.dinesh.mynotes.app

import androidx.appcompat.app.AppCompatActivity
import com.dinesh.mynotes.R

//open class ColorScheme : ExternalStoragePermission() {
open class ColorScheme : AppCompatActivity() {
    open val lightBackground: Int = R.color.white
    open val darkOnBackground: Int = R.color.black

    open val toolbarBackground: Int = R.color.white
    open val toolbarOnBackground: Int = R.color.black
//    open val toolbarBackground: Int = R.color.black
//    open val toolbarOnBackground: Int = R.color.white

}