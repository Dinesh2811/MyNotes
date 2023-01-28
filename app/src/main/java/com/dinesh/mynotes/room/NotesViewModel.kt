package com.dinesh.mynotes.room

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dinesh.mynotes.rv.RvMain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow

@RequiresApi(Build.VERSION_CODES.O)
class NotesViewModel(application: Application) : AndroidViewModel(application) {

    val noteDao: NotesDao = NotesDatabase.getInstance(application).notesDao()

//    val notesDatabase: NotesDatabase = NotesDatabase.getInstance(application)
//    public val noteDao: NotesDao = notesDatabase.notesDao()
//    var notesLiveList: LiveData<List<Note>> = noteDao.getAllNotes()
//    fun closeDb(){
//        notesDatabase.close()
//    }



    fun getMaxId(): Long{
        return noteDao.getMaxNoteId()
    }

    fun getNoteById(id: Long): LiveData<Note> {
        return noteDao.getNoteById(id)
    }

    fun getAllNotes(): LiveData<List<Note>> {
        return noteDao.getAllNotes()
    }

    fun getNoteByTitleAndDescription(title: String, description: String): LiveData<Note> {
        return noteDao.getNoteByTitleAndDescription(title, description)
    }


    fun getNoteByTitleAndDescriptionAsNote(title: String, description: String): Note {
        return noteDao.getNoteByTitleAndDescriptionAsNote(title, description)
    }

    fun insert(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.insert(note)
        }
    }

    fun update(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.update(note)
        }
    }

    fun updateSelected(id: Long, selected: Boolean) {
        viewModelScope.launch {
            noteDao.updateSelected(id, selected)
        }
    }

    fun delete(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.delete(note)
        }
    }
}