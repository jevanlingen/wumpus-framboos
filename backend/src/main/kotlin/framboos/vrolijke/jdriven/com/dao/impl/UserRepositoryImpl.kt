package framboos.vrolijke.jdriven.com.dao.impl

import framboos.vrolijke.jdriven.com.dao.UserRepository
import framboos.vrolijke.jdriven.com.dao.model.CreateUser
import framboos.vrolijke.jdriven.com.dao.model.User
import framboos.vrolijke.jdriven.com.dao.model.Users
import framboos.vrolijke.jdriven.com.utils.hashPassword
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

class UserRepositoryImpl : CrudRepositoryImpl<CreateUser, User, Users>(Users), UserRepository {
    override fun rowToObject(row: ResultRow) = User(
        id = row[Users.id],
        name = row[Users.name]
    )

    override fun insert(it: InsertStatement<Number>, entity: CreateUser) {
        check(entity.password != null) { "You cannot insert a user without a password" }

        it[Users.name] = entity.name
        it[Users.password] = hashPassword(entity.password)
    }

    override fun update(it: UpdateStatement, entity: User) {
        check(entity.password != null) { "You cannot update a user without a password" }

        it[Users.name] = entity.name
        it[Users.password] = hashPassword(entity.password)
    }

    override suspend fun getByName(name: String) =
        Users
            .select { Users.name eq name }
            .map(::rowToObject)
            .singleOrNull()

    internal fun createAdminUser() {
        Users.insert {
            it[name] = "admin"
            it[password] = hashPassword("8MumblingRastusNominee2")
            it[admin] = true
        }
    }
}

val userRepo: UserRepository = UserRepositoryImpl().apply {
    runBlocking { if (all().isEmpty()) createAdminUser() }
}
