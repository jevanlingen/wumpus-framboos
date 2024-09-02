package framboos.vrolijke.jdriven.com

import framboos.vrolijke.jdriven.com.Mode.HACKING
import framboos.vrolijke.jdriven.com.Mode.CONTEST
import framboos.vrolijke.jdriven.com.dao.DatabaseSingleton
import framboos.vrolijke.jdriven.com.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

enum class Mode {
    HACKING, CONTEST
}

val MODE = CONTEST

fun main() {
    embeddedServer(Netty, host = "0.0.0.0", port = 80, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseSingleton.init()
    configureSecurity()
    configureHTTP()
    configureSerialization()
    configureRouting()
    configureAdminRouting()
}
