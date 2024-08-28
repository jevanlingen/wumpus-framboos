package framboos.vrolijke.jdriven.com.dao.model

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption.CASCADE

data class Coordinate(val x: Int, val y: Int)

data class Wumpus(override val id: Int, val coordinate: Coordinate) : Dto
data class Pit(override val id: Int, val coordinate: Coordinate) : Dto
data class Treasure(override val id: Int, val coordinate: Coordinate) : Dto
data class Game(override val id: Int, val name: String, val gridSize: Int, val pits: List<Pit> = listOf(), val wumpus: Wumpus, val treasure: Treasure, val players: List<Player> = listOf()) : Dto {
    val startingLocation get() = Coordinate(1, 1)
}
data class GameForPlayer(override val id: Int, val gridSize: Int, val pits: Int) : Dto {
    val startingLocation get() = Coordinate(1, 1)
}

object Games : IntIdTable() {
    val name = varchar("name", 255)
    val gridSize = integer("grid_size")
}

abstract class GameElement : IntIdTable() {
    val x = integer("x")
    val y = integer("y")
    val gameId = reference("game_id", Games, onDelete = CASCADE)
}

object Wumpusses : GameElement()

object Pits : GameElement()

object Treasures : GameElement()
