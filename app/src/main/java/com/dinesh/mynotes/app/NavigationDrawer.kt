package com.dinesh.mynotes.app


//open class NavigationDrawer : ToolbarMain() {
//
//}

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.dinesh.mynotes.R
import com.google.android.material.navigation.NavigationView
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


open class NavigationDrawer : ToolbarMain(), NavigationView.OnNavigationItemSelectedListener {
    private val TAG = "log_" + NavigationDrawer::class.java.name.split(NavigationDrawer::class.java.name.split(".").toTypedArray()[2] + ".").toTypedArray()[1]

    open lateinit var drawerLayout: DrawerLayout
    open lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    open lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nav_drawer)
//        toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)
//        toolbar = findViewById(R.id.toolbar)
        setNavigationDrawer()

    }

    open fun setNavigationDrawer() {
        setToolbar()
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        actionBarDrawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle!!)
        actionBarDrawerToggle!!.syncState()
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        navigationView.setNavigationItemSelectedListener(this)

        val fragmentTransaction = supportFragmentManager.beginTransaction()

        //        val fragmentOne = HomeFragment()
        //        fragmentTransaction.replace(R.id.frameLayoutInflateAdapter, fragmentOne)
        //        fragmentTransaction.commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuShare -> shareApp()
            R.id.menuAbout -> Toast.makeText(this, "This is a Simple Notes App", Toast.LENGTH_SHORT).show()
//            R.id.menuBackup -> {
//                // request for the storage permission
//                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_EXTERNAL_STORAGE)
//                } else {
//                    backupDatabase()
//                }
//            }
//            R.id.menuRestore -> restoreDatabase()
        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }

    private fun restoreDatabase() {
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

    private val REQUEST_EXTERNAL_STORAGE = 1

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

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My App")
        val shareMessage = "\nLet me recommend you this application\n\n" + "https://play.google.com/store/apps/details?id=" + packageName + "\n\n"
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(shareIntent, "choose one"))
    }


    //  ToolbarMain
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)

        // TODO: NavigationIcon
//        toolbar!!.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        val navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_menu_24)
        navigationIcon!!.setTint(ContextCompat.getColor(this, toolbarOnBackground))
        toolbar.navigationIcon = navigationIcon

        return true
    }


    //  ToolbarMain
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
//        val id = item.itemId
//        return when (id) {
//            R.id.menuMain -> {
//                Log.d(TAG, "onOptionsItemSelected: Testing menuMain")
//                Toast.makeText(this, TAG, Toast.LENGTH_LONG).show()
//                true
//            }
//            R.id.sort -> {
//                Log.d(TAG, "onOptionsItemSelected: Testing sort")
//                Toast.makeText(this, "Admin Testing sort", Toast.LENGTH_LONG).show()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
    }

}

