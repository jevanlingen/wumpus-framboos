package framboos.vrolijke.jdriven.com.plugins

import framboos.vrolijke.jdriven.com.dao.CrudRepository
import framboos.vrolijke.jdriven.com.dao.ReadRepository
import framboos.vrolijke.jdriven.com.dao.impl.gameRepo
import framboos.vrolijke.jdriven.com.dao.impl.userRepo
import framboos.vrolijke.jdriven.com.dao.model.CreateUser
import framboos.vrolijke.jdriven.com.service.doGameAction
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NoContent
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
                get("ids") { call.respond(gameRepo.allIds()) }
                post("{id}/action/{action}") { handleGameAction() }
            }
        }

        // TODO: filter these endpoints away from swagger
        authenticate("admin") {
            route("users") {
                get { call.respond(userRepo.all()) }
                get("{id}") { getById(userRepo) }
                delete("{id}") { deleteById(userRepo) }
            }

            route("games") {
                get("{id}") { getById(gameRepo) }
            }
        }

        staticResources("/", "static")
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleGameAction() {
    getId()
        ?.let { doGameAction(it, call.parameters["action"], userId()) }
        ?.let { call.respond(it) }
        ?: call.respond(BadRequest)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.getById(repository: ReadRepository<*>) =
    getId()
        ?.let { repository.findById(it) }
        ?.let { call.respond(it) }
        ?: call.respond(BadRequest)

private suspend fun PipelineContext<Unit, ApplicationCall>.deleteById(repository: CrudRepository<*, *>) =
    getId()
        ?.let { repository.deleteById(it) }
        ?.let { call.respond(NoContent) }
        ?: call.respond(BadRequest)

private fun PipelineContext<Unit, ApplicationCall>.getId() =
    call.parameters["id"]?.toIntOrNull()

private fun PipelineContext<Unit, ApplicationCall>.userId() =
    call.principal<UserPrincipal>()!!.id




/**/