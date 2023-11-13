package com.dinesh.mynotes.app

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.dinesh.mynotes.R

open class ToolbarMain : ColorScheme() {
    private val TAG = "log_ToolbarMain"

    lateinit var toolbar: Toolbar
    lateinit var tvToolbar: TextView
    lateinit var toolbarSearchView: SearchView
    open var pressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.toolbar_layout)
        setToolbar()

        /*
        toolbar.setNavigationOnClickListener {
            if (pressedTime + 2000 > System.currentTimeMillis()) {
                finish()
                finishAndRemoveTask()
                finishAffinity()
                exitProcess(0)
            } else {
                Log.d(TAG, "onCreate: Press back again to exit")
                Toast.makeText(baseContext, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
            pressedTime = System.currentTimeMillis()
        }
         */

    }

    open fun setToolbar() {
        toolbar = findViewById(R.id.toolbar)
        tvToolbar = findViewById(R.id.tvToolbar)
        toolbarSearchView = findViewById(R.id.searchView)
        setSupportActionBar(toolbar)

        /*
        toolbarSearchView.setQuery("toolbarSearchView",true)

        // Enable the back button on the toolbar
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
         */
    }

    /*
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
//        toolbar.setBackgroundColor(ContextCompat.getColor(this, toolbarBackground))
//        tvToolbar.setTextColor(ContextCompat.getColor(this, toolbarOnBackground))
//
//        // TODO: NavigationIcon
//        val navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_arrow_back_24)
////        navigationIcon!!.setTint(ContextCompat.getColor(this, toolbarOnBackground))
//        toolbar.navigationIcon = navigationIcon
//
//        // TODO: CollapseIcon
//        val backCollapseIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_arrow_back_24)
////        backCollapseIcon!!.setTint(ContextCompat.getColor(this, toolbarOnBackground))
//        toolbar.collapseIcon = backCollapseIcon
//
//        var optionDrawableIcon = menu.findItem(R.id.sort).icon
//        optionDrawableIcon = DrawableCompat.wrap(optionDrawableIcon!!)
////        DrawableCompat.setTint(optionDrawableIcon, ContextCompat.getColor(this, toolbarOnBackground))
//        menu.findItem(R.id.sort).icon = optionDrawableIcon
//
//        /*
////        toolbarSearchView.setBackgroundColor(ContextCompat.getColor(this, toolbarBackground))
////        toolbarSearchView.queryHint = "Search Here"
////        toolbarSearchView.maxWidth = Int.MAX_VALUE
////        toolbarSearchView.isIconified = false
////        toolbarSearchView.setQuery(resources.getText(R.string.app_name),true)
////        toolbarSearchView.clearFocus()
//
////        val searchClose = toolbarSearchView.findViewById<View>(androidx.appcompat.R.id.search_close_btn) as ImageView
////        searchClose.setImageResource(R.drawable.ic_baseline_menu_24)
////        searchClose.setBackgroundColor(ContextCompat.getColor(this, toolbarBackground))
//         */
//
//        var menuMainDrawableIcon = menu.findItem(R.id.menuMain).icon
//        menuMainDrawableIcon = DrawableCompat.wrap(menuMainDrawableIcon!!)
////        DrawableCompat.setTint(menuMainDrawableIcon, ContextCompat.getColor(this, toolbarOnBackground))
//        menu.findItem(R.id.menuMain).icon = menuMainDrawableIcon
//
//        var searchDrawableIcon = menu.findItem(R.id.action_search).icon
//        searchDrawableIcon = DrawableCompat.wrap(searchDrawableIcon!!)
////        DrawableCompat.setTint(searchDrawableIcon, ContextCompat.getColor(this, toolbarOnBackground))
//        menu.findItem(R.id.action_search).icon = searchDrawableIcon
//
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView?
//        searchView!!.setBackgroundColor(com.google.android.material.R.attr.colorSurfaceContainerHigh)
//        searchView!!.setBackgroundColor(com.google.android.material.R.attr.colorSurface)
        searchView!!.queryHint = "Search Here"
        searchView.maxWidth = Int.MAX_VALUE
        searchView.isIconified = false
        searchView.clearFocus()
        val searchEditText = searchView?.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
//        searchEditText?.setTextColor(com.google.android.material.R.attr.colorOnSurface)


//        val searchClose = searchView.findViewById<View>(androidx.appcompat.R.id.search_close_btn) as ImageView
//        val searchCloseIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_close_24)
//        searchCloseIcon!!.setTint(ContextCompat.getColor(this, R.color.Error60))
//        searchClose.background = searchCloseIcon
//        searchClose.setImageResource(R.drawable.ic_baseline_close_24)
//////        searchClose.setBackgroundColor(ContextCompat.getColor(this, lightBackground))
////        searchClose.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
////        searchClose.setBackgroundColor(com.google.android.material.R.attr.colorOnSurface)
//
//        val typedValue = TypedValue()
//        theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerHigh, typedValue, true)
//        val colorPrimary = typedValue.data
//        searchClose.setBackgroundColor(colorPrimary)
//        searchClose.background


        return true
    }

     */


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return when (id) {
            android.R.id.home -> {
                Log.d(TAG, "onOptionsItemSelected: Back Menu")
                finish()
                true
            }

            R.id.sort -> {
                Log.d(TAG, "onOptionsItemSelected: sort")
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}




