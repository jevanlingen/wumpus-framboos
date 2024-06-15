package framboos.vrolijke.jdriven.com.dao.impl

import framboos.vrolijke.jdriven.com.dao.DatabaseSingleton.dbQuery
import framboos.vrolijke.jdriven.com.dao.UserRepository
import framboos.vrolijke.jdriven.com.dao.model.CreateUser
import framboos.vrolijke.jdriven.com.dao.model.User
import framboos.vrolijke.jdriven.com.dao.model.Users
import framboos.vrolijke.jdriven.com.utils.hashPassword
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

class UserRepositoryImpl : CrudRepositoryImpl<CreateUser, User>(Users), UserRepository {
    override fun toDto(row: ResultRow) = User(
        id = row[Users.id].value,
        name = row[Users.name],
        password = row[Users.password],
        admin = row[Users.admin]
    )

    override fun insert(it: InsertStatement<Number>, creator: CreateUser) {
        check(creator.password != null) { "You cannot insert a user without a password" }

        it[Users.name] = creator.name
        it[Users.password] = hashPassword(creator.password)
    }

    override fun update(it: UpdateStatement, dto: User) {
        check(dto.password != null) { "You cannot update a user without a password" }

        it[Users.name] = dto.name
        it[Users.password] = hashPassword(dto.password)
    }

    override suspend fun all() =
        super.all().map { it.withHiddenPassword() }

    override suspend fun findById(id: Int) =
        super.findById(id)?.withHiddenPassword()

    override suspend fun findByName(name: String) = dbQuery {
        Users
            .selectAll().where { Users.name eq name }
            .map(::toDto)
            .singleOrNull()
    }

    private fun User.withHiddenPassword() =
        copy(password = "******")
}

val userRepo: UserRepository = UserRepositoryImpl()
