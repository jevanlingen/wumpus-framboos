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
    ) = request(POST, url, body, authHeader)

    fun get(
        url: String,
        authHeader: String? = null,
    ) = request(GET, url, null, authHeader)

    private fun request(
        method: HttpMethod,
        url: String,
        body: String?,
        authHeader: String?,
    ): String? {
        if (body == null) println("${method.name}: $url") else println("${method.name}: $url\n$body")

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

        println("${response.statusCode()} - ${response.body()}\n")

        return response.body()
    }
}
