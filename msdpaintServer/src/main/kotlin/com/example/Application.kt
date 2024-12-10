package com.example

import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.google.auth.oauth2.GoogleCredentials
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.InputStream
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

// setting up firebase
object FirebaseAdmin {
    private val serviceAccount: InputStream? =
        this::class.java.classLoader.getResourceAsStream("ktor-firebase-auth-adminsdk.json")

    private val options: FirebaseOptions = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build()

    fun init(): FirebaseApp = FirebaseApp.initializeApp(options)
}

/* Kotlin's way of making static classes.
This is just a singleton object when instantiated, which is done below in Application.module()
This object happens to be a way for us to implement database configuration and management. */

object DBSettings {

    /* "Most recently created" DB will get used by all transactions automatically.
    This is an in-memory DB, but we could connect to mysql or something else. */
    /* The first parameter is the connection string that says:
    *   + "jdbc:h2:mem:test": An H2 database in memory with the name test.
        + "MODE=MYSQL": Configures the database to emulate MySQL behavior, making it easier to
           work with MySQL-compatible queries.
        + "DB_CLOSE_DELAY=-1": Keeps the database open even when there are no
           active connections, preventing it from being closed until the application terminates. */

    val db by lazy {
        Database.connect("jdbc:h2:mem:test;MODE=MYSQL;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
    }

    fun init() {

        /* This is an Exposed function that creates the tables when called, i.e. a "CREATE" statement. */

        transaction(db) {
            SchemaUtils.create(Doodle, User, Shared)
        }

    }

}


fun main() {

    /*
        + `embeddedServer`: This is a function from the Ktor framework that creates an embedded server instance. It allows you to run the application within the same process, which is useful for development and testing.
        + `Netty`: This specifies the server engine to use. Ktor supports various engines, with Netty being one of the most common. It’s a popular asynchronous event-driven network application framework.
        + `port = 8080`: This sets the port number for the server to listen on. In this case, it’s set to 8080, which is commonly used for web applications.
        + `host = "0.0.0.0"`: This specifies the host address the server should bind to. The address "0.0.0.0" means the server will listen for incoming connections on all available network interfaces. This is useful for making the server accessible from outside the host machine.
        + `module = Application::module`: This is where you specify the Ktor application module to be executed. `Application::module` refers to the `module` extension function defined for the `Application` class, which contains the configuration logic for the application (like routing, security, etc.). It’s a way to indicate which setup function Ktor should run when the server starts.
        + `.start(wait = true)`: This starts the server. The `wait = true` parameter means the function will block the current thread and keep the server running until it’s explicitly stopped (e.g., by a user interrupt).
    */

    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
            .start(wait = true)

}


/* This is defining an "extension function" for the Application class. Syntactic sugar.
It basically follows the conceptual syntax of a method reference: ClassName.yourExtensionFunction()
and it "adds" (doesn't actually modify the source code) that functionality to the class.
You can do this with literally any class in Kotlin. Very powerful/flexible feature that
reduces a lot of boilerplate and avoids having to work with an instance of the object which
introduces state and other messy things. */

fun Application.module() {

    DBSettings.init()
    FirebaseAdmin.init()
//    configureSecurity()
//    configureHTTP()
//    configureMonitoring()
    configureSerialization()
    configureResources()
    configureRouting()

}