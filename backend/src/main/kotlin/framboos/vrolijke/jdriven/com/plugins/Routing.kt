package framboos.vrolijke.jdriven.com.plugins

import framboos.vrolijke.jdriven.com.dao.impl.competitionRepo
import framboos.vrolijke.jdriven.com.dao.impl.gameRepo
import framboos.vrolijke.jdriven.com.dao.impl.userRepo
import framboos.vrolijke.jdriven.com.dao.model.CreateUser
import framboos.vrolijke.jdriven.com.plugins.Role.ADMIN
import framboos.vrolijke.jdriven.com.plugins.Role.GAMER
import framboos.vrolijke.jdriven.com.service.doGameAction
import framboos.vrolijke.jdriven.com.utils.checkPassword
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private var delay = 0

fun Application.configureRouting() {
    routing {
        post("create-account") {
            val newUser = call.receive<CreateUser>()
            val existingUser = userRepo.findByName(newUser.name)
            if (existingUser == null || !checkPassword(newUser.password, existingUser.password)) userRepo.add(newUser)
            call.respond(Created)
        }

        authenticate(GAMER()) {
            post("games/{id}/action/{action}") {
                val player = getId()?.let { doGameAction(it, call.parameters["action"], userId()) }
                if (player == null)
                    call.respond(BadRequest)
                else {
                    if (delay > 0) delayRequest()
                    call.respond(player)
                }
            }
        }

        authenticate(GAMER(), ADMIN()) {
            route("games") {
                get("ids") { call.respond(gameRepo.allIds()) }
            }
            route("competitions") {
                get("{id}") {
                    val competition =
                        when (role()) {
                            GAMER -> getId()?.let { competitionRepo.findById(it) }
                            ADMIN -> getId()?.let { competitionRepo.findByIdWithScore(it) }
                        }
                    if (competition == null) call.respond(BadRequest) else call.respond(competition)
                }
                get("ids") { call.respond(competitionRepo.allIds()) }
            }
        }

        // TODO: filter these endpoints away from player swagger and make another endpoint for admin swagger
        authenticate(ADMIN()) {
            route("users") {
                get { call.respond(userRepo.all()) }
                get("{id}") {
                    val user = getId()?.let { userRepo.findById(it) }
                    if (user == null) call.respond(BadRequest) else call.respond(user)
                }
                delete("{id}") {
                    getId()?.let { userRepo.deleteById(it) }
                    call.respond(NoContent)
                }
            }

            route("games") {
                get("{id}") {
                    val game = getId()?.let { gameRepo.findById(it) }
                    if (game == null) call.respond(BadRequest) else call.respond(game)
                }
            }

            route("competitions") {
                post("{id}/action/advance") {
                    getId()?.let { competitionRepo.advance(it) }
                    call.respond(NoContent)
                }
            }

            route("admin") {
                post("delay/{delay}") {
                    delay = call.parameters["delay"]?.toIntOrNull() ?: return@post call.respond(BadRequest)
                    call.respond(OK)
                }
            }
        }

        staticResources("/", "static")
    }
}

private fun PipelineContext<Unit, ApplicationCall>.getId() =
    call.parameters["id"]?.toIntOrNull()

private fun PipelineContext<Unit, ApplicationCall>.userId() =
    call.principal<UserPrincipal>()!!.id

private fun PipelineContext<Unit, ApplicationCall>.role() =
    call.principal<UserPrincipal>()!!.role

private suspend fun delayRequest() {
    delay((maxOf(delay - 80, 0)..delay + 80).random().milliseconds)
}
