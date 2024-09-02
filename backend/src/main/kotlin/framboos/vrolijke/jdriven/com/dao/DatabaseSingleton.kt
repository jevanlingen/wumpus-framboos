package framboos.vrolijke.jdriven.com.dao

import framboos.vrolijke.jdriven.com.Mode.CONTEST
import framboos.vrolijke.jdriven.com.Mode.HACKING
import framboos.vrolijke.jdriven.com.dao.impl.competitionRepo
import framboos.vrolijke.jdriven.com.dao.impl.gameRepo
import framboos.vrolijke.jdriven.com.dao.impl.userRepo
import framboos.vrolijke.jdriven.com.dao.model.*
import framboos.vrolijke.jdriven.com.MODE
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
            SchemaUtils.create(Competitions)
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
        if (gameRepo.all().isEmpty()) {
            when (MODE) {
                HACKING -> createGames()
                CONTEST -> createGamesForContest()
            }
            competitionRepo.create()
        }
    }
}
