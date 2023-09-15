//package com.dinesh.mynotes
//
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import androidx.appcompat.app.AppCompatActivity
//import com.dinesh.mynotes.app.FancySnackbar
//import com.dinesh.mynotes.app.FancySnackbarLayout
//import com.dinesh.mynotes.rv.RvMain
//import com.google.android.material.snackbar.Snackbar
//
////class MainActivity : ExternalStoragePermission() {
//class MainActivity : AppCompatActivity() {
//    private val TAG = "log_" + MainActivity::class.java.name.split(MainActivity::class.java.name.split(".").toTypedArray()[2] + ".").toTypedArray()[1]
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
////
////        FancySnackbarLayout()
////            .makeCustomLayout(findViewById(android.R.id.content), this,"Backup saved to ", Snackbar.LENGTH_INDEFINITE, {
//////                Log.e(TAG, "onCreate: btn1")
//////                FancySnackbar.snackbarTextView?.visibility = View.GONE
////            },{},initFancySnackbarLayoutView())
////            .show()
//
//
////        startActivity(Intent(this, TestActivity::class.java))
////        startActivity(Intent(this, BackupNotes::class.java))
////        startActivity(Intent(this, com.dinesh.mynotes.test.MainActivity::class.java))
//        startActivity(Intent(this, RvMain::class.java))
////        startActivity(Intent(this, T1::class.java))
//        finish()
//
//
////
////        FancySnackbar()
////            .make(findViewById(android.R.id.content), "Backup saved to ${backupFile.absolutePath}", Snackbar.LENGTH_INDEFINITE)
////            .setBackgroundColor(Color.WHITE)
////            .setActionTextColor(Color.BLUE)
////            .setTextColor(Color.BLACK)
////            .setAction("Dismiss") { }
////            .show()
//
////        FancySnackbar()
////            .make(findViewById(android.R.id.content), "Error saving backup", Snackbar.LENGTH_INDEFINITE)
////            .setBackgroundColor(Color.RED)
////            .setActionTextColor(Color.LTGRAY)
////            .setTextColor(Color.WHITE)
////            .setAction("Dismiss") { }
////            .show()
//
////        FancySnackbar()
////            .makeCustomLayout(findViewById(android.R.id.content), "Backup saved to ", Snackbar.LENGTH_INDEFINITE, {
////                Log.e(TAG, "onCreate: btn1")
////                FancySnackbar().dismiss()
////            })
////            .show()
//    }
//
//    private fun initFancySnackbarLayoutView() {
//        Log.e(TAG, "initFancySnackbarLayoutView: ${FancySnackbarLayout.snackbarTextView}")
//        FancySnackbarLayout.btnCopy?.visibility = View.GONE
//        FancySnackbarLayout.btnShare?.setImageResource(R.drawable.ic_baseline_close_24)
//    }
//
////    override fun onBackPressed() {
////        Log.e(TAG, "onBackPressed: ")
////
////    }
//
//
//    private fun getView(viewID: Int): View? {
//        return findViewById<View>(viewID)
//    }
//
//}
