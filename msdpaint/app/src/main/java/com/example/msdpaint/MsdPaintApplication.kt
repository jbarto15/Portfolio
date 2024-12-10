package com.example.msdpaint

import android.app.Application
import com.example.msdpaint.storage.DoodleDatabase
import com.example.msdpaint.storage.DoodleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob


class MsdPaintApplication : Application() {


    private val scope = CoroutineScope(SupervisorJob())

    private val db by lazy {
        DoodleDatabase.getDatabase(applicationContext)
    }

    val doodleRepository by lazy {
        DoodleRepository(scope, db.doodleDao())
    }


}