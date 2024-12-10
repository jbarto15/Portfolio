package com.example.msdpaint.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface DoodleDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDoodle(data: Doodle)


    @Query("SELECT * FROM Doodles WHERE doodleName = :inputName")
    fun retrieveDoodle(inputName: String) : Flow<Doodle?>


    @Query("SELECT * FROM Doodles ORDER BY timestamp DESC")
    fun allDoodles() : Flow<List<Doodle>>


    @Query("DELETE FROM Doodles")
    fun deleteAll()


    @Query("DELETE FROM Doodles WHERE doodleName = :inputName")
    fun deleteDoodle(inputName: String)


}