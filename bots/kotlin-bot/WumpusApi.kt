import java.util.Base64

class WumpusApi(
    private val baseUrl: String,
) {
    private val client = SimpleHttpClient()
    private var authHeader: String? = null

    fun createAccount(
        username: String,
        password: String,
    ): Boolean {
        val jsonBody = """{"name":"$username","password":"$password"}"""
        val response = client.post("$baseUrl/create-account", jsonBody)

        if (response != null) {
            val auth = Base64.getEncoder().encodeToString("$username:$password".toByteArray())
            authHeader = "Basic $auth"
            return true
        }
        return false
    }

    fun getCompetitions(): List<Int>? {
        val response = client.get("$baseUrl/competitions/ids", authHeader)
        return response?.let { parseJsonArray(it).map { it.toString().toInt() } }
    }

    fun getCompetition(id: Int): Competition? {
        val response = client.get("$baseUrl/competitions/$id", authHeader)
        return response?.let { parseCompetition(parseJsonObject(it)) }
    }

    fun getGame(id: Int): Game? {
        val response = client.get("$baseUrl/games/$id", authHeader)
        return response?.let { parseGame(parseJsonObject(it)) }
    }

    fun performAction(
        gameId: Int,
        action: String,
    ): Player? {
        val response = client.post("$baseUrl/games/$gameId/action/$action", null, authHeader)
        return response?.let { parsePlayer(parseJsonObject(it)) }
    }

    private fun parseCompetition(data: Map<String, Any?>): Competition {
        val gameIds = (data["gameIds"] as List<*>).map { it.toString().toInt() }
        return Competition(
            id = data["id"].toString().toInt(),
            currentGameId = data["currentGameId"].toString().toInt(),
            gameIds = gameIds,
        )
    }

    private fun parseGame(data: Map<String, Any?>): Game =
        Game(
            id = data["id"].toString().toInt(),
            gridSize = data["gridSize"].toString().toInt(),
            pits = data["pits"].toString().toInt(),
        )

    private fun parsePlayer(data: Map<String, Any?>): Player {
        val userMap = data["user"] as Map<String, Any?>
        val coordinateMap = data["coordinate"] as Map<String, Any?>

        return Player(
            id = data["id"].toString().toInt(),
            user = parseUser(userMap),
            gameId = data["gameId"].toString().toInt(),
            direction = data["direction"].toString(),
            perceptions = (data["perceptions"] as List<*>).map { it.toString() },
            coordinate = parseCoordinate(coordinateMap),
            points = data["points"].toString().toInt(),
            arrows = data["arrows"].toString().toInt(),
            planks = data["planks"].toString().toInt(),
            wumpusAlive = data["wumpusAlive"] as Boolean,
            hasTreasure = data["hasTreasure"] as Boolean,
            gameCompleted = data["gameCompleted"] as Boolean,
            death = data["death"] as Boolean,
        )
    }

    private fun parseUser(data: Map<String, Any?>): User =
        User(
            id = data["id"].toString().toInt(),
            name = data["name"].toString(),
            password = data["password"].toString(),
            admin = data["admin"] as Boolean,
            shirtColor = data["shirtColor"].toString(),
            trouserColor = data["trouserColor"].toString(),
            skinColor = data["skinColor"].toString(),
        )

    private fun parseCoordinate(data: Map<String, Any?>): Coordinate =
        Coordinate(
            x = data["x"].toString().toInt(),
            y = data["y"].toString().toInt(),
        )
}
