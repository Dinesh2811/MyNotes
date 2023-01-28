package com.dinesh.mynotes

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.dinesh.mynotes.databinding.ActivityMainBinding
import com.dinesh.mynotes.rv.RvMain

//class MainActivity : ExternalStoragePermission() {
class MainActivity : AppCompatActivity() {
    private val TAG = "log_" + MainActivity::class.java.name.split(MainActivity::class.java.name.split(".").toTypedArray()[2] + ".").toTypedArray()[1]

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
