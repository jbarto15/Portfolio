package com.example.msdpaint

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.File


class MainActivity : AppCompatActivity() {


    companion object {
        var SHOW_SPLASH = true
        lateinit var dir : File
        var GLOBAL_USER = Firebase.auth.currentUser
    }


    override fun onCreate(savedInstanceState :Bundle?) {

        dir = getExternalFilesDir(null)!!

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

}

// todo Shapes menu button selector one-at-a-time
// todo brushColor and paint is messy (have to update both always) -> probably should just make paint LiveData
// todo bitmap and Doodle nullability be more safe and consistent outside of ViewModel

// todo new doodle button should be renamed Go To Studio and wherever you left off should be there
//      creating a new doodle, unsaved confirmation, etc., should be a three-dot menu thing


// STRETCH
// todo seekbar as a vertical popup menu
// todo stop marble if any menus are showing
// todo marble as a class and with its own bitmap
// todo package structure by concept, not type
// todo all of mvvm should be reviewed it is a mess
// todo test on different devices