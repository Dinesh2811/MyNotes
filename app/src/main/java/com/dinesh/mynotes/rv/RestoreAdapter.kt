package com.dinesh.mynotes.rv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.dinesh.mynotes.R
import com.dinesh.mynotes.room.Note

class RestoreAdapter(private val notes: List<Note>) : RecyclerView.Adapter<RestoreAdapter.ViewHolder>() {
    private val selectedNotes = mutableListOf<Note>()
    var selectedNotesLiveData = MutableLiveData<List<Note>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.restore_item_note, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        if (note.title.trim().isEmpty()) {
            holder.tvTitle.visibility = View.GONE
        } else{
            holder.tvTitle.text = note.title
            holder.tvTitle.visibility = View.VISIBLE
        }

        if (note.notes.trim().isEmpty()){
            holder.tvNote.visibility = View.GONE
        } else{
            holder.tvNote.text = note.notes
            holder.tvNote.visibility = View.VISIBLE
        }

        holder.checkBox.isChecked = selectedNotes.contains(note)

        val itemClickListener = View.OnClickListener {
            if (selectedNotes.contains(note)) {
                selectedNotes.remove(note)
                holder.checkBox.isChecked = false
            } else {
                selectedNotes.add(note)
                holder.checkBox.isChecked = true
            }
            selectedNotesLiveData.value = selectedNotes
        }
        holder.rvCardView.setOnClickListener(itemClickListener)
        holder.itemView.setOnClickListener(itemClickListener)
        holder.tvTitle.setOnClickListener(itemClickListener)
        holder.tvNote.setOnClickListener(itemClickListener)
        holder.checkBox.setOnClickListener(itemClickListener)
    }

    override fun getItemCount() = notes.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rvCardView: CardView = itemView.findViewById(R.id.rvCardView)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvNote: TextView = itemView.findViewById(R.id.tvNote)
    }

    fun getSelectedNotes(): List<Note> {
        return selectedNotes
    }

    fun updateSelection(isChecked: Boolean) {
        if (isChecked) {
            selectedNotes.clear()
            selectedNotes.addAll(notes)
        } else {
            selectedNotes.clear()
        }
        notifyDataSetChanged()
    }

}

