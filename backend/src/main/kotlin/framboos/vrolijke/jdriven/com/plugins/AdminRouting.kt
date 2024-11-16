package framboos.vrolijke.jdriven.com.plugins

import framboos.vrolijke.jdriven.com.dao.impl.competitionRepo
import framboos.vrolijke.jdriven.com.dao.impl.userRepo
import framboos.vrolijke.jdriven.com.plugins.Role.ADMIN
import framboos.vrolijke.jdriven.com.utils.deleteX
import framboos.vrolijke.jdriven.com.utils.getX
import framboos.vrolijke.jdriven.com.utils.postX
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

fun Application.configureAdminRouting() {
    routing {
        authenticate(ADMIN()) {
            route("users") {
                getX { call.respond(userRepo.all()) }
                getX("{id}") {
                    val user = getId()?.let { userRepo.findById(it) }
                    if (user == null) call.respond(BadRequest) else call.respond(user)
                }
                deleteX("{id}") {
                    getId()?.let { userRepo.deleteById(it) }
                    call.respond(NoContent)
                }
            }

            route("competitions") {
                postX("{id}/action/advance") {
                    getId()?.let { competitionRepo.advance(it) }
                    call.respond(NoContent)
                }
            }

            route("admin") {
                postX("delay/{delay}") {
                    delay = call.parameters["delay"]?.toIntOrNull() ?: return@postX call.respond(BadRequest)
                    call.respond(OK)
                }
            }
        }
    }
}

private fun PipelineContext<Unit, ApplicationCall>.getId() =
    call.parameters["id"]?.toIntOrNull()
