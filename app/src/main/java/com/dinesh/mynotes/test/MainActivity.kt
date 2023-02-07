package com.dinesh.mynotes.test


import android.accounts.AccountManager.KEY_PASSWORD
import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.dinesh.mynotes.R
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import java.security.Key
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@RequiresApi(Build.VERSION_CODES.P)
class MainActivity : AppCompatActivity() {
    private val TAG = "log_" + MainActivity::class.java.name.split(MainActivity::class.java.name.split(".").toTypedArray()[2] + ".").toTypedArray()[1]

    private val keystoreName = "AndroidKeyStore"
    private val key = "Key_Name"
    private val transformation = "AES/GCM/NoPadding"
    private val valueToStore = "ABC".toByteArray()
    private val sharedPreferencesName = "your_shared_preferences_name"
    private val ivKey = "iv_key"
    private val encryptedValueKey = "encrypted_value_key"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        bioAuthentication()

        createKey()
        storeValueInKeyStore()
//        retrieveValueFromKeyStore()

    }

    private fun bioAuthentication() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "App can authenticate using biometrics.")

                val executor = Executors.newSingleThreadExecutor()
                val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Log.d(TAG, "Authentication succeeded")
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Authentication succeeded!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Log.d(TAG, "Authentication error: $errString")
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Log.d(TAG, "Authentication failed")
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                })

                val promptInfo = PromptInfo.Builder()
                    .setTitle("Biometric Login")
                    .setSubtitle("Log in using your biometric credential")
                    .setNegativeButtonText("Cancel")
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Log.e(TAG, "No biometric features available on this device.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Log.e(TAG, "Biometric features are currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Log.e(TAG, "The user hasn't associated any biometric credentials with their account.")
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> Log.e(TAG, "A security update is required to use biometric authentication.")
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> Log.e(TAG, "This device does not support biometric authentication.")
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> Log.e(TAG, "The biometric authentication status is unknown.")
        }
    }



    private fun createKey() {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, keystoreName)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(key, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    private fun storeValueInKeyStore() {
//        val keyStore = KeyStore.getInstance(keystoreName)
//        keyStore.load(null)
//        val secretKey = keyStore.getKey(key, null) as SecretKey
//        val cipher = Cipher.getInstance(transformation)
//
//        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
//        val iv = cipher.iv
//        val encryptedValue = cipher.doFinal(valueToStore)
//
//        val sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
//        sharedPreferences.edit().putString(ivKey, iv.toString(Charsets.UTF_8)).apply()
//
////        Log.e(TAG, "storeValueInKeyStore: ${encryptedValue}")
//        Log.e(TAG, "storeValueInKeyStore: ${Base64.encodeToString(encryptedValue, Base64.DEFAULT)}")


        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID_KEYSTORE)
        keyStore.load(null)
        val encryptionKeyBytes = Base64.decode("2x2+69Nf9M9av+IqaEi3A10jMvt6CWt3Fvm6bAD4s2I=", Base64.DEFAULT)
        var encryptionKey =  SecretKeySpec(encryptionKeyBytes, 0, encryptionKeyBytes.size, "AES")

        val secretKeySpec = SecretKeySpec(encryptionKey.encoded, "AES")
        val secretKeyEntry = KeyStore.SecretKeyEntry(secretKeySpec)

        //  To be completed
        val keyStoreProtectionParameter = KeyStore.PasswordProtection(KEY_PASSWORD.toCharArray())
        keyStore.setEntry(KEY_ALIAS, secretKeyEntry, keyStoreProtectionParameter)

        // Save the encrypted value and initialization vector to persistent storage.
    }

    private fun retrieveValueFromKeyStore() {
        val sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val ivString = sharedPreferences.getString(ivKey, "")
        if (ivString == "") {
            // Handle error when iv is not found in shared preferences
            Log.e(TAG, "retrieveValueFromKeyStore: Handle error when iv is not found in shared preferences")
            return
        }
        Log.i(TAG, "retrieveValueFromKeyStore: ${ivString}")
        Log.i(TAG, "retrieveValueFromKeyStore: ${ivString?.toByteArray(Charsets.UTF_8)}")
        val iv = ivString?.toByteArray(Charsets.UTF_8)

        val keyStore = KeyStore.getInstance(keystoreName)
        keyStore.load(null)
        val secretKey = keyStore.getKey(key, null) as SecretKey
        val cipher = Cipher.getInstance(transformation)
        val gcmParameterSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)

        val encryptedValueString = sharedPreferences.getString(encryptedValueKey, "default")
        if (encryptedValueString == "default") {
            // Handle error when encrypted value is not found in shared preferences
            Log.e(TAG, "retrieveValueFromKeyStore: Handle error when encrypted value is not found in shared preferences")
            return
        }
        val encryptedValue = encryptedValueString?.toByteArray(Charsets.UTF_8)
        val decryptedValue = cipher.doFinal(encryptedValue)
        val decryptedString = decryptedValue.toString(Charsets.UTF_8)

        Log.i(TAG, "retrieveValueFromKeyStore: ${decryptedString}")

    }

//    private fun retrieveValueFromKeyStore() {
////        val keyStore = KeyStore.getInstance(keystoreName)
////        keyStore.load(null)
////        val secretKey = keyStore.getKey(key, null) as SecretKey
////        val cipher = Cipher.getInstance(transformation)
////        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
////        val decryptedValue = cipher.doFinal(encryptedValue)
////        val decryptedString = String(decryptedValue)
//    }


    private fun stringToKey(encryptionKeyString: String): Key {
        val encryptionKeyBytes = Base64.decode(encryptionKeyString, Base64.DEFAULT)
        return SecretKeySpec(encryptionKeyBytes, 0, encryptionKeyBytes.size, "AES")
    }

    private val KEYSTORE_PROVIDER_ANDROID_KEYSTORE = "AndroidKeyStore"
    private val KEY_ALIAS = "EncryptionKey"


//    private fun storeEncryptionKeyInKeyStore(key: Key) {
//        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID_KEYSTORE)
//        keyStore.load(null)
//
//        val secretKeySpec = SecretKeySpec(key.encoded, "AES")
//        val secretKeyEntry = KeyStore.SecretKeyEntry(secretKeySpec)
//
//        val executor = ContextCompat.getMainExecutor(this)
//        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
//            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
//                super.onAuthenticationError(errorCode, errString)
//                Log.e(TAG, "Authentication error: $errString")
//            }
//
//            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
//                super.onAuthenticationSucceeded(result)
//                Log.d(TAG, "Authentication succeeded")
////                keyStore.setEntry(KEY_ALIAS, secretKeyEntry, result.cryptoObject!!.getCipher())
////                keyStore.setEntry(KEY_ALIAS, secretKeyEntry, KeyStore.ProtectionParameter(result.cryptoObject))
////                keyStore.setEntry(KEY_ALIAS, secretKeyEntry, result.cryptoObject)
//                keyStore.setEntry(KEY_ALIAS, secretKeyEntry, KeyStore.Builder(this)
//                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
//                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
//                    .build());
//
//            }
//
//            override fun onAuthenticationFailed() {
//                super.onAuthenticationFailed()
//                Log.e(TAG, "Authentication failed")
//            }
//        })
//
//        val promptInfo = PromptInfo.Builder()
//            .setTitle("Biometric Login")
//            .setSubtitle("Login using biometric credentials")
//            .setNegativeButtonText("Cancel")
//            .build()
//
//        biometricPrompt.authenticate(
//            promptInfo, BiometricPrompt.CryptoObject(
//                Cipher.getInstance(
//                    KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" +
//                            KeyProperties.ENCRYPTION_PADDING_PKCS7
//                )
//            )
//        )
//    }


}





/*

class MainActivity : AppCompatActivity() {

    // create a CancellationSignal variable and assign a value null to it
    private var cancellationSignal: CancellationSignal? = null

    // create an authenticationCallback
    private val authenticationCallback: BiometricPrompt.AuthenticationCallback
        get() = @RequiresApi(Build.VERSION_CODES.P)
        object : BiometricPrompt.AuthenticationCallback() {
            // here we need to implement two methods
            // onAuthenticationError and onAuthenticationSucceeded
            // If the fingerprint is not recognized by the app it will call
            // onAuthenticationError and show a toast
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                super.onAuthenticationError(errorCode, errString)
                notifyUser("Authentication Error : $errString")
            }

            // If the fingerprint is recognized by the app then it will call
            // onAuthenticationSucceeded and show a toast that Authentication has Succeed
            // Here you can also start a new activity after that
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                super.onAuthenticationSucceeded(result)
                notifyUser("Authentication Succeeded")

                // or start a new Activity

            }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkBiometricSupport()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onBackPressed() {
        // This creates a dialog of biometric auth and
        // it requires title , subtitle ,
        // and description
        // In our case there is a cancel button by
        // clicking it, it will cancel the process of
        // fingerprint authentication
        val biometricPrompt = BiometricPrompt.Builder(this)
            .setTitle("Title of Prompt")
            .setSubtitle("Subtitle")
            .setDescription("Uses FP")
            .setNegativeButton("Cancel", this.mainExecutor, DialogInterface.OnClickListener { dialog, which ->
                notifyUser("Authentication Cancelled")
            }).build()

        // start the authenticationCallback in mainExecutor
        biometricPrompt.authenticate(getCancellationSignal(), mainExecutor, authenticationCallback)

    }

    // it will be called when authentication is cancelled by the user
    private fun getCancellationSignal(): CancellationSignal {
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            notifyUser("Authentication was Cancelled by the user")
        }
        return cancellationSignal as CancellationSignal
    }

    // it checks whether the app the app has fingerprint permission
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkBiometricSupport(): Boolean {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!keyguardManager.isDeviceSecure) {
            notifyUser("Fingerprint authentication has not been enabled in settings")
            return false
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
            notifyUser("Fingerprint Authentication Permission is not enabled")
            return false
        }
        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            true
        } else true
    }

    // this is a toast method which is responsible for showing toast
    // it takes a string as parameter
    private fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}*/
