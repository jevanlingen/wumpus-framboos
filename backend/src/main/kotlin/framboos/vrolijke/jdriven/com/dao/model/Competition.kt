package framboos.vrolijke.jdriven.com.dao.model

import org.jetbrains.exposed.dao.id.IntIdTable

data class Competition(override val id: Int, val currentGame: Int = -1, val games: List<Int> = emptyList()) : Dto

object Competitions : IntIdTable() {
    val currentGameId = integer("current_game_id").uniqueIndex()
    val gameIds = array<Int>("game_ids").default(emptyList())
}
