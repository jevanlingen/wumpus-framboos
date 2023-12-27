package framboos.vrolijke.jdriven.com.plugins

import framboos.vrolijke.jdriven.com.dao.impl.userRepo
import framboos.vrolijke.jdriven.com.dao.model.User
import framboos.vrolijke.jdriven.com.utils.checkPassword
import io.ktor.server.application.*
import io.ktor.server.auth.*

fun Application.configureSecurity() {
    authentication {
        basic("gamers") {
            validate { validateUser(it) }
        }

        basic("admin") {
            validate { validateUser(it) { user -> user.admin } }
        }
    }
}

private suspend fun validateUser(
    credentials: UserPasswordCredential,
    extraCheck: (User) -> Boolean = { true }
) =
    userRepo.getByName(credentials.name)
        ?.let { checkPassword(credentials.password, it.password) && extraCheck(it) }
        ?.let { if (it) UserIdPrincipal(credentials.name) else null }

