package com.dinesh.mynotes.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.dinesh.mynotes.R

private val TAG = "log_ColorScheme"

//class ColorScheme : AppCompatActivity() {
////    private lateinit var binding: x
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
////        binding = x.inflate(layoutInflater)
////        setContentView(binding.root)
//    }
//}

open class ColorScheme : ExternalStoragePermission() {
//open class ColorScheme : AppCompatActivity() {

    open val lightBackground: Int = R.color.white
    open val darkOnBackground: Int = R.color.black

    open val toolbarBackground: Int = R.color.white
    open val toolbarOnBackground: Int = R.color.black
//    open val toolbarBackground: Int = R.color.black
//    open val toolbarOnBackground: Int = R.color.white

}