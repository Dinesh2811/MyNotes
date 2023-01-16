package com.dinesh.mynotes.room

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.snapshots.Snapshot.Companion.observe
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.*
//import kotlinx.coroutines.*
//import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.InternalCoroutinesApi


//    var notesList: Flow<List<Note>> = MutableSharedFlow<List<Note>>()
//@RequiresApi(Build.VERSION_CODES.O)
class Main : AppCompatActivity() {
    private val TAG = "log_" + Main::class.java.name.split(Main::class.java.name.split(".").toTypedArray()[2] + ".").toTypedArray()[1]

    private lateinit var notesViewModel: NotesViewModel
    var notesList: LiveData<List<Note>> = MutableLiveData()
    var id: LiveData<Int> = MutableLiveData()

//    private lateinit var notesViewModel: NotesViewModel
//    private lateinit var notesList: Flow<List<Note>>
//    private lateinit var id: Flow<Int>


    @OptIn(InternalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notesViewModel = ViewModelProvider(this)[NotesViewModel::class.java]
        notesList = notesViewModel.getAllNotes()
//        id = notesList.map { it.last().id }

//        notesViewModel = ViewModelProvider(this)[NotesViewModel::class.java]
//
//        lifecycleScope.launch(Dispatchers.IO) {
//            notesViewModel.getAllNotes().flowOn(Dispatchers.IO).collect {
//                if (it.isNotEmpty()) {
////                    runBlocking(Dispatchers.Main) {
////                        notesList.value = it
////                        id.value = notesList.value!!.last().id
////                    }
//                    notesList.postValue(it)
//                    id.postValue(it.last().id)
//                    Log.d(TAG, "onCreate: ${it.last().id} ")
//                }
//            }
//        }
//
//        id.observe(this, Observer {
//            Log.e(TAG, "notesList: " + (notesList.value?.get(it.minus(1)) ?: "idNull"))
//            lifecycleScope.launch(Dispatchers.IO) {
//                notesViewModel.getNoteById(it).flowOn(Dispatchers.IO).collect { note ->
////                Log.e(TAG, "id.observe: $note")
//                }
//            }
//        })

//        lifecycleScope.launch(Dispatchers.IO) {
//            Log.e(TAG, "onCreate: ${id.take(1).first()}")
////            id.collect {
////                Log.e(TAG, "onCreate: $it")
////            }
//        }


//        lifecycleScope.launch(Dispatchers.IO) {
//            id.collect {
//                Log.d(TAG, "onCreate: ${it}")
//            }
////            notesList.collect {
////                if (it.isNotEmpty()) {
////                    Log.e(TAG, "onCreate: ${it.last().id}")
////                    id = flow { it.last().id }
////                    id.collect { noteId ->
////                        notesViewModel.getNoteById(noteId).collect { note ->
////                            Log.e(TAG, "id.observe: $note")
////                        }
////                    }
////
//////                    runBlocking(Dispatchers.Main) {
//////                        id.collect { noteId ->
//////                            notesViewModel.getNoteById(noteId).collect { note ->
//////                                Log.e(TAG, "id.observe: $note")
//////                            }
//////                        }
//////                    }
////                }
////            }
//        }

//        lifecycleScope.launch(Dispatchers.IO) {
//            notesList.collect{
////                Log.d(TAG, "onCreate: ${notesList.take(1).first() }}")
//            }
//        }


        notesList.observe(this){
            if (it.isNotEmpty()){
            Log.d(TAG, "onCreate: ${it.last()}")
        }
        }

    }

    var i: Int = 1
    override fun onBackPressed() {
//        super.onBackPressed()
        Log.e(TAG, "onBackPressed: ")

        notesViewModel.insert(
            Note(
                title = "title $i",
                notes = "note $i",
                dateCreated = LocalDateTime.now()
            )
        )

        i++
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy: ")
//        notesViewModel.shutdown()
        NotesDatabase.destroyInstance()
    }

}