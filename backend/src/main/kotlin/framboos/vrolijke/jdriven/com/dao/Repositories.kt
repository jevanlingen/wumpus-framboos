package framboos.vrolijke.jdriven.com.dao

import framboos.vrolijke.jdriven.com.dao.model.CreateUser
import framboos.vrolijke.jdriven.com.dao.model.Game
import framboos.vrolijke.jdriven.com.dao.model.Pit
import framboos.vrolijke.jdriven.com.dao.model.User

interface ReadRepository<DTO> {
    suspend fun all(): List<DTO>
    suspend fun findById(id: Int): DTO?
}

interface CrudRepository<Creator, DTO> : ReadRepository<DTO> {
    suspend fun add(creator: Creator): DTO?
    suspend fun edit(dto: DTO): Boolean
    suspend fun delete(id: Int): Boolean
}

interface UserRepository : CrudRepository<CreateUser, User> {
    suspend fun findByName(name: String): User?
}

interface GameRepository : ReadRepository<Game>

interface PitRepository : ReadRepository<Pit> {
    suspend fun findByGameId(gameId: Int): List<Pit>
}