package framboos.vrolijke.jdriven.com.dao

import framboos.vrolijke.jdriven.com.dao.impl.gameRepo
import framboos.vrolijke.jdriven.com.dao.impl.userRepo
import framboos.vrolijke.jdriven.com.dao.model.*
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
            SchemaUtils.create(Games)
            SchemaUtils.create(Treasures)
            SchemaUtils.create(Wumpusses)
            SchemaUtils.create(Pits)
            SchemaUtils.create(Players)
        }

        runBlocking {
            initDefaultEntities()
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T) =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    private suspend fun initDefaultEntities() {
        if (userRepo.all().none { it.admin }) createAdminUser()
        if (gameRepo.all().isEmpty()) createGame()
    }

    private suspend fun createAdminUser() = dbQuery {
        Users.insert {
            it[name] = "admin"
            it[password] = hashPassword("8MumblingRastusNominee2")
            it[admin] = true
        }
    }

    // FROM https://github.com/alexroque91/wumpus-world-prolog/blob/master/worldBuilder.pl
    private suspend fun createGame() = dbQuery {
        val id = Games.insertAndGetId { it[gridSize] = 4 }

        Treasures.insert { it[x] = 2; it[y] = 3; it[gameId] = id }
        Wumpusses.insert { it[x] = 1; it[y] = 3; it[gameId] = id }
        Pits.insert { it[x] = 3; it[y] = 1; it[gameId] = id }
        Pits.insert { it[x] = 3; it[y] = 3; it[gameId] = id }
        Pits.insert { it[x] = 4; it[y] = 4; it[gameId] = id }
    }
}
