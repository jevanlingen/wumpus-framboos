package framboos.vrolijke.jdriven.com.dao

import framboos.vrolijke.jdriven.com.dao.model.CreateUser
import framboos.vrolijke.jdriven.com.dao.model.User

interface CrudRepository<Creator, DTO> {
    suspend fun all(): List<DTO>
    suspend fun getById(id: Int): DTO?
    suspend fun add(entity: Creator): DTO?
    suspend fun edit(entity: DTO): Boolean
    suspend fun delete(id: Int): Boolean
}

interface UserRepository : CrudRepository<CreateUser, User> {
    suspend fun getByName(name: String): User?
}