package framboos.vrolijke.jdriven.com

import framboos.vrolijke.jdriven.com.dao.DatabaseSingleton
import framboos.vrolijke.jdriven.com.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseSingleton.init()
    configureSecurity()
    configureHTTP()
    configureSerialization()
    configureRouting()
}
