package com.dinesh.mynotes

import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.AppBarLayout

private val TAG = "log_" + TMain::class.java.name.split(TMain::class.java.name.split(".").toTypedArray()[2] + ".").toTypedArray()[1]


class TMain : AppCompatActivity() {
    lateinit var toolbar: Toolbar
    lateinit var tvToolbar: TextView
    lateinit var toolbarSearchView: SearchView
    lateinit var appBarLayout: AppBarLayout
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.toolbar_layout)
        toolbar = findViewById(R.id.toolbar)
        tvToolbar = findViewById(R.id.tvToolbar)
        toolbarSearchView = findViewById(R.id.searchView)
        appBarLayout = findViewById(R.id.appBarLayout)
        setSupportActionBar(toolbar)

    }

override fun onBackPressed() {
    if (actionMode == null) {
        actionMode = toolbar.startActionMode(ActionModeCallback())
        invalidateOptionsMenu()
    } else {
        actionMode?.finish()
    }
}

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

//        tvToolbar = findViewById(R.id.tvToolbar)
//        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_200))
//        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)

        return true
    }


    inner class ActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate the menu and add items to it
            mode.menuInflater.inflate(R.menu.context_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Update the menu items as needed
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            // Handle menu item clicks
            when (item.itemId) {
                R.id.actionSelect -> {
                    Toast.makeText(this@TMain, "Action item 1 clicked", Toast.LENGTH_SHORT).show()
                    return true
                }
                R.id.actionReOrder -> {
                    Toast.makeText(this@TMain, "Action item 2 clicked", Toast.LENGTH_SHORT).show()
                    return true
                }
                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            // Clean up resources and finish the action mode
            actionMode = null
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return when (id) {
            android.R.id.home -> {
                Log.d(TAG, "onOptionsItemSelected: Back Menu")
                Toast.makeText(this, "Back Menu", Toast.LENGTH_LONG).show()
                true
            }
            R.id.sort -> {
                Log.d(TAG, "onOptionsItemSelected: sort")
                Toast.makeText(this, "sort", Toast.LENGTH_LONG).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}



/*

class TMain : ToolbarMain() {
    private val TAG = "log_" + TMain::class.java.name.split(TMain::class.java.name.split(".").toTypedArray()[2] + ".").toTypedArray()[1]
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        setNavigationDrawer()
        setToolbar()
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: ")
        val newToolbar = Toolbar(this)
        newToolbar.title = "New Toolbar"
        newToolbar.setNavigationIcon(R.drawable.ic_baseline_close_24)
        newToolbar.setNavigationOnClickListener {
            // Replace the new toolbar with the previous custom toolbar when the close icon is clicked
            setSupportActionBar(toolbar)
        }
        setSupportActionBar(newToolbar)
    }

//    override fun onBackPressed() {
//        if (actionMode == null) {
//            actionMode = toolbar.startActionMode(ActionModeCallback())
//        } else {
//            actionMode?.finish()
//        }
//    }


    inner class ActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate the menu and add items to it
            mode.menuInflater.inflate(R.menu.context_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Update the menu items as needed
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            // Handle menu item clicks
            when (item.itemId) {
                R.id.action_item_1 -> {
                    Toast.makeText(this@TMain, "Action item 1 clicked", Toast.LENGTH_SHORT).show()
                    return true
                }
                R.id.action_item_2 -> {
                    Toast.makeText(this@TMain, "Action item 2 clicked", Toast.LENGTH_SHORT).show()
                    return true
                }
                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            // Clean up resources and finish the action mode
            actionMode = null
        }
    }
}


*/
