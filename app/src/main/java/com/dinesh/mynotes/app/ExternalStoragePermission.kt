package com.dinesh.mynotes.app

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData


open class ExternalStoragePermission : AppCompatActivity() {
    private val TAG = "log_" + ExternalStoragePermission::class.java.name.split(ExternalStoragePermission::class.java.name.split(".").toTypedArray()[2] + ".").toTypedArray()[1]

    var activityResultLauncher: ActivityResultLauncher<Array<String>>? = null
    var permissionList = ArrayList<String>()
    var permanentlyDeniedPermissionList = ArrayList<String>()
    private var isAllPermissionGranted = true

    val isPermissionGranted = MutableLiveData<Boolean>()

    override fun onStart() {
        super.onStart()
        rootFunction(this)
    }

    fun rootFunction(context: Context) {
        window.statusBarColor = Color.GRAY
        activityResultLauncher = registerForActivityResult<Array<String>, Map<String, Boolean>>(
            ActivityResultContracts.RequestMultiplePermissions(),
            ActivityResultCallback<Map<String, Boolean>> { result ->
                Log.i(TAG, "onActivityResult: ----> $result")
                isPermissionGranted.value = !result.values.contains(false)
                if (!result.isEmpty()) {
                    val booleanCollection = result.values
                    for (booleanResult in booleanCollection) {
                        isAllPermissionGranted = booleanResult && isAllPermissionGranted
                    }
                    if (!isAllPermissionGranted) {
                        requestPermissionRational(context)
                    }
                }
            })
//        requestPermission(this)

//        isPermissionGranted.value = permanentlyDeniedPermissionList.isEmpty() && permissionList.isEmpty()
    }

    fun requestPermission(context: Context) {
        permissionList.clear()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){ }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //Permission Granted
                Log.i(TAG, "requestPermission: ----> " + Manifest.permission.WRITE_EXTERNAL_STORAGE + " Granted")
            } else {
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //Permission Granted
                Log.i(TAG, "requestPermission: ----> " + Manifest.permission.READ_EXTERNAL_STORAGE + " Granted")
            } else {
                permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (!permissionList.isEmpty()) {
            Log.i(TAG, "requestPermissionList: $permissionList")
            activityResultLauncher!!.launch(permissionList.toTypedArray())
        } else{
            Log.e(TAG, "requestPermissionList: $permissionList")
        }
    }

    fun requestPermissionRational(context: Context) {
        permissionList.clear()
        permanentlyDeniedPermissionList.clear()

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "requestPermissionRational--> Permission Granted: " + Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale((context as Activity?)!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //This block here means PERMANENTLY DENIED PERMISSION
                    Log.i(TAG, "requestPermissionRational: AlertDialog ----> PERMANENTLY DENIED PERMISSION: " + Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    permanentlyDeniedPermissionList.add("WRITE_EXTERNAL_STORAGE")
                } else {
                    Log.e(TAG, "requestPermissionRational--> Permission Denied & Requesting Permission Again: " + Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "requestPermissionRational--> Permission Granted: " + Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale((context as Activity?)!!, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //This block here means PERMANENTLY DENIED PERMISSION
                    Log.i(TAG, "requestPermissionRational: AlertDialog ----> PERMANENTLY DENIED PERMISSION: " + Manifest.permission.READ_EXTERNAL_STORAGE)
                    permanentlyDeniedPermissionList.add("READ_EXTERNAL_STORAGE")
                } else {
                    Log.e(TAG, "requestPermissionRational--> Permission Denied & Requesting Permission Again: " + Manifest.permission.READ_EXTERNAL_STORAGE)
                    permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }


        if (!permissionList.isEmpty()) {
            activityResultLauncher!!.launch(permissionList.toTypedArray())
        }
        if (!permanentlyDeniedPermissionList.isEmpty()) {
            alertDialogForPermanentlyDeniedPermissions(this)
            Log.e(TAG, "permanentlyDeniedPermissionList: $permanentlyDeniedPermissionList")
        }
    }

    fun alertDialogForPermanentlyDeniedPermissions(context: Context?) {
        AlertDialog.Builder(context!!)
            .setCancelable(true)
            .setTitle("Request Permission")
            .setMessage(
                "You have permanently denied 'Storage' permission.\n\n" +
                        "To backup the notes we will be creating a backup file in your local storage. In order to do that please allow 'Storage' permission. " +
                        "\n\n\n" +
                        "Do you want to go to settings to allow permission?"
            )
            .setPositiveButton("Go to Settings") { dialog, which ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
                dialog.dismiss()
            }.setNegativeButton("Cancel") { dialog, which ->
//                Toast.makeText(context, "Exit the app", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
//                finish()
//                finishAffinity()
//                finishAndRemoveTask()
            }
            .show()
    }
}

