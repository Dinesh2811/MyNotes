package com.dinesh.mynotes.room

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.dinesh.mynotes.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


@Entity(tableName = "table_name")
@TypeConverters(LocalDateTimeConverter::class)
data class Note(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var title: String = "",
    var notes: String = "",
    var dateCreated: LocalDateTime = LocalDateTime.now(),
    var dateModified: LocalDateTime = LocalDateTime.now(),
    var customPosition: Int = 0,
    var isPinned: Boolean = false,
    var color: Int = R.color.white,
    var label: String = "nullLabel",
    var selected: Boolean = false
):java.io.Serializable
