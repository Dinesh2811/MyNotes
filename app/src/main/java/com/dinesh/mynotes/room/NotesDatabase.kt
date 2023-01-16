package com.dinesh.mynotes.room

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@RequiresApi(Build.VERSION_CODES.O)
@Database(entities = [Note::class], version = 1, exportSchema = false)
@TypeConverters(LocalDateTimeConverter::class)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDao

    companion object {
        @Volatile
        private var INSTANCE: NotesDatabase? = null

        fun getInstance(context: Context): NotesDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context, NotesDatabase::class.java, "notes_database")
                            .fallbackToDestructiveMigration()
                            .build()
                    }
                }
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}

/*

@Database(entities = [Note::class], version = 2, exportSchema = false)
//@TypeConverters(LocalDateTimeConverter::class)
abstract class NotesDatabase : RoomDatabase() {

    abstract fun notesDao(): NotesDao

    companion object {
        @Volatile
        private var instance: NotesDatabase? = null


        fun getInstance(context: Context): NotesDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context, NotesDatabase::class.java, "notes_database")
                    .build()
                    .also { instance = it }
            }
        }

    }
}

//class Migration1To2 : Migration(1, 2) {
//    override fun migrate(database: SupportSQLiteDatabase) {
//        database.execSQL("ALTER TABLE table_name ADD COLUMN new_column INTEGER NOT NULL DEFAULT 0")
//    }
//}

//class Migration1To2 : Migration(1, 2) {
//    override fun migrate(database: SupportSQLiteDatabase) {
//        // Create a new table with the correct schema
//        database.execSQL("CREATE TABLE table_name_new (id INTEGER PRIMARY KEY NOT NULL, title TEXT NOT NULL, notes TEXT NOT NULL, dateCreated TEXT NOT NULL, dateModified TEXT NOT NULL, customPosition INTEGER NOT NULL, isPinned INTEGER NOT NULL, color TEXT NOT NULL, label TEXT NOT NULL, new_column INTEGER NOT NULL DEFAULT 0)")
//        // Copy the data from the old table to the new table
//        database.execSQL("INSERT INTO table_name_new (id, title, notes, dateCreated, dateModified, customPosition, isPinned, color, label) SELECT id, title, notes, dateCreated, dateModified, customPosition, isPinned, color, label FROM table_name")
//        // Drop the old table
//        database.execSQL("DROP TABLE table_name")
//        // Rename the new table to the old table
//        database.execSQL("ALTER TABLE table_name_new RENAME TO table_name")
//    }
//}

class Migration1To2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE table_name")
//        database.execSQL("DROP TABLE table_name_new")
//        // Create a new table with the correct schema
//        database.execSQL("CREATE TABLE table_name_new (id INTEGER PRIMARY KEY NOT NULL, title TEXT NOT NULL, notes TEXT NOT NULL, dateCreated TEXT NOT NULL, dateModified TEXT NOT NULL, customPosition INTEGER NOT NULL, isPinned INTEGER NOT NULL, color TEXT NOT NULL, label TEXT NOT NULL, new_column INTEGER NOT NULL DEFAULT 0)")
//        // Copy the data from the old table to the new table
//        database.execSQL("INSERT INTO table_name_new (id, title, notes, dateModified, customPosition, isPinned, color, label) SELECT id, title, notes, dateModified, customPosition, isPinned, color, label FROM table_name")
//        // Drop the old table
//        database.execSQL("DROP TABLE table_name")
//        // Rename the new table to the old table
//        database.execSQL("ALTER TABLE table_name_new RENAME TO table_name")
    }
}


 */

//@Database(entities = [Note::class], version = 1, exportSchema = false)
////@TypeConverters(LocalDateTimeConverter::class)
//abstract class NotesDatabase : RoomDatabase() {
//    abstract fun notesDao(): NotesDao
//
//    companion object {
//        @Volatile
//        private var instance: NotesDatabase? = null
//
//        fun getInstance(context: Context): NotesDatabase {
//            return instance ?: synchronized(this) {
//                instance ?: Room.databaseBuilder(context, NotesDatabase::class.java, "notes_database")
//                    .build()
//                    .also { instance = it }
//            }
//        }
//    }
//}


//@Database(entities = [Note::class], version = 1, exportSchema = false)
//abstract class NotesDatabase : RoomDatabase() {
//    abstract fun notesDao(): NotesDao
//}


