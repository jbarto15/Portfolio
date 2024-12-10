package com.example

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserRecord
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

fun Application.configureResources() {

    routing {

        get ("/getuser/{userEmail}") {

            val user = call.parameters["userEmail"]

            if ( user != null ) {

                val userEmail = newSuspendedTransaction(Dispatchers.IO) {

                    User.select { User.email eq user }
                        .singleOrNull()
                        ?.get(User.email)
                }

                call.respondText("User Email: $userEmail")

            }

        }

        // Add a user
        post("/user/{email}") {
            //val response: HttpResponse = client.post("http://10.0.2.2:8080/user/{$email}") {

            val userData = call.receive<UserData>()

            val userEmail = userData.email

            if ( userEmail != null )
            {
                // add the user to the User table
                newSuspendedTransaction(Dispatchers.IO) {
                    User.insertIgnore {
                        it[email] = userEmail
                    }
                }

                call.respond("User added successfully")

                val userID = newSuspendedTransaction(Dispatchers.IO) {
                    User.select { User.email eq userEmail }
                        .singleOrNull()
                        ?.get(User.id)
                }

                println("User: " + userEmail + " UserID: " + userID)


            } else {
                call.respondText { "User Email is null" }
            }

        }

        // Create a doodle AKA Backup doodle // Adds to doodle table when a user saves their image
        post("/backup") {

            //val response: HttpResponse = client.post("http://10.0.2.2:8080/backup/{$path}/{$doodleName}/{$user}") {

            println("In the create doodle")

            val userEmail = call.request.headers["Authorization"]

            println("User email: $userEmail")

            if (userEmail != null) {
                val verifiedClient = verifyEmail(userEmail)

                println("Verified client: $verifiedClient")

                if (verifiedClient != null) {
                    val newDoodleData = call.receive<DoodleData>()
//                    val base64String = newDoodleData.blob
//                    val decodedBytes = Base64.getDecoder().decode(base64String)
//                    val blob = ExposedBlob(decodedBytes)

                    // Retrieve UserID for the given email
                    val userID = newSuspendedTransaction(Dispatchers.IO) {
                        User.select { User.email eq userEmail }
                            .singleOrNull()
                            ?.get(User.id)
                    }

                    println("User ID: $userID")

                    if (userID != null) {
                        // Insert the doodle using the UserID
                        newSuspendedTransaction(Dispatchers.IO) {
                            Doodle.insertIgnore {
                                it[name] = newDoodleData.doodleName
                                it[user] = userID // Use userID instead of userEmail
                                it[timestamp] = newDoodleData.timestamp
                                it[data] = newDoodleData.blob
                            }
                        }

                        call.respondText("Doodle created successfully")
                        println("==========================================User: " + userID + " UserEmail: " + userEmail)


                    } else {
                        println("==========================================backup: user not found: " + userEmail)
                        println("==========================================backup: userID not found: " + userID)

                        call.respondText("User not found")
                    }
                } else {
                    call.respondText("User not valid")
                }
            } else {
                call.respondText("No header")
            }
        }

        // Share a doodle from one user to another. Add an entry to the shared table
        post("/share") {

            // authenticate user
            val userEmail = call.request.headers["Authorization"]

            if ( userEmail != null ) {

                val verifiedClient = verifyEmail(userEmail)

                if ( verifiedClient != null) {

                    val sharedDoodleData = call.receive<SharedDoodleData>()


                    // need to get the user ID for user1
                    val user1 = newSuspendedTransaction(Dispatchers.IO) {
                        User.select { User.email eq sharedDoodleData.user1 }
                            .singleOrNull()
                            ?.get(User.id)
                    }

                    println("User1: $user1")

                    // need to get the user ID for user2
                    val user2 = newSuspendedTransaction(Dispatchers.IO) {
                        User.select { User.email eq sharedDoodleData.user2 }
                            .singleOrNull()
                            ?.get(User.id)
                    }

                    println("User1: $user2")

                    // Doodle.select(Doodle.data eq sharedDoodleData.blobString)

                    // Get the doodle to be shared
                    val doodleID = newSuspendedTransaction(Dispatchers.IO) {
                        Doodle.select { Doodle.name eq sharedDoodleData.doodleName }
                              .singleOrNull()
                            ?.get(Doodle.id)
                    }

                    println("DoodleID: $doodleID")

                    if ( user1 != null && user2 != null && doodleID != null ) {

                        newSuspendedTransaction(Dispatchers.IO) {

                            Shared.insert {
                                it[Shared.user1] = user1
                                it[Shared.user2] = user2
                                it[Shared.doodle] = doodleID
                            }

                        }

                        call.respondText("Successfully shared doodle")

                    } else {
                        call.respondText("Could not share doodle with user")
                    }

                } else {
                // end of verifiedClient if statement
                    call.respondText("not a verified client")
                }

            } else {
            // end of userEmail if statement
                call.respondText("No header")
            }

        }

        // Retrieve a specific user's images
        get("/retrieve") {

            //val userEmail = call.parameters["user"]

            val userEmail = call.request.headers["Authorization"]

            if ( userEmail != null ) {

                val verifiedClient = verifyEmail(userEmail)

                if (verifiedClient != null) {

                    // Get the id of the user
                    val userID = newSuspendedTransaction(Dispatchers.IO) {
                        User.select { User.email eq userEmail }
                            .singleOrNull()
                            ?.get(User.id)
                    }

                    if ( userID != null ) {

                        val doodles = newSuspendedTransaction(Dispatchers.IO) {

                            // Get all the doodles for that user
                            Doodle.select(Doodle.user eq userID)
                                .map {

                                    // Encode the blob to a blobstring
//                                    val encodedBlobString = Base64.getEncoder().encodeToString(blob.doo)

                                    DoodleData(
                                        user = userEmail, //userID.value,
                                        doodleName = it[Doodle.name],
                                        timestamp = it[Doodle.timestamp],
                                        blob = it[Doodle.data]
                                    )
                                } // end of map

                        } // end of doodles

                        call.respond(doodles)

                    } else {
//                        call.respondText("No doodles for specified user")
                        call.respond(listOf<DoodleData>())
                    }

                } else {
                    call.respondText("Not a verified client")
                }

            } else {
                call.respondText("No header")
            }

        }

        // retrieve all images shared to a specific user
        get("retrieveShared") {

            val userEmail = call.request.headers["Authorization"]

            if (userEmail != null) {

                val verifiedClient = verifyEmail(userEmail)

                if (verifiedClient != null) {

                    // get the id of the user from the database
                    val userID = newSuspendedTransaction(Dispatchers.IO) {
                        User.select { User.email eq userEmail }
                            .singleOrNull()
                            ?.get(User.id)
                    }

                    if (userID != null) {

                        val sharedDoodles = newSuspendedTransaction(Dispatchers.IO) {
                            // Ensure join is correct based on your schema
                            (Shared innerJoin Doodle).select { Shared.user2 eq userID }
                                .map {
                                    val blob = it[Doodle.data]
//                                    val encodedBlobString = Base64.getEncoder().encodeToString(blob.bytes)

                                    DoodleData(
                                        user = userEmail,
                                        doodleName = it[Doodle.name],
                                        timestamp = it[Doodle.timestamp],
                                        blob = it[Doodle.data]
                                    )
                                }
                        }
                        call.respond(sharedDoodles)

                    } else {
                        call.respond(listOf<DoodleData>())
//                        call.respondText("No doodles shared to user: $userEmail")
                    }

                } else {
                    call.respondText("Not a verified client")
                }
            } else {
                call.respondText("No header")
            }
        }


        // Delete a doodle
        delete("/doodles/delete/{user}/{name}") {

            val userEmail = call.request.headers["Authorization"]

            if (userEmail != null) {
                val verifiedClient = verifyEmail(userEmail)

                if (verifiedClient != null) {

                    val name = call.parameters["name"]

                    if (name != null) {

                        // get the id of the user based on email
                        val userID = newSuspendedTransaction(Dispatchers.IO) {
                            User.select { User.email eq userEmail }
                                .singleOrNull()
                                ?.get(User.id)
                        }

                        if (userID != null) {
                            // Delete doodle based on user ID and name
                            val rowsDeleted = newSuspendedTransaction(Dispatchers.IO) {
                                Doodle.deleteWhere { (Doodle.user eq userID) and (Doodle.name eq name) }
                            }

                            if (rowsDeleted > 0) {
                                call.respondText("Doodle deleted successfully")
                            } else {
                                call.respondText("Doodle not found")
                            }

                        } else {
                            call.respondText("delete: User not found")
                        }

                    } else {
                        call.respondText("Invalid doodle name")
                    }

                } else {
                    call.respondText("Not a verified client")
                }
            } else {
                call.respondText("No header")
            }
        }


    } // routing{}

} // configureResources()

fun verifyEmail(email: String) : UserRecord? {
    return try {
        FirebaseAuth.getInstance().getUserByEmail(email)
    } catch (e: FirebaseAuthException) {
        null
    }
}


@Serializable
data class DoodleData(val user: String, val doodleName: String, val timestamp: Long, val blob: String)
//data class UserDoodle(
//    val user: String,
//    val doodleName: String,
//    val timestamp: Long,
//    val blob: String
//)

@Serializable
data class SharedDoodleData(val user1: String, val user2: String, val doodleName: String, val timestamp: Long, val blob: String)

@Serializable
data class UserData(val email: String)

@Serializable
data class DeleteDoodle(val user: String, val name: String)


