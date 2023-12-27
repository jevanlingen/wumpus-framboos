package framboos.vrolijke.jdriven.com.dao

import framboos.vrolijke.jdriven.com.dao.impl.userRepo
import framboos.vrolijke.jdriven.com.dao.model.Users
import framboos.vrolijke.jdriven.com.utils.hashPassword
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*
import org.jetbrains.exposed.sql.transactions.experimental.*

object DatabaseSingleton {
    fun init() {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:file:./build/db"
        //val jdbcURL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;"
        val database = Database.connect(jdbcURL, driverClassName)
        transaction(database) {
            SchemaUtils.create(Users)
        }

        runBlocking {
            initDefaultEntities()
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T) =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    private suspend fun initDefaultEntities() {
        if (userRepo.all().none { it.admin }) createAdminUser()
    }

    private suspend fun createAdminUser() = dbQuery {
        Users.insert {
            it[name] = "admin"
            it[password] = hashPassword("8MumblingRastusNominee2")
            it[admin] = true
        }
    }
}