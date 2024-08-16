import HttpMethod.GET
import HttpMethod.POST
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers.noBody
import java.net.http.HttpRequest.BodyPublishers.ofString
import java.net.http.HttpResponse

enum class HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
}

class SimpleHttpClient {
    fun post(
        url: String,
        body: String?,
        authHeader: String? = null,
        username: String? = null,
    ) = request(POST, url, body, authHeader, username)

    fun get(
        url: String,
        authHeader: String? = null,
        username: String? = null,
    ) = request(GET, url, null, authHeader, username)

    private fun request(
        method: HttpMethod,
        url: String,
        body: String?,
        authHeader: String?,
        username: String?,
    ): String? {
        println("$username | ${method.name}: $url")
        // if (body == null) println("$username | ${method.name}: $url") else println("$username | ${method.name}: $url\n$body")

        val requestBuilder =
            HttpRequest
                .newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")

        when (method) {
            GET -> requestBuilder.GET()
            POST -> requestBuilder.POST(if (body == null) noBody() else ofString(body))
            else -> TODO()
        }

        authHeader?.let { requestBuilder.header("Authorization", it) }

        val request = requestBuilder.build()
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())

        // println("${response.statusCode()} - ${response.body()}\n")

        return response.body()
    }
}
