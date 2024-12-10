package com.example.msdpaint

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.ByteArrayOutputStream
import com.google.gson.Gson
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject


class KtorClient(private val user: FirebaseUser?) {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(
                Json{
                    prettyPrint = true
                    isLenient = true
                    useAlternativeNames = true
                    ignoreUnknownKeys = true
                    encodeDefaults = false

                }
            )
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.v("KTOR LOGGER", message)
                }
            }
            level = LogLevel.ALL
        }
        install(ResponseObserver) {
            onResponse { response ->
                Log.d("HTTP Status: ", "${response.status.value}")
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 15000
        }

        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }

        defaultRequest {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }

    } // END OF CLIENT


    // Get Firebase provided token
    private fun getEmail(): String? {
        return user!!.email
    }

    suspend fun sendUser(email: String): HttpResponse {

        val json = buildJsonObject {
            put("email", email)
        }

        val response: HttpResponse = client.post("http://10.0.2.2:8080/user/{$email}") {
            header(HttpHeaders.Authorization, email)
            setBody(json)
        }
        return response
    }

    // Backup doodles
    suspend fun backupLocal(path: String, doodleName: String, user: String, timestamp: Long): HttpResponse {
        val email = getEmail()

        val base64Image = encodeImageToBase64(path)

        val gson = Gson()
        val doodleInfo = UserDoodle (
            user = email!!,
            doodleName = doodleName,
            timestamp = timestamp,
            blob = base64Image
        )

        val json = gson.toJson(doodleInfo)
        val response: HttpResponse = client.post("http://10.0.2.2:8080/backup") {
            header(HttpHeaders.Authorization, email)
            setBody(json)
        }
        return response
    }


    // Share image with another user todo Will have to extract receivers email from UI element
    suspend fun shareImage (path: String, doodleName: String, user: String, email: String, timestamp: Long): HttpResponse {
        val userEmail = getEmail()
        val base64Image = encodeImageToBase64(path)

        val gson = Gson()
        val shareInfo = SharedDoodle (
            user1 = user,
            user2 = email,
            doodleName = doodleName,
            timestamp = timestamp,
            blob = base64Image
        )

        val json = gson.toJson(shareInfo)

        val response: HttpResponse = client.post("http://10.0.2.2:8080/share") {
            header(HttpHeaders.Authorization, userEmail)
            setBody(json)
        }
        return response
    }

    suspend fun retrieveMyImages (user: String): HttpResponse {

        val email = getEmail()
        val response : HttpResponse = client.get("http://10.0.2.2:8080/retrieve") {
            header(HttpHeaders.Authorization, email)
        }
        return response.body()
    }

    suspend fun retrieveSharedImages(user: String): HttpResponse {
        val email = getEmail()
        val response : HttpResponse = client.get("http://10.0.2.2:8080/retrieveShared") {
            header(HttpHeaders.Authorization, email)
        }
        return response.body()
    }

//    // Un-share image  todo Implement if we have time
//    suspend fun removeImage (doodleName: String): HttpResponse? {
//        val token = getToken() ?: throw Exception("Token retrieval failed")
//
//        val response: HttpResponse = client.delete("http://10.0.2.2:8080/delete/{$doodleName}")
//        return null
//    }
//
//    // Retrieve feed  todo Implement if we have time





    private fun encodeImageToBase64(path: String): String {
        val bitmap = BitmapFactory.decodeFile(path) ?: throw IllegalArgumentException("Image file not found at path: $path")
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }
}


@Serializable
data class UserDoodle(
    val user: String,
    val doodleName: String,
    val timestamp: Long,
    val blob: String
)

@Serializable
data class SharedDoodle(
    val user1: String,
    val user2: String,
    val doodleName: String,
    val timestamp: Long,
    val blob: String
)