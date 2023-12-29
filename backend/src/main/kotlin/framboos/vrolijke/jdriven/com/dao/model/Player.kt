package framboos.vrolijke.jdriven.com.dao.model

data class CreatePlayer(val userId: Int, val gameId: Int, val arrows: Int = 1, val planks: Int = 0)
data class Player(override val id: Int, val user: String, val coordinate: List<Int>, val points: Int, val arrows: Int, val planks: Int, val wumpusAlive: Boolean, val hasTreasure: Boolean, val gameCompleted : Boolean) : Dto

object Players : GameElement() {
    val userId = reference("user_id", Users)
    val points = integer("points").default(0)
    val arrows = integer("arrows")
    val planks = integer("planks")
    val wumpusAlive = bool("wumpus-alive").default(true)
    val hasTreasure = bool("has-treasure").default(false)
    val gameCompleted = bool("game-completed").default(false)
}
