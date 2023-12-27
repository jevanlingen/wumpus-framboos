package framboos.vrolijke.jdriven.com.dao.model

import org.jetbrains.exposed.sql.*

abstract class Entity : Table() {
    val id = integer("id").autoIncrement()
    override val primaryKey = PrimaryKey(id)
}
