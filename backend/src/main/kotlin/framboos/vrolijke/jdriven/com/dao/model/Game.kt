package framboos.vrolijke.jdriven.com.dao.model

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption.CASCADE

data class Wumpus(override val id: Int, val coordinate: List<Int>) : Dto
data class Pit(override val id: Int, val coordinate: List<Int>) : Dto
data class Treasure(override val id: Int, val coordinate: List<Int>) : Dto
data class Player(override val id: Int, val coordinate: List<Int>) : Dto
data class Game(override val id: Int, val gridSize: Int, val pits: List<Pit> = listOf(), val wumpus: Wumpus, val treasure: Treasure, val players: List<Player> = listOf()) : Dto

object Games : IntIdTable() {
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

object Players : GameElement() {
    val userId = reference("user_id", Users)
    val points = integer("points")
    val arrows = integer("arrows")
    val planks = integer("planks")
    val wumpusAlive = bool("wumpus-alive").default(true)
    val hasTreasure = bool("has-treasure").default(false)
    // completed??
}



/*
{
  "grid-size": 16
  "pits: [{
     coordinate: [1,2],
   }],
   "wumpus": {
     coordinate: [1,2]
   },
   "treasure": {},
   "players": [
     {
       player: userX,
       coordinate: [1,2],
       points: int,
       attributes: {
         arrow: int,
         planks: int
       },
       "wumpus-alive": true
     }
   }
}
 */