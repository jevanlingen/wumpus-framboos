package framboos.vrolijke.jdriven.com.dao

import framboos.vrolijke.jdriven.com.dao.model.User

interface CrudRepository<DTO> {
    suspend fun all(): List<DTO>
    suspend fun getById(id: Int): DTO?
    suspend fun add(entity: DTO): DTO?
    suspend fun edit(entity: DTO): Boolean
    suspend fun delete(id: Int): Boolean
}

interface UserRepository : CrudRepository<User> {
    suspend fun getHashedPasswordByName(name: String): String?
}