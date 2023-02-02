package com.dinesh.mynotes.rv

import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.dinesh.mynotes.R
import com.dinesh.mynotes.activity.AddNote
import com.dinesh.mynotes.activity.EditNote
import com.dinesh.mynotes.app.*
import com.dinesh.mynotes.room.Note
import com.dinesh.mynotes.room.NotesViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.time.LocalDateTime
import java.util.*
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec

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
    private lateinit var saveFileLauncher: ActivityResultLauncher<String>
    private lateinit var openFileLauncher: ActivityResultLauncher<Array<String>>

    var encryptionKey = "QRY9fqKaBlsBJZLoUNfOZg=="
    var isEncrypted = false

    var i: Int = 0
    private var selectedFileToRestore: String? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeRecyclerView()

        notesLiveList.observe(this) {
//            Log.d(TAG, "onCreate: ${it}")
            Log.d(TAG, "onCreate: ${it.size}")
            notesList = it
            rvAdapter.notesList = it
            rvAdapter.notifyDataSetChanged()
            if (it.isNotEmpty()) {
                i = it.last().id.toInt()
            }
        }

        isPermissionGranted.observe(this) {
            Log.e(TAG, "onCreate: isPermissionGranted ->> ${it}")
            if (it && ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                backup()
            }
        }


        restoreLauncher()
        backupLauncher()
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
                    true
                }
                R.id.menu_item_duplicate -> {
                    setItemClick = true
                    callback.setDragEnable(false)
                    Log.e(TAG, "longClickMenu: ${notesList[position]}")
                    lifecycleScope.launch(Dispatchers.IO) {
                        val maxId = notesViewModel.getMaxId().plus(1L)
                        note = notesList[position]
                        note.id = maxId
                        note.title = notesList[position].title + "(copy)"
                        notesViewModel.insert(note)
                        withContext(Dispatchers.Main) {
                            rvAdapter.notifyDataSetChanged()
                            showSnackbar("Duplicate notes created", Snackbar.LENGTH_SHORT)
                        }
                    }
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
//        for (i in i..(i+100)){
//        notesViewModel.insert(Note(title = "title $i", notes = "notes $i", dateCreated = LocalDateTime.now()))
//            }
//        }

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
                showSnackbar("${selectedItems.size} Notes deleted", Snackbar.LENGTH_SHORT)
                mode?.finish()
                return true
            }
            R.id.menu_item_selectAll -> {
                if (item.title.toString() == "Select All") {
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
                actionMode!!.title = "count selected"
                actionMode?.invalidate()
                return true
            }
            else -> {
                mode?.finish()
                return false
            }
        }
    }


    private var menuItemSelectAll: MenuItem? = null
    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        // Inflate the menu for the contextual action mode
        val inflater = mode?.menuInflater
        inflater?.inflate(R.menu.contextual_action_mode_menu, menu)
        menuItemSelectAll = menu?.findItem(R.id.menu_item_selectAll)
        if (!setItemClick) {
            menu!!.findItem(R.id.menu_item_delete).isVisible = false
            menu.findItem(R.id.menu_item_selectAll).isVisible = false
        }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        // Update the menu items here
        val count = rvAdapter.selectedItemCount
        val title = "$count selected"
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
            showSnackbar("Notes are successfully Re-Ordered", Snackbar.LENGTH_SHORT)
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

        if (notesList.isNotEmpty()) {
            if (notesList.size == count) {
                menuItemSelectAll?.title = "UnSelect All"
            } else {
                menuItemSelectAll?.title = "Select All"
            }
        }

        actionMode!!.title = "count selected"
        actionMode?.invalidate()
    }

    // TODO: Toolbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return when (id) {
            R.id.action_search -> {
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
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    backup()
                } else {
                    requestPermission(this)
                }
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            R.id.menuRestore -> {
                openFileLauncher.launch(arrayOf("*/*"))
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            else -> super.onNavigationItemSelected(item)
        }
    }


    private fun backupLauncher() {
        saveFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) {
                // Write dummy data to the file
                val fileOutputStream = contentResolver.openOutputStream(uri)

                if (isEncrypted) {
                    val notesList = notesLiveList.value
                    if (!notesList.isNullOrEmpty()) {
                        val gsonBuilder = GsonBuilder()
                        gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, object : TypeAdapter<LocalDateTime>() {
                            override fun write(out: JsonWriter?, value: LocalDateTime?) {
                                out?.value(value.toString())
                            }

                            override fun read(input: JsonReader?): LocalDateTime {
                                return LocalDateTime.parse(input?.nextString())
                            }
                        })
                        val gson = gsonBuilder.create()
                        val json = gson.toJson(notesList)

                        try {
                            val secretKey = encryptionKey.toByteArray()
                            val skey = SecretKeySpec(secretKey, 0, secretKey.size, "AES")
                            val cipher = Cipher.getInstance("AES")
                            cipher.init(Cipher.ENCRYPT_MODE, skey)
                            val encrypted = cipher.doFinal(json.toByteArray())
                            fileOutputStream?.let {
                                it.write(encrypted)
                                it.close()
                                Log.d(TAG, "File saved successfully at $uri")
                            } ?: Log.e(TAG, "Failed to save file at $uri")
                        } catch (e: IOException) {
                            Log.e(TAG, "backupDatabaseToJSON: Error saving backup: ${e.message}")
                            showErrorSnackbar("Error saving backup: ${e.message}")
                        } catch (e: NoSuchAlgorithmException) {
                            Log.e(TAG, "backupDatabaseToJSON: Error encrypting backup: ${e.message}")
                            showErrorSnackbar("Error encrypting backup: ${e.message}")
                        } catch (e: NoSuchPaddingException) {
                            Log.e(TAG, "backupDatabaseToJSON: Error encrypting backup: ${e.message}")
                            showErrorSnackbar("Error encrypting backup: ${e.message}")
                        } catch (e: InvalidKeyException) {
                            Log.e(TAG, "backupDatabaseToJSON: Error encrypting backup: ${e.message}")
                            showErrorSnackbar("Error encrypting backup: ${e.message}")
                        } catch (e: IllegalBlockSizeException) {
                            Log.e(TAG, "backupDatabaseToJSON: Error encrypting backup: ${e.message}")
                            showErrorSnackbar("Error encrypting backup: ${e.message}")
                        } catch (e: BadPaddingException) {
                            Log.e(TAG, "backupDatabaseToJSON: Error encrypting backup: ${e.message}")
                            showErrorSnackbar("Error encrypting backup: ${e.message}")
                        }
                    } else {
                        Log.e(TAG, "backupDatabaseToJSON: No notes to backup")
                        showErrorSnackbar("No notes to backup")
                    }

                } else {
                    val notesList = notesLiveList.value
                    if (!notesList.isNullOrEmpty()) {
                        val gsonBuilder = GsonBuilder()
                        gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, object : TypeAdapter<LocalDateTime>() {
                            override fun write(out: JsonWriter?, value: LocalDateTime?) {
                                out?.value(value.toString())
                            }

                            override fun read(input: JsonReader?): LocalDateTime {
                                return LocalDateTime.parse(input?.nextString())
                            }
                        })

//                    @SuppressLint("SuspiciousIndentation")
                        val gson = gsonBuilder.create()
                        val json = gson.toJson(notesList)
                        fileOutputStream?.let {
                            it.write(json.toByteArray())
                            it.close()
                            Log.d(TAG, "File saved successfully at $uri")
                        } ?: Log.e(TAG, "Failed to save file at $uri")
                    } else {
                        Log.e(TAG, "backupDatabaseToJSON: No notes to backup")
                        showErrorSnackbar("No notes to backup")
                    }
                }
            } else {
                Log.e(TAG, "No file URI received")
            }
        }
    }

    private fun backup() {
        isEncrypted = false
        encryptionKey = "QRY9fqKaBlsBJZLoUNfOZg=="
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.dialog_save, null)
        builder.setView(view)
        val dialog = builder.create()
        dialog.show()
//        val etFileName = view.findViewById<EditText>(R.id.etFileName)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnCancelBackup = view.findViewById<Button>(R.id.btnCancelBackup)
        val switch1 = view.findViewById<SwitchCompat>(R.id.switch1)
        val qrCodeImage = view.findViewById<ImageView>(R.id.qrCodeImage)
        val tvEncryptionKey = view.findViewById<TextView>(R.id.tvEncryptionKey)
        val etFileNameInputLayout = view.findViewById<TextInputLayout>(R.id.etFileNameInputLayout)
        val etFileName = view.findViewById<TextInputEditText>(R.id.etFileName)
        val tvUserMsg = view.findViewById<TextView>(R.id.tvUserMsg)

        etFileName.addTextChangedListener {
            if (etFileName.text.toString().isNotEmpty()) {
                etFileNameInputLayout.isHelperTextEnabled = false
            } else {
                etFileNameInputLayout.isHelperTextEnabled = true
                etFileNameInputLayout.helperText = "*Default file name will be 'notes'"
            }
        }


        switch1.text = "Toggle to encrypt the Backup"
        qrCodeImage.visibility = View.GONE
        tvEncryptionKey.visibility = View.GONE
        tvUserMsg.visibility = View.GONE

        switch1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switch1.text = "Backup will be encrypted"
                isEncrypted = true

                val keyGenerator = KeyGenerator.getInstance("AES")
                keyGenerator.init(128)
                val secretKey = keyGenerator.generateKey()
                val key = secretKey.encoded
                encryptionKey = Base64.getEncoder().encodeToString(key)

                tvEncryptionKey.text = "Copy the encryption key\n$encryptionKey"
                tvEncryptionKey.setOnClickListener {
                    val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("keyBase64", encryptionKey)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, "Text copied to clipboard: $encryptionKey", Toast.LENGTH_SHORT).show()
                }
                tvEncryptionKey.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_content_copy_24, 0)
                tvEncryptionKey.compoundDrawablePadding = 32

                try {
                    val bitMatrix = MultiFormatWriter().encode(encryptionKey, BarcodeFormat.QR_CODE, 250, 250)
                    val barcodeEncoder = BarcodeEncoder()
                    val bitmap = barcodeEncoder.createBitmap(bitMatrix)
                    qrCodeImage.setImageBitmap(bitmap)
                } catch (e: WriterException) {
                    e.printStackTrace()
                }

                qrCodeImage.visibility = View.VISIBLE
                tvEncryptionKey.visibility = View.VISIBLE
                tvUserMsg.visibility = View.VISIBLE

            } else {
                encryptionKey = "QRY9fqKaBlsBJZLoUNfOZg=="
                isEncrypted = false
                switch1.text = "Toggle to encrypt the Backup"
                qrCodeImage.visibility = View.GONE
                tvEncryptionKey.visibility = View.GONE
                tvUserMsg.visibility = View.GONE
            }
        }

        btnSave.setOnClickListener {
            if (!notesList.isNullOrEmpty()) {
                if (isEncrypted) {
                    if (etFileName.text.toString().trim().isNotEmpty()) {
                        saveFileLauncher.launch(etFileName.text.toString() + ".enc.json")
                    } else {
                        saveFileLauncher.launch("notes.enc.json")
                    }
                } else {
                    if (etFileName.text.toString().trim().isNotEmpty()) {
                        saveFileLauncher.launch(etFileName.text.toString() + ".json")
                    } else {
                        saveFileLauncher.launch("notes.json")
                    }
                }
            } else {
                Log.e(TAG, "backupDatabaseToJSON: No notes to backup")
                showErrorSnackbar("No notes to backup")
            }
            dialog.cancel()
        }

        btnCancelBackup.setOnClickListener {
            dialog.cancel()
        }
    }


    private fun restoreLauncher() {
        openFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            val cursor = uri?.let { contentResolver.query(it, null, null, null, null) }
            cursor?.use {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                selectedFileToRestore = cursor.getString(nameIndex)
                Log.d(TAG, "Selected file: $selectedFileToRestore")
                Log.d(TAG, "Selected file path: ${uri.path}")

                val inputStream = contentResolver.openInputStream(uri)
                inputStream?.use { inputStream ->
                    val data = inputStream.bufferedReader().readText()
//                    Log.e(TAG, "restoreLauncher: $data")
                    if (!selectedFileToRestore.isNullOrEmpty()) {
                        isEncrypted = false
                        if (selectedFileToRestore.toString().contains(".enc.json")) {
                            isEncrypted = true
                            val encryptedBuilder = AlertDialog.Builder(this)
                            val encryptedInflater = layoutInflater
                            val encryptedView = encryptedInflater.inflate(R.layout.dialog_to_enter_decryption_key, null)
                            encryptedBuilder.setView(encryptedView)
                            val encryptedDialog = encryptedBuilder.create()
                            encryptedDialog.show()

//                            val etDecryptionKey = encryptedView.findViewById<EditText>(R.id.etDecryptionKey)
                            val btnDecryptionKey = encryptedView.findViewById<Button>(R.id.btnDecryptionKey)
                            val btnCancelDecryption = encryptedView.findViewById<Button>(R.id.btnCancelDecryption)
                            val etDecryptionKeyInputLayout = encryptedView.findViewById<TextInputLayout>(R.id.etDecryptionKeyInputLayout)
                            val etDecryptionKey = encryptedView.findViewById<TextInputEditText>(R.id.etDecryptionKey)

                            etDecryptionKeyInputLayout.setEndIconOnClickListener {
                                val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
                                    val text = clipboard.primaryClip?.getItemAt(0)?.text.toString()
                                    etDecryptionKey.setText(text)
                                    etDecryptionKey.setSelection(etDecryptionKey.length())
                                    etDecryptionKey.requestFocus()
                                }
                            }

                            etDecryptionKey.addTextChangedListener {
                                if (etDecryptionKey.text.toString().length == 24) {
                                    etDecryptionKeyInputLayout.isHelperTextEnabled = false
                                } else {
                                    etDecryptionKeyInputLayout.isHelperTextEnabled = true
                                    etDecryptionKeyInputLayout.helperText = "*Decryption key must have 24 character"
                                }
                            }

                            btnDecryptionKey.setOnClickListener {
                                if (etDecryptionKey.text.toString().trim().length > 15) {
                                    restoreRvDialog(data, uri, etDecryptionKey.text.toString().trim())
                                    encryptedDialog.cancel()
                                }
                            }

                            btnCancelDecryption.setOnClickListener {
                                encryptedDialog.cancel()
                            }
                        } else if (selectedFileToRestore.toString().contains(".json") && !selectedFileToRestore.toString().contains(".enc.json")) {
                            Log.e(TAG, "restoreLauncher: $data")
                            isEncrypted = false
                            restoreRvDialog(data, uri)
                        } else {
                            isEncrypted = false

                        }
                    }
                } ?: Log.e(TAG, "Failed to read file data")
            }
        }
    }

    private fun restoreRvDialog(selectedFileToRestore: String, uri: Uri, decryptKey: String = "QRY9fqKaBlsBJZLoUNfOZg==") {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.dialog_restore_rv, null)
        builder.setView(view)
        val dialog = builder.create()

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val btnOk = view.findViewById<Button>(R.id.btnOk)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val checkBoxForAll = view.findViewById<CheckBox>(R.id.checkBoxForAll)

        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, object : TypeAdapter<LocalDateTime>() {
            override fun write(out: JsonWriter?, value: LocalDateTime?) {
                out?.value(value.toString())
            }

            override fun read(input: JsonReader?): LocalDateTime {
                return LocalDateTime.parse(input?.nextString())
            }
        })
        val gson = gsonBuilder.create()
        val notesList: MutableList<Note> = mutableListOf()

        if (isEncrypted) {
            val inputStream = contentResolver.openInputStream(uri)
            try {
                inputStream?.use { dataInputStream ->
                    val encryptedData = dataInputStream.readBytes()
                    val secretKey = decryptKey.toByteArray()
                    val skey = SecretKeySpec(secretKey, 0, secretKey.size, "AES")
                    val cipher = Cipher.getInstance("AES")
                    cipher.init(Cipher.DECRYPT_MODE, skey)
                    val decrypted = cipher.doFinal(encryptedData)
                    val decryptedString = decrypted.toString(Charsets.UTF_8)

                    val gsonBuilder = GsonBuilder()
                    gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, object : TypeAdapter<LocalDateTime>() {
                        override fun write(out: JsonWriter?, value: LocalDateTime?) {
                            out?.value(value.toString())
                        }

                        override fun read(input: JsonReader?): LocalDateTime {
                            return LocalDateTime.parse(input?.nextString())
                        }
                    })
                    val decryptedGson = gsonBuilder.create()
                    val decryptedNotesList = decryptedGson.fromJson(decryptedString, Array<Note>::class.java).toList()

                    Log.d(TAG, "Decrypted notes: $decryptedNotesList")
                    notesList.addAll(decryptedNotesList)
                }
            } catch (e: Exception) {
//                restoreErrorDialog("Error during decryption")     //  pad block corrupted
                Log.e(TAG, "restoreRvDialog: ${e.message}")
            }
        } else {
            try {
                notesList.addAll(gson.fromJson(selectedFileToRestore, Array<Note>::class.java).toList())
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring notes from JSON: ${e.message}")
                Toast.makeText(this@RvMain, "Error restoring notes from JSON", Toast.LENGTH_SHORT).show()
                showErrorSnackbar("Error restoring notes from JSON: ${e.message}")
            }
        }

        val adapter = RestoreAdapter(notesList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter.selectedNotesLiveData.observe(this) {
            if (!it.isNullOrEmpty()) {
                if (it.size == notesList.size) {
                    checkBoxForAll.isChecked = true
                    checkBoxForAll.text = "UnSelect All"
                } else {
                    checkBoxForAll.isChecked = false
                    checkBoxForAll.text = "Select All"
                }
            }
        }

        checkBoxForAll.setOnClickListener {
            val isChecked = checkBoxForAll.isChecked
            adapter.updateSelection(isChecked)
            if (isChecked) {
                checkBoxForAll.text = "UnSelect All"
            } else {
                checkBoxForAll.text = "Select All"
            }

        }

        if (checkBoxForAll.isChecked) {
            checkBoxForAll.text = "UnSelect All"
        } else {
            checkBoxForAll.text = "Select All"
        }

        btnOk.setOnClickListener {
            Log.d(TAG, "btnOk: ${adapter.getSelectedNotes()}")
            val selectedNotesList: List<Note> = adapter.getSelectedNotes()
            if (!selectedNotesList.isNullOrEmpty()) {
                var noteID = 0L
                var noteInsertedCount = 0

                val dialog = Dialog(this)
                dialog.setContentView(R.layout.progress_bar)
                dialog.setCancelable(false)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()

                @Suppress("SENSELESS_COMPARISON")
                lifecycleScope.launch(Dispatchers.IO) {
                    noteID = notesViewModel.getMaxId()
                    selectedNotesList.forEach {
                        noteID = noteID.plus(1L)
                        val note = it
                        note.id = noteID
                        val existingNote = notesViewModel.getNoteByTitleAndDescriptionAsNote(it.title, it.notes)
                        if (existingNote == null) {
                            notesViewModel.insert(note)
                            noteInsertedCount = noteInsertedCount.plus(1)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        showSnackbar("$noteInsertedCount Notes has been restored", Snackbar.LENGTH_LONG)
                        dialog.cancel()
                    }
                }
            }
            dialog.cancel()
        }

        btnCancel.setOnClickListener { dialog.cancel() }

        if (!notesList.isNullOrEmpty()) {
            dialog.show()
        } else {
            restoreErrorDialog("Error")
        }
    }

    private fun restoreErrorDialog(Title: String = getString(R.string.app_name)) {
        val dialog = FancyDialog(this)
            .setTitle(Title)
            .setMessage(
                "Make sure the backup file is not tampered & if you're decrypting then the file name should be '*.enc.json' and check if have entered correct decryption key. ('*' can be any valid file name) " +
                        "\n\n\n " +
                        "If you haven't made any changes manually to the backup file and if you still face this error again then please immediately contact the developer.\n\n" +
                        "You can contact the developer via email at dk2811testmail@gmail.com"
            )
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


//    private val REQUEST_EXTERNAL_STORAGE = 1000
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                backupDialog()
//            } else {
//                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }


}
