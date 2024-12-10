package com.example.msdpaint.storage

import androidx.lifecycle.asLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date


class DoodleRepository(
    private val scope: CoroutineScope,
    private val dao: DoodleDao
) {


    val allDoodles = dao.allDoodles().asLiveData()


    fun addDoodle(
        name: String,
        timeStamp: Date,
        path: String,
    ) {

        scope.launch {
             dao.addDoodle(Doodle(name, timeStamp, path))
        }

    }


    fun deleteDoodle(
        name: String,
        timeStamp: Date,
        path: String,
    ) {

        scope.launch {
            dao.deleteDoodle(name)
        }

    }


    fun deleteAll() {

        scope.launch {
            dao.deleteAll()
        }

    }


}