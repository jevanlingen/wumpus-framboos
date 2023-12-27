package framboos.vrolijke.jdriven.com.dao.impl

import framboos.vrolijke.jdriven.com.dao.UserRepository
import framboos.vrolijke.jdriven.com.dao.model.User
import framboos.vrolijke.jdriven.com.dao.model.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.mindrot.jbcrypt.BCrypt

class UserRepositoryImpl : CrudRepositoryImpl<User, Users>(Users), UserRepository {
    override fun rowToObject(row: ResultRow) = User(
        id = row[Users.id],
        name = row[Users.name]
    )

    override fun insert(it: InsertStatement<Number>, entity: User) {
        check(entity.password != null) { "You cannot insert a user without a password" }

        it[Users.name] = entity.name
        it[Users.password] = BCrypt.hashpw(entity.password, BCrypt.gensalt())
    }

    override fun update(it: UpdateStatement, entity: User) {
        check(entity.password != null) { "You cannot update a user without a password" }

        it[Users.name] = entity.name
        it[Users.password] = BCrypt.hashpw(entity.password, BCrypt.gensalt())
    }

    override suspend fun getHashedPasswordByName(name: String) =
        Users
            .select { Users.name eq name }
            .map { it[Users.password] }
            .singleOrNull()
}

val userRepo: UserRepository = UserRepositoryImpl()
