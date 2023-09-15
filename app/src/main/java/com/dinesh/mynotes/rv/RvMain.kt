package com.dinesh.mynotes.rv

import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
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
import androidx.documentfile.provider.DocumentFile
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
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.lang.reflect.Type
import java.security.InvalidKeyException
import java.security.Key
import java.security.NoSuchAlgorithmException
import java.time.LocalDateTime
import java.util.*
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
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
    private lateinit var recyclerView: RecyclerView
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var callback: MyItemTouchHelperCallback
    var setItemClick: Boolean = true
    var rvMultiSelectList = ArrayList<RvMultiSelectModel>()
    var newNotesList = ArrayList<Note>()
    private lateinit var saveFileLauncher: ActivityResultLauncher<String>
    private lateinit var openFileLauncher: ActivityResultLauncher<Array<String>>

    var encryptionKey = ""
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

        // TODO: requestPermission
//        isPermissionGranted.observe(this) {
//            Log.e(TAG, "onCreate: isPermissionGranted ->> ${it}")
//            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
//                if (it && ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                    backup()
//                }
//            } else {
//                backup()
//            }
//        }

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
            // TODO: requestPermission
            R.id.menuBackup -> {
                backup()
//                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
//                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                        backup()
//                    } else {
//                        requestPermission(this)
//                    }
//                } else {
//                    backup()
//                }

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


    private lateinit var notesJson: String
    private lateinit var encryptedData: ByteArray

    companion object {
        private const val AES_MODE = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
        private const val AES_KEY_SIZE = 256
    }

    private lateinit var ENCRYPTION_KEY: Key

    private fun backupLauncher() {
        saveFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) {
                val file = DocumentFile.fromSingleUri(this, uri)
                val fileName = file?.name
                Log.d(TAG, "File name: $fileName")


                if ((fileName != null)) {
                    if (!fileName.contains(".json")) {
                        Log.e(TAG, "Error: file name doesn't end with '.json', deleting the file")
                        showErrorSnackbar("Error: File name doesn't end with '.json'", Snackbar.LENGTH_INDEFINITE)
                        file.delete()
                        return@registerForActivityResult
                    }
                    if (isEncrypted) {
                        if (!((fileName.contains(".enc")))) {
                            Log.e(TAG, "Error: file name doesn't end with '.enc.json', deleting the file")
                            showErrorSnackbar("Error: File name doesn't end with '.enc.json'", Snackbar.LENGTH_INDEFINITE)
                            file.delete()
                            return@registerForActivityResult
                        }
                    } else {
                        if (fileName.contains(".enc")) {
                            Log.e(TAG, "Error: file name doesn't end with '.json' or has '.enc', deleting the file")
                            showErrorSnackbar("Error: File name can't have '.enc'", Snackbar.LENGTH_INDEFINITE)
                            file.delete()
                            return@registerForActivityResult
                        }
                    }
                }

                val gsonBuilder = GsonBuilder()
                gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
                gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
                val gsonWithCustomSerializer = gsonBuilder.create()

                if (!notesList.isNullOrEmpty()) {
                    notesJson = gsonWithCustomSerializer.toJson(notesList)

                    try {
                        contentResolver.openOutputStream(uri)?.use { outputStream ->
                            if (isEncrypted) {
                                val cipher = Cipher.getInstance(AES_MODE)
                                cipher.init(Cipher.ENCRYPT_MODE, ENCRYPTION_KEY, getGCMParameterSpec())
                                encryptedData = cipher.doFinal(notesJson.toByteArray(Charsets.UTF_8))

                                outputStream.write(encryptedData)

                                val snackBarTitle = "Please make sure you safely secure the encryption key in a password manager. " +
                                        "Without the key you won't able to decrypt the backup file. \n\n" +
                                        "(Long press to dismiss)"

                                val btnCopyClickListener: (v: View) -> Unit = {
                                    val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("keyBase64", encryptionKey)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(this, "Text copied to clipboard: $encryptionKey", Toast.LENGTH_SHORT).show()
                                    FancySnackbarLayout().dismiss()
                                }
                                val btnShareClickListener: (v: View) -> Unit = {
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "The encryption key for the file '$fileName' is '$encryptionKey'")
                                        type = "text/plain"
                                    }

                                    startActivity(Intent.createChooser(sendIntent, "Share using"))
                                    FancySnackbarLayout().dismiss()
                                }
                                val tvLongClickListener = View.OnLongClickListener {
                                    FancySnackbarLayout().dismiss()
                                    true
                                }

                                FancySnackbarLayout()
                                    .makeCustomLayout(
                                        findViewById(android.R.id.content),
                                        this,
                                        snackBarTitle,
                                        Snackbar.LENGTH_INDEFINITE,
                                        btnCopyClickListener,
                                        btnShareClickListener,
                                        tvLongClickListener
                                    )
                                    .show()

                            } else {
                                outputStream.write(notesJson.toByteArray())
                            }
                            Log.i(TAG, "File successfully created at: $uri")
//                            Toast.makeText(this, "File successfully created at: $uri", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving backup: ${e.message}")
                        showErrorSnackbar("Error saving backup: ${e.message}", Snackbar.LENGTH_LONG)
                    }
                } else {
                    Log.e(TAG, "backupDatabaseToJSON: No notes to backup")
                    showErrorSnackbar("No notes to backup", Snackbar.LENGTH_LONG)
                }
            } else {
                Log.e(TAG, "Error creating file")
                showErrorSnackbar("Error creating file", Snackbar.LENGTH_LONG)
            }
        }
    }

    private fun backup() {
        isEncrypted = false
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.dialog_save, null)
        builder.setView(view)
        val dialog = builder.create()
        dialog.show()

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
                etFileNameInputLayout.isHelperTextEnabled = true
                etFileNameInputLayout.helperText = " "
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
                getEncryptionKey()
                encryptionKey = Base64.encodeToString(ENCRYPTION_KEY.encoded, Base64.DEFAULT)

                tvEncryptionKey.text = "Copy the encryption key:\n\n$encryptionKey"
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

    private fun decryptDataAsString(encryptedData: ByteArray, Key: String): String {
        val encryptionKeyBytes = Base64.decode(Key, Base64.DEFAULT)
        val decryptionKey = SecretKeySpec(encryptionKeyBytes, 0, encryptionKeyBytes.size, "AES")

        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.DECRYPT_MODE, decryptionKey, getGCMParameterSpec())
        return String(cipher.doFinal(encryptedData), Charsets.UTF_8)
    }

    private fun restoreLauncher() {
        openFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri == null) {
                Log.e(TAG, "Uri is null, the file selection has been cancelled")
                Toast.makeText(this, "The file selection has been cancelled", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            isEncrypted = false

            try {
                uri.let {
                    val file = DocumentFile.fromSingleUri(this, it)
                    val fileName = file?.name
                    Log.d(TAG, "File name: $fileName")

                    val inputStream = contentResolver.openInputStream(it)
                    inputStream?.let {
                        val byteArray = inputStream.readBytes()
                        var restoreData: String = "[{}]"

                        if (fileName != null) {
                            if (fileName.contains(".enc") && fileName.contains(".json")) {
                                isEncrypted = true

                                val encryptedBuilder = AlertDialog.Builder(this)
                                val encryptedInflater = layoutInflater
                                val encryptedView = encryptedInflater.inflate(R.layout.dialog_to_enter_decryption_key, null)
                                encryptedBuilder.setView(encryptedView)
                                val encryptedDialog = encryptedBuilder.create()
                                encryptedDialog.show()

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
                                        etDecryptionKeyInputLayout.helperText = "*Decryption key must have 44 character"
                                    }
                                }

                                btnDecryptionKey.setOnClickListener {
                                    if (etDecryptionKey.text.toString().trim().length > 15) {
                                        encryptionKey = etDecryptionKey.text.toString().trim()
//                                        Log.e(TAG, "restoreLauncher: decryptionKey --> ${etDecryptionKey.text.toString().trim()}")

                                        try {
                                            encryptedDialog.cancel()
                                            restoreData = decryptDataAsString(byteArray, encryptionKey)
                                            if (!validateEncryptedData(byteArray, restoreData, encryptionKey)) {
                                                Log.e(TAG, "The encrypted data in the file has been changed")
                                                showErrorSnackbar("The encrypted data in the file must been changed", Snackbar.LENGTH_INDEFINITE)
                                                Toast.makeText(this, "The encrypted data in the file has been changed", Toast.LENGTH_SHORT).show()
                                                return@setOnClickListener
                                            } else {
                                                Log.i(TAG, "The encrypted data in the file hasn't changed")
                                            }
                                            restoreRvDialog(convertStringToList(restoreData))
                                        } catch (e: IOException) {
                                            Log.e(TAG, "backupDatabaseToJSON: Error restoring the backup: ${e.message}")
//                            showErrorSnackbar("Error saving backup: ${e.message.toString()}")
                                            FancySnackbarLayout()
                                                .makeCustomLayout(findViewById(android.R.id.content), this, e.message.toString(),
                                                    btnShareClickListener = { FancySnackbarLayout().dismiss() })
                                                .setInit(btnCopyView = View.GONE, btnShareImage = R.drawable.ic_baseline_close_24)
                                                .show()
                                        } catch (e: NoSuchAlgorithmException) {
                                            Log.e(TAG, "backupDatabaseToJSON: Error decrypting backup: ${e.message}")
//                            showErrorSnackbar("Error decrypting backup: ${e.message.toString()}")
                                            FancySnackbarLayout()
                                                .makeCustomLayout(findViewById(android.R.id.content), this, e.message.toString(),
                                                    btnShareClickListener = { FancySnackbarLayout().dismiss() })
                                                .setInit(btnCopyView = View.GONE, btnShareImage = R.drawable.ic_baseline_close_24)
                                                .show()
                                        } catch (e: NoSuchPaddingException) {
                                            Log.e(TAG, "backupDatabaseToJSON: Error decrypting backup: ${e.message}")
//                            showErrorSnackbar("Error decrypting backup: ${e.message.toString()}")
                                            FancySnackbarLayout()
                                                .makeCustomLayout(findViewById(android.R.id.content), this, e.message.toString(),
                                                    btnShareClickListener = { FancySnackbarLayout().dismiss() })
                                                .setInit(btnCopyView = View.GONE, btnShareImage = R.drawable.ic_baseline_close_24)
                                                .show()
                                        } catch (e: InvalidKeyException) {
                                            Log.e(TAG, "backupDatabaseToJSON: Error decrypting backup: ${e.message}")
//                            showErrorSnackbar("Error decrypting backup: ${e.message.toString()}")
                                            FancySnackbarLayout()
                                                .makeCustomLayout(findViewById(android.R.id.content), this, e.message.toString(),
                                                    btnShareClickListener = { FancySnackbarLayout().dismiss() })
                                                .setInit(btnCopyView = View.GONE, btnShareImage = R.drawable.ic_baseline_close_24)
                                                .show()
                                        } catch (e: IllegalBlockSizeException) {
                                            Log.e(TAG, "backupDatabaseToJSON: Error decrypting backup: ${e.message}")
//                            showErrorSnackbar("Error decrypting backup: ${e.message.toString()}")
                                            FancySnackbarLayout()
                                                .makeCustomLayout(findViewById(android.R.id.content), this, e.message.toString(),
                                                    btnShareClickListener = { FancySnackbarLayout().dismiss() })
                                                .setInit(btnCopyView = View.GONE, btnShareImage = R.drawable.ic_baseline_close_24)
                                                .show()
                                        } catch (e: BadPaddingException) {
                                            Log.e(TAG, "backupDatabaseToJSON: Error decrypting backup: ${e.message}")
//                            showErrorSnackbar("Error decrypting backup: ${e.message.toString()}")
                                            FancySnackbarLayout()
                                                .makeCustomLayout(findViewById(android.R.id.content), this, e.message.toString(),
                                                    btnShareClickListener = { FancySnackbarLayout().dismiss() })
                                                .setInit(btnCopyView = View.GONE, btnShareImage = R.drawable.ic_baseline_close_24)
                                                .show()
                                        }
                                    }
                                }

                                btnCancelDecryption.setOnClickListener {
                                    encryptedDialog.cancel()
                                }
                            } else if (fileName.contains(".json")) {
                                restoreData = byteArray.toString(Charsets.UTF_8)
                                Log.i(TAG, "restoreLauncher: ${convertStringToList(restoreData)}")
                                restoreRvDialog(convertStringToList(restoreData))
                            }
                        }
                    }
                    inputStream?.close()
                } ?: Log.e(TAG, "Uri is null")
            } catch (e: Exception) {
                Log.e(TAG, "Error while reading the file: ${e.message}")
                Toast.makeText(this, "Error while reading the file", Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun restoreRvDialog(listOfNotes: List<Note>) {
        Log.e(TAG, "restoreRvDialog: ${listOfNotes.size}")
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.dialog_restore_rv, null)
        builder.setView(view)
        val dialog = builder.create()

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val btnOk = view.findViewById<Button>(R.id.btnOk)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val checkBoxForAll = view.findViewById<CheckBox>(R.id.checkBoxForAll)

        val adapter = RestoreAdapter(listOfNotes)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter.selectedNotesLiveData.observe(this) {
            if (!it.isNullOrEmpty()) {
                if (it.size == listOfNotes.size) {
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

        if (!listOfNotes.isNullOrEmpty()) {
            dialog.show()
        } else {
            restoreErrorDialog("Error")
        }
    }

    private fun getEncryptionKey(): Key {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(AES_KEY_SIZE)
        ENCRYPTION_KEY = keyGen.generateKey()
        return ENCRYPTION_KEY
    }

    private fun getGCMParameterSpec(): GCMParameterSpec {
        val iv = ByteArray(GCM_IV_LENGTH)
        return GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
    }

    private fun validateEncryptedData(encryptedData: ByteArray, decryptedData: String, decryptionKey: String): Boolean {
        val encryptionKeyBytes = Base64.decode(decryptionKey, Base64.DEFAULT)
        val DECRYPTION_KEY = SecretKeySpec(encryptionKeyBytes, 0, encryptionKeyBytes.size, "AES")

        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, DECRYPTION_KEY, getGCMParameterSpec())
        val encryptedDataCheck = cipher.doFinal(decryptedData.toByteArray(Charsets.UTF_8))

        return encryptedData.contentEquals(encryptedDataCheck)
    }

    private fun convertStringToList(restoreData: String): List<Note> {
        try {
            //  convertListToJson
            val gsonBuilder = GsonBuilder()
            gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
            gsonBuilder.registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
            val gsonWithCustomSerializer = gsonBuilder.create()
//            val notesJson = gsonWithCustomSerializer.toJson(listOfNotes)

            //  convertJsonToList
            val notesType = object : TypeToken<List<Note>>() {}.type
            val notesList = gsonWithCustomSerializer.fromJson<List<Note>>(restoreData, notesType)

            return notesList
        } catch (e: Exception) {
            Log.e(TAG, "${e.message}")
            return emptyList()
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

}

class LocalDateTimeSerializer : JsonSerializer<LocalDateTime> {
    override fun serialize(src: LocalDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src.toString())
    }
}

class LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDateTime {
        return LocalDateTime.parse(json?.asString)
    }
}


