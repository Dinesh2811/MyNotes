package com.dinesh.mynotes.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dinesh.mynotes.R
import com.dinesh.mynotes.app.NavigationDrawer
import com.dinesh.mynotes.room.Note
import com.dinesh.mynotes.room.NotesViewModel
import com.dinesh.mynotes.rv.RvMain
import java.time.LocalDateTime

class EditNote : NavigationDrawer() {

    lateinit var v: View
    private lateinit var notesViewModel: NotesViewModel
    lateinit var etTitle: EditText
    lateinit var etNote: EditText
    private var note: Note = Note()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeRecyclerView()
        retrieveNote()

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
            note.title = it.toString()
        }
        etNote.addTextChangedListener {
            note.notes = it.toString()
        }

    }

    private fun initializeRecyclerView() {
        setContentView(R.layout.activity_main)
//        setNavigationDrawer()
        setToolbar()

        val parentLayout = findViewById<LinearLayout>(R.id.parent_layout)

        v = LayoutInflater.from(this).inflate(R.layout.edit_note, parentLayout, false)
        parentLayout.addView(v)

        etTitle = v.findViewById(R.id.etTitle)
        etNote = v.findViewById(R.id.etNote)

        notesViewModel = ViewModelProvider(this)[NotesViewModel::class.java]
    }

    private fun retrieveNote() {
        val noteId = intent.getLongExtra("NOTE_ID", -1)
        if (noteId == -1L) {
            return
        }
        note.id = intent.getLongExtra("NOTE_ID", 0)
        note.dateModified = LocalDateTime.now()
        notesViewModel.getNoteById(noteId).observe(this, Observer { retrievedNote ->
            if (retrievedNote != null) {
                note = retrievedNote
                etTitle.setText(note.title)
                etNote.setText(note.notes)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        updateNote()
    }

    private fun updateNote() {
        if ((!(etTitle.text.isNullOrEmpty())) || (!(etNote.text.isNullOrEmpty()))) {
            note.id = intent.getLongExtra("NOTE_ID", 0)
            note.dateModified = LocalDateTime.now()
            notesViewModel.update(note)
//            Toast.makeText(this, "Your note is Updated successfully", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
//        super.onPrepareOptionsMenu(menu)

        // TODO: NavigationIcon
        val navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_arrow_back_24)
        navigationIcon!!.setTint(ContextCompat.getColor(this, toolbarOnBackground))
        toolbar.navigationIcon = navigationIcon


        menu?.findItem(R.id.menuMain)?.isVisible = false
        menu?.findItem(R.id.sort)?.isVisible = false
        menu?.findItem(R.id.action_search)?.isVisible = false

        val saveIcon = menu?.findItem(R.id.menuSave)
        saveIcon!!.isVisible = true
        saveIcon.setIcon(R.drawable.ic_baseline_save_24)
        saveIcon.setOnMenuItemClickListener {
            updateNote()
            true
        }

        val deleteIcon = menu.findItem(R.id.menuDelete)
        deleteIcon!!.isVisible = true
        deleteIcon.setIcon(R.drawable.ic_baseline_delete_24)
        deleteIcon.setOnMenuItemClickListener {
            notesViewModel.delete(note)
            Toast.makeText(this, "Your note was deleted successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(this@EditNote, RvMain::class.java)
            startActivity(intent)


//            finish()
//            rvMain.deleteNote()
//            rvMain.rvAdapter.notifyDataSetChanged()
            true
        }

        return true
    }

}

