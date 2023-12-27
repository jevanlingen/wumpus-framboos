package framboos.vrolijke.jdriven.com.dao.model

import org.jetbrains.exposed.dao.id.IntIdTable

data class CreateUser(val name: String, val password: String? = null)
data class User(override val id: Int, val name: String, val password: String? = null, val admin: Boolean = false) : Dto

object Users : IntIdTable() {
    val name = varchar("name", 128).uniqueIndex()
    val password = varchar("password", 1024)
    val admin = bool("admin").default(false)
}
