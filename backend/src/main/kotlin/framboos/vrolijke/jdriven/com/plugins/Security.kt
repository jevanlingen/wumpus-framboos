package framboos.vrolijke.jdriven.com.plugins

import framboos.vrolijke.jdriven.com.dao.impl.userRepo
import framboos.vrolijke.jdriven.com.dao.model.User
import framboos.vrolijke.jdriven.com.plugins.Role.ADMIN
import framboos.vrolijke.jdriven.com.plugins.Role.GAMER
import framboos.vrolijke.jdriven.com.utils.checkPassword
import io.ktor.server.application.*
import io.ktor.server.auth.*

enum class Role {
    GAMER, ADMIN;

    operator fun invoke() = name
}

data class UserPrincipal(val id: Int, val name: String, val role: Role) : Principal

fun Application.configureSecurity() {
    authentication {
        basic(GAMER()) {
            validate { validateUser(it, GAMER) { user -> !user.admin } }
        }

        basic(ADMIN()) {
            validate { validateUser(it, ADMIN) { user -> user.admin } }
        }
    }
}

private suspend fun validateUser(credentials: UserPasswordCredential, role: Role, extraCheck: (User) -> Boolean) =
    userRepo.findByName(credentials.name)
        ?.takeIf { checkPassword(credentials.password, it.password) && extraCheck(it) }
        ?.let { UserPrincipal(it.id, it.name, role) }
