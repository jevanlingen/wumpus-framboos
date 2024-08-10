import HttpMethod.GET
import HttpMethod.POST
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_CREATED
import java.net.HttpURLConnection.HTTP_OK
import java.net.URL

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
        val connection =
            (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = method.name
                setRequestProperty("Content-Type", "application/json")
                authHeader?.let { setRequestProperty("Authorization", it) }
                body?.let {
                    doOutput = true
                    DataOutputStream(outputStream).use { stream -> stream.writeBytes(it) }
                }
            }

        val responseCode = connection.responseCode
        return if (responseCode == HTTP_OK || responseCode == HTTP_CREATED) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() }
        }
    }
}
