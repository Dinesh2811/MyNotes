package com.dinesh.mynotes.rv

import android.content.res.ColorStateList
import android.util.SparseBooleanArray
import android.util.TypedValue
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.dinesh.mynotes.R
import com.dinesh.mynotes.room.Note
import com.dinesh.mynotes.room.NotesViewModel
import java.time.format.DateTimeFormatter

class RvAdapter(var notesList: List<Note>, private val listener: RvInterface, private val notesViewModel: NotesViewModel, private val rvMain: RvMain) :
    RecyclerView.Adapter<RvAdapter.ViewHolder>() {
    private val TAG = "log_RvAdapter"

    var actionMode: ActionMode? = null
    val selectedItems = SparseBooleanArray()
    var rvSelectedItemCount: LiveData<Int> = MutableLiveData()

    fun setFilteredList(notesList: List<Note>) {
        this.notesList = notesList
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View, var notesList: List<Note>, listener: RvInterface, private val notesViewModel: NotesViewModel) : RecyclerView.ViewHolder(itemView) {

// TODO: initialization

        val rvTitle: TextView = itemView.findViewById(R.id.rvTitle)
        val rvNote: TextView = itemView.findViewById(R.id.rvNote)
        val rvDateCreated: TextView = itemView.findViewById(R.id.rvDateCreated)
        val rvDateModified: TextView = itemView.findViewById(R.id.rvDateModified)
        val rvCardView: CardView = itemView.findViewById(R.id.rvCardView)
//        val rvCardView: ConstraintLayout = itemView.findViewById(R.id.rvConstraintLayout)
        val rvConstraintLayout: ConstraintLayout = itemView.findViewById(R.id.rvConstraintLayout)

        init {
            (rvSelectedItemCount as MutableLiveData).value = selectedItemCount

            val noteClickListener = View.OnClickListener {
                listener.onItemClick(it, adapterPosition)
            }

            val noteLongClickListener = View.OnLongClickListener {
                listener.onLongClick(it, adapterPosition, selectedItemCount)
                true
            }

// TODO: setting OnClickListener and OnLongClickListener

            rvTitle.setOnClickListener(noteClickListener)
            rvNote.setOnClickListener(noteClickListener)
            rvDateCreated.setOnClickListener(noteClickListener)
            rvDateModified.setOnClickListener(noteClickListener)
            rvCardView.setOnClickListener(noteClickListener)
            itemView.setOnClickListener(noteClickListener)

            rvTitle.setOnLongClickListener(noteLongClickListener)
            rvNote.setOnLongClickListener(noteLongClickListener)
            rvDateCreated.setOnLongClickListener(noteLongClickListener)
            rvDateModified.setOnLongClickListener(noteLongClickListener)
            rvCardView.setOnLongClickListener(noteLongClickListener)
            itemView.setOnLongClickListener(noteLongClickListener)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_list, parent, false)
        return ViewHolder(view, notesList, listener, notesViewModel)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notesList[position]
        if (note.title.trim().isEmpty()) {
            holder.rvTitle.visibility = View.GONE
        } else {
            holder.rvTitle.visibility = View.VISIBLE
            holder.rvTitle.text = note.title
        }
        if (note.notes.trim().isEmpty()) {
            holder.rvNote.visibility = View.GONE
        } else {
            holder.rvNote.visibility = View.VISIBLE
            holder.rvNote.text = note.notes
        }
        holder.rvDateCreated.text = note.dateCreated.format(DateTimeFormatter.ofPattern("EEE, MMM d, hh:mm a"))
        val dateModifiedString = "Modified on : " + note.dateModified.format(DateTimeFormatter.ofPattern("EEE, MMM d, hh:mm a"))
        holder.rvDateModified.text = dateModifiedString

        val typedValue = TypedValue()
        if (getSelectedItems().contains(position)) {
//            holder.rvConstraintLayout.setBackgroundColor(Color.DKGRAY)
            holder.itemView.context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimaryContainer, typedValue, true)
        } else {
//            holder.rvConstraintLayout.setBackgroundColor(Color.WHITE)
//            holder.itemView.context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true)
            holder.itemView.context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceInverse, typedValue, true)
        }
        val color = typedValue.data
        val colorStateList = ColorStateList.valueOf(color)
        holder.rvCardView.backgroundTintList = colorStateList

    }

    override fun getItemCount(): Int {
        return notesList.size
    }

//    fun clearSelection() {
//        (rvSelectedItemCount as MutableLiveData).value = 0
//        selectedItems.clear()
//        actionMode = null
//        notifyDataSetChanged()
//    }
//

    val selectedItemCount: Int
        get() = selectedItems.size()

    fun getSelectedItems(): List<Int> {
        val items: MutableList<Int> = ArrayList(selectedItems.size())
        for (i in 0 until selectedItems.size()) {
            items.add(selectedItems.keyAt(i))
        }
        return items
    }

    fun toggleSelection(position: Int) {
//        Log.d(TAG, "toggleSelection: ${position}")
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position)
        } else {
            selectedItems.put(position, true)
        }
        notifyItemChanged(position)
    }

//    fun getSelectedItemCount(): Int {
//        return selectedItems.size()
//    }

    fun clearSelection() {
        (rvSelectedItemCount as MutableLiveData).value = 0
        selectedItems.clear()
        actionMode = null
        notifyDataSetChanged()
    }

}

