package com.dinesh.mynotes.app

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
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

//        toolbar.setNavigationOnClickListener {
//            if (pressedTime + 2000 > System.currentTimeMillis()) {
//                finish()
//                finishAndRemoveTask()
//                finishAffinity()
//                exitProcess(0)
//            } else {
//                Log.d(TAG, "onCreate: Press back again to exit")
//                Toast.makeText(baseContext, "Press back again to exit", Toast.LENGTH_SHORT).show()
//            }
//            pressedTime = System.currentTimeMillis()
//        }

    }

    open fun setToolbar() {
        toolbar = findViewById(R.id.toolbar)
        tvToolbar = findViewById(R.id.tvToolbar)
        toolbarSearchView = findViewById(R.id.searchView)
        setSupportActionBar(toolbar)

//        toolbarSearchView.setQuery("toolbarSearchView",true)

//        // Enable the back button on the toolbar
//        supportActionBar!!.setDisplayShowHomeEnabled(true)
//        supportActionBar!!.setHomeButtonEnabled(true)
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
//        tvToolbar = findViewById(R.id.tvToolbar)
        toolbar.setBackgroundColor(ContextCompat.getColor(this, toolbarBackground))
        tvToolbar.setTextColor(ContextCompat.getColor(this, toolbarOnBackground))

        // TODO: NavigationIcon
//        toolbar!!.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        val navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_arrow_back_24)
        navigationIcon!!.setTint(ContextCompat.getColor(this, toolbarOnBackground))
        toolbar.navigationIcon = navigationIcon

        // TODO: CollapseIcon
//        toolbar!!.setCollapseIcon(R.drawable.ic_baseline_arrow_back_24)
        val backCollapseIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_arrow_back_24)
        backCollapseIcon!!.setTint(ContextCompat.getColor(this, toolbarOnBackground))
        toolbar.collapseIcon = backCollapseIcon

        var optionDrawableIcon = menu.findItem(R.id.sort).icon
        optionDrawableIcon = DrawableCompat.wrap(optionDrawableIcon!!)
        DrawableCompat.setTint(optionDrawableIcon, ContextCompat.getColor(this, toolbarOnBackground))
        menu.findItem(R.id.sort).icon = optionDrawableIcon

        /*

//        toolbarSearchView.setBackgroundColor(ContextCompat.getColor(this, toolbarBackground))
//        toolbarSearchView.queryHint = "Search Here"
//        toolbarSearchView.maxWidth = Int.MAX_VALUE
//        toolbarSearchView.isIconified = false
//        toolbarSearchView.setQuery(resources.getText(R.string.app_name),true)
//        toolbarSearchView.clearFocus()

//        val searchClose = toolbarSearchView.findViewById<View>(androidx.appcompat.R.id.search_close_btn) as ImageView
//        searchClose.setImageResource(R.drawable.ic_baseline_menu_24)
//        searchClose.setBackgroundColor(ContextCompat.getColor(this, toolbarBackground))

         */

        var menuMainDrawableIcon = menu.findItem(R.id.menuMain).icon
        menuMainDrawableIcon = DrawableCompat.wrap(menuMainDrawableIcon!!)
        DrawableCompat.setTint(menuMainDrawableIcon, ContextCompat.getColor(this, toolbarOnBackground))
        menu.findItem(R.id.menuMain).icon = menuMainDrawableIcon

        var searchDrawableIcon = menu.findItem(R.id.action_search).icon
        searchDrawableIcon = DrawableCompat.wrap(searchDrawableIcon!!)
        DrawableCompat.setTint(searchDrawableIcon, ContextCompat.getColor(this, toolbarOnBackground))
        menu.findItem(R.id.action_search).icon = searchDrawableIcon

        val searchItem = menu.findItem(R.id.action_search)
        var searchView = searchItem.actionView as SearchView?
        searchView!!.setBackgroundColor(ContextCompat.getColor(this, lightBackground))
        searchView.queryHint = "Search Here"
        searchView.maxWidth = Int.MAX_VALUE
        searchView.isIconified = false
        searchView.clearFocus()

//        searchView.setOnSearchClickListener {
//            searchView.setQuery(resources.getText(R.string.app_name), true)
//        }

        val searchClose = searchView.findViewById<View>(androidx.appcompat.R.id.search_close_btn) as ImageView
        val searchCloseIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_close_24)
        searchCloseIcon!!.setTint(ContextCompat.getColor(this, darkOnBackground))
        searchClose.background = searchCloseIcon
        searchClose.setImageResource(R.drawable.ic_baseline_close_24)
        searchClose.setBackgroundColor(ContextCompat.getColor(this, lightBackground))

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return when (id) {
            android.R.id.home -> {
                Log.d(TAG, "onOptionsItemSelected: Back Menu")
                finish()
//                Toast.makeText(this, "Back Menu", Toast.LENGTH_LONG).show()
                true
            }

            R.id.sort -> {
                Log.d(TAG, "onOptionsItemSelected: sort")
//                Toast.makeText(this, "sort", Toast.LENGTH_LONG).show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}




