package com.example.msdpaint.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.example.msdpaint.KtorClient
import com.example.msdpaint.MainActivity
import com.example.msdpaint.MsdPaintApplication
import com.example.msdpaint.R
import com.example.msdpaint.databinding.FragmentSignInBinding
import com.example.msdpaint.viewmodels.StorageViewModel
import com.example.msdpaint.viewmodels.StorageViewModelFactory
import com.example.msdpaint.viewmodels.StudioViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlin.getValue


class SignInFragment : Fragment() {

    private lateinit var client: KtorClient
    private val storageVM: StorageViewModel by viewModels{
        StorageViewModelFactory((requireActivity().application as MsdPaintApplication).doodleRepository)
    }
    private val studioVM: StudioViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentSignInBinding.inflate(inflater)

        binding.composeView.setContent {

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {

                LoginArea(modifier = Modifier
                    .align(Alignment.Center)
                )

                BackButton(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }

        }

        return binding.root

    }


    @Composable
    fun LoginArea(modifier: Modifier) {

        Surface(
            modifier = modifier
                .padding(32.dp)
                .wrapContentSize(),
            color = MaterialTheme.colorScheme.background
        ) {

            var user by remember { mutableStateOf(MainActivity.GLOBAL_USER) }

            Column (
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (user == null) {
                    SignInStuff()
                } else {
                    SignOutStuff()
                }

            }

        }

    }


    @Composable
    fun SignInStuff() {

        Column (
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }

//            var email = "sonic@utah.edu"
//            var password = "ABC12345%"

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                },
                label = {
                    Text("Email")
                }
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                },
                label = {
                    Text("Password")
                },
                visualTransformation = PasswordVisualTransformation()
            )

            Row {

                Button(onClick = {
                    Firebase.auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                MainActivity.GLOBAL_USER = Firebase.auth.currentUser
                                client = KtorClient(MainActivity.GLOBAL_USER)
                                lifecycleScope.launch() {
                                    client.sendUser(MainActivity.GLOBAL_USER!!.email.toString())
                                }
                                findNavController(this@SignInFragment).navigate(R.id.action_signInFragment_to_homeFragment)
                            } else {
                                email = "login failed, try again"
                            }
                        }
                }) {
                    Text("Log In")
                }

                Button(onClick = {
                    Firebase.auth.createUserWithEmailAndPassword(
                        email,
                        password
                    )
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                MainActivity.GLOBAL_USER = Firebase.auth.currentUser
                                Toast.makeText(context, "SignUp successful!", Toast.LENGTH_LONG).show()
                            } else {
                                email = "Create user failed, try again"
                                Log.e("Create user error", "${task.exception}")
                            }
                        }
                }) {
                    Text("Sign Up")
                }

            } // Row()

        } // Column()

    } // SignInStuff()


    @Composable
    fun SignOutStuff() {

        Button(onClick = {
            Firebase.auth.signOut()
            MainActivity.GLOBAL_USER = null
            storageVM.deleteAll()
            studioVM.resetStudio()
            findNavController(this@SignInFragment).navigate(R.id.action_signInFragment_to_homeFragment)
        }) {
            Text("Sign out")
        }

        Button(onClick = {
            findNavController(this@SignInFragment).navigate(R.id.action_signInFragment_to_homeFragment)
        }) {
            Text("Go To Home")
        }

    }


    @Composable
    fun BackButton(modifier: Modifier) {

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {

            Image(
                painter = painterResource(id = R.drawable.back_arrow),
                contentDescription = "sign in fragment back (to home) button",
                modifier = Modifier.clickable{ findNavController(this@SignInFragment).navigate(R.id.action_signInFragment_to_homeFragment) }
            )

        }

    }


    private fun firebaseSignIn() {



    }


    private fun firebaseSignUp() {



    }


}