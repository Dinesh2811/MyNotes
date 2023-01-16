package com.dinesh.mynotes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dinesh.mynotes.activity.EditNote
import com.dinesh.mynotes.app.ToolbarMain
import com.dinesh.mynotes.databinding.ActivityMainBinding
import com.dinesh.mynotes.rv.RvMain

class MainActivity : AppCompatActivity() {
    private val TAG = "log_" + MainActivity::class.java.name.split(MainActivity::class.java.name.split(".").toTypedArray()[2] + ".").toTypedArray()[1]

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val parentLayout = binding.parentLayout

        val includedLayout = LayoutInflater.from(this).inflate(R.layout.z_test_layout, parentLayout, false)
        parentLayout.addView(includedLayout)

        startActivity(Intent(this, RvMain::class.java))
//        startActivity(Intent(this, MyActivity::class.java))
        finish()

    }

    private fun getView(viewID: Int): View? {
        return findViewById<View>(viewID)
    }

}
