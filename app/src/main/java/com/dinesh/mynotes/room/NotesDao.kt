package com.dinesh.mynotes.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NotesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Query("UPDATE table_name SET selected = :selected WHERE id = :id")
    suspend fun updateSelected(id: Long, selected: Boolean)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM table_name ORDER BY customPosition ASC, id ASC, dateModified ASC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM table_name WHERE id = :id")
     fun getNoteById(id: Long): LiveData<Note>

    @Query("SELECT MAX(id) FROM table_name")
    fun getMaxNoteId(): Long

    @Query("SELECT * FROM table_name WHERE title = :title AND notes = :description")
    fun getNoteByTitleAndDescription(title: String, description: String): LiveData<Note>

    @Query("SELECT * FROM table_name WHERE title = :title AND notes = :description")
    fun getNoteByTitleAndDescriptionAsNote(title: String, description: String): Note

}

/*

@Dao
interface NotesDao {

    @Transaction
    @Insert
    fun insert(note: Note)

    @Transaction
//    @Query("UPDATE table_name SET notes = :note.title WHERE title = :note.title")
    @Update
    fun update(note: Note)

    @Transaction
    @Delete
    fun delete(note: Note)

    @Transaction
    @Query("SELECT * FROM table_name")
    fun getAllNotes(): LiveData<List<Note>>

    @Transaction
    @Query("SELECT * FROM table_name WHERE id = :noteId")
    fun getNoteById(noteId: Long): LiveData<Note>

    @Transaction
    @Query("SELECT COUNT(*) FROM table_name")
    fun getSize(): LiveData<Int>

}

 */