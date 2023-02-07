package com.dinesh.mynotes

import android.os.Build
import android.os.Bundle
import android.security.KeyChain
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.dinesh.mynotes.room.Note
import com.dinesh.mynotes.room.NotesViewModel
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.time.LocalDateTime
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.charset.Charset
import java.security.Key
import java.security.KeyStore
import java.security.SecureRandom
import java.util.ArrayList
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*

private val TAG = "log_" + Test2::class.java.name.split(Test2::class.java.name.split(".").toTypedArray()[2] + ".").toTypedArray()[1]

class Test2 : AppCompatActivity() {
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
}



//class Test2 : AppCompatActivity() {
//    private lateinit var notesViewModel: NotesViewModel
//    var notesLiveList: LiveData<List<Note>> = MutableLiveData()
//    private lateinit var note: Note
//    lateinit var notesList: List<Note>
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        notesViewModel = ViewModelProvider(this)[NotesViewModel::class.java]
//        notesLiveList = notesViewModel.getAllNotes()
//
//        notesLiveList.observe(this) {
//            notesList = it
////            var gson = Gson()
////            val notesJson = gson.toJson(notesList)
////            Log.d(TAG, notesJson)
//
//
//
//            val gsonBuilder = GsonBuilder()
//            gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, object : TypeAdapter<LocalDateTime>() {
//                override fun write(out: JsonWriter?, value: LocalDateTime?) {
//                    out?.value(value.toString())
//                }
//
//                override fun read(input: JsonReader?): LocalDateTime {
//                    return LocalDateTime.parse(input?.nextString())
//                }
//            })
//            var gson = gsonBuilder.create()
//            val json = gson.toJson(notesList)
//
//            Log.d(TAG, json)
//        }
//    }
//}




//class Test2 : AppCompatActivity() {
//    private lateinit var notesViewModel: NotesViewModel
//    var notesLiveList: LiveData<List<Note>> = MutableLiveData()
//    private lateinit var note: Note
//    lateinit var notesList: List<Note>
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        notesViewModel = ViewModelProvider(this)[NotesViewModel::class.java]
//        notesLiveList = notesViewModel.getAllNotes()
//
//        notesLiveList.observe(this) {
//            notesList = it
//
//            val gsonBuilder = GsonBuilder()
//            gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
//            gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
//            val gsonWithCustomSerializer = gsonBuilder.create()
//
//            val notesJson = gsonWithCustomSerializer.toJson(notesList)
//            val notesType = object : TypeToken<List<Note>>() {}.type
//            val notesList = gsonWithCustomSerializer.fromJson<List<Note>>(notesJson, notesType)
//
//            Log.e(TAG, notesJson)
//            Log.d(TAG, "notesList -> $notesList")
//        }
//    }
//}

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

class Test2 : AppCompatActivity() {
    private lateinit var notesViewModel: NotesViewModel
    var notesLiveList: LiveData<List<Note>> = MutableLiveData()
    private lateinit var note: Note
    lateinit var notesList: List<Note>
//    private lateinit var notesList: MutableList<Note>
//    private var notesList = ArrayList<Note>()
    var i = 1
    var x = "2023-02-03T22:48:12.112"

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
            val plainText = "This is the plain text that needs to be encrypted."
            Log.d(TAG, "Plaintext: $plainText")

            val encryptedData = encryptData(plainText)
            Log.d(TAG, "Encrypted Data: ${Base64.encodeToString(encryptedData, Base64.DEFAULT)}")

            val decryptedData = decryptData(encryptedData)
            Log.d(TAG, "Decrypted Data: $decryptedData")
        }
    }

    private fun encryptData(plainText: String): ByteArray {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getEncryptionKey(), getGCMParameterSpec())

        return cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
    }

    private fun decryptData(encryptedData: ByteArray): String {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.DECRYPT_MODE, getEncryptionKey(), getGCMParameterSpec())

        return String(cipher.doFinal(encryptedData), Charsets.UTF_8)
    }

    private fun getEncryptionKey(): Key {
        return SecretKeySpec(ENCRYPTION_KEY.toByteArray(Charsets.UTF_8), "AES")
    }

    private fun getGCMParameterSpec(): GCMParameterSpec {
        val iv = ByteArray(GCM_IV_LENGTH)
        return GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
    }
}
 */