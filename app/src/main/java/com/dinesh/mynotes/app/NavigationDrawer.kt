package com.dinesh.mynotes.app


//open class NavigationDrawer : ToolbarMain() {
//
//}

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.dinesh.mynotes.R
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
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
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
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
            R.id.menuAbout -> {
                val dialog = FancyDialog(this)
                    .setTitle(getString(R.string.app_name))
                    .setMessage("This is a Simple Notes App. \n\n " +
                            "This is just the initial build of the app, I plan to include new feature if any users demands. \n\n" +
                            "Please don't hesitate to contact me to report any bugs or give any suggestion.")
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
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
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


}

