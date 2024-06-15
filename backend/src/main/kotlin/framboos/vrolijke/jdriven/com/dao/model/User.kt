package framboos.vrolijke.jdriven.com.dao.model

import framboos.vrolijke.jdriven.com.utils.Color
import org.jetbrains.exposed.dao.id.IntIdTable

data class CreateUser(val name: String, val password: String? = null)
data class User(override val id: Int, val name: String, val password: String? = null, val admin: Boolean = false, val shirtColor: Color, val trouserColor: Color, val skinColor: Color) : Dto

object Users : IntIdTable() {
    val name = varchar("name", 128).uniqueIndex()
    val password = varchar("password", 1024)
    val admin = bool("admin").default(false)
    val shirtColor = varchar("shirt-color", length = 6)
    val trouserColor = varchar("trouser-color", length = 6)
    val skinColor = varchar("skin-color", length = 6)
}
