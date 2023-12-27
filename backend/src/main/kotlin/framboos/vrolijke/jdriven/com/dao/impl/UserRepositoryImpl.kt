package framboos.vrolijke.jdriven.com.dao.impl

import framboos.vrolijke.jdriven.com.dao.DatabaseSingleton.dbQuery
import framboos.vrolijke.jdriven.com.dao.UserRepository
import framboos.vrolijke.jdriven.com.dao.model.CreateUser
import framboos.vrolijke.jdriven.com.dao.model.User
import framboos.vrolijke.jdriven.com.dao.model.Users
import framboos.vrolijke.jdriven.com.utils.hashPassword
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

class UserRepositoryImpl : CrudRepositoryImpl<CreateUser, User, Users>(Users), UserRepository {
    override fun rowToObject(row: ResultRow) = User(
        id = row[Users.id],
        name = row[Users.name],
        password = row[Users.password],
        admin = row[Users.admin]
    )

    override fun insert(it: InsertStatement<Number>, entity: CreateUser) {
        check(entity.password != null) { "You cannot insert a user without a password" }

        it[Users.name] = entity.name
        it[Users.password] = hashPassword(entity.password)
        it[Users.admin] = false
    }

    override fun update(it: UpdateStatement, entity: User) {
        check(entity.password != null) { "You cannot update a user without a password" }

        it[Users.name] = entity.name
        it[Users.password] = hashPassword(entity.password)
    }

    override suspend fun getByName(name: String) = dbQuery {
        Users
            .select { Users.name eq name }
            .map(::rowToObject)
            .singleOrNull()
    }
}

val userRepo: UserRepository = UserRepositoryImpl()