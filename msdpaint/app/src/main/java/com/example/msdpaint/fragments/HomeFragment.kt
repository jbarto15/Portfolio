package com.example.msdpaint.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.example.msdpaint.KtorClient
import com.example.msdpaint.MainActivity
import com.example.msdpaint.MsdPaintApplication
import com.example.msdpaint.R
import com.example.msdpaint.databinding.FragmentHomeBinding
import com.example.msdpaint.storage.Doodle
import com.example.msdpaint.viewmodels.StorageViewModel
import com.example.msdpaint.viewmodels.StorageViewModelFactory
import com.example.msdpaint.viewmodels.StudioViewModel
import com.google.firebase.auth.FirebaseUser
import io.ktor.client.call.body
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import com.example.msdpaint.UserDoodle
import java.util.Base64



class HomeFragment : Fragment() {


    private val studioVM: StudioViewModel by activityViewModels()
    private val storageVM: StorageViewModel by viewModels {
        StorageViewModelFactory(
            (requireActivity().application as MsdPaintApplication).doodleRepository
        )
    }
    private val client = KtorClient(MainActivity.GLOBAL_USER)
    private val user_ = MainActivity.GLOBAL_USER?.email


    override fun onCreateView(
        inflater :LayoutInflater,
        container :ViewGroup?,
        savedInstanceState :Bundle?
    ) :View {

        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        if (MainActivity.GLOBAL_USER != null ) {
            lifecycleScope.launch {
                getMyDrawings(MainActivity.GLOBAL_USER!!)
                getSharedDrawing(MainActivity.GLOBAL_USER!!)
            }
        }

        binding.composeView.setContent {
            HomeScreen()
        }

        return binding.root

    }


    @Composable
    fun HomeScreen() {

        MaterialTheme {

            Column (
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                UserInfo()
                Spacer(modifier = Modifier.padding(top = 60.dp))

                HomeScreenLogo()
                Spacer(modifier = Modifier.padding(top = 60.dp))

                NewDoodleButton()
                Spacer(modifier = Modifier.padding(top = 60.dp))

                DoodleLazyList()

            }

            Column( modifier =  Modifier.fillMaxSize()) {

                if (MainActivity.SHOW_SPLASH)  {
                    SplashScreen()
                }

            }

        }

    }


    @Composable
    fun UserInfo() {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {

            if (MainActivity.GLOBAL_USER != null) {
                Text(
                    text = "Welcome, ${MainActivity.GLOBAL_USER!!.email}! ",
                    modifier = Modifier.padding(5.dp)
                )
            } else {
                Text("")
            }

            Image(
                painter = painterResource(id = R.drawable.sign_in_person),
                contentDescription = "home fragment sign-in/account button",
                modifier = Modifier.clickable{ findNavController(this@HomeFragment).navigate(R.id.action_homeFragment_to_signInFragment) }
            )

        }

    }


    @Composable
    fun DoodleLazyList() {

        val allDoodles by storageVM.allDoodles.observeAsState()

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            for (data in allDoodles ?: listOf()){
                item {
                    DoodleRow(doodle = data)
                }
            }
            Modifier.verticalScroll(ScrollState(0))
        }

    }


    @Composable
    fun DoodleRow(doodle: Doodle) {
        Row {
            ThumbNameTime(doodle)
//            TrashButton(doodle)  todo Can delete if no encountered problems deleting from home
        }
    }


    @Composable
    fun ThumbNameTime(doodle: Doodle) {

        lateinit var bitmap :Bitmap
        if (!doodle.isDefault()) {
            bitmap = BitmapFactory.decodeFile(doodle.path)
        }

        ListItem(
            modifier = Modifier
                .height(80.dp)
                .clickable {
                    goToStudio(bitmap, doodle)
                },
            leadingContent = {
                DoodleThumbnail(bitmap)
            },
            headlineContent = {
                Text(text = doodle.doodleName, fontSize = 15.sp)
            },
            trailingContent = {
                TrashAndShareButtons(doodle)
            },
            supportingContent = {
                if (!doodle.isDefault()) {
                    val formatter = DateTimeFormatter.ofPattern("EEE MMM d h:mma")
                    val datetime = formatTimestamp(doodle.timestamp, formatter)
                    Text(
                        text = datetime,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .alpha(0.6f)
                    )
                }
            }
        )

    }


    @Composable
    fun TrashAndShareButtons(doodle: Doodle, ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TrashButton(doodle)
            if (user_ != null) {
                ShareButton(doodle)
            }
        }
    }


    // from online
    private fun formatTimestamp(date: Date, formatter: DateTimeFormatter): String {
        val dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
        return dateTime.format(formatter)
    }


    @Composable
    fun TrashButton(data: Doodle?) {

        Button( onClick = {
                trashButtonOnClick(data)
            },
            colors = ButtonDefaults.buttonColors(Color.Transparent,
                contentColor = Color.Unspecified
            )
        ) {
            Image(
                painter = painterResource(id = R.drawable.delete),
                contentDescription = "list delete trash can button"
            )
        }
    }

    @Composable
    fun ShareButton(data: Doodle?) {

        Button( onClick = {
            sendButtonOnClick(data!!)
        },
            colors = ButtonDefaults.buttonColors(Color.Transparent,
                contentColor = Color.Unspecified
            )
        ) {



            Image(
                painter = painterResource(id = R.drawable.send_airplane),
                contentDescription = "list send button",
            )
        }
    }

    @Composable
    fun DoodleThumbnail(bitmap: Bitmap) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .width(50.dp)
                .height(50.dp)
        )
    }

    @Composable
    fun SplashScreen() {

        var showSplash by remember { mutableStateOf(true) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(key1 = null) {
            scope.launch {
                delay(1500)
                showSplash = false
                MainActivity.SHOW_SPLASH = false
            }
        }

        AnimatedVisibility(
            visible = showSplash,
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.doodlebob),
                        contentDescription = "splash screen image"
                    )
                    Text(
                        text = "NI MINOY MANOH",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }

    }


    @Composable
    fun HomeScreenLogo() {
        Image(
            painter = painterResource(id = R.drawable.artlogo),
            contentDescription = "home screen logo",
            modifier = Modifier.size(150.dp)
        )
    }


    @Composable
    fun NewDoodleButton() {
        Button(onClick = {
            //studioVM.updateDoodle(
            //    Bitmap.createBitmap(1440, 2160, Bitmap.Config.ARGB_8888 ),
            //    Doodle()
            //)
            findNavController(this).navigate(R.id.action_homeFragment_to_studioFragment)
        }) {
            Text(text = "Go to Studio")
        }
    }


private fun sendButtonOnClick(doodle: Doodle) {

    val dialogBox = AlertDialog.Builder(requireActivity())
    val editText = EditText(requireContext())

    dialogBox.setTitle("Send this drawing?")
        .setMessage("Enter the name of the recipient")
        .setView(editText)
        .setPositiveButton("Send") { dialog, _ ->
                lifecycleScope.launch() {

                    val response = client.shareImage(
                        doodle.path,
                        doodle.doodleName,
                        user_.toString(),
                        editText.text.toString(),
                        doodle.timestamp.time
                    )
                    if (response.status.value in 200..299) {
                        Toast.makeText(context, "Successful response!", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        Toast.makeText(context, "Unsuccessful response!", Toast.LENGTH_LONG)
                            .show()
                    }
                }


            dialog.dismiss()
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
    dialogBox.create().show()
}


    private fun trashButtonOnClick(doodle: Doodle?) {
        if (doodle != null) {
            storageVM.deleteDoodle(doodle.doodleName, doodle.timestamp, doodle.path)
        }
    }


    private fun goToStudio(bitmap :Bitmap, doodle: Doodle) {
        studioVM.updateDoodle(bitmap, doodle)
        findNavController(this).navigate(R.id.action_homeFragment_to_studioFragment)
    }


//    private suspend fun getMyDrawings(user: FirebaseUser) {
//
//        val doodleInfos :List<UserDoodle> = client.retrieveMyImages(user.email!!).body<List<UserDoodle>>()
//        for ( i in 0..doodleInfos.size  ) {
//            // Extract fields from json
//            val doodleName = doodleInfos[i].doodleName
//            val timestamp = Date(doodleInfos[i].timestamp)
//            val bitmapBytes = Base64.getDecoder().decode(doodleInfos[i].blob)
//            val bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.size)
//            // Save locally and add to Room
//            val path = storageVM.saveBitmapAsFile(doodleName, timestamp.toString(), bitmap)
//            storageVM.addDoodle(doodleName, timestamp, path)
//            }
//        }


        private suspend fun getMyDrawings(user: FirebaseUser) {

            if (user.email == null) { return }

            val response = client.retrieveMyImages(user.email!!)

            val doodleInfos: List<UserDoodle> = response.body()

            if (doodleInfos.isEmpty() ) { return }

            doodleInfos.forEach { doodle ->
                val doodleName = doodle.doodleName
                val timestamp = Date(doodle.timestamp)
                val cleanedBlob = doodle.blob.replace("\n", "")
                val bitmapBytes = Base64.getDecoder().decode(cleanedBlob)
                val bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.size)

                // Save locally and add to Room
                val path = storageVM.saveBitmapAsFile(doodleName, timestamp.toString(), bitmap)
                storageVM.addDoodle(doodleName, timestamp, path)
            }

        }

    private suspend fun getSharedDrawing( user: FirebaseUser) {

        if (user.email == null) { return }

        val doodleInfos :List<UserDoodle> = client.retrieveSharedImages(user.email!!).body<List<UserDoodle>>()

        if (doodleInfos.isEmpty() ) { return }

        doodleInfos.forEach { doodle ->
            val doodleName = doodle.doodleName
            val timestamp = Date(doodle.timestamp)
            val cleanedBlob = doodle.blob.replace("\n", "")
            val bitmapBytes = Base64.getDecoder().decode(cleanedBlob)
            val bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.size)

            // Save locally and add to Room
            val path = storageVM.saveBitmapAsFile(doodleName, timestamp.toString(), bitmap)
            storageVM.addDoodle(doodleName, timestamp, path)
        }
    }


}