package framboos.vrolijke.jdriven.com.dao.model

enum class Direction { NORTH, EAST, SOUTH, WEST }
enum class Perception { STENCH, BREEZE, GLITTER, BUMP, SCREAM, LADDER }

data class CreatePlayer(val userId: Int, val gameId: Int, val startingLocation: Coordinate, val points: Int = 0, val arrows: Int = 1, val planks: Int = 0)
data class Player(override val id: Int, val userId: Int, val user: String, val gameId: Int, val direction: Direction, val perceptions: List<Perception> = listOf(), val coordinate: Coordinate, val points: Int, val arrows: Int, val planks: Int, val wumpusAlive: Boolean, val hasTreasure: Boolean, val gameCompleted : Boolean, val death : Boolean) : Dto

object Players : GameElement() {
    val userId = reference("user_id", Users)
    val direction = enumerationByName<Direction>("direction", 5)
    val points = integer("points").default(0)
    val arrows = integer("arrows")
    val planks = integer("planks")
    val wumpusAlive = bool("wumpus-alive").default(true)
    val hasTreasure = bool("has-treasure").default(false)
    val gameCompleted = bool("game-completed").default(false)
    val death = bool("death").default(false)
}
