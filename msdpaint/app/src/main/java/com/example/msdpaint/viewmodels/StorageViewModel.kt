package com.example.msdpaint.viewmodels

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.msdpaint.MainActivity
import com.example.msdpaint.storage.Doodle
import com.example.msdpaint.storage.DoodleRepository
import java.io.File
import java.io.FileOutputStream
import java.util.Date


class StorageViewModel(
    private val repository: DoodleRepository
) : ViewModel() {


    val allDoodles : LiveData<List<Doodle>> = repository.allDoodles


    fun addDoodle(
        name :String,
        timeStamp : Date,
        path :String,
    ) {
        repository.addDoodle(name, timeStamp, path)
    }


    fun deleteDoodle(
        name :String,
        timeStamp : Date,
        path :String,
    ) {
        repository.deleteDoodle(name, timeStamp, path)
    }


    fun deleteAll() {
        repository.deleteAll()
    }


    fun saveBitmapAsFile(drawingName: String, timeStamp: String, bitmap: Bitmap?): String {

        val filename = "$drawingName.png"
        val dir = MainActivity.dir
        val dest = File(dir, filename)

        try {

            val out =  FileOutputStream(dest);
            bitmap!!.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();

            // Don't delete
            if (!dest.exists()) {
                Log.e("FILE ERROR", "$dest does not exist")
            }

        } catch (e: Exception) {
            e.message
        }

        return dest.absolutePath
    }


}