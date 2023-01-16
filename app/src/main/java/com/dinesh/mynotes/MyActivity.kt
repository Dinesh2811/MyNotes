package com.dinesh.mynotes

import android.os.Bundle
import android.util.Log
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.PopupMenu

class MyActivity : AppCompatActivity() {
    private val TAG = "log_" + MyActivity::class.java.name.split(MyActivity::class.java.name.split(".").toTypedArray()[2] + ".").toTypedArray()[1]
    private lateinit var popupWindow: PopupWindow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.x_activity_main)

        // Set a click listener for the text view
        val textView = findViewById<TextView>(R.id.text_view)
        textView.setOnClickListener {
//            popupWindow.dismiss()
        }


        textView.setOnLongClickListener {
            Log.d(TAG, "onCreate: ")
            val popup = PopupMenu(this, it)
            popup.inflate(R.menu.context_menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.actionSelect -> {
                        // Do something
                        true
                    }
                    R.id.actionReOrder -> {
                        // Do something
                        true
                    }
                    else -> false
                }
            }
            popup.show()
            true
        }

    }
}