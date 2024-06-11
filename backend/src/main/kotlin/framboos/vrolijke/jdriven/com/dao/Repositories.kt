package framboos.vrolijke.jdriven.com.dao

import framboos.vrolijke.jdriven.com.dao.model.*

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
    suspend fun findByGameIdAndUserId(gameId: Int, userId: Int): Player?
}

interface GameRepository : ReadRepository<Game> {
    suspend fun allIds(): List<Int>
}

interface PitRepository : ReadRepository<Pit> {
    suspend fun findByGameId(gameId: Int): List<Pit>
}

interface CompetitionRepository : ReadRepository<Competition> {
    suspend fun allIds(): List<Int>
    suspend fun advance(id: Int): Boolean
}
