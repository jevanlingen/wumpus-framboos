package framboos.vrolijke.jdriven.com.dao.impl

import framboos.vrolijke.jdriven.com.dao.DatabaseSingleton.dbQuery
import framboos.vrolijke.jdriven.com.dao.GameRepository
import framboos.vrolijke.jdriven.com.dao.model.*
import org.jetbrains.exposed.sql.JoinType.INNER
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId

class GameRepositoryImpl : ReadRepositoryImpl<Game>(Games), GameRepository {
    override fun table() =
        Games
            .join(Wumpusses, INNER, onColumn = Games.id, otherColumn = Wumpusses.gameId)
            .join(Treasures, INNER, onColumn = Games.id, otherColumn = Treasures.gameId)

    override fun toDto(row: ResultRow) = Game(
        id = row[Games.id].value,
        gridSize = row[Games.gridSize],
        wumpus = Wumpus(row[Wumpusses.id].value, Coordinate(row[Wumpusses.x], row[Wumpusses.y])),
        treasure = Treasure(row[Treasures.id].value, Coordinate(row[Treasures.x], row[Treasures.y])),
    )

    override suspend fun create(gridSize: Int, treasure: Coordinate, wumpus: Coordinate, pits: List<Coordinate>) = dbQuery {
        val id = Games.insertAndGetId { it[this.gridSize] = gridSize }

        Treasures.insert { it[x] = treasure.x; it[y] = treasure.y; it[gameId] = id }
        Wumpusses.insert { it[x] = wumpus.x; it[y] = wumpus.y; it[gameId] = id }
        pits.forEach { pit -> Pits.insert { it[x] = pit.x; it[y] = pit.y; it[gameId] = id } }

        id.value
    }

    override suspend fun allIds(): List<Int> = dbQuery {
        Games
            .select(Games.id)
            .map { it[Games.id].value }
    }

    override suspend fun findById(id: Int) =
        super.findById(id)
            ?.copy(pits = pitRepo.findByGameId(id), players = playerRepo.findByGameId(id))
}

val gameRepo: GameRepository = GameRepositoryImpl()
