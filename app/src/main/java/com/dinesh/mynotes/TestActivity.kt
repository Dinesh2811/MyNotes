//package com.dinesh.mynotes
//
//import android.content.ClipData
//import android.content.ClipboardManager
//import android.content.Context
//import android.net.Uri
//import android.os.Bundle
//import android.provider.OpenableColumns
//import androidx.appcompat.app.AppCompatActivity
//import android.util.Log
//import android.view.View
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.widget.SwitchCompat
//import androidx.core.net.toUri
//import androidx.core.widget.addTextChangedListener
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModelProvider
//import com.dinesh.mynotes.app.showErrorSnackbar
//import com.dinesh.mynotes.databinding.ActivityMainBinding
//import com.dinesh.mynotes.room.Note
//import com.dinesh.mynotes.room.NotesViewModel
//import com.google.android.material.textfield.TextInputEditText
//import com.google.android.material.textfield.TextInputLayout
//import com.google.gson.Gson
//import com.google.gson.GsonBuilder
//import com.google.gson.TypeAdapter
//import com.google.gson.reflect.TypeToken
//import com.google.gson.stream.JsonReader
//import com.google.gson.stream.JsonWriter
//import com.google.zxing.BarcodeFormat
//import com.google.zxing.MultiFormatWriter
//import com.google.zxing.WriterException
//import com.journeyapps.barcodescanner.BarcodeEncoder
//import java.io.FileInputStream
//import java.io.IOException
//import java.security.SecureRandom
//import java.time.LocalDateTime
//import java.time.format.DateTimeFormatter
//import javax.crypto.Cipher
//import javax.crypto.CipherInputStream
//import javax.crypto.KeyGenerator
//import javax.crypto.spec.GCMParameterSpec
//import javax.crypto.spec.SecretKeySpec
//
////import org.apache.commons.codec.binary.Base64
//
//private val TAG = "log_" + TestActivity::class.java.name.split(TestActivity::class.java.name.split(".").toTypedArray()[2] + ".").toTypedArray()[1]
//
//
//class TestActivity : AppCompatActivity() {
//    private lateinit var saveFileLauncher: ActivityResultLauncher<String>
//    private lateinit var openFileLauncher: ActivityResultLauncher<Array<String>>
//    private lateinit var binding: ActivityMainBinding
//    private lateinit var notesViewModel: NotesViewModel
//    var notesLiveList: LiveData<List<Note>> = MutableLiveData()
//    lateinit var notesList: List<Note>
//    var encryptionKey = "QRY9fqKaBlsBJZLoUNfOZg=="
////    var uri = "content://com.android.providers.downloads.documents/document/raw%3A%2Fstorage%2Femulated%2F0%2FDownload%2FMyNote%2Fnotes.enc.json".toUri()
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val btnSave = Button(this)
//        btnSave.text = "Save"
//        btnSave.setOnClickListener {
//            saveFileLauncher.launch("notes.enc.json")
//        }
//
//        setContentView(btnSave)
//
//
//        notesViewModel = ViewModelProvider(this)[NotesViewModel::class.java]
//        notesLiveList = notesViewModel.getAllNotes()
//
//        notesLiveList.observe(this) {
////            Log.d(TAG, "onCreate: ${it}")
//            Log.d(TAG, "onCreate: ${it.size}")
//            notesList = it
//        }
//
//
//        backupLauncher()
//        openFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
//            val cursor = uri?.let { contentResolver.query(it, null, null, null, null) }
//            cursor?.use {
//                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                cursor.moveToFirst()
//                Log.d(TAG, "Selected file: ${cursor.getString(nameIndex)}")
//                Log.d(TAG, "Selected file path: ${uri.path}")
//
//                val inputStream = contentResolver.openInputStream(uri)
//                inputStream?.use { inputStream ->
//                    val data = inputStream.bufferedReader().readText()
//                    restoreRvDialog(uri)
//                }
//            }
//        }
//    }
//
//    override fun onBackPressed() {
//        Log.e(TAG, "onBackPressed: ")
//        openFileLauncher.launch(arrayOf("*/*"))
//
//
//
//
//    }
//
//
//    //   Test 1
//    private fun backupLauncher() {
//        saveFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
//
//            if (uri != null) {
//                val fileOutputStream = contentResolver.openOutputStream(uri)
//                val notesList = notesLiveList.value
//                if (!notesList.isNullOrEmpty()) {
//                    val gsonBuilder = GsonBuilder()
//                    gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, object : TypeAdapter<LocalDateTime>() {
//                        override fun write(out: JsonWriter?, value: LocalDateTime?) {
//                            out?.value(value.toString())
//                        }
//
//                        override fun read(input: JsonReader?): LocalDateTime {
//                            return LocalDateTime.parse(input?.nextString())
//                        }
//                    })
//                    val gson = gsonBuilder.create()
//                    val json = gson.toJson(notesList)
//
//                    try {
//                        val secretKey = encryptionKey.toByteArray()
//                        val skey = SecretKeySpec(secretKey, "AES")
//                        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
//                        val iv = ByteArray(cipher.blockSize)
//                        val random = SecureRandom()
//                        random.nextBytes(iv)
//                        val ivSpec = GCMParameterSpec(128, iv)
//                        cipher.init(Cipher.ENCRYPT_MODE, skey, ivSpec)
//                        val encrypted = cipher.doFinal(json.toByteArray())
//                        fileOutputStream?.let {
//                            it.write(iv)
//                            it.write(encrypted)
//                            it.close()
//                            Log.d(TAG, "File saved successfully at $uri")
//                        } ?: Log.e(TAG, "Failed to save file at $uri")
//                    } catch (e: Exception) {
//                        Log.e(TAG, "backupDatabaseToJSON: Error saving backup: ${e.message}")
//                    }
//                } else {
//                    Log.e(TAG, "backupDatabaseToJSON: No notes to backup")
//                }
//            }
//        }
//    }
//    private fun restoreRvDialog(uri: Uri, selectedFileToRestore: String = "", decryptKey: String = "QRY9fqKaBlsBJZLoUNfOZg==") {
//        val gsonBuilder = GsonBuilder()
//        gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, object : TypeAdapter<LocalDateTime>() {
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
//        val inputStream = contentResolver.openInputStream(uri)
//        try {
//            inputStream?.use { input ->
//                val iv = ByteArray(12)
//                input.read(iv)
//                val encryptedData = input.readBytes()
//                val secretKey = Base64.decodeBase64(decryptKey)
//                val skey = SecretKeySpec(secretKey, "AES")
//                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
//                val ivSpec = GCMParameterSpec(128, iv)
//                cipher.init(Cipher.DECRYPT_MODE, skey, ivSpec)
//                val decryptedData = cipher.doFinal(encryptedData)
//                val decryptedString = String(decryptedData, Charsets.UTF_8)
//
//                val decryptedNotesList = gson.fromJson(decryptedString, Array<Note>::class.java).toList()
//
//                Log.d(TAG, "Decrypted notes: $decryptedNotesList")
//                notesList.addAll(decryptedNotesList)
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "restoreRvDialog: ${e.message}")
//        }
//    }
//
//
////    private fun backup() {
////        val keyGenerator = KeyGenerator.getInstance("AES")
////        keyGenerator.init(128)
////        val secretKey = keyGenerator.generateKey()
////        val key = secretKey.encoded
////        encryptionKey = Base64.encodeBase64String(key);
////    }
//
//}
//
