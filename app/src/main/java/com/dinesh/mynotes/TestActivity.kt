package com.dinesh.mynotes

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import com.dinesh.mynotes.app.showErrorSnackbar
import com.dinesh.mynotes.room.Note
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.FileInputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.SecretKeySpec


private val TAG = "log_" + TestActivity::class.java.name.split(TestActivity::class.java.name.split(".").toTypedArray()[2] + ".").toTypedArray()[1]


class TestActivity : AppCompatActivity() {
    private lateinit var saveFileLauncher: ActivityResultLauncher<String>
    private lateinit var openFileLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val btnSave = Button(this)
        btnSave.text = "Save"

        saveFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) {
                // Write dummy data to the file
                val fileOutputStream = contentResolver.openOutputStream(uri)
                val notesList = listOf(
                    Note(title = "Note 1", notes = "This is a sample note", dateCreated = LocalDateTime.now(), dateModified = LocalDateTime.now()),
                    Note(title = "Note 2", notes = "This is another sample note", dateCreated = LocalDateTime.now(), dateModified = LocalDateTime.now())
                )
                val gsonBuilder = GsonBuilder()
                gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, object: TypeAdapter<LocalDateTime>() {
                    override fun write(out: JsonWriter?, value: LocalDateTime?) {
                        out?.value(value.toString())
                    }

                    override fun read(input: JsonReader?): LocalDateTime {
                        return LocalDateTime.parse(input?.nextString())
                    }
                })
                val gson = gsonBuilder.create()
                val json = gson.toJson(notesList)

                try {
                    val secretKey = "1234567890123456".toByteArray()
                    val skey = SecretKeySpec(secretKey, 0, secretKey.size, "AES")
                    val cipher = Cipher.getInstance("AES")
                    cipher.init(Cipher.ENCRYPT_MODE, skey)
                    val encrypted = cipher.doFinal(json.toByteArray())

                    fileOutputStream?.let {
                        it.write(encrypted)
                        it.close()
                        Log.d(TAG, "File saved successfully at $uri")
                    } ?: Log.e(TAG, "Failed to save file at $uri")

                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "backupDatabaseToJSON: Error saving backup: ${e.message}")
                    showErrorSnackbar("Error saving backup: ${e.message}")
                }
            } else {
                Log.e(TAG, "No file URI received")
            }
        }

        btnSave.setOnClickListener {
            saveFileLauncher.launch("notes.json")
        }

        setContentView(btnSave)

//        test()
//        test1()
        test2()
    }

    private fun test2() {
        openFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            val cursor = contentResolver.query(uri!!, null, null, null, null)
            cursor?.use {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                val fileName = cursor.getString(nameIndex)
                if (!fileName.endsWith(".json")) {
                    Log.e(TAG, "Error: Only '.json' files are supported")
                    Toast.makeText(this, "Error: Only '.json' files are supported", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                Log.d(TAG, "Selected file: $fileName")
                Log.d(TAG, "Selected file path: ${uri.path}")
                val inputStream = contentResolver.openInputStream(uri)
                inputStream?.use { dataInputStream ->
                    val encryptedData = dataInputStream.readBytes()
                    val secretKey = "1234567890123456".toByteArray()
                    val skey = SecretKeySpec(secretKey, 0, secretKey.size, "AES")
                    val cipher = Cipher.getInstance("AES")
                    cipher.init(Cipher.DECRYPT_MODE, skey)
                    val decrypted = cipher.doFinal(encryptedData)
                    val decryptedString = decrypted.toString(Charsets.UTF_8)

                    val gsonBuilder = GsonBuilder()
                    gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, object: TypeAdapter<LocalDateTime>() {
                        override fun write(out: JsonWriter?, value: LocalDateTime?) {
                            out?.value(value.toString())
                        }

                        override fun read(input: JsonReader?): LocalDateTime {
                            return LocalDateTime.parse(input?.nextString())
                        }
                    })
                    val gson = gsonBuilder.create()
                    val notesList = gson.fromJson(decryptedString, Array<Note>::class.java).toList()

                    Log.d(TAG, "Decrypted notes: $notesList")
                }
            }
        }
    }

    private fun test1() {
        openFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            val cursor = contentResolver.query(uri!!, null, null, null, null)
            cursor?.use {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                val fileName = cursor.getString(nameIndex)
                if (!fileName.endsWith(".json")) {
                    Log.e(TAG, "Error: Only '.json' files are supported")
                    Toast.makeText(this, "Error: Only '.json' files are supported", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                Log.d(TAG, "Selected file: $fileName")
                Log.d(TAG, "Selected file path: ${uri.path}")

                val inputStream = contentResolver.openInputStream(uri)
                inputStream?.use { dataInputStream ->
                    val data = dataInputStream.bufferedReader().readText()
                    val secretKey = "1234567890123456".toByteArray()
                    val skey = SecretKeySpec(secretKey, 0, secretKey.size, "AES")
                    val cipher = Cipher.getInstance("AES")
                    cipher.init(Cipher.DECRYPT_MODE, skey)
                    val decrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

                    val gsonBuilder = GsonBuilder()
                    gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, object: TypeAdapter<LocalDateTime>() {
                        override fun write(out: JsonWriter?, value: LocalDateTime?) {
                            out?.value(value.toString())
                        }

                        override fun read(input: JsonReader?): LocalDateTime {
                            return LocalDateTime.parse(input?.nextString())
                        }
                    })
                    val gson = gsonBuilder.create()
                    val notesList: List<Note> = gson.fromJson(String(decrypted), object: TypeToken<List<Note>>() {}.type)
                    Log.d(TAG, "Decrypted notes: $notesList")
                }
            }
        }
    }

    private fun test() {
        openFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            val cursor = contentResolver.query(uri!!, null, null, null, null)
            cursor?.use {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                val fileName = cursor.getString(nameIndex)
                if (!fileName.endsWith(".json")) {
                    Log.e(TAG, "Error: Only '.json' files are supported")
                    Toast.makeText(this, "Error: Only '.json' files are supported", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                Log.d(TAG, "Selected file: $fileName")
                Log.d(TAG, "Selected file path: ${uri.path}")

                val inputStream = contentResolver.openInputStream(uri)
                inputStream?.use { dataInputStream ->
                    val data = dataInputStream.bufferedReader().readText()
                    val gsonBuilder = GsonBuilder()
                    gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, object : TypeAdapter<LocalDateTime>() {
                        override fun write(out: JsonWriter?, value: LocalDateTime?) {
                            out?.value(value.toString())
                        }

                        override fun read(input: JsonReader?): LocalDateTime {
                            return LocalDateTime.parse(input?.nextString())
                        }
                    })
                    val gson = gsonBuilder.create()
                    val notesList: MutableList<Note> = mutableListOf()

                    val secretKeySpec = SecretKeySpec("1234567890123456".toByteArray(), "AES")
                    val cipher = Cipher.getInstance("AES")
                    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
                    val cipherInputStream = CipherInputStream(contentResolver.openInputStream(uri), cipher)

                    val json = cipherInputStream.bufferedReader().use { it.readText() }
                    notesList.addAll(gson.fromJson(json, Array<Note>::class.java).toList())

                    Log.d(TAG, "Notes List: $notesList")
                    Log.e(TAG, "Notes List: ${notesList.size}")
                } ?: Log.e(TAG, "Failed to read file data")
            }
        }
    }

    override fun onBackPressed() {
        Log.e(TAG, "onBackPressed: ")
        openFileLauncher.launch(arrayOf("*/*"))
    }
}



/*



//class TestActivity : AppCompatActivity() {
//    private lateinit var saveFileLauncher: ActivityResultLauncher<String>
//    private lateinit var openFileLauncher: ActivityResultLauncher<Array<String>>
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val btnSave = Button(this)
//        btnSave.text = "Save"
//
////        saveFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
////            if (uri != null) {
////                // Write dummy data to the file
////                val fileOutputStream = contentResolver.openOutputStream(uri)
////                val notesList = listOf(
////                    Note(title = "Note 1", notes = "This is a sample note", dateCreated = LocalDateTime.now(), dateModified = LocalDateTime.now()),
////                    Note(title = "Note 2", notes = "This is another sample note", dateCreated = LocalDateTime.now(), dateModified = LocalDateTime.now())
////                )
////                val json = Gson().toJson(notesList)
////                fileOutputStream?.let {
////                    it.write(json.toByteArray())
////                    it.close()
////                    Log.d(TAG, "File saved successfully at $uri")
////                } ?: Log.e(TAG, "Failed to save file at $uri")
////            } else {
////                Log.e(TAG, "No file URI received")
////            }
////        }
//
//
//        saveFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
//            if (uri != null) {
//                // Write dummy data to the file
//                val fileOutputStream = contentResolver.openOutputStream(uri)
//                val notesList = listOf(
//                    Note(title = "Note 1", notes = "This is a sample note", dateCreated = LocalDateTime.now(), dateModified = LocalDateTime.now()),
//                    Note(title = "Note 2", notes = "This is another sample note", dateCreated = LocalDateTime.now(), dateModified = LocalDateTime.now())
//                )
//                val gsonBuilder = GsonBuilder()
//                gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, object: TypeAdapter<LocalDateTime>() {
//                    override fun write(out: JsonWriter?, value: LocalDateTime?) {
//                        out?.value(value.toString())
//                    }
//
//                    override fun read(input: JsonReader?): LocalDateTime {
//                        return LocalDateTime.parse(input?.nextString())
//                    }
//                })
//                val gson = gsonBuilder.create()
//                val json = gson.toJson(notesList)
//
//                try {
//                    val secretKey = "1234567890123456".toByteArray()
//                    val skey = SecretKeySpec(secretKey, 0, secretKey.size, "AES")
//                    val cipher = Cipher.getInstance("AES")
//                    cipher.init(Cipher.ENCRYPT_MODE, skey)
//                    val encrypted = cipher.doFinal(json.toByteArray())
//
//
//                    fileOutputStream?.let {
////                                it.write(json.toByteArray())
//                        it.write(encrypted)
//                        it.close()
//                        Log.d(TAG, "File saved successfully at $uri")
//                    } ?: Log.e(TAG, "Failed to save file at $uri")
//
//                } catch (e: java.lang.Exception) {
//                    Log.e(TAG, "backupDatabaseToJSON: Error saving backup: ${e.message}")
//                    showErrorSnackbar("Error saving backup: ${e.message}")
//                }
//            } else {
//                Log.e(TAG, "No file URI received")
//            }
//        }
//
//        btnSave.setOnClickListener {
//            saveFileLauncher.launch("notes.json")
//        }
//
//        setContentView(btnSave)
//
//
////        openFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
////            val cursor = contentResolver.query(uri!!, null, null, null, null)
////            cursor?.use {
////                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
////                cursor.moveToFirst()
////                val fileName = cursor.getString(nameIndex)
////                Log.d(TAG, "Selected file: $fileName")
////                Log.d(TAG, "Selected file path: ${uri.path}")
////            }
////        }
//
//
////        openFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
////            if (uri != null) {
////                val cursor = contentResolver.query(uri, null, null, null, null)
////                cursor?.use {
////                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
////                    cursor.moveToFirst()
////                    val fileName = cursor.getString(nameIndex)
////                    Log.d(TAG, "Selected file: $fileName")
////                    Log.d(TAG, "Selected file path: ${uri.path}")
////                }
////            } else {
////                Log.e(TAG, "No file selected")
////            }
////        }
//
//
////        // Write dummy data to the file
////        val fileName = "notes.json"
////        val fileOutputStream = openFileOutput(fileName, Context.MODE_PRIVATE)
////        val dummyData = "Dummy data to write to file"
////        fileOutputStream.write(dummyData.toByteArray())
////        fileOutputStream.close()
////        Log.d(TAG, "File saved successfully as $fileName")
//
//        openFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
//            val cursor = contentResolver.query(uri!!, null, null, null, null)
//            cursor?.use {
//                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                cursor.moveToFirst()
//                val fileName = cursor.getString(nameIndex)
//                if (!fileName.endsWith(".json")) {
//                    Log.e(TAG, "Error: Only '.json' files are supported")
//                    Toast.makeText(this, "Error: Only '.json' files are supported", Toast.LENGTH_SHORT).show()
//                    return@registerForActivityResult
//                }
//                Log.d(TAG, "Selected file: $fileName")
//                Log.d(TAG, "Selected file path: ${uri.path}")
////                Log.d(TAG, "File saved successfully at $uri")
////                Log.d(TAG, "File saved successfully at ${uri.lastPathSegment}")
//
//                val inputStream = contentResolver.openInputStream(uri)
//                inputStream?.use {dataInputStream ->
//                    val data = dataInputStream.bufferedReader().readText()
//                    val gsonBuilder = GsonBuilder()
//                    gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, object: TypeAdapter<LocalDateTime>() {
//                        override fun write(out: JsonWriter?, value: LocalDateTime?) {
//                            out?.value(value.toString())
//                        }
//
//                        override fun read(input: JsonReader?): LocalDateTime {
//                            return LocalDateTime.parse(input?.nextString())
//                        }
//                    })
//                    val gson = gsonBuilder.create()
//                    val notesList: MutableList<Note> = mutableListOf()
//
//                    val secretKeySpec = SecretKeySpec("1234567890123456".toByteArray(), "AES")
//                    val cipher = Cipher.getInstance("AES")
//                    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
//                    val cipherInputStream = CipherInputStream(contentResolver.openInputStream(uri), cipher)
//
//                    val json = cipherInputStream.bufferedReader().use { it.readText() }
//                    notesList.addAll(gson.fromJson(json, Array<Note>::class.java).toList())
//
//                    Log.d(TAG, "Notes List: $notesList")
//                    Log.e(TAG, "Notes List: ${notesList.size}")
//                } ?: Log.e(TAG, "Failed to read file data")
//            }
//        }
//    }
//
//    private fun testing() {
//        val gsonBuilder = GsonBuilder()
//        gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, object: TypeAdapter<LocalDateTime>() {
//            override fun write(out: JsonWriter?, value: LocalDateTime?) {
//                out?.value(value.toString())
//            }
//
//            override fun read(input: JsonReader?): LocalDateTime {
//                return LocalDateTime.parse(input?.nextString())
//            }
//        })
//        val gson = gsonBuilder.create()
//        val notesList: MutableList<Note> = mutableListOf()
//
//        val fileInputStream = FileInputStream("notes.json")
//
//        val secretKeySpec = SecretKeySpec("1234567890123456".toByteArray(), "AES")
//        val cipher = Cipher.getInstance("AES")
//        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
//        val cipherInputStream = CipherInputStream(fileInputStream, cipher)
//
//        val json = cipherInputStream.bufferedReader().use {
//                    Log.e(TAG, "restoreRvDialog: ${it.readText()}")
//            it.readText()
//        }
//        notesList.addAll(gson.fromJson(json, Array<Note>::class.java).toList())
//    }
//
//    override fun onBackPressed() {
//        Log.e(TAG, "onBackPressed: ")
//        openFileLauncher.launch(arrayOf(""))
//}
//
//
//}



// */



//private fun getRealPathFromURI(contentUri: Uri?): String? {
//    if (contentUri == null) return null
//    var cursor: Cursor? = null
//    return try {
//        val proj = arrayOf(MediaStore.Images.Media.DATA)
//        cursor = contentResolver.query(contentUri, proj, null, null, null)
//        val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//        cursor.moveToFirst()
//        Log.e(TAG, "getRealPathFromURI: ${cursor.getString(column_index)}")
//        cursor.getString(column_index)
//    } finally {
//        cursor?.close()
//    }
//
//
//class TestActivity : AppCompatActivity() {
//    private lateinit var folderPickerLauncher: ActivityResultLauncher<String>
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val button = Button(this)
//        button.text = "Choose Location"
//
//        folderPickerLauncher = registerForActivityResult(CreateDocument("application/json")) { uri ->
//            if (uri != null) {
//            }
//        }
//
//        button.setOnClickListener {
//            folderPickerLauncher.launch("notes.enc")
//        }
//
//        setContentView(button)
//    }
//}
//

private fun test() {
    Log.d(TAG, "test: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("d/MM/yy-HH:mm:ss"))}")
}





//
//
//class TestActivity : AppCompatActivity() {
//    private lateinit var openFile: ActivityResultLauncher<Array<String>>
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        openFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
//            val cursor = contentResolver.query(uri!!, null, null, null, null)
//            cursor?.use {
//                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                cursor.moveToFirst()
//                val fileName = cursor.getString(nameIndex)
//                Log.d(TAG, "Selected file: $fileName")
//                Log.d(TAG, "Selected file path: ${uri.path}")
//
//            }
//        }
//
//    }
//
//    override fun onBackPressed() {
//        Log.e(TAG, "onBackPressed: ")
//        openFile.launch(arrayOf("*/*"))
//    }
//
//
//    private fun getRealPathFromURI(contentUri: Uri?): String? {
//        if (contentUri == null) return null
//        var cursor: Cursor? = null
//        return try {
//            val proj = arrayOf(MediaStore.Images.Media.DATA)
//            cursor = contentResolver.query(contentUri, proj, null, null, null)
//            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//            cursor.moveToFirst()
//            Log.e(TAG, "getRealPathFromURI: ${cursor.getString(column_index)}")
//            cursor.getString(column_index)
//        } finally {
//            cursor?.close()
//        }
//    }
//}
