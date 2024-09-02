package framboos.vrolijke.jdriven.com.dao

import framboos.vrolijke.jdriven.com.dao.DatabaseSingleton.dbQuery
import framboos.vrolijke.jdriven.com.dao.impl.gameRepo
import framboos.vrolijke.jdriven.com.dao.model.Users
import framboos.vrolijke.jdriven.com.utils.Color
import framboos.vrolijke.jdriven.com.utils.hashPassword
import org.jetbrains.exposed.sql.insert
import framboos.vrolijke.jdriven.com.dao.model.Coordinate as xy

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

internal suspend fun createGames() = dbQuery {
    // GridSize: 4
    gameRepo.create(name = "A", gridSize = 4, treasure = xy(2, 3), wumpus = xy(1, 3), pits = listOf(xy(3, 1), xy(3, 3), xy(4, 4)))
    gameRepo.create(name = "B", gridSize = 4, treasure = xy(3, 1), wumpus = xy(2, 2), pits = listOf(xy(3, 4), xy(4, 4)))
    gameRepo.create(name = "C", gridSize = 4, treasure = xy(2, 2), wumpus = xy(4, 3), pits = listOf(xy(3, 3), xy(3, 4)))
    gameRepo.create(name = "D", gridSize = 4, treasure = xy(4, 3), wumpus = xy(4, 1), pits = listOf(xy(1, 3), xy(4, 4)))
    gameRepo.create(name = "E", gridSize = 4, treasure = xy(3, 3), wumpus = xy(4, 1), pits = listOf(xy(3, 1), xy(1, 4)))

    // GridSize: 5
    gameRepo.create(name = "F", gridSize = 5, treasure = xy(4,1), wumpus = xy(5, 5), pits = listOf(xy(1, 4), xy(3, 1), xy(4, 4)))
    gameRepo.create(name = "G", gridSize = 5, treasure = xy(1,5), wumpus = xy(2, 5), pits = listOf(xy(2, 3), xy(5, 1), xy(5, 5)))
    gameRepo.create(name = "H", gridSize = 5, treasure = xy(2,5), wumpus = xy(3, 1), pits = listOf(xy(3, 3), xy(3, 4), xy(5, 3)))
    gameRepo.create(name = "I", gridSize = 5, treasure = xy(3,5), wumpus = xy(1, 3), pits = listOf(xy(4, 1), xy(4, 4), xy(5, 2)))
    gameRepo.create(name = "J", gridSize = 5, treasure = xy(3,4), wumpus = xy(1, 3), pits = listOf(xy(2, 4), xy(5, 5)))
    gameRepo.create(name = "K", gridSize = 5, treasure = xy(5,1), wumpus = xy(5, 5), pits = listOf(xy(1, 3), xy(3, 5), xy(4, 1)))

    // GridSize: 6
    gameRepo.create(name = "L", gridSize = 6, treasure = xy(2,6), wumpus = xy(5, 1), pits = listOf(xy(2, 3), xy(4, 4)))
    gameRepo.create(name = "M", gridSize = 6, treasure = xy(4,5), wumpus = xy(3, 1), pits = listOf(xy(3, 5), xy(4, 6), xy(6, 3)))
}

internal suspend fun createGamesForContest() = dbQuery {
    gameRepo.create(name = "Of Paths, Peace and Treasure", gridSize = 3, treasure = xy(3, 3), wumpus = xy(1, 3), pits = listOf())
    gameRepo.create(name = "Into the Wumpus' Lair", gridSize = 4, treasure = xy(3, 4), wumpus = xy(1, 3), pits = listOf(xy(4, 1)))
    gameRepo.create(name = "Quest for the Golden Relic", gridSize = 4, treasure = xy(3, 1), wumpus = xy(2, 2), pits = listOf(xy(3, 4)))
    gameRepo.create(name = "Echoes of Ancient Kings", gridSize = 4, treasure = xy(4, 4), wumpus = xy(2, 3), pits = listOf(xy(3, 3), xy(1, 4)))
    gameRepo.create(name = "Secrets Beneath the Wumpus Cave", gridSize = 5, treasure = xy(5,5), wumpus = xy(4, 2), pits = listOf(xy(1, 4)))
    gameRepo.create(name = "The Hidden Vault of Valor", gridSize = 5, treasure = xy(4, 4), wumpus = xy(4, 2), pits = listOf(xy(2, 3), xy(2, 4)))
    gameRepo.create(name = "The Dark Descent", gridSize = 5, treasure = xy(5, 5), wumpus = xy(4, 4), pits = listOf(xy(2, 2), xy(3, 3)))
    gameRepo.create(name = "Maze of Forsaken Beauty", gridSize = 6, treasure = xy(4,6), wumpus = xy(3, 1), pits = listOf(xy(2, 3), xy(5, 4), xy(2, 5)))
    gameRepo.create(name = "The Lost Cavern of Wonders", gridSize = 6, treasure = xy(6,5), wumpus = xy(3, 1), pits = listOf(xy(3, 5), xy(2, 2), xy(4, 3), xy(6, 3)))
    gameRepo.create(name = "The Hunter's Last Stand", gridSize = 7, treasure = xy(1,6), wumpus = xy(3,6),
        pits = listOf(
            xy(1, 3), xy(2, 3), xy(3, 3), xy(4, 3), xy(4, 4),
            xy(6, 1), xy(6, 2), xy(6, 3), xy(6, 4), xy(6, 5), xy(6, 6),
        ))
}
