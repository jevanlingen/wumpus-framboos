package framboos.vrolijke.jdriven.com.plugins

import framboos.vrolijke.jdriven.com.dao.impl.gameRepo
import framboos.vrolijke.jdriven.com.dao.impl.userRepo
import framboos.vrolijke.jdriven.com.dao.model.CreateUser
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

fun Application.configureRouting() {
    routing {
        post("create-account") {
            userRepo.add(call.receive<CreateUser>())
            call.respond(Created)
        }

        authenticate("gamers", "admin") {
            route("games") {
                get { call.respondText("list of games :P...") } // { "grid-size": 16, active: true }
                get("active") { call.respondText("get active game") }
                post("enter/{id}") { call.respondText("enter ${getId()} game, to play it") }
                // action/{left-turn|right-turn|move-forward|grab|release|shoot}
                //     => response: new-state | not-in-game | game-ended
            }
        }

        // TODO: filter these endpoints away from swagger
        authenticate("admin") {
            route("users") {
                get { call.respond(userRepo.all()) }
                get("{id}") {
                    getId()
                        ?.let { userRepo.findById(it) }
                        ?.let { call.respond(it) }
                        ?: call.respond(BadRequest)
                }
                delete("{id}") {
                    getId()
                        ?.let { userRepo.delete(it) }
                        ?.let { call.respond(NoContent) }
                        ?: call.respond(BadRequest)
                }
            }

            route("games") {
                get("{id}") {
                    getId()
                        ?.let { gameRepo.findById(it) }
                        ?.let { call.respond(it) }
                        ?: call.respond(BadRequest)
                }
                get("active") { call.respondText("Get the state of the active game (shortcut for /games/{id}") }
            }
        }

        staticResources("/", "static")
    }
}

private fun PipelineContext<Unit, ApplicationCall>.getId() =
    call.parameters["id"]?.toIntOrNull()

@JvmName("respondWithType")
suspend inline fun <reified T : Any> ApplicationCall.respond(status: HttpStatusCode, message: T?) =
    if (message == null) response.status(NotFound)
    else {
        response.status(status)
        respond(message)
    }
