package framboos.vrolijke.jdriven.com.dao.impl

import framboos.vrolijke.jdriven.com.dao.DatabaseSingleton.dbQuery
import framboos.vrolijke.jdriven.com.dao.PlayerRepository
import framboos.vrolijke.jdriven.com.dao.model.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

class PlayerRepositoryImpl : CrudRepositoryImpl<CreatePlayer, Player>(Players), PlayerRepository {
    override fun table() = (Players innerJoin Users).slice(Players.columns + Users.name)

    override fun toDto(row: ResultRow): Player {
        val userName = row.getOrNull(Users.name)
            ?: runBlocking { userRepo.findById(row[Players.userId].value)!!.name }

        return Player(
            id = row[Players.id].value,
            user = userName,
            coordinate = listOf(row[Players.x], row[Players.y]),
            points = row[Players.points],
            arrows = row[Players.arrows],
            planks = row[Players.planks],
            wumpusAlive = row[Players.wumpusAlive],
            hasTreasure = row[Players.hasTreasure],
            gameCompleted = row[Players.gameCompleted]
        )
    }

    override fun insert(it: InsertStatement<Number>, creator: CreatePlayer) {
        it[Players.userId] = creator.userId
        it[Players.gameId] = creator.gameId
        it[Players.x] = 1
        it[Players.y] = 1
        it[Players.arrows] = creator.arrows
        it[Players.planks] = creator.planks
    }

    override fun update(it: UpdateStatement, dto: Player) {
        it[Players.x] = dto.coordinate[0]
        it[Players.y] = dto.coordinate[1]
        it[Players.arrows] = dto.arrows
        it[Players.planks] = dto.planks
    }

    override suspend fun add(creator: CreatePlayer) = dbQuery {
        table().select { Players.userId eq creator.userId }.map(::toDto).singleOrNull()
            ?: super.add(creator)
    }

    override suspend fun findByGameId(gameId: Int) = dbQuery {
        table().select { Players.gameId eq gameId }.map(::toDto)
    }
}

val playerRepo: PlayerRepository = PlayerRepositoryImpl()