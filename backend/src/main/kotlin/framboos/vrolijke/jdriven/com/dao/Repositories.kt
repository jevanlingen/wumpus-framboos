package framboos.vrolijke.jdriven.com.dao

import framboos.vrolijke.jdriven.com.dao.model.*
import org.jetbrains.exposed.sql.statements.InsertStatement

interface ReadRepository<DTO> {
    suspend fun all(): List<DTO>
    suspend fun findById(id: Int): DTO?
}

interface CrudRepository<Creator, DTO> : ReadRepository<DTO> {
    suspend fun add(creator: Creator): DTO?
    suspend fun edit(dto: DTO): DTO?
    suspend fun deleteById(id: Int): Boolean
}

interface UserRepository : CrudRepository<CreateUser, User> {
    suspend fun findByName(name: String): User?
}

interface PlayerRepository: CrudRepository<CreatePlayer, Player> {
    suspend fun findByGameId(gameId: Int): List<Player>
}

interface GameRepository : ReadRepository<Game> {
    suspend fun create(name: String, gridSize: Int, treasure: Coordinate, wumpus: Coordinate, pits: List<Coordinate>): Int
    suspend fun allIds(): List<Int>
}

interface PitRepository : ReadRepository<Pit> {
    suspend fun findByGameId(gameId: Int): List<Pit>
}

interface CompetitionRepository : ReadRepository<Competition> {
    suspend fun create(): InsertStatement<Number>
    suspend fun allIds(): List<Int>
    suspend fun findByIdWithScore(id: Int): CompetitionWithScore?
    suspend fun isCurrentGame(gameId: Int): Boolean
    suspend fun advance(id: Int): Boolean
}
