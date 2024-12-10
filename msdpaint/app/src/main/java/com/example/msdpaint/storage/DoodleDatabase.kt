package com.example.msdpaint.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@TypeConverters(SQLConverters::class)

@Database(
    entities= [Doodle::class],
    version = 2,
    exportSchema = false
)

abstract class DoodleDatabase: RoomDatabase() {


    abstract fun doodleDao(): DoodleDao


    companion object {

        @Volatile
        private var INSTANCE: DoodleDatabase? = null

        fun getDatabase(context: Context): DoodleDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DoodleDatabase::class.java,
                    "doodle_database"
                )
                //.fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                instance

            }

        }

    }


}
