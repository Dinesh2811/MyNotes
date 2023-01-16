//package com.dinesh.mynotes.room
//
//import androidx.lifecycle.LiveData
//import java.util.concurrent.Executors
//
//class NotesRepository(private val notesDao: NotesDao) {
//    private val executor = Executors.newSingleThreadExecutor()
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
//    fun getAllNotes(): LiveData<List<Note>> {
//        return notesDao.getAllNotes()
//    }
//
//
//    fun getNoteById(noteId: Long): LiveData<Note> {
//        return notesDao.getNoteById(noteId)
//    }
//
//    fun getSize(): LiveData<Int> {
//        return notesDao.getSize()
//    }
//
//    fun shutdown() {
//        executor.shutdown()
//    }
//}