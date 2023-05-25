package com.dinesh.mynotes.test

import android.accounts.AccountManager.KEY_PASSWORD
import android.app.Dialog
import android.content.*
import android.hardware.biometrics.BiometricPrompt
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.provider.OpenableColumns
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dinesh.mynotes.R
import com.dinesh.mynotes.app.FancyDialog
import com.dinesh.mynotes.app.showErrorSnackbar
import com.dinesh.mynotes.app.showSnackbar
import com.dinesh.mynotes.room.Note
import com.dinesh.mynotes.room.NotesViewModel
import com.dinesh.mynotes.rv.RestoreAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.lang.reflect.Type
import java.security.Key
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.callback.CallbackHandler

class BackupNotes : AppCompatActivity() {
    private val TAG = "log_" + BackupNotes::class.java.name.split(BackupNotes::class.java.name.split(".").toTypedArray()[2] + ".").toTypedArray()[1]

    private lateinit var notesViewModel: NotesViewModel
    var notesLiveList: LiveData<List<Note>> = MutableLiveData()
    private lateinit var note: Note
    private lateinit var notesList: List<Note>
    private lateinit var notesJson: String
    private lateinit var encryptedData: ByteArray

    companion object {
        private const val AES_MODE = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
        private const val AES_KEY_SIZE = 256
    }

    private lateinit var ENCRYPTION_KEY: Key

    private lateinit var saveFileLauncher: ActivityResultLauncher<String>
    private lateinit var openFileLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notesViewModel = ViewModelProvider(this)[NotesViewModel::class.java]
        notesLiveList = notesViewModel.getAllNotes()

        notesLiveList.observe(this) {
            notesList = it

            val gsonBuilder = GsonBuilder()
            gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
            gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
            val gsonWithCustomSerializer = gsonBuilder.create()
            notesJson = gsonWithCustomSerializer.toJson(notesList)

//            ENCRYPTION_KEY = stringToKey("2x2+69Nf9M9av+IqaEi3A10jMvt6CWt3Fvm6bAD4s2I=")
//            encryptedData = encryptDataAsString(notesJson)
//            Log.e(TAG, "Encrypted Data: ${Base64.encodeToString(encryptedData, Base64.DEFAULT)}")
//            Log.e(TAG, "Decrypted Data: ${decryptDataAsString(encryptedData)}")
//            Log.i(TAG, "Decrypted Data: ${decryptDataAsList(encryptedData)}")

//            convertListToJson()


//            // Calculate SHA-256 hash value of the data
//            val messageDigest = MessageDigest.getInstance("SHA-256")
////            val hash = messageDigest.digest(notesJson.toByteArray())
//            val hash = messageDigest.digest(encryptedData)
//            Log.i(TAG, "Hashed data: ${hash.contentToString()}")
        }

//        ENCRYPTION_KEY = getEncryptionKey()
//        var encryptionKeyAsString: String = Base64.encodeToString(ENCRYPTION_KEY.encoded, Base64.DEFAULT)
//        Log.e(TAG, "onCreate: $encryptionKeyAsString")
////        Log.e(TAG, "onCreate: ${encryptionKeyAsString.length}")
//        val decryptedEncryptionKey = stringToKey(encryptionKeyAsString)
//        Log.i(TAG, "onCreate: ${decryptedEncryptionKey}")


        val btn = Button(this)
//        btn.text = "backup"
        btn.text = "restore"
        btn.setOnClickListener {
//            saveFileLauncher.launch("notes")
//            saveFileLauncher.launch("notes.json")
//            saveFileLauncher.launch("notes.enc.json")
            openFileLauncher.launch(arrayOf("application/json"))
//            backup()
        }

        setContentView(btn)
        backupLauncher()
        restoreLauncher()
    }

    override fun onBackPressed() {
//            openFileLauncher.launch(arrayOf("application/json"))
        backup()
    }



    private fun restoreLauncher() {
        openFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri == null) {
                Log.e(TAG, "Uri is null, the file selection has been cancelled")
                return@registerForActivityResult
            }
            isEncrypted = false

            try {
                uri.let {
                    val file = DocumentFile.fromSingleUri(this, it)
                    val fileName = file?.name
                    Log.d(TAG, "File name: $fileName")

                    val inputStream = contentResolver.openInputStream(it)
                    inputStream?.let {
                        val byteArray = inputStream.readBytes()
                        var restoreData: String = "[{}]"

                        if (fileName != null) {
                            if (fileName.contains(".enc") && fileName.contains(".json")) {
                                /**
                                 *
                                 */

                                isEncrypted = true

                                val encryptedBuilder = AlertDialog.Builder(this)
                                val encryptedInflater = layoutInflater
                                val encryptedView = encryptedInflater.inflate(R.layout.dialog_to_enter_decryption_key, null)
                                encryptedBuilder.setView(encryptedView)
                                val encryptedDialog = encryptedBuilder.create()
                                encryptedDialog.show()

                                val btnDecryptionKey = encryptedView.findViewById<Button>(R.id.btnDecryptionKey)
                                val btnCancelDecryption = encryptedView.findViewById<Button>(R.id.btnCancelDecryption)
                                val etDecryptionKeyInputLayout = encryptedView.findViewById<TextInputLayout>(R.id.etDecryptionKeyInputLayout)
                                val etDecryptionKey = encryptedView.findViewById<TextInputEditText>(R.id.etDecryptionKey)

                                etDecryptionKeyInputLayout.setEndIconOnClickListener {
                                    val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
                                        val text = clipboard.primaryClip?.getItemAt(0)?.text.toString()
                                        etDecryptionKey.setText(text)
                                        etDecryptionKey.setSelection(etDecryptionKey.length())
                                        etDecryptionKey.requestFocus()
                                    }
                                }

                                etDecryptionKey.addTextChangedListener {
                                    if (etDecryptionKey.text.toString().length == 24) {
                                        etDecryptionKeyInputLayout.isHelperTextEnabled = false
                                    } else {
                                        etDecryptionKeyInputLayout.isHelperTextEnabled = true
                                        etDecryptionKeyInputLayout.helperText = "*Decryption key must have 45 character"
                                    }
                                }

                                btnDecryptionKey.setOnClickListener {
                                    if (etDecryptionKey.text.toString().trim().length > 15) {
//                                        restoreRvDialog(data, uri, etDecryptionKey.text.toString().trim())
                                        encryptedDialog.cancel()
                                    }
                                }

                                btnCancelDecryption.setOnClickListener {
                                    encryptedDialog.cancel()
                                }






                                /**
                                 *
                                 */

                                encryptionKey = "MPUIbWrxZjEQjlgnjzLpvi9Ll+QPaOTfma6eiRORrVc="
                                restoreData = decryptDataAsString(byteArray,encryptionKey)
                                if (!validateEncryptedData(byteArray, restoreData,encryptionKey)) {
                                    Log.e(TAG, "The encrypted data in the file has been changed")
                                    return@registerForActivityResult
                                } else {
                                    Log.i(TAG, "The encrypted data in the file hasn't changed")
                                }
                            } else if (fileName.contains(".json")) {
                                restoreData = byteArray.toString(Charsets.UTF_8)
                            }
                        }
//                        Log.e(TAG, "restoreLauncher: ${convertStringToList(restoreData)}")
                        restoreRvDialog(convertStringToList(restoreData))
                    }
                    inputStream?.close()
                } ?: Log.e(TAG, "Uri is null")
            } catch (e: Exception) {
                Log.e(TAG, "Error while reading the file: ${e.message}")
            }

        }
    }


    private fun restoreRvDialog(listOfNotes: List<Note>) {
        Log.e(TAG, "restoreRvDialog: ${listOfNotes}")

        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.dialog_restore_rv, null)
        builder.setView(view)
        val dialog = builder.create()

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val btnOk = view.findViewById<Button>(R.id.btnOk)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val checkBoxForAll = view.findViewById<CheckBox>(R.id.checkBoxForAll)

        val adapter = RestoreAdapter(listOfNotes)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter.selectedNotesLiveData.observe(this) {
            if (!it.isNullOrEmpty()) {
                if (it.size == listOfNotes.size) {
                    checkBoxForAll.isChecked = true
                    checkBoxForAll.text = "UnSelect All"
                } else {
                    checkBoxForAll.isChecked = false
                    checkBoxForAll.text = "Select All"
                }
            }
        }

        checkBoxForAll.setOnClickListener {
            val isChecked = checkBoxForAll.isChecked
            adapter.updateSelection(isChecked)
            if (isChecked) {
                checkBoxForAll.text = "UnSelect All"
            } else {
                checkBoxForAll.text = "Select All"
            }

        }

        if (checkBoxForAll.isChecked) {
            checkBoxForAll.text = "UnSelect All"
        } else {
            checkBoxForAll.text = "Select All"
        }

        btnOk.setOnClickListener {
            Log.d(TAG, "btnOk: ${adapter.getSelectedNotes()}")
            val selectedNotesList: List<Note> = adapter.getSelectedNotes()
            if (!selectedNotesList.isNullOrEmpty()) {
                var noteID = 0L
                var noteInsertedCount = 0

                val dialog = Dialog(this)
                dialog.setContentView(R.layout.progress_bar)
                dialog.setCancelable(false)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()

                @Suppress("SENSELESS_COMPARISON")
                lifecycleScope.launch(Dispatchers.IO) {
                    noteID = notesViewModel.getMaxId()
                    selectedNotesList.forEach {
                        noteID = noteID.plus(1L)
                        val note = it
                        note.id = noteID
                        val existingNote = notesViewModel.getNoteByTitleAndDescriptionAsNote(it.title, it.notes)
                        if (existingNote == null) {
                            notesViewModel.insert(note)
                            noteInsertedCount = noteInsertedCount.plus(1)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        showSnackbar("$noteInsertedCount Notes has been restored", Snackbar.LENGTH_LONG)
                        dialog.cancel()
                    }
                }
            }
            dialog.cancel()
        }

        btnCancel.setOnClickListener { dialog.cancel() }

        if (!listOfNotes.isNullOrEmpty()) {
            dialog.show()
        } else {
            restoreErrorDialog("Error")
        }
    }
    private fun restoreErrorDialog(Title: String = getString(R.string.app_name)) {
        val dialog = FancyDialog(this)
            .setTitle(Title)
            .setMessage(
                "Make sure the backup file is not tampered & if you're decrypting then the file name should be '*.enc.json' and check if have entered correct decryption key. ('*' can be any valid file name) " +
                        "\n\n\n " +
                        "If you haven't made any changes manually to the backup file and if you still face this error again then please immediately contact the developer.\n\n" +
                        "You can contact the developer via email at dk2811testmail@gmail.com"
            )
            .setPositiveButton("Contact", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("dk2811testmail@gmail.com"))
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                    intent.type = "message/rfc822"
                    startActivity(Intent.createChooser(intent, "Choose an Email client :"))
                    dialog.cancel()
                }
            })
            .setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.cancel()
                }
            })
        dialog.show()
    }




//    private fun restoreLauncher() {
//        openFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
//            if (uri == null) {
//                Log.e(TAG, "Uri is null, the file selection has been cancelled")
//                return@registerForActivityResult
//            }
//
//            try {
//                uri.let {
//                    val file = DocumentFile.fromSingleUri(this, it)
//                    val fileName = file?.name
//                    Log.d(TAG, "File name: $fileName")
//
//                    val inputStream = contentResolver.openInputStream(it)
//                    inputStream?.let {
//                        val byteArray = inputStream.readBytes()
//                        var restoreData: String = "[{}]"
//
//                        if (fileName != null) {
//                            if (fileName.contains(".enc") && fileName.contains(".json")) {
//                                encryptionKey = "MPUIbWrxZjEQjlgnjzLpvi9Ll+QPaOTfma6eiRORrVc="
//                                restoreData = decryptDataAsString(byteArray,encryptionKey)
//                                if (!validateEncryptedData(byteArray, restoreData,encryptionKey)) {
//                                    Log.e(TAG, "The encrypted data in the file has been changed")
//                                    return@registerForActivityResult
//                                } else {
//                                    Log.i(TAG, "The encrypted data in the file hasn't changed")
//                                }
//                            } else if (fileName.contains(".json")) {
//                                restoreData = byteArray.toString(Charsets.UTF_8)
//                            }
//                        }
//                        Log.e(TAG, "restoreLauncher: ${convertStringToList(restoreData)}")
//                    }
//                    inputStream?.close()
//                } ?: Log.e(TAG, "Uri is null")
//            } catch (e: Exception) {
//                Log.e(TAG, "Error while reading the file: ${e.message}")
//            }
//
//        }
//    }

    private fun backupLauncher() {
        saveFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) {
                val file = DocumentFile.fromSingleUri(this, uri)
                val fileName = file?.name
                Log.d(TAG, "File name: $fileName")

                if (fileName != null) {
                    if (isEncrypted) {
                        if (!((fileName.contains(".enc")) && (fileName.contains(".json")))) {
                            Log.e(TAG, "Error: file name doesn't end with '.enc.json', deleting the file")
                            file.delete()
                            return@registerForActivityResult
                        }
                    } else {
                        if (!((fileName.contains(".enc")) && (fileName.contains(".json")))) {
                            Log.e(TAG, "Error: file name doesn't end with '.json' or has '.enc', deleting the file")
                            file.delete()
                            return@registerForActivityResult
                        }
                    }
                }

                val gsonBuilder = GsonBuilder()
                gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
                gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
                val gsonWithCustomSerializer = gsonBuilder.create()

                if (!notesList.isNullOrEmpty()) {
                    notesJson = gsonWithCustomSerializer.toJson(notesList)

                    try {
                        contentResolver.openOutputStream(uri)?.use { outputStream ->
//                        //  .json
//                        outputStream.write(notesJson.toByteArray())
//
////                        //  .enc.json
//                        outputStream.write(encryptedData)

                            if (isEncrypted) {
                                encryptionKey = Base64.encodeToString(ENCRYPTION_KEY.encoded, Base64.DEFAULT)
                                Log.e(TAG, "backupLauncher: $encryptionKey")

                                val cipher = Cipher.getInstance(AES_MODE)
                                cipher.init(Cipher.ENCRYPT_MODE, ENCRYPTION_KEY, getGCMParameterSpec())
                                encryptedData = cipher.doFinal(notesJson.toByteArray(Charsets.UTF_8))

                                outputStream.write(encryptedData)
                            } else {
                                outputStream.write(notesJson.toByteArray())
                            }

                        }
                        Log.i(TAG, "File successfully created at: $uri")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving backup: ${e.message}")
                    }
                } else {
                    Log.e(TAG, "backupDatabaseToJSON: No notes to backup")
                }
            } else {
                Log.e(TAG, "Error creating file")
            }
        }
    }


    var encryptionKey = "2x2+69Nf9M9av+IqaEi3A10jMvt6CWt3Fvm6bAD4s2I="
    var isEncrypted = false
    private fun backup() {
        isEncrypted = false
        encryptionKey = "2x2+69Nf9M9av+IqaEi3A10jMvt6CWt3Fvm6bAD4s2I="
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.dialog_save, null)
        builder.setView(view)
        val dialog = builder.create()
        dialog.show()
//        val etFileName = view.findViewById<EditText>(R.id.etFileName)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnCancelBackup = view.findViewById<Button>(R.id.btnCancelBackup)
        val switch1 = view.findViewById<SwitchCompat>(R.id.switch1)
        val qrCodeImage = view.findViewById<ImageView>(R.id.qrCodeImage)
        val tvEncryptionKey = view.findViewById<TextView>(R.id.tvEncryptionKey)
        val etFileNameInputLayout = view.findViewById<TextInputLayout>(R.id.etFileNameInputLayout)
        val etFileName = view.findViewById<TextInputEditText>(R.id.etFileName)
        val tvUserMsg = view.findViewById<TextView>(R.id.tvUserMsg)

        etFileName.addTextChangedListener {
            if (etFileName.text.toString().isNotEmpty()) {
                etFileNameInputLayout.isHelperTextEnabled = false
            } else {
                etFileNameInputLayout.isHelperTextEnabled = true
                etFileNameInputLayout.helperText = "*Default file name will be 'notes'"
            }
        }

        switch1.text = "Toggle to encrypt the Backup"
        qrCodeImage.visibility = View.GONE
        tvEncryptionKey.visibility = View.GONE
        tvUserMsg.visibility = View.GONE

        switch1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switch1.text = "Backup will be encrypted"
                isEncrypted = true

//                val gsonBuilder = GsonBuilder()
//                gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
//                gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
//                val gsonWithCustomSerializer = gsonBuilder.create()
//                notesJson = gsonWithCustomSerializer.toJson(notesList)

//                val keyGen = KeyGenerator.getInstance("AES")
//                keyGen.init(AES_KEY_SIZE)
                getEncryptionKey()
                encryptionKey = Base64.encodeToString(ENCRYPTION_KEY.encoded, Base64.DEFAULT)

//                val cipher = Cipher.getInstance(AES_MODE)
//                cipher.init(Cipher.ENCRYPT_MODE, ENCRYPTION_KEY, getGCMParameterSpec())
//                encryptionKey = Base64.encodeToString(ENCRYPTION_KEY.encoded, Base64.DEFAULT)
                Log.e(TAG, "backup: $encryptionKey")
//
//                encryptedData = cipher.doFinal(notesJson.toByteArray(Charsets.UTF_8))

                tvEncryptionKey.text = "Copy the encryption key\n$encryptionKey"
                tvEncryptionKey.setOnClickListener {
                    val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("keyBase64", encryptionKey)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, "Text copied to clipboard: $encryptionKey", Toast.LENGTH_SHORT).show()
                }
                tvEncryptionKey.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_content_copy_24, 0)
                tvEncryptionKey.compoundDrawablePadding = 32

                try {
                    val bitMatrix = MultiFormatWriter().encode(encryptionKey, BarcodeFormat.QR_CODE, 250, 250)
                    val barcodeEncoder = BarcodeEncoder()
                    val bitmap = barcodeEncoder.createBitmap(bitMatrix)
                    qrCodeImage.setImageBitmap(bitmap)
                } catch (e: WriterException) {
                    e.printStackTrace()
                }

                qrCodeImage.visibility = View.VISIBLE
                tvEncryptionKey.visibility = View.VISIBLE
                tvUserMsg.visibility = View.VISIBLE

            } else {
                encryptionKey = "2x2+69Nf9M9av+IqaEi3A10jMvt6CWt3Fvm6bAD4s2I="
                isEncrypted = false
                switch1.text = "Toggle to encrypt the Backup"
                qrCodeImage.visibility = View.GONE
                tvEncryptionKey.visibility = View.GONE
                tvUserMsg.visibility = View.GONE
            }
        }

        btnSave.setOnClickListener {
            if (!notesList.isNullOrEmpty()) {
                if (isEncrypted) {
                    if (etFileName.text.toString().trim().isNotEmpty()) {
                        saveFileLauncher.launch(etFileName.text.toString() + ".enc.json")
                    } else {
                        saveFileLauncher.launch("notes.enc.json")
                    }
                } else {
                    if (etFileName.text.toString().trim().isNotEmpty()) {
                        saveFileLauncher.launch(etFileName.text.toString() + ".json")
                    } else {
                        saveFileLauncher.launch("notes.json")
                    }
                }
            } else {
                Log.e(TAG, "backupDatabaseToJSON: No notes to backup")
                showErrorSnackbar("No notes to backup")
            }
            dialog.cancel()
        }

        btnCancelBackup.setOnClickListener {
            dialog.cancel()
        }
    }


    private fun stringToKey(encryptionKeyString: String): Key {
        val encryptionKeyBytes = Base64.decode(encryptionKeyString, Base64.DEFAULT)
        return SecretKeySpec(encryptionKeyBytes, 0, encryptionKeyBytes.size, "AES")
    }

    private fun encryptDataAsString(listOfNotesAsString: String): ByteArray {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(AES_KEY_SIZE)
//        getEncryptionKey()
        ENCRYPTION_KEY = keyGen.generateKey()

        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, ENCRYPTION_KEY, getGCMParameterSpec())

        var encryptionKeyAsString: String = Base64.encodeToString(ENCRYPTION_KEY.encoded, Base64.DEFAULT)
//        Log.e(TAG, "onCreate: $encryptionKeyAsString")

        return cipher.doFinal(listOfNotesAsString.toByteArray(Charsets.UTF_8))
    }

//    private fun decryptDataAsString(encryptedData: ByteArray, Key: String = "2x2+69Nf9M9av+IqaEi3A10jMvt6CWt3Fvm6bAD4s2I="): String {
    private fun decryptDataAsString(encryptedData: ByteArray, Key: String): String {
        val encryptionKeyBytes = Base64.decode(Key, Base64.DEFAULT)
        val decryptionKey = SecretKeySpec(encryptionKeyBytes, 0, encryptionKeyBytes.size, "AES")

        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.DECRYPT_MODE, decryptionKey, getGCMParameterSpec())
        return String(cipher.doFinal(encryptedData), Charsets.UTF_8)
    }

//    private fun decryptDataAsList(encryptedData: ByteArray): List<Note> {
//        val gsonBuilder = GsonBuilder()
//        gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
//        gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
//        val gsonWithCustomSerializer = gsonBuilder.create()
//
//        val notesType = object : TypeToken<List<Note>>() {}.type
//        return gsonWithCustomSerializer.fromJson(decryptDataAsString(encryptedData), notesType)
//    }


    private fun getEncryptionKey(): Key {
//        return SecretKeySpec(ENCRYPTION_KEY.toByteArray(Charsets.UTF_8), "AES")
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(AES_KEY_SIZE)
        ENCRYPTION_KEY = keyGen.generateKey()
        return ENCRYPTION_KEY
    }

    private fun getGCMParameterSpec(): GCMParameterSpec {
        val iv = ByteArray(GCM_IV_LENGTH)
        return GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
    }

    private fun validateEncryptedData(encryptedData: ByteArray, decryptedData: String, decryptionKey: String): Boolean {
        val encryptionKeyBytes = Base64.decode(decryptionKey, Base64.DEFAULT)
        val DECRYPTION_KEY = SecretKeySpec(encryptionKeyBytes, 0, encryptionKeyBytes.size, "AES")

        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, DECRYPTION_KEY, getGCMParameterSpec())
        val encryptedDataCheck = cipher.doFinal(decryptedData.toByteArray(Charsets.UTF_8))

        return encryptedData.contentEquals(encryptedDataCheck)
    }

    private fun convertStringToList(restoreData: String): List<Note> {
        //  convertListToJson
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
        gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
        val gsonWithCustomSerializer = gsonBuilder.create()
//        val notesJson = gsonWithCustomSerializer.toJson(listOfNotes)

        //  convertJsonToList
        val notesType = object : TypeToken<List<Note>>() {}.type
        val notesList = gsonWithCustomSerializer.fromJson<List<Note>>(restoreData, notesType)

//        Log.e(TAG, notesJson)
//        Log.d(TAG, "notesList -> $notesList")
        return notesList
    }

//    private fun convertListToJson(listOfNotes: List<Note>) {
//        //  convertListToJson
//        val gsonBuilder = GsonBuilder()
//        gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
//        gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
//        val gsonWithCustomSerializer = gsonBuilder.create()
//        val notesJson = gsonWithCustomSerializer.toJson(listOfNotes)
//
//        //  convertJsonToList
//        val notesType = object : TypeToken<List<Note>>() {}.type
//        val notesList = gsonWithCustomSerializer.fromJson<List<Note>>(notesJson, notesType)
//
//        Log.e(TAG, notesJson)
//        Log.d(TAG, "notesList -> $notesList")
//    }
}


class LocalDateTimeSerializer : JsonSerializer<LocalDateTime> {
    override fun serialize(src: LocalDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src.toString())
    }
}

class LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDateTime {
        return LocalDateTime.parse(json?.asString)
    }
}


/*


class BackupNotes : AppCompatActivity() {
    private val TAG = "log_" + BackupNotes::class.java.name.split(BackupNotes::class.java.name.split(".").toTypedArray()[2] + ".").toTypedArray()[1]

    private lateinit var notesViewModel: NotesViewModel
    var notesLiveList: LiveData<List<Note>> = MutableLiveData()
    private lateinit var note: Note
    private lateinit var notesList: List<Note>
    private lateinit var notesJson: String
    private lateinit var encryptedData: ByteArray

    companion object {
        private const val AES_MODE = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
        private const val AES_KEY_SIZE = 256
    }
    private lateinit var ENCRYPTION_KEY : Key

    private lateinit var saveFileLauncher: ActivityResultLauncher<String>
    private lateinit var openFileLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notesViewModel = ViewModelProvider(this)[NotesViewModel::class.java]
        notesLiveList = notesViewModel.getAllNotes()

        notesLiveList.observe(this) {
            notesList = it

            val gsonBuilder = GsonBuilder()
            gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
            gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
            val gsonWithCustomSerializer = gsonBuilder.create()
            notesJson = gsonWithCustomSerializer.toJson(notesList)

            ENCRYPTION_KEY = stringToKey("2x2+69Nf9M9av+IqaEi3A10jMvt6CWt3Fvm6bAD4s2I=")
            encryptedData = encryptDataAsString(notesJson)
//            Log.e(TAG, "Encrypted Data: ${Base64.encodeToString(encryptedData, Base64.DEFAULT)}")
//            Log.e(TAG, "Decrypted Data: ${decryptDataAsString(encryptedData)}")
//            Log.i(TAG, "Decrypted Data: ${decryptDataAsList(encryptedData)}")

//            convertListToJson()



            // Calculate SHA-256 hash value of the data
            val messageDigest = MessageDigest.getInstance("SHA-256")
//            val hash = messageDigest.digest(notesJson.toByteArray())
            val hash = messageDigest.digest(encryptedData)
            Log.i(TAG, "Hashed data: ${hash.contentToString()}")
        }

//        ENCRYPTION_KEY = getEncryptionKey()
//        var encryptionKeyAsString: String = Base64.encodeToString(ENCRYPTION_KEY.encoded, Base64.DEFAULT)
//        Log.e(TAG, "onCreate: $encryptionKeyAsString")
////        Log.e(TAG, "onCreate: ${encryptionKeyAsString.length}")
//        val decryptedEncryptionKey = stringToKey(encryptionKeyAsString)
//        Log.i(TAG, "onCreate: ${decryptedEncryptionKey}")


        val btn = Button(this)
        btn.text = "restore"
        btn.setOnClickListener {
//            saveFileLauncher.launch("notes")
//            saveFileLauncher.launch("notes.json")
//            saveFileLauncher.launch("notes.enc.json")
            openFileLauncher.launch(arrayOf("application/json"))
        }

        setContentView(btn)
        backupLauncher()
        restoreLauncher()
    }

    private fun restoreLauncher() {
        openFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri == null) {
                Log.e(TAG, "Uri is null, the file selection has been cancelled")
                return@registerForActivityResult
            }

            try {
                uri.let {
                    val file = DocumentFile.fromSingleUri(this, it)
                    val fileName = file?.name
                    Log.d(TAG, "File name: $fileName")

                    val inputStream = contentResolver.openInputStream(it)
                    inputStream?.let {
                        val byteArray = inputStream.readBytes()
                        var restoreData: String = "[{}]"

                        if (fileName != null) {
                            if (fileName.contains(".json")) {
//                            if (fileName.contains(".enc") && fileName.contains(".json")) {
                                restoreData = decryptDataAsString(byteArray)
                                if (!validateEncryptedData(byteArray, restoreData)) {
                                    Log.e(TAG, "The encrypted data in the file has been changed")
                                    return@registerForActivityResult
                                } else{
                                    Log.i(TAG, "The encrypted data in the file hasn't changed")
                                }
                            }
//                            else if (fileName.contains(".json")) {
//                                restoreData = byteArray.toString(Charsets.UTF_8)
//                            }
                        }
                        Log.e(TAG, "restoreLauncher: ${convertStringToList(restoreData)}")
                    }
                    inputStream?.close()
                } ?: Log.e(TAG, "Uri is null")
            } catch (e: Exception) {
                Log.e(TAG, "Error while reading the file: ${e.message}")
            }

        }
    }

    private fun backupLauncher() {
        saveFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) {
                val file = DocumentFile.fromSingleUri(this, uri)
                val fileName = file?.name
                Log.d(TAG, "File name: $fileName")

//                if (fileName != null) {
//                        if (!fileName.contains(".enc")){
//                        Log.e(TAG, "Error: file name doesn't end with '.json', deleting the file")
//                        file?.delete()
//                        return@registerForActivityResult
//                    }
//                }

                // Calculate SHA-256 hash value of the data
                val messageDigest = MessageDigest.getInstance("SHA-256")
                val hash = messageDigest.digest(notesJson.toByteArray())
                Log.i(TAG, "Hashed data: ${hash.contentToString()}")

                try {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        //  .json
                        outputStream.write(notesJson.toByteArray())

//                        //  .enc.json
                        outputStream.write(encryptedData)
                    }
                    Log.i(TAG, "File successfully created at: $uri")
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving backup: ${e.message}")
                }
            } else {
                Log.e(TAG, "Error creating file")
            }
        }
    }


    var encryptionKey = "QRY9fqKaBlsBJZLoUNfOZg=="
    var isEncrypted = false
    private fun backup() {
        isEncrypted = false
        encryptionKey = "QRY9fqKaBlsBJZLoUNfOZg=="
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.dialog_save, null)
        builder.setView(view)
        val dialog = builder.create()
        dialog.show()
//        val etFileName = view.findViewById<EditText>(R.id.etFileName)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnCancelBackup = view.findViewById<Button>(R.id.btnCancelBackup)
        val switch1 = view.findViewById<SwitchCompat>(R.id.switch1)
        val qrCodeImage = view.findViewById<ImageView>(R.id.qrCodeImage)
        val tvEncryptionKey = view.findViewById<TextView>(R.id.tvEncryptionKey)
        val etFileNameInputLayout = view.findViewById<TextInputLayout>(R.id.etFileNameInputLayout)
        val etFileName = view.findViewById<TextInputEditText>(R.id.etFileName)
        val tvUserMsg = view.findViewById<TextView>(R.id.tvUserMsg)

        etFileName.addTextChangedListener {
            if (etFileName.text.toString().isNotEmpty()) {
                etFileNameInputLayout.isHelperTextEnabled = false
            } else {
                etFileNameInputLayout.isHelperTextEnabled = true
                etFileNameInputLayout.helperText = "*Default file name will be 'notes'"
            }
        }

        switch1.text = "Toggle to encrypt the Backup"
        qrCodeImage.visibility = View.GONE
        tvEncryptionKey.visibility = View.GONE
        tvUserMsg.visibility = View.GONE

        switch1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switch1.text = "Backup will be encrypted"
                isEncrypted = true


                val keyGenerator = KeyGenerator.getInstance("AES")
                keyGenerator.init(128)
                val secretKey = keyGenerator.generateKey()
                val key = secretKey.encoded
                encryptionKey = Base64.encodeToString(key)

                tvEncryptionKey.text = "Copy the encryption key\n$encryptionKey"
                tvEncryptionKey.setOnClickListener {
                    val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("keyBase64", encryptionKey)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, "Text copied to clipboard: $encryptionKey", Toast.LENGTH_SHORT).show()
                }
                tvEncryptionKey.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_content_copy_24, 0)
                tvEncryptionKey.compoundDrawablePadding = 32

                try {
                    val bitMatrix = MultiFormatWriter().encode(encryptionKey, BarcodeFormat.QR_CODE, 250, 250)
                    val barcodeEncoder = BarcodeEncoder()
                    val bitmap = barcodeEncoder.createBitmap(bitMatrix)
                    qrCodeImage.setImageBitmap(bitmap)
                } catch (e: WriterException) {
                    e.printStackTrace()
                }

                qrCodeImage.visibility = View.VISIBLE
                tvEncryptionKey.visibility = View.VISIBLE
                tvUserMsg.visibility = View.VISIBLE

            } else {
                encryptionKey = "QRY9fqKaBlsBJZLoUNfOZg=="
                isEncrypted = false
                switch1.text = "Toggle to encrypt the Backup"
                qrCodeImage.visibility = View.GONE
                tvEncryptionKey.visibility = View.GONE
                tvUserMsg.visibility = View.GONE
            }
        }

        btnSave.setOnClickListener {
            if (!notesList.isNullOrEmpty()) {
                if (isEncrypted) {
                    if (etFileName.text.toString().trim().isNotEmpty()) {
                        saveFileLauncher.launch(etFileName.text.toString() + ".enc.json")
                    } else {
                        saveFileLauncher.launch("notes.enc.json")
                    }
                } else {
                    if (etFileName.text.toString().trim().isNotEmpty()) {
                        saveFileLauncher.launch(etFileName.text.toString() + ".json")
                    } else {
                        saveFileLauncher.launch("notes.json")
                    }
                }
            } else {
                Log.e(TAG, "backupDatabaseToJSON: No notes to backup")
                showErrorSnackbar("No notes to backup")
            }
            dialog.cancel()
        }

        btnCancelBackup.setOnClickListener {
            dialog.cancel()
        }
    }

    private fun stringToKey(encryptionKeyString: String): Key {
        val encryptionKeyBytes = Base64.decode(encryptionKeyString, Base64.DEFAULT)
        return SecretKeySpec(encryptionKeyBytes, 0, encryptionKeyBytes.size, "AES")
    }

    private fun encryptDataAsString(listOfNotesAsString: String): ByteArray {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(AES_KEY_SIZE)
        getEncryptionKey()
//        ENCRYPTION_KEY = keyGen.generateKey()

        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, ENCRYPTION_KEY, getGCMParameterSpec())

        var encryptionKeyAsString: String = Base64.encodeToString(ENCRYPTION_KEY.encoded, Base64.DEFAULT)
//        Log.e(TAG, "onCreate: $encryptionKeyAsString")

        return cipher.doFinal(listOfNotesAsString.toByteArray(Charsets.UTF_8))
    }
    private fun decryptDataAsString(encryptedData: ByteArray, Key: String = "2x2+69Nf9M9av+IqaEi3A10jMvt6CWt3Fvm6bAD4s2I="): String {
        val encryptionKeyBytes = Base64.decode(Key, Base64.DEFAULT)
        val decryptionKey = SecretKeySpec(encryptionKeyBytes, 0, encryptionKeyBytes.size, "AES")

        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.DECRYPT_MODE, decryptionKey, getGCMParameterSpec())
        return String(cipher.doFinal(encryptedData), Charsets.UTF_8)
    }

    private fun decryptDataAsList(encryptedData: ByteArray): List<Note> {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
        gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
        val gsonWithCustomSerializer = gsonBuilder.create()

        val notesType = object : TypeToken<List<Note>>() {}.type
        return gsonWithCustomSerializer.fromJson(decryptDataAsString(encryptedData), notesType)
    }


    private fun getEncryptionKey(): Key {
//        return SecretKeySpec(ENCRYPTION_KEY.toByteArray(Charsets.UTF_8), "AES")
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(AES_KEY_SIZE)
        ENCRYPTION_KEY = keyGen.generateKey()
        return ENCRYPTION_KEY
    }

    private fun getGCMParameterSpec(): GCMParameterSpec {
        val iv = ByteArray(GCM_IV_LENGTH)
        return GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
    }

    private fun validateEncryptedData(encryptedData: ByteArray, decryptedData: String): Boolean {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, ENCRYPTION_KEY, getGCMParameterSpec())
        val encryptedDataCheck = cipher.doFinal(decryptedData.toByteArray(Charsets.UTF_8))

        return encryptedData.contentEquals(encryptedDataCheck)
    }

    private fun convertStringToList(restoreData: String): List<Note> {
        //  convertListToJson
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
        gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
        val gsonWithCustomSerializer = gsonBuilder.create()
//        val notesJson = gsonWithCustomSerializer.toJson(listOfNotes)

        //  convertJsonToList
        val notesType = object : TypeToken<List<Note>>() {}.type
        val notesList = gsonWithCustomSerializer.fromJson<List<Note>>(restoreData, notesType)

//        Log.e(TAG, notesJson)
//        Log.d(TAG, "notesList -> $notesList")
        return notesList
    }

//    private fun convertListToJson(listOfNotes: List<Note>) {
//        //  convertListToJson
//        val gsonBuilder = GsonBuilder()
//        gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
//        gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
//        val gsonWithCustomSerializer = gsonBuilder.create()
//        val notesJson = gsonWithCustomSerializer.toJson(listOfNotes)
//
//        //  convertJsonToList
//        val notesType = object : TypeToken<List<Note>>() {}.type
//        val notesList = gsonWithCustomSerializer.fromJson<List<Note>>(notesJson, notesType)
//
//        Log.e(TAG, notesJson)
//        Log.d(TAG, "notesList -> $notesList")
//    }
}




class LocalDateTimeSerializer : JsonSerializer<LocalDateTime> {
    override fun serialize(src: LocalDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src.toString())
    }
}

class LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDateTime {
        return LocalDateTime.parse(json?.asString)
    }
}
 */


/*


    private fun encryptDataAsList(listOfNotes: List<Note>): ByteArray {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getEncryptionKey(), getGCMParameterSpec())

        val baos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(baos)
        oos.writeObject(listOfNotes)
        oos.close()
        return cipher.doFinal(baos.toByteArray())
    }
    private fun decryptDataAsList(encryptedData: ByteArray): List<Note> {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.DECRYPT_MODE, ENCRYPTION_KEY, getGCMParameterSpec())

        val bais = ByteArrayInputStream(cipher.doFinal(encryptedData))
        val ois = ObjectInputStream(bais)
        return ois.readObject() as List<Note>
    }


 */

/*

class BackupNotes : AppCompatActivity() {
    private val TAG = "log_" + BackupNotes::class.java.name.split(BackupNotes::class.java.name.split(".").toTypedArray()[2] + ".").toTypedArray()[1]

    private lateinit var notesViewModel: NotesViewModel
    var notesLiveList: LiveData<List<Note>> = MutableLiveData()
    private lateinit var note: Note
    lateinit var notesList: List<Note>

    companion object {
        private const val AES_MODE = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
        private const val ENCRYPTION_KEY = "AESEncryptionKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notesViewModel = ViewModelProvider(this)[NotesViewModel::class.java]
        notesLiveList = notesViewModel.getAllNotes()

        notesLiveList.observe(this) {
            notesList = it

            Log.d(TAG, "Original Notes List: $notesList")

            val encryptedData = encryptData(notesList)
            Log.d(TAG, "Encrypted Data: ${Base64.encodeToString(encryptedData, Base64.DEFAULT)}")

            val decryptedData = decryptData(encryptedData)
            Log.d(TAG, "Decrypted Data: $decryptedData")

//            convertListToJson()
        }
    }


    private fun encryptData(notesList: List<Note>): ByteArray {
        val baos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(baos)
        oos.writeObject(notesList)
        oos.close()
        val objectData = baos.toByteArray()

        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getEncryptionKey(), getGCMParameterSpec())

        return cipher.doFinal(objectData)
    }

    private fun decryptData(encryptedData: ByteArray): List<Note> {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.DECRYPT_MODE, getEncryptionKey(), getGCMParameterSpec())

        val objectData = cipher.doFinal(encryptedData)

        val bais = ByteArrayInputStream(objectData)
        val ois = ObjectInputStream(bais)
        val decryptedData = ois.readObject() as List<Note>
        ois.close()

        return decryptedData
    }

    private fun getEncryptionKey(): Key {
        return SecretKeySpec(ENCRYPTION_KEY.toByteArray(Charsets.UTF_8), "AES")
    }

    private fun getGCMParameterSpec(): GCMParameterSpec {
        val iv = ByteArray(GCM_IV_LENGTH)
        return GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
    }

    private fun convertListToJson() {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
        gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
        val gsonWithCustomSerializer = gsonBuilder.create()

        val notesJson = gsonWithCustomSerializer.toJson(notesList)
        val notesType = object : TypeToken<List<Note>>() {}.type
        val notesList = gsonWithCustomSerializer.fromJson<List<Note>>(notesJson, notesType)

        Log.e(TAG, notesJson)
        Log.d(TAG, "notesList -> $notesList")
    }
}
 */


/*


        notesLiveList.observe(this) {
            notesList = it

            val gsonBuilder = GsonBuilder()
            gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
            gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
            val gsonWithCustomSerializer = gsonBuilder.create()

            val notesJson = gsonWithCustomSerializer.toJson(notesList)
//            val notesType = object : TypeToken<List<Note>>() {}.type
//            val notesList = gsonWithCustomSerializer.fromJson<List<Note>>(notesJson, notesType)

//            Log.e(TAG, notesJson)
//            Log.d(TAG, "notesList -> $notesList")

//            val plainText = notesList.toString()
//            Log.d(TAG, "Plaintext: $plainText")

            val encryptedData = encryptDataAsString(notesJson)
//            Log.d(TAG, "Encrypted Data: ${Base64.encodeToString(encryptedData, Base64.DEFAULT)}")
//            Log.e(TAG, "Decrypted Data: ${decryptDataAsString(encryptedData)}")
//
//            val notesType = object : TypeToken<List<Note>>() {}.type
//            val listOfNotes = gsonWithCustomSerializer.fromJson<List<Note>>(decryptDataAsString(encryptedData), notesType)
//            Log.i(TAG, "Decrypted Data: ${listOfNotes}")
//            Log.i(TAG, "Decrypted Data: ${decryptDataAsList(encryptedData)}")

//            val encryptedData = encryptDataAsList(notesList)
            Log.d(TAG, "Encrypted Data: ${Base64.encodeToString(encryptedData, Base64.DEFAULT)}")
            Log.e(TAG, "Decrypted Data: ${decryptDataAsString(encryptedData)}")
            Log.i(TAG, "Decrypted Data: ${decryptDataAsList(encryptedData)}")

//            convertListToJson()
        }

 */