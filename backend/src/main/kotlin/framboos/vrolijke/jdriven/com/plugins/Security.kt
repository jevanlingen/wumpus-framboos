package framboos.vrolijke.jdriven.com.plugins

import framboos.vrolijke.jdriven.com.dao.impl.userRepo
import io.ktor.server.application.*
import io.ktor.server.auth.*
import org.mindrot.jbcrypt.BCrypt

fun Application.configureSecurity() {
    authentication {
        basic("auth") {
            realm = "Ktor Server"
            validate { credentials ->
                userRepo.getHashedPasswordByName(credentials.name)
                    ?.let { BCrypt.checkpw(credentials.password, it) }
                    ?.let { if (it) UserIdPrincipal(credentials.name) else null }
            }
        }
    }
}
