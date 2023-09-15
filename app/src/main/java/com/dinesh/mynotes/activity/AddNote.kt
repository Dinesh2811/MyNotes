package com.dinesh.mynotes.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.dinesh.mynotes.R
import com.dinesh.mynotes.app.NavigationDrawer
import com.dinesh.mynotes.room.Note
import com.dinesh.mynotes.room.NotesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AddNote : NavigationDrawer() {
    private val TAG = "log_AddNote"

    lateinit var v: View
    private lateinit var notesViewModel: NotesViewModel
    lateinit var etTitle: EditText
    lateinit var etNote: EditText
    private var title: String = ""
    private var noteDes: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeRecyclerView()
        notesViewModel = ViewModelProvider(this)[NotesViewModel::class.java]

        title = etTitle.text.toString()
        noteDes = etNote.text.toString()

//        val editable = etNote.text as Editable
//
//        val spannable = SpannableStringBuilder.valueOf(editable)
//        Linkify.addLinks(spannable, Linkify.WEB_URLS)
//
//        val clickableSpan = object : ClickableSpan() {
//            override fun onClick(widget: View) {
//                // Handle the URL click event here
//                val url = (widget as EditText).text.subSequence(widget.selectionStart, widget.selectionEnd).toString()
//                // Open the URL using an appropriate method (e.g., using an Intent to open a browser)
//            }
//        }
//
//        val spans = spannable.getSpans(0, spannable.length, URLSpan::class.java)
//        for (span in spans) {
//            val start = spannable.getSpanStart(span)
//            val end = spannable.getSpanEnd(span)
//            val flags = spannable.getSpanFlags(span)
//            spannable.removeSpan(span)
//            spannable.setSpan(clickableSpan, start, end, flags)
//        }
//
//        etNote.text = spannable
//        etNote.movementMethod = LinkMovementMethod.getInstance()

        etTitle.addTextChangedListener {
            if (!it.isNullOrBlank()) {
                title = it.toString()
            }
        }
        etNote.addTextChangedListener {
            if (!it.isNullOrBlank()) {
                noteDes = it.toString()
            }
        }

//        Log.e(TAG, "onCreate: ${intent.getLongExtra("ID", 0)}")

    }

    override fun onPause() {
        super.onPause()
        addNote()
    }

    private fun addNote() {
        val trimmedTitle = title
        val trimmedNoteDes = noteDes

        if (trimmedTitle.isNotBlank() || trimmedNoteDes.isNotBlank()) {

            CoroutineScope(Dispatchers.IO).launch {
                notesViewModel.insert(Note(id = (intent.getLongExtra("ID", 0).plus(1L)), title = trimmedTitle, notes = trimmedNoteDes, dateCreated = LocalDateTime.now()))
            }
//            Toast.makeText(this, "Your note is Created successfully", Toast.LENGTH_SHORT).show()

//            val existingNoteLiveData = notesViewModel.getNoteByTitleAndDescription(trimmedTitle, trimmedNoteDes)
//            existingNoteLiveData.observe(this) {
//                if (it == null) {
//                    CoroutineScope(Dispatchers.IO).launch {
//                        notesViewModel.insert(Note(id= (intent.getLongExtra("ID",0).plus(1L)),title = trimmedTitle, notes = trimmedNoteDes, dateCreated = LocalDateTime.now()))
//                    }
//                    Toast.makeText(this, "Your note is Created successfully", Toast.LENGTH_SHORT).show()
//                }
//            }
        }
    }

    private fun initializeRecyclerView() {
        setContentView(R.layout.activity_main)
        setToolbar()

        val parentLayout = findViewById<LinearLayout>(R.id.parent_layout)

        v = LayoutInflater.from(this).inflate(R.layout.edit_note, parentLayout, false)
        parentLayout.addView(v)

        etTitle = v.findViewById(R.id.etTitle)
        etNote = v.findViewById(R.id.etNote)

    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)

        // TODO: NavigationIcon
        val navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_arrow_back_24)
        navigationIcon!!.setTint(ContextCompat.getColor(this, toolbarOnBackground))
        toolbar.navigationIcon = navigationIcon

        menu?.findItem(R.id.menuMain)?.isVisible = false
        menu?.findItem(R.id.sort)?.isVisible = false
        menu?.findItem(R.id.action_search)?.isVisible = false
        menu?.findItem(R.id.menuDelete)?.isVisible = false
        val saveIcon = menu?.findItem(R.id.menuSave)
        saveIcon!!.isVisible = true
        saveIcon.setIcon(R.drawable.ic_baseline_save_24)
        saveIcon.setOnMenuItemClickListener {
            addNote()
//            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            true
        }

        return true
    }

}


//    private fun getView(viewID: Int): View? {
//        return v.findViewById(viewID)
//    }