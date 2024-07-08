package framboos.vrolijke.jdriven.com.utils

import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

/**
 * Builds a route to match `GET` requests.
 * Can only be reached from localhost.
 * @see [Application.routing]
 */
fun Route.getX(body: PipelineInterceptor<Unit, ApplicationCall>) =
    method(Get) { handle { handle(body) } }

/**
 * Builds a route to match `GET` requests with the specified [path].
 * Can only be reached from localhost.
 * @see [Application.routing]
 */
fun Route.getX(path: String, body: suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit) =
    get(path) { handle(body) }

/**
 * Builds a route to match `POST` requests with the specified [path].
 * Can only be reached from localhost.
 * @see [Application.routing]
 */
fun Route.postX(path: String, body: suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit) =
    post(path) { handle(body) }

/**
 * Builds a route to match `DELETE` requests with the specified [path].
 * Can only be reached from localhost.
 * @see [Application.routing]
 */
fun Route.deleteX(path: String, body: suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit) =
    delete(path) { handle(body) }

private suspend inline fun PipelineContext<Unit, ApplicationCall>.handle(body: suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit) =
    if (call.isLocal()) body(Unit) else call.respond(NotFound)

private fun ApplicationCall.isLocal() =
    request.origin.remoteHost == "localhost" || request.origin.remoteHost == "127.0.0.1"
