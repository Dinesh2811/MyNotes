package com.dinesh.mynotes.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
//import javax.crypto.SecretKey //
//import javax.crypto.Cipher
//import java.security.KeyStore
//import javax.crypto.KeyGenerator
//import android.security.keystore.KeyGenParameterSpec
//import android.security.keystore.KeyProperties
//import android.util.Base64


import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import java.util.concurrent.Executors
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
//import androidx.core.util.Base64Utils


private val TAG = "log_" + T1::class.java.name.split(T1::class.java.name.split(".").toTypedArray()[2] + ".").toTypedArray()[1]

class T1 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cryptoManager = CryptoManager()

        notesJson = "Hello"
//        backupLauncher()
//        storeDataInKeyStore(notesJson)
//        Log.e(TAG, "onCreate: ${retrieveDataFromKeyStore()}")

//        test(notesJson)
        val file = File(filesDir, "secret.txt")
        val messageToEncrypt = cryptoManager.decrypt(
            inputStream = FileInputStream(file)
        ).decodeToString()
        Log.e(TAG, "onCreate: ${messageToEncrypt}")
    }

    private fun test(notesJson: String) {
        val cryptoManager = CryptoManager()

        val bytes = notesJson.encodeToByteArray()
        val file = File(filesDir, "secret.txt")
        if(!file.exists()) {
            file.createNewFile()
        }
        val fos = FileOutputStream(file)

        var messageToDecrypt = cryptoManager.encrypt(
            bytes = bytes,
            outputStream = fos
        ).decodeToString()


        Log.e(TAG, "test: ${messageToDecrypt}")


        var messageToEncrypt = cryptoManager.decrypt(
            inputStream = FileInputStream(file)
        ).decodeToString()
    }

    fun storeDataInKeyStore(data: String) {
        try {
            // Set up the KeyStore
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            // Generate the key
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keySpec = KeyGenParameterSpec.Builder("key_alias", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            keyGenerator.init(keySpec)
            val secretKey = keyGenerator.generateKey()

            // Encrypt the data
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            val encodedEncryptedData = Base64.encodeToString(encryptedData, Base64.DEFAULT)

            // Store the encrypted data in the KeyStore
            keyStore.setEntry("key_alias", KeyStore.SecretKeyEntry(secretKey), null)

            Log.d(TAG, "Encrypted data: $encodedEncryptedData")     //  cl1p5sSZfN8E3xz3YCx1cSsvRDPl
//            retrieveDataFromKeyStore(secretKey)
//            accessDataFromKeyStore()
        } catch (e: Exception) {
            Log.e(TAG, "Error storing data in KeyStore: ${e.message}")
        }
    }

//    fun accessDataFromKeyStore() {
//        try {
//            // Get the KeyStore instance
//            val keyStore = KeyStore.getInstance("AndroidKeyStore")
//            keyStore.load(null)
//
//            // Get the SecretKey from the KeyStore
//            val secretKey = keyStore.getKey("key_alias", null) as SecretKey
//
//            // Create a Cipher for decryption
//            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
//            cipher.init(Cipher.DECRYPT_MODE, secretKey)
//
//            // Check if biometric authentication is available and use it if it is
//            val biometricManager = BiometricManager.from(this)
////            val biometricManager = getSystemService(Context.BIOMETRIC_SERVICE) as BiometricManager
//            if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
//                val executor = Executors.newSingleThreadExecutor()
//                val biometricPrompt = BiometricPrompt(this, executor,
//                    object : BiometricPrompt.AuthenticationCallback() {
//                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
//                            super.onAuthenticationError(errorCode, errString)
//                            Log.e("KeyStoreSample", "Error accessing data from KeyStore: $errString")
//                            executor.shutdown()
//                        }
//
//                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
//                            super.onAuthenticationSucceeded(result)
//                            val encryptedData = Base64.decode(result.cryptoObject!!.cipher, Base64.DEFAULT)
//
////                            val encryptedData = Base64.decode(encodedEncryptedData, Base64.DEFAULT)
//
//                            val decryptedData = String(cipher.doFinal(encryptedData), Charsets.UTF_8)
//                            Log.d("KeyStoreSample", "Decrypted data: $decryptedData")
//                            executor.shutdown()
//                        }
//
//                        override fun onAuthenticationFailed() {
//                            super.onAuthenticationFailed()
//                            Log.e("KeyStoreSample", "Error accessing data from KeyStore: Authentication failed")
//                            executor.shutdown()
//                        }
//                    })
//                biometricPrompt.authenticate(
//                    BiometricPrompt.CryptoObject(cipher),
//                    ContextCompat.getMainExecutor(this),
//                    CancellationSignal()
//                )
//            } else {
//                Log.e("KeyStoreSample", "Error accessing data from KeyStore: Biometric authentication not available")
//            }
//        } catch (e: Exception) {
//            Log.e("KeyStoreSample", "Error accessing data from KeyStore: $e")
//        }
//    }

//    fun accessDataFromKeyStore() {
//        try {
//            // Get the KeyStore instance
//            val keyStore = KeyStore.getInstance("AndroidKeyStore")
//            keyStore.load(null)
//
//            // Get the SecretKey from the KeyStore
//            val secretKey = keyStore.getKey("key_alias", null) as SecretKey
//
//            // Create a Cipher for decryption
//            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
//            cipher.init(Cipher.DECRYPT_MODE, secretKey)
//
//            // Check if biometric authentication is available and use it if it is
//            val biometricManager = getSystemService(Context.BIOMETRIC_SERVICE) as BiometricManager
//            if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
//                val biometricPrompt = BiometricPrompt(this,
//                    Executors.newSingleThreadExecutor(),
//                    object : BiometricPrompt.AuthenticationCallback() {
//                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
//                            super.onAuthenticationError(errorCode, errString)
//                            Log.e("KeyStoreSample", "Error accessing data from KeyStore: $errString")
//                        }
//
//                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
//                            super.onAuthenticationSucceeded(result)
//                            val encryptedData = Base64.decode(result.cryptoObject!!.cipher, Base64.DEFAULT)
//                            val decryptedData = String(cipher.doFinal(encryptedData), Charsets.UTF_8)
//                            Log.d("KeyStoreSample", "Decrypted data: $decryptedData")
//                        }
//
//                        override fun onAuthenticationFailed() {
//                            super.onAuthenticationFailed()
//                            Log.e("KeyStoreSample", "Error accessing data from KeyStore: Authentication failed")
//                        }
//                    })
//                biometricPrompt.authenticate(
//                    BiometricPrompt.CryptoObject(cipher),
//                    ContextCompat.getMainExecutor(this),
//                    object : CancellationSignal() {}
//                )
//            } else {
//                Log.e("KeyStoreSample", "Error accessing data from KeyStore: Biometric authentication not available")
//            }
//        } catch (e: Exception) {
//            Log.e("KeyStoreSample", "Error accessing data from KeyStore: $e")
//        }
//    }


    private fun retrieveDataFromKeyStore(key: SecretKey): String {
        try {
            // Set up the KeyStore
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            // Retrieve the key from the KeyStore
            val secretKey = keyStore.getKey("key_alias", null) as SecretKey

            // Decrypt the data
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val encodedEncryptedData = "the encoded encrypted data obtained from the storeDataInKeyStore method"
            val encryptedData = Base64.decode(encodedEncryptedData, Base64.DEFAULT)
            val decryptedData = String(cipher.doFinal(encryptedData), Charsets.UTF_8)

            Log.d(TAG, "Decrypted data: $decryptedData")
            return decryptedData
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving data from KeyStore: $e")
            return ""
        }
    }

    private lateinit var notesJson: String
    private lateinit var encryptedData: ByteArray

    companion object {
        private const val AES_MODE = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
        private const val AES_KEY_SIZE = 256
        private const val ITERATION_COUNT = 100000
        private const val SALT_LENGTH = 16
        private const val KEY_LENGTH = AES_KEY_SIZE / 8
    }

    private fun backupLauncher() {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)

        val password = getPassword()
        val key = getEncryptionKey(password, salt)

        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, key, getGCMParameterSpec())
        encryptedData = cipher.doFinal(notesJson.toByteArray(Charsets.UTF_8))

        Log.i(TAG, "Salt: ${Base64.encodeToString(salt, Base64.DEFAULT)}")
        Log.i(TAG, "Encrypted Data: ${Base64.encodeToString(encryptedData, Base64.DEFAULT)}")
        Log.i(TAG, "Key: ${Base64.encodeToString(key.encoded, Base64.DEFAULT)}")

        restoreLauncher(encryptedData,key)
    }

    private fun getGCMParameterSpec(): GCMParameterSpec {
        val iv = ByteArray(GCM_IV_LENGTH)
        return GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
    }


    private fun restoreLauncher(encryptedData: ByteArray, key: SecretKey) {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.DECRYPT_MODE, key, getGCMParameterSpec())
        val decryptedData = cipher.doFinal(encryptedData)
        val decryptedNotesJson = String(decryptedData, Charsets.UTF_8)

        Log.e(TAG, "Decrypted Data: $decryptedNotesJson")
    }

    private fun getPassword(): CharArray {
        val password = CharArray(35)
        val random = SecureRandom()
        for (i in password.indices) {
            password[i] = (random.nextInt(94) + 33).toChar()
        }
        Log.i(TAG, "Password: ${String(password)}")
        return password
    }

    private fun getEncryptionKey(password: CharArray, salt: ByteArray): SecretKey {
        val keySpec = PBEKeySpec(password, salt, ITERATION_COUNT, KEY_LENGTH * 8)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val keyBytes = factory.generateSecret(keySpec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

}

/*


    private lateinit var notesJson: String
    private lateinit var encryptedData: ByteArray

    companion object {
        private const val AES_MODE = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
        private const val AES_KEY_SIZE = 256
        private const val ITERATION_COUNT = 10000
        private const val SALT_LENGTH = 16
        private const val KEY_LENGTH = AES_KEY_SIZE / 8
    }

    private fun backupLauncher() {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)

        val password = getPassword()
        val key = getEncryptionKey(password, salt)

        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, key, getGCMParameterSpec())
        encryptedData = cipher.doFinal(notesJson.toByteArray(Charsets.UTF_8))

        Log.i(TAG, "Salt: ${Base64.encodeToString(salt, Base64.DEFAULT)}")
        Log.i(TAG, "Encrypted Data: ${Base64.encodeToString(encryptedData, Base64.DEFAULT)}")
        Log.i(TAG, "Key: ${Base64.encodeToString(key.encoded, Base64.DEFAULT)}")

        restoreLauncher(encryptedData,key)
    }

    private fun getGCMParameterSpec(): GCMParameterSpec {
        val iv = ByteArray(GCM_IV_LENGTH)
        return GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
    }


    private fun restoreLauncher(encryptedData: ByteArray, key: SecretKey) {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.DECRYPT_MODE, key, getGCMParameterSpec())
        val decryptedData = cipher.doFinal(encryptedData)
        val decryptedNotesJson = String(decryptedData, Charsets.UTF_8)

        Log.e(TAG, "Decrypted Data: $decryptedNotesJson")
    }

    private fun getPassword(): CharArray {
        val password = CharArray(16)
        val random = SecureRandom()
        for (i in password.indices) {
            password[i] = (random.nextInt(94) + 33).toChar()
        }
        Log.i(TAG, "Password: ${String(password)}")
        return password
    }

    private fun getEncryptionKey(password: CharArray, salt: ByteArray): SecretKey {
        val keySpec = PBEKeySpec(password, salt, ITERATION_COUNT, KEY_LENGTH * 8)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val keyBytes = factory.generateSecret(keySpec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }


 */