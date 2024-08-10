data class CreateUser(
    val name: String,
    val password: String,
)

data class Competition(
    val id: Int,
    val currentGameId: Int,
    val gameIds: List<Int>,
)

data class Game(
    val id: Int,
    val gridSize: Int,
    val pits: Int,
)

data class Player(
    val id: Int,
    val user: User,
    val gameId: Int,
    val direction: String,
    val perceptions: List<String>,
    val coordinate: Coordinate,
    val points: Int,
    val arrows: Int,
    val planks: Int,
    val wumpusAlive: Boolean,
    val hasTreasure: Boolean,
    val gameCompleted: Boolean,
    val death: Boolean,
)

data class User(
    val id: Int,
    val name: String,
    val password: String,
    val admin: Boolean,
    val shirtColor: String,
    val trouserColor: String,
    val skinColor: String,
)

data class Coordinate(
    val x: Int,
    val y: Int,
)
