package framboos.vrolijke.jdriven.com.plugins

import framboos.vrolijke.jdriven.com.MODE
import framboos.vrolijke.jdriven.com.Mode.HACKING
import framboos.vrolijke.jdriven.com.dao.impl.competitionRepo
import framboos.vrolijke.jdriven.com.dao.impl.gameRepo
import framboos.vrolijke.jdriven.com.dao.impl.userRepo
import framboos.vrolijke.jdriven.com.dao.model.CreateUser
import framboos.vrolijke.jdriven.com.dao.model.GameForPlayer
import framboos.vrolijke.jdriven.com.plugins.Role.ADMIN
import framboos.vrolijke.jdriven.com.plugins.Role.GAMER
import framboos.vrolijke.jdriven.com.service.doGameAction
import framboos.vrolijke.jdriven.com.utils.checkPassword
import framboos.vrolijke.jdriven.com.utils.isLocal
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

var delay = 280

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
                val id = getId() ?: return@post call.respond(BadRequest)
                if (delay > 0) delayRequest()

                doGameAction(id, call.parameters["action"], userId())
                    .fold(
                        onFailure = { call.respond(BadRequest.copy(description = it.message ?: "")) },
                        onSuccess = { call.respond(it) }
                    )
            }
        }

        authenticate(GAMER(), ADMIN()) {
            route("games") {
                get("{id}") {
                    val game = getId()?.let { gameRepo.findById(it) }?.let {
                        when {
                            role() == ADMIN && (call.isLocal() || MODE == HACKING) -> it
                            else -> GameForPlayer(it.id, it.gridSize, it.pits.size)
                        }
                    }
                    if (game == null) call.respond(BadRequest) else call.respond(game)
                }
                get("ids") { call.respond(gameRepo.allIds()) }
            }
            route("competitions") {
                get("{id}") {
                    val competition =
                        when {
                            role() == ADMIN && (call.isLocal() || MODE == HACKING) -> getId()?.let { competitionRepo.findByIdWithScore(it) }
                            else -> getId()?.let { competitionRepo.findById(it) }
                        }
                    if (competition == null) call.respond(BadRequest) else call.respond(competition)
                }
                get("ids") { call.respond(competitionRepo.allIds()) }
            }
        }

        if (MODE == HACKING) {
            staticResources("/", "static")
        }
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
