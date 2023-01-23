package com.dinesh.mynotes

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.dinesh.mynotes.activity.EditNote
import com.dinesh.mynotes.app.FancyDialog
import com.dinesh.mynotes.app.FancySnackbar
import com.dinesh.mynotes.app.ToolbarMain
import com.dinesh.mynotes.databinding.ActivityMainBinding
import com.dinesh.mynotes.rv.RvMain
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private val TAG = "log_" + MainActivity::class.java.name.split(MainActivity::class.java.name.split(".").toTypedArray()[2] + ".").toTypedArray()[1]

    private lateinit var binding: ActivityMainBinding

//    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        val parentLayout = binding.parentLayout
//
//        val includedLayout = LayoutInflater.from(this).inflate(R.layout.z_test_layout, parentLayout, false)
//        parentLayout.addView(includedLayout)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startActivity(Intent(this, RvMain::class.java))
        finish()
    } else{
        Log.e(TAG, "This app doesn't support below Android 8(Oreo)")

        val dialog = FancyDialog(this)
            .setTitle("Not Supported")
            .setMessage("This app doesn't support below Android 8(Oreo)")
            .setNegativeButton("Exit", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.dismiss()
                    finish()
                }
            })
        dialog.show()

        FancySnackbar()
            .make(findViewById(android.R.id.content), "This app doesn't support below Android 8(Oreo)", Snackbar.LENGTH_INDEFINITE)
            .setBackgroundColor(Color.WHITE)
            .setActionTextColor(Color.BLUE)
            .setTextColor(Color.BLACK)
            .setAction("Exit", View.OnClickListener {
                finish()
            })
            .show()

    }


}

    private fun getView(viewID: Int): View? {
        return findViewById<View>(viewID)
    }

}
