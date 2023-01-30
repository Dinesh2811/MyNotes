package com.dinesh.mynotes

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dinesh.mynotes.databinding.ActivityMainBinding
import com.dinesh.mynotes.rv.RvMain
import java.io.File
import java.io.IOException

//class MainActivity : ExternalStoragePermission() {
class MainActivity : AppCompatActivity() {
    private val TAG = "log_" + MainActivity::class.java.name.split(MainActivity::class.java.name.split(".").toTypedArray()[2] + ".").toTypedArray()[1]

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


//        startActivity(Intent(this, TestActivity::class.java))
        startActivity(Intent(this, RvMain::class.java))
        finish()

//
//        FancySnackbar()
//            .make(findViewById(android.R.id.content), "Backup saved to ${backupFile.absolutePath}", Snackbar.LENGTH_INDEFINITE)
//            .setBackgroundColor(Color.WHITE)
//            .setActionTextColor(Color.BLUE)
//            .setTextColor(Color.BLACK)
//            .setAction("Dismiss") { }
//            .show()

//        FancySnackbar()
//            .make(findViewById(android.R.id.content), "Error saving backup", Snackbar.LENGTH_INDEFINITE)
//            .setBackgroundColor(Color.RED)
//            .setActionTextColor(Color.LTGRAY)
//            .setTextColor(Color.WHITE)
//            .setAction("Dismiss") { }
//            .show()
    }

    override fun onBackPressed() {
        Log.e(TAG, "onBackPressed: ")
//        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "example.txt")
//        try {
//            file.createNewFile()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }

//        test()

//        val directory = File(getFilesDir(), "MyNote")
//        if (!directory.exists()) {
//            directory.mkdirs()
//        }
//        val file = File(directory, "example.txt")
//        file.createNewFile()
//        val fileCount = directory.listFiles()?.size
//        Log.e(TAG, "File count: $fileCount")
//        Log.e(TAG, "File absoluteFile: ${file.absoluteFile}")


//        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MyNote/")
//        if (!directory.exists()) {
//            directory.mkdirs()
//        }
//        val file = File(directory, "example.txt")
//        file.createNewFile()
//        val files = directory.listFiles()
//
////        val directory = File(getExternalFilesDir(null), "MyNote")
////        if (!directory.exists()) {
////            directory.mkdirs()
////        }
////        val file = File(directory, "example.txt")
////        file.createNewFile()
////        val files = directory.listFiles()
//
//        if (files != null) {
//            Log.e(TAG, "file: ${files.size}")
//            for (file in files) {
//                Log.e(TAG, "File: ${file.name}")
//            }
//        } else {
//            Log.e(TAG, "No files found.")
//        }

    }

    private val REQUEST_CODE = 1
    private fun test() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        startActivityForResult(intent, REQUEST_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            val cursor = contentResolver.query(uri!!, null, null, null, null)
            cursor?.use {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                val fileName = cursor.getString(nameIndex)
                Log.d(TAG, "Selected file: $fileName")
            }

        }
    }


//    override fun onResume() {
//        super.onResume()
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
//            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
//        ) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                startActivity(Intent(this, RvMain::class.java))
//                finish()
//            } else {
//                Log.e(TAG, "This app doesn't support below Android 8(Oreo)")
//
//                val dialog = FancyDialog(this)
//                    .setTitle("Not Supported")
//                    .setMessage("This app doesn't support below Android 8(Oreo)")
//                    .setNegativeButton("Exit", object : DialogInterface.OnClickListener {
//                        override fun onClick(dialog: DialogInterface, which: Int) {
//                            dialog.dismiss()
//                            finish()
//                        }
//                    })
//                dialog.show()
//
//                FancySnackbar()
//                    .make(findViewById(android.R.id.content), "This app doesn't support below Android 8(Oreo)", Snackbar.LENGTH_INDEFINITE)
//                    .setBackgroundColor(Color.WHITE)
//                    .setActionTextColor(Color.BLUE)
//                    .setTextColor(Color.BLACK)
//                    .setAction("Exit", View.OnClickListener {
//                        finish()
//                    })
//                    .show()
//
//            }
//        }
//    }

    private fun getView(viewID: Int): View? {
        return findViewById<View>(viewID)
    }

}
