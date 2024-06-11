package framboos.vrolijke.jdriven.com.dao.model

import org.jetbrains.exposed.dao.id.IntIdTable

data class Score(val userId: Int, val username: String, val points: Int)
data class Competition(override val id: Int, val currentGameId: Int = -1, val gameIds: List<Int> = emptyList(), val score: List<Score> = emptyList()) : Dto

object Competitions : IntIdTable() {
    val currentGameId = integer("current_game_id").uniqueIndex()
    val gameIds = array<Int>("game_ids").default(emptyList())
}
