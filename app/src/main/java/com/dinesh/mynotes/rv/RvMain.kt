package com.dinesh.mynotes.rv

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
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

    var encryptionKey = "QRY9fqKaBlsBJZLoUNfOZg=="
    var isEncrypted = false

    var i: Int = 0

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

        isPermissionGranted.observe(this){
            Log.e(TAG, "onCreate: isPermissionGranted ->> ${it}")
            if (it && ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                backupDialog()
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
                actionMode!!.title = "count selected"
                actionMode?.invalidate()
                return true
            }
            else -> { mode?.finish()
                return false }
        }
    }


    private var menuItemSelectAll: MenuItem? = null
    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        // Inflate the menu for the contextual action mode
        val inflater = mode?.menuInflater
        inflater?.inflate(R.menu.contextual_action_mode_menu, menu)
        menuItemSelectAll = menu?.findItem(R.id.menu_item_selectAll)
        if(!setItemClick){
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

        if (notesList.isNotEmpty()){
            if (notesList.size == count) {
                menuItemSelectAll?.title = "UnSelect All"
            } else{
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
                    backupDialog()
                } else {
                    requestPermission(this)
                }
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            R.id.menuRestore -> {
                restoreSpinnerDialog()
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            else -> super.onNavigationItemSelected(item)
        }
    }

    private fun backupDialog(){
        isEncrypted = false
        encryptionKey = "QRY9fqKaBlsBJZLoUNfOZg=="
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.dialog_save, null)
        builder.setView(view)
        val dialog = builder.create()
        dialog.show()
        val etFileName = view.findViewById<EditText>(R.id.etFileName)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnCancelBackup = view.findViewById<Button>(R.id.btnCancelBackup)
        val switch1 = view.findViewById<SwitchCompat>(R.id.switch1)
        val qrCodeImage = view.findViewById<ImageView>(R.id.qrCodeImage)
        val tvEncryptionKey = view.findViewById<TextView>(R.id.tvEncryptionKey)

        switch1.text = "Toggle to encrypt the Backup"
        qrCodeImage.visibility = View.GONE
        tvEncryptionKey.visibility = View.GONE

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

            } else {
                encryptionKey = "QRY9fqKaBlsBJZLoUNfOZg=="
                isEncrypted = false
                switch1.text = "Toggle to encrypt the Backup"
                qrCodeImage.visibility = View.GONE
                tvEncryptionKey.visibility = View.GONE
            }
        }

        btnSave.setOnClickListener {
            if(isEncrypted){
                if (etFileName.text.trim().isNotEmpty()) {
                    backupDatabaseToJSON(etFileName.text.toString()+".enc.json", encryptionKey, isEncrypted)
                } else{
                    backupDatabaseToJSON("notes.enc.json", encryptionKey, isEncrypted)
                }
            } else {
                if (etFileName.text.trim().isNotEmpty()) {
                    backupDatabaseToJSON(etFileName.text.toString()+".json", encryptionKey, isEncrypted)
                } else{
                    backupDatabaseToJSON("notes.json", encryptionKey, isEncrypted)
                }
            }
            dialog.cancel()
        }

        btnCancelBackup.setOnClickListener {
            dialog.cancel()
        }
    }

    private fun backupDatabaseToJSON(fileName: String, secretEncryptionKey: String = "QRY9fqKaBlsBJZLoUNfOZg==", isEncrypted: Boolean = false) {
        lifecycleScope.launch(Dispatchers.IO) {
            val backupFolder = File("/storage/emulated/0/Download/MyNote")
            if (!backupFolder.exists()) {
                backupFolder.mkdir()
            }

            if (isEncrypted){
                val backupFile = File(backupFolder, fileName)

                val notesList = notesLiveList.value
                if (!notesList.isNullOrEmpty()) {
                    val gsonBuilder = GsonBuilder()
                    gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, object: TypeAdapter<LocalDateTime>() {
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
                        val secretKey = secretEncryptionKey.toByteArray()
                        val skey = SecretKeySpec(secretKey, 0, secretKey.size, "AES")
                        val cipher = Cipher.getInstance("AES")
                        cipher.init(Cipher.ENCRYPT_MODE, skey)
                        val encrypted = cipher.doFinal(json.toByteArray())
                        backupFile.createNewFile()
                        val fos = FileOutputStream(backupFile)
                        fos.write(encrypted)
                        fos.flush()
                        fos.close()
                        Log.i(TAG, "backupDatabaseToJSON: Backup saved to ${backupFile.absolutePath}")
                        showSnackbar("Backup saved to ${backupFile.absolutePath}")
                        //save the key here to shared preference or any other secure storage
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

            } else{
                val backupFile = File(backupFolder, fileName)

                val notesList = notesLiveList.value
                if (!notesList.isNullOrEmpty()) {
                val gsonBuilder = GsonBuilder()
                gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, object: TypeAdapter<LocalDateTime>() {
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

                    try {
                        backupFile.createNewFile()
                        val fileWriter = FileWriter(backupFile)
                        fileWriter.write(json)
                        fileWriter.flush()
                        fileWriter.close()
                        Log.i(TAG, "backupDatabaseToJSON: Backup saved to ${backupFile.absolutePath}")
                        showSnackbar("Backup saved to ${backupFile.absolutePath}")
                    } catch (e: IOException) {
                        Log.e(TAG, "backupDatabaseToJSON: Error saving backup: ${e.message}")
                        showErrorSnackbar("Error saving backup: ${e.message}")
                    }
                } else {
                    Log.e(TAG, "backupDatabaseToJSON: No notes to backup")
                    showErrorSnackbar("No notes to backup")
                }
            }
        }
    }

    private fun restoreSpinnerDialog() {
        isEncrypted = false
        val directory = File("/storage/emulated/0/Download/MyNote/")
        val files = directory.listFiles()
        var spinnerItem = mutableListOf<String>()
        for (file in files) {
            if (file.extension == "json") {
                spinnerItem.add(file.name)
            }
        }

        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.dialog_restore, null)
        builder.setView(view)
        val dialog = builder.create()
        dialog.show()

        val autoCompleteTextView = view.findViewById<AutoCompleteTextView>(R.id.filled_exposed)

        if (!spinnerItem.isNullOrEmpty()) {
            val arrayAdapter = ArrayAdapter(this, R.layout.basic_spinner_custom_drop_down, spinnerItem)
            autoCompleteTextView.setAdapter(arrayAdapter)
            autoCompleteTextView.hint = "Select a backup file"

            autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                Toast.makeText(this, spinnerItem[position], Toast.LENGTH_SHORT).show()

                val backupFolder = File("/storage/emulated/0/Download/MyNote")
                val backupFile = File(backupFolder, spinnerItem[position])
                if (spinnerItem[position].endsWith(".enc.json")) {
                    isEncrypted = true
                    val encryptedBuilder = AlertDialog.Builder(this)
                    val encryptedInflater = layoutInflater
                    val encryptedView = encryptedInflater.inflate(R.layout.dialog_to_enter_decryption_key, null)
                    encryptedBuilder.setView(encryptedView)
                    val encryptedDialog = encryptedBuilder.create()
                    encryptedDialog.show()

                    val etDecryptionKey = encryptedView.findViewById<EditText>(R.id.etDecryptionKey)
                    val btnDecryptionKey = encryptedView.findViewById<Button>(R.id.btnDecryptionKey)
                    val btnCancelDecryption = encryptedView.findViewById<Button>(R.id.btnCancelDecryption)

                    btnDecryptionKey.setOnClickListener {
                        if (etDecryptionKey.text.toString().trim().length > 15) {
                            restoreRvDialog(backupFile, etDecryptionKey.text.toString().trim())
                            encryptedDialog.cancel()
                            dialog.cancel()
                        }
                    }

                    btnCancelDecryption.setOnClickListener {
                        encryptedDialog.cancel()
                        dialog.cancel()
                    }


                } else if (spinnerItem[position].endsWith(".json")) {
                    isEncrypted = false
                    restoreRvDialog(backupFile)
                    dialog.cancel()
                }
            }
        } else{
            autoCompleteTextView.hint = "Seems no backup file exist in path ${directory.absoluteFile}"
        }
    }


    private fun restoreRvDialog(fileName: File, decryptKey: String = "QRY9fqKaBlsBJZLoUNfOZg==") {
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
        gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, object: TypeAdapter<LocalDateTime>() {
            override fun write(out: JsonWriter?, value: LocalDateTime?) {
                out?.value(value.toString())
            }

            override fun read(input: JsonReader?): LocalDateTime {
                return LocalDateTime.parse(input?.nextString())
            }
        })
        val gson = gsonBuilder.create()
        val notesList: MutableList<Note> = mutableListOf()

        if (isEncrypted){
            try {
                val fileInputStream = FileInputStream(fileName)

                val secretKeySpec = SecretKeySpec(decryptKey.toByteArray(), "AES")
                val cipher = Cipher.getInstance("AES")
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
                val cipherInputStream = CipherInputStream(fileInputStream, cipher)

                val json = cipherInputStream.bufferedReader().use { it.readText() }
                notesList.addAll(gson.fromJson(json, Array<Note>::class.java).toList())
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring notes from JSON: ${e.message}")
                Toast.makeText(this@RvMain, "Error restoring notes from JSON", Toast.LENGTH_SHORT).show()
            }
        } else{
            try {
                val json = fileName.readText()
                notesList.addAll(gson.fromJson(json, Array<Note>::class.java).toList())
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring notes from JSON: ${e.message}")
                Toast.makeText(this@RvMain, "Error restoring notes from JSON", Toast.LENGTH_SHORT).show()
                showErrorSnackbar("Error restoring notes from JSON: ${e.message}")
            }
        }

        val adapter = RestoreAdapter(notesList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        checkBoxForAll.setOnCheckedChangeListener { buttonView, isChecked ->
            adapter.updateSelection(isChecked)
            if (isChecked){
                checkBoxForAll.text = "UnSelect All"
            } else{
                checkBoxForAll.text = "Select All"
            }
        }
        if (checkBoxForAll.isChecked){
            checkBoxForAll.text = "UnSelect All"
        } else{
            checkBoxForAll.text = "Select All"
        }

        btnOk.setOnClickListener {
            Log.d(TAG, "btnOk: ${adapter.getSelectedNotes()}")
            restoreDatabaseByInsertingNotesByJSON(adapter.getSelectedNotes())
            dialog.cancel()
        }

        btnCancel.setOnClickListener { dialog.cancel() }

        if(!notesList.isNullOrEmpty()){
            dialog.show()
        } else{
            val dialog = FancyDialog(this)
                .setTitle(getString(R.string.app_name))
                .setMessage("Make sure the backup file is not tampered or empty. \n\n\n " +
                        "If you haven't made any changes manually to the backup file and if you still face this error again then please immediately contact the developer.\n\n" +
                        "You can contact the developer via email at dk2811testmail@gmail.com")
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

    private fun restoreDatabaseByInsertingNotesByJSON(notes: List<Note>?) {
        if (!notes.isNullOrEmpty()) {
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
                notes.forEach {
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
