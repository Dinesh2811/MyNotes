package com.dinesh.mynotes.rv

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import androidx.room.Room
import com.dinesh.mynotes.R
import com.dinesh.mynotes.activity.AddNote
import com.dinesh.mynotes.activity.EditNote
import com.dinesh.mynotes.app.NavigationDrawer
import com.dinesh.mynotes.room.Note
import com.dinesh.mynotes.room.NotesViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class RvMain : NavigationDrawer(), RvInterface, ActionMode.Callback {
    private val TAG = "log_" + RvMain::class.java.name.split(RvMain::class.java.name.split(".").toTypedArray()[2] + ".").toTypedArray()[1]


    // TODO: initialization
    lateinit var v: View
    private lateinit var notesViewModel: NotesViewModel
    var notesLiveList: LiveData<List<Note>> = MutableLiveData()
    lateinit var rvAdapter: RvAdapter
    private lateinit var note: Note
    lateinit var notesList: List<Note>
    private var actionMode: ActionMode? = null
    var rvIsItemSelected: LiveData<Boolean> = MutableLiveData()
    private lateinit var recyclerView: RecyclerView
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var callback: MyItemTouchHelperCallback
    var setItemClick: Boolean = true
    var rvMultiSelectList = ArrayList<RvMultiSelectModel>()
    var newNotesList = ArrayList<Note>()

    var i: Int = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeRecyclerView()

        notesLiveList.observe(this) {
            Log.d(TAG, "onCreate: ${it}")
            Log.d(TAG, "onCreate: ${it.size}")
            notesList = it
            rvAdapter.notesList = it
            rvAdapter.notifyDataSetChanged()
            if (it.isNotEmpty()) {
                i = it.last().id.toInt()
            }
        }
    }

    private val addNoteClickListener = View.OnClickListener {
        CoroutineScope(Dispatchers.IO).launch {
            val intent = Intent(this@RvMain, AddNote::class.java)
            intent.putExtra("ID", notesViewModel.getMaxId())
            startActivity(intent)
        }
    }

    private fun initializeRecyclerView() {
        setContentView(R.layout.activity_main)
        setNavigationDrawer()

        val parentLayout = findViewById<LinearLayout>(R.id.parent_layout)

        v = LayoutInflater.from(this).inflate(R.layout.rv_main, parentLayout, false)
        parentLayout.addView(v)

        recyclerView = v.findViewById(R.id.recyclerView)
        floatingActionButton = v.findViewById(R.id.floatingActionButton)
        toolbarSearchView = findViewById(R.id.searchView)
        tvToolbar = findViewById(R.id.tvToolbar)

        toolbarSearchView.visibility = View.GONE
        tvToolbar.visibility = View.VISIBLE

        notesViewModel = ViewModelProvider(this)[NotesViewModel::class.java]
        notesLiveList = notesViewModel.getAllNotes()

        rvAdapter = RvAdapter(emptyList(), this@RvMain, notesViewModel, this@RvMain)
//        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
//        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.adapter = rvAdapter

        callback = MyItemTouchHelperCallback(rvAdapter, this@RvMain)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recyclerView)
        callback.setDragEnable(false)

        floatingActionButton.setOnClickListener(addNoteClickListener)
    }

    override fun onItemClick(view: View?, position: Int) {
        if (setItemClick) {
            if (actionMode != null) {
                toggleSelection(position)
            } else {
                val intent = Intent(this, EditNote::class.java)
                intent.putExtra("NOTE_ID", notesList[position].id)
                Log.d(TAG, "onItemClick: ${notesList[position].id}")
                startActivity(intent)
            }
        }
    }

    override fun onLongClick(view: View?, position: Int, rvSelectedItemCount: Int) {
        if (actionMode == null) {
            longClickMenu(view, position)
        }

        Log.d(TAG, "onLongClick: ${rvAdapter.selectedItems.get(position, false)}")
    }

    private fun longClickMenu(view: View?, position: Int) {
        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.context_menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.actionSelect -> {
                    actionMode = startActionMode(this)
                    toggleSelection(position)
                    callback.setDragEnable(false)
                    true
                }
                R.id.actionReOrder -> {
                    setItemClick = false
                    actionMode = startActionMode(this)
                    callback.setDragEnable(true)
                    actionMode!!.title = "Drag & Re-Order"
                    recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
//                    recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

//    override fun onBackPressed() {
//        Log.e(TAG, "onBackPressed: ")
//        var i = 0
//        for (i in i..(i+11)){
//        notesViewModel.insert(Note(title = "title $i", notes = "notes $i", dateCreated = LocalDateTime.now()))
//            }
//        }
//    }

    // TODO: ActionMode
    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        // Handle menu item clicks here
        when (item?.itemId) {
            R.id.menu_item_delete -> {
                // Delete the selected items
                val selectedItems = rvAdapter.getSelectedItems()
                for (i in selectedItems.size - 1 downTo 0) {
                    val position = selectedItems[i]
                    notesViewModel.delete(notesList[position])
//                    (notesList as MutableList<Note>).removeAt(position)
                    rvAdapter.notifyItemRemoved(position)
                }
                mode?.finish()
                return true
            }
            R.id.menu_item_selectAll -> {
                if (item.title.toString() == "Select All"){
                    notesList.forEachIndexed { i, it ->
                        rvAdapter.selectedItems.put(i, true)
                    }
                    item.title = "UnSelect All"
                } else {
                    notesList.forEachIndexed { i, it ->
                        rvAdapter.selectedItems.delete(i)
                    }
                    item.title = "Select All"
                }
                rvAdapter.notifyDataSetChanged()
                actionMode!!.title = "count items selected"
                actionMode?.invalidate()
                return true
            }
            else -> { mode?.finish()
                return false }
        }
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        // Inflate the menu for the contextual action mode
        val inflater = mode?.menuInflater
        inflater?.inflate(R.menu.contextual_action_mode_menu, menu)
        if(!setItemClick){
//            if (menu != null) {
                menu!!.findItem(R.id.menu_item_delete).isVisible = false
                menu.findItem(R.id.menu_item_selectAll).isVisible = false
//            }
        }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        // Update the menu items here
        val count = rvAdapter.selectedItemCount
        val title = "$count items selected"
        mode?.title = title
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        // End the action mode here
        (rvAdapter.rvSelectedItemCount as MutableLiveData).value = 0
        actionMode = null
        callback.setDragEnable(false)
        rvAdapter.clearSelection()
        Log.e(TAG, "onDestroyActionMode: ")

        if (!setItemClick) {
            rvMultiSelectList.clear()
            newNotesList = notesList as ArrayList<Note>
            newNotesList.forEachIndexed { i, it ->
                rvMultiSelectList.add(RvMultiSelectModel(it.id.toInt(), i))
            }
            rvMultiSelectList.forEachIndexed { i, it ->
                note = notesList[i]
                note.customPosition = it.toPosition
                notesViewModel.update(note)
            }
            setItemClick = true
            recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }


    private fun toggleSelection(position: Int) {
        if (rvAdapter.selectedItems.get(position, false)) {
            rvAdapter.selectedItems.delete(position)
        } else {
            rvAdapter.selectedItems.put(position, true)
        }
        rvAdapter.notifyItemChanged(position)
        val count = rvAdapter.selectedItemCount
        if (count == 0) {
            actionMode?.finish()
            callback.setDragEnable(false)

//            if (!setItemClick) {
//            newNotesList = notesList as ArrayList<Note>
//            newNotesList.forEachIndexed { i, it ->
//                rvMultiSelectList.add(RvMultiSelectModel(it.id.toInt(), i))
//            }
//            rvMultiSelectList.forEachIndexed{i,it->
//                note = notesList[i]
//                note.customPosition = it.toPosition
//                notesViewModel.update(note)
//            }
//            setItemClick = true
//            recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
//            }
        } else {
            actionMode!!.title = "count items selected"
            actionMode?.invalidate()
        }
    }

    // TODO: Toolbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return when (id) {
            R.id.action_search -> {
                Log.d(TAG, "onOptionsItemSelected: action_search")
                Toast.makeText(this, "action_search", Toast.LENGTH_LONG).show()
                val searchView = item.actionView as SearchView
                searchView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                val searchClose = searchView.findViewById<View>(androidx.appcompat.R.id.search_close_btn) as ImageView
                searchClose.setImageResource(R.drawable.ic_baseline_close_24)

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        filterList(newText)
                        return false
                    }
                })
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // TODO: RvSearchFilter
    private fun filterList(query: String?) {
        if (query != null) {
            val filteredList = ArrayList<Note>()
            for (i in notesList) {
                if (i.title.lowercase(Locale.ROOT).contains(query, false) ||
                    i.notes.lowercase(Locale.ROOT).contains(query, false)
                ) {
                    filteredList.add(i)
                }
            }
            if (filteredList.isEmpty()) {
                rvAdapter.setFilteredList(filteredList)
                Log.d(TAG, "filterList: No Data found")
            } else {
                rvAdapter.setFilteredList(filteredList)
            }
        }
    }

    // TODO: MyItemTouchHelperCallback
//    fun onItemMove(fromPosition: Int, toPosition: Int) {
//        CoroutineScope(Dispatchers.Main).launch {
//            // Move the item in the data source
//            Collections.swap(notesList, fromPosition, toPosition)
//            Log.d(TAG, "fromPosition: ${fromPosition}          toPosition: ${toPosition}")
//            // Notify the adapter that an item moved
//            rvAdapter.notifyItemMoved(fromPosition, toPosition)
//        }
//    }


    fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(notesList, fromPosition, toPosition)
        Log.d(TAG, "fromPosition: ${fromPosition}          toPosition: ${toPosition}")
        // Notify the adapter that an item moved
        rvAdapter.notifyItemMoved(fromPosition, toPosition)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//        return super.onNavigationItemSelected(item)
        return when (item.itemId) {
            R.id.menuBackup -> {
                Toast.makeText(this, "This feature will be added soon", Toast.LENGTH_SHORT).show()
//                // request for the storage permission
//                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_EXTERNAL_STORAGE)
//                } else {
//                    backupDatabase()
//                }
                drawerLayout!!.closeDrawer(GravityCompat.START)
                true
            }
            R.id.menuRestore -> {
                Toast.makeText(this, "This feature will be added soon", Toast.LENGTH_SHORT).show()
//                restoreDatabase()
//                notesLiveList.observe(this) {
//                    Log.d(TAG, "onCreate: ${it}")
//                    Log.d(TAG, "onCreate: ${it.size}")
//                    notesList = it
//                    rvAdapter.notesList = it
//                    rvAdapter.notifyDataSetChanged()
//                    if (it.isNotEmpty()) {
//                        i = it.last().id.toInt()
//                    }
//                }
//                notesViewModel.notesLiveList = notesViewModel.noteDao.getAllNotes()
//                rvAdapter.notifyDataSetChanged()
                drawerLayout!!.closeDrawer(GravityCompat.START)
                true
            }
            else -> super.onNavigationItemSelected(item)
        }
    }

    private fun restoreDatabase() {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val myNoteDir = File(downloadDir, "MyNote")
        val exportFile = File(myNoteDir, "notes.db")
        val inputFile = FileInputStream(exportFile)

        // Close the original database
//        notesViewModel.closeDb()
        val databaseFile = getDatabasePath("notes_database")
        // Delete the original database file
        databaseFile.delete()
        val outputFile = FileOutputStream(databaseFile)

        val buffer = ByteArray(1024)
        var length: Int
        while (inputFile.read(buffer).also { length = it } > 0) {
            outputFile.write(buffer, 0, length)
        }
        inputFile.close()
        outputFile.flush()
        outputFile.close()
        Toast.makeText(this, "Database imported from Download/MyNote folder", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "restoreDatabase: Database imported ${exportFile.absolutePath}")
        // Reopen the database
//        notesViewModel.openDb()
    }

//    private fun restoreDatabase() {
//        // Restore the Database
//        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//        val myNoteDir = File(downloadDir, "MyNote")
//        val file = File(myNoteDir, "notes.db")
//        val inputFile = FileInputStream(file)
//        val outputFile = FileOutputStream(getDatabasePath("notes_database"))
//        val buffer = ByteArray(1024)
//        var length: Int
//        while (inputFile.read(buffer).also { length = it } > 0) {
//            outputFile.write(buffer, 0, length)
//        }
//        inputFile.close()
//        outputFile.flush()
//        outputFile.close()
//        Toast.makeText(this, "Database imported from Download/MyNote folder", Toast.LENGTH_SHORT).show()
//        Log.d(TAG, "restoreDatabase: Database imported ${file.absolutePath}")
//
//    }

    private val REQUEST_EXTERNAL_STORAGE = 1

    private fun backupDatabase() {
//        val backupFile = File(Environment.getExternalStorageDirectory(), "Download/MyNotes/note")
//        val currentDB = application.getDatabasePath("notes_database").path
//        val backupDB = backupFile.path
//
//        val src = FileInputStream(currentDB).channel
//        val dst = FileOutputStream(backupDB).channel
//        dst.transferFrom(src, 0, src.size())
//        src.close()
//        dst.close()
        Log.d(TAG, "backupDatabase: ")
        Toast.makeText(this, "backupDatabase", Toast.LENGTH_SHORT).show()
        val backupFile = File(Environment.getExternalStorageDirectory(), "Download/MyNotes/note")
        if (!backupFile.parentFile.exists()) {
            backupFile.parentFile.mkdirs()
        }
        if (!backupFile.exists()) {
            backupFile.createNewFile()
        }
        val currentDB = application.getDatabasePath("notes_database").path
        val backupDB = backupFile.path
        val src = FileInputStream(currentDB).channel
        val dst = FileOutputStream(backupDB).channel
    }
//    private fun backupDatabase() {
//        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//        val myNoteDir = File(downloadDir, "MyNote")
//        if (!myNoteDir.exists()) {
//            myNoteDir.mkdirs()
//        }
//        val file = File(myNoteDir, "notes.db")
//        val inputFile = FileInputStream(getDatabasePath("notes_database"))
//        val outputFile = FileOutputStream(file)
//        val buffer = ByteArray(1024)
//        var length: Int
//        while (inputFile.read(buffer).also { length = it } > 0) {
//            outputFile.write(buffer, 0, length)
//        }
//        inputFile.close()
//        outputFile.flush()
//        outputFile.close()
//        Toast.makeText(this, "Database exported to Download/MyNote folder", Toast.LENGTH_SHORT).show()
//        Log.d(TAG, "backupDatabase: Database exported ${file.absolutePath}")
//    }

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


}
