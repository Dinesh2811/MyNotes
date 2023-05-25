package com.dinesh.mynotes

import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.dinesh.mynotes.room.NotesDatabase
import java.io.*

class BackupActivity : AppCompatActivity() {
    private val TAG = "log_BackupActivity"
    private val REQUEST_EXTERNAL_STORAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // request for the storage permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_EXTERNAL_STORAGE)
        } else {
            backupDatabase()
        }

    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: ")
        // Restore the Database
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val myNoteDir = File(downloadDir, "MyNote")
        val file = File(myNoteDir, "notes.db")
        val inputFile = FileInputStream(file)
        val outputFile = FileOutputStream(getDatabasePath("notes_database"))
        val buffer = ByteArray(1024)
        var length: Int
        while (inputFile.read(buffer).also { length = it } > 0) {
            outputFile.write(buffer, 0, length)
        }
        inputFile.close()
        outputFile.flush()
        outputFile.close()
        Toast.makeText(this, "Database imported from Download/MyNote folder", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "restoreDatabase: Database imported ${file.absolutePath}")

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                backupDatabase()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun backupDatabase() {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val myNoteDir = File(downloadDir, "MyNote")
        if (!myNoteDir.exists()) {
            myNoteDir.mkdirs()
        }
        val file = File(myNoteDir, "notes.db")
        val inputFile = FileInputStream(getDatabasePath("notes_database"))
        val outputFile = FileOutputStream(file)
        val buffer = ByteArray(1024)
        var length: Int
        while (inputFile.read(buffer).also { length = it } > 0) {
            outputFile.write(buffer, 0, length)
        }
        inputFile.close()
        outputFile.flush()
        outputFile.close()
        Toast.makeText(this, "Database exported to Download/MyNote folder", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "backupDatabase: Database exported ${file.absolutePath}")
    }

    private fun shareExportedFile() {
        val exportDir = File(Environment.getExternalStorageDirectory(), "NotesApp")
        val file = File(exportDir, "notes.db")
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(this, "com.example.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "application/octet-stream"
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(Intent.createChooser(shareIntent, "Share file"))
        } else {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
        }
    }

}



//    private fun backupDatabase() {
//        if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) {
//            Toast.makeText(this, "External storage is not available", Toast.LENGTH_SHORT).show()
//            Log.d(TAG, "backupDatabase: External storage is not available")
//            return
//        }
//        val root = Environment.getExternalStorageDirectory()
//        val myNoteDir = File(root, "MyNote")
//        if (!myNoteDir.exists()) {
//            myNoteDir.mkdirs()
//        }
//        val file = File(myNoteDir, "notes.db")
//        val inputFile = FileInputStream(getDatabasePath("notes_database"))
//        val outputFile = FileOutputStream(file)
//        val buffer = ByteArray(1024)
//        var length: Int
//        while (inputFile.read(buffer).also { length = it } > 0) {
//            outputFile.write(buffer, 0, length)
//        }
//        inputFile.close()
//        outputFile.flush()
//        outputFile.close()
//        Toast.makeText(this, "Database exported to MyNote folder", Toast.LENGTH_SHORT).show()
//        Log.d(TAG, "backupDatabase: Database exported ${file.absolutePath}")
//    }



/*

private fun backupDatabase() {
    val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val myNoteDir = File(downloadDir, "MyNote")
    if (!myNoteDir.exists()) {
        myNoteDir.mkdirs()
    }
    val file = File(myNoteDir, "notes.db")
    val inputFile = FileInputStream(getDatabasePath("notes_database"))
    val outputFile = FileOutputStream(file)
    val buffer = ByteArray(1024)
    var length: Int
    while (inputFile.read(buffer).also { length = it } > 0) {
        outputFile.write(buffer, 0, length)
    }
    inputFile.close()
    outputFile.flush()
    outputFile.close()
    Toast.makeText(this, "Database exported to Download/MyNote folder", Toast.LENGTH_SHORT).show()
        val filePath = file.absolutePath
    Log.d(TAG, "backupDatabase: Database exported")

    Log.e(TAG, "backupDatabase: ${filePath}")
}


//    private fun backupDatabase() {
//        val rootDir = getFilesDir()
//        val myNoteDir = File(rootDir, "MyNote")
//        if (!myNoteDir.exists()) {
//            myNoteDir.mkdirs()
//        }
//        val file = File(myNoteDir, "notes.db")
//        val inputFile = FileInputStream(getDatabasePath("notes_database"))
//        val outputFile = FileOutputStream(file)
//        val buffer = ByteArray(1024)
//        var length: Int
//        while (inputFile.read(buffer).also { length = it } > 0) {
//            outputFile.write(buffer, 0, length)
//        }
//        inputFile.close()
//        outputFile.flush()
//        outputFile.close()
//        Toast.makeText(this, "Database exported to MyNote folder", Toast.LENGTH_SHORT).show()
//        val filePath = file.absolutePath
//        Log.d(TAG, "backupDatabase: Database exported")
//
//        Log.e(TAG, "backupDatabase: ${filePath}")
//    }



//    private fun backupDatabase() {
//        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//        val file = File(downloadDir, "notes.db")
//        val filePath = file.absolutePath
//        val inputFile = FileInputStream(getDatabasePath("notes_database"))
//        val outputFile = FileOutputStream(file)
//        val buffer = ByteArray(1024)
//        var length: Int
//        while (inputFile.read(buffer).also { length = it } > 0) {
//            outputFile.write(buffer, 0, length)
//        }
//        inputFile.close()
//        outputFile.flush()
//        outputFile.close()
//        Toast.makeText(this, "Database exported to Download folder", Toast.LENGTH_SHORT).show()
//        Log.d(TAG, "backupDatabase: Database exported")
//
//        Log.e(TAG, "backupDatabase: ${filePath}")
//    }

//
//    private fun backupDatabase() {
//        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//        val file = File(downloadDir, "notes.db")
//        val filePath = file.absolutePath
//        val inputFile = FileInputStream(getDatabasePath("notes_database"))
//        val outputFile = FileOutputStream(file)
//        val buffer = ByteArray(1024)
//        var length: Int
//        while (inputFile.read(buffer).also { length = it } > 0) {
//            outputFile.write(buffer, 0, length)
//        }
//        inputFile.close()
//        outputFile.flush()
//        outputFile.close()
//        Toast.makeText(this, "Database exported to Download folder", Toast.LENGTH_SHORT).show()
//        Log.d(TAG, "backupDatabase: Database exported")
//
//        Log.e(TAG, "backupDatabase: ${filePath}")
//    }


//    private fun backupDatabase() {
//        val exportDir = File(getFilesDir(), "NotesApp")
//        if (!exportDir.exists()) {
//            exportDir.mkdirs()
//        }
//
//        val file = File(exportDir, "notes.db")
//        val filePath = file.absolutePath
//        val inputFile = FileInputStream(getDatabasePath("notes_database"))
//        val outputFile = FileOutputStream(file)
//        val buffer = ByteArray(1024)
//        var length: Int
//        while (inputFile.read(buffer).also { length = it } > 0) {
//            outputFile.write(buffer, 0, length)
//        }
//        inputFile.close()
//        outputFile.flush()
//        outputFile.close()
//        Toast.makeText(this, "Database exported", Toast.LENGTH_SHORT).show()
//            Log.d(TAG, "backupDatabase: Database exported")
//
//        Log.e(TAG, "backupDatabase: ${filePath}")
//    }


//    private fun backupDatabase() {
//        val exportDir = File(getFilesDir(), "NotesApp")
//        if (!exportDir.exists()) {
//            exportDir.mkdirs()
//        }
//        val file = File(exportDir, "notes.db")
//        val dbFile = getDatabasePath("notes.db")
//        Log.d(TAG, "dbFile: ${dbFile.absolutePath}")
//        if(dbFile.exists()){
//            Log.d(TAG, "dbFile exist")
//            val inputFile = FileInputStream(dbFile)
//            val outputFile = FileOutputStream(file)
//            val buffer = ByteArray(1024)
//            var length: Int
//            while (inputFile.read(buffer).also { length = it } > 0) {
//                outputFile.write(buffer, 0, length)
//            }
//            inputFile.close()
//            outputFile.flush()
//            outputFile.close()
//            Toast.makeText(this, "Database exported", Toast.LENGTH_SHORT).show()
//            Log.d(TAG, "backupDatabase: Database exported")
//        }else{
//            Log.d(TAG, "dbFile not exist")
//        }
//    }

//    private fun backupDatabase() {
//        val exportDir = File(getFilesDir(), "NotesApp")
//        if (!exportDir.exists()) {
//            exportDir.mkdirs()
//        }
//        val file = File(exportDir, "notes.db")
//        val inputFile = FileInputStream(getDatabasePath("notes.db"))
//        val outputFile = FileOutputStream(file)
//        val buffer = ByteArray(1024)
//        var length: Int
//        while (inputFile.read(buffer).also { length = it } > 0) {
//            outputFile.write(buffer, 0, length)
//        }
//        inputFile.close()
//        outputFile.flush()
//        outputFile.close()
//        Toast.makeText(this, "Database exported to internal storage", Toast.LENGTH_SHORT).show()
//        Log.d(TAG, "backupDatabase: Database exported")
//    }

//    private fun backupDatabase() {
//        val exportDir = File(Environment.getExternalStorageDirectory(), "NotesApp")
//        if (!exportDir.exists()) {
//            exportDir.mkdirs()
//        }
//        val file = File(exportDir, "notes.db")
//        val inputFile = FileInputStream(getDatabasePath("notes.db"))
//        val outputFile = FileOutputStream(file)
//        val buffer = ByteArray(1024)
//        var length: Int
//        while (inputFile.read(buffer).also { length = it } > 0) {
//            outputFile.write(buffer, 0, length)
//        }
//        inputFile.close()
//        outputFile.flush()
//        outputFile.close()
//        Toast.makeText(this, "Database exported", Toast.LENGTH_SHORT).show()
//        Log.d(TAG, "backupDatabase: Database exported")
//    }


//    private fun backupDatabase() {
//        val exportDir = File(Environment.getExternalStorageDirectory(), "NotesApp")
//        if (!exportDir.exists()) {
//            exportDir.mkdirs()
//        }
//        val file = File(exportDir, "notes.db")
//        try {
//            file.createNewFile()
//            val fos = FileOutputStream(file)
////            NotesDatabase.getInstance(this).exportSchema(fos)
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                NotesDatabase.getInstance(this).exportSchema(fos)
//            }
//
//            fos.close()
//            Toast.makeText(this, "Database exported", Toast.LENGTH_SHORT).show()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }



 */