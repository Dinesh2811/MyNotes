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

//@RequiresApi(Build.VERSION_CODES.O)
//class NotesViewModel(application: Application) : AndroidViewModel(application) {
//    private val notesRepository: NotesRepository
//
//    val allNotes: LiveData<List<Note>>
//    val size: LiveData<Int>
//
//    init {
//        val notesDao = NotesDatabase.getInstance(application).notesDao()
//        notesRepository = NotesRepository(notesDao)
//        allNotes = notesRepository.getAllNotes()
//        size = notesRepository.getSize()
//    }
//
//    fun getNoteById(noteId: Long): LiveData<Note> {
//        return notesRepository.getNoteById(noteId)
//    }
//
//    fun insert(note: Note) {
//        notesRepository.insert(note)
//    }
//
//    fun update(note: Note) {
//        notesRepository.update(note)
//    }
//
//    fun delete(note: Note) {
//        notesRepository.delete(note)
//    }
//
//
//    fun shutdown() {
//        notesRepository.shutdown()
//    }
//}


//class NotesViewModel(application: Application) : AndroidViewModel(application) {
//    private val notesDao: NotesDao
//    private val executor = Executors.newSingleThreadExecutor()
//
//    val allNotes: LiveData<List<Note>>
//
//    init {
//        val notesDatabase = NotesDatabase.getInstance(application)
//        notesDao = notesDatabase.notesDao()
//        allNotes = notesDao.getAllNotes()
//    }
//
//    fun insert(note: Note) {
//        executor.execute { notesDao.insert(note) }
//    }
//
//    fun update(note: Note) {
//        executor.execute { notesDao.update(note) }
//    }
//
//    fun delete(note: Note) {
//        executor.execute { notesDao.delete(note) }
//    }
//
//    fun shutdown() {
//        executor.shutdown()
//    }
//}


class NotesViewModel(application: Application) : AndroidViewModel(application) {

    public val noteDao: NotesDao = NotesDatabase.getInstance(application).notesDao()

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


//    fun getNoteByTitleAndDescription(title: String, description: String): Note {
//        return noteDao.getNoteByTitleAndDescription(title, description)
//    }

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