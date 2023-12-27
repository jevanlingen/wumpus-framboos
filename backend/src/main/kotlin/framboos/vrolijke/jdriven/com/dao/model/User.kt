package framboos.vrolijke.jdriven.com.dao.model

data class User(override val id: Int, val name: String, val password: String? = null) : Dto

object Users : Entity() {
    val name = varchar("name", 128)
    val password = varchar("password", 1024)
}
