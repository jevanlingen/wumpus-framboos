package framboos.vrolijke.jdriven.com.dao

import framboos.vrolijke.jdriven.com.dao.DatabaseSingleton.dbQuery
import framboos.vrolijke.jdriven.com.dao.model.*
import framboos.vrolijke.jdriven.com.utils.Color
import framboos.vrolijke.jdriven.com.utils.hashPassword
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId

internal suspend fun createAdminUser() = dbQuery {
    Users.insert {
        it[name] = "admin"
        it[password] = hashPassword("8MumblingRastusNominee2")
        it[admin] = true
        it[shirtColor] = Color("FF3845")()
        it[trouserColor] = Color("0036FF")()
        it[skinColor] = Color("5B0000")()
    }
}

internal suspend fun createCompetition(gameIds: List<Int>) = dbQuery {
    Competitions.insert {
        it[currentGameId] = gameIds.first()
        it[this.gameIds] = gameIds
    }
}

// Games from https://github.com/alexroque91/wumpus-world-prolog/blob/master/worldBuilder.pl
internal suspend fun createGame1() = dbQuery {
    createGame(
        treasure = Coordinate(2, 3),
        wumpus = Coordinate(1, 3),
        pits = listOf(
            Coordinate(3, 1),
            Coordinate(3, 3),
            Coordinate(4, 4),
        )
    )
}

internal suspend fun createGame2() = dbQuery {
    createGame(
        treasure = Coordinate(2, 4),
        wumpus = Coordinate(1, 2),
        pits = listOf(Coordinate(2, 1))
    )
}

internal suspend fun createGame3() = dbQuery {
    createGame(
        treasure = Coordinate(2, 1),
        wumpus = Coordinate(3, 1),
        pits = listOf(
            Coordinate(1, 3),
            Coordinate(2, 3),
            Coordinate(2, 4),
        )
    )
}

internal suspend fun createGame4() = dbQuery {
    createGame(
        treasure = Coordinate(1, 4),
        wumpus = Coordinate(4, 4),
        pits = listOf(
            Coordinate(1, 4),
            Coordinate(2, 3),
            Coordinate(3, 2),
            Coordinate(4, 2),
        )
    )
}

internal suspend fun createGame5() = dbQuery {
    createGame(
        treasure = Coordinate(3, 2),
        wumpus = Coordinate(3, 2), // extra hard, the wumpus protects the treasure
        pits = listOf(
            Coordinate(1, 4),
            Coordinate(2, 2),
            Coordinate(2, 3),
            Coordinate(4, 4),
        )
    )
}

private suspend fun createGame(treasure: Coordinate, wumpus: Coordinate, pits: List<Coordinate>) = dbQuery {
    val id = Games.insertAndGetId { it[gridSize] = 4 }

    Treasures.insert { it[x] = treasure.x; it[y] = treasure.y; it[gameId] = id }
    Wumpusses.insert { it[x] = wumpus.x; it[y] = wumpus.y; it[gameId] = id }
    pits.forEach { pit -> Pits.insert { it[x] = pit.x; it[y] = pit.y; it[gameId] = id } }

    id.value
}
