package framboos.vrolijke.jdriven.com.dao.impl

import framboos.vrolijke.jdriven.com.dao.DatabaseSingleton.dbQuery
import framboos.vrolijke.jdriven.com.dao.PlayerRepository
import framboos.vrolijke.jdriven.com.dao.model.*
import framboos.vrolijke.jdriven.com.dao.model.Direction.EAST
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
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
            coordinate = Coordinate(row[Players.x], row[Players.y]),
            direction = row[Players.direction],
            points = row[Players.points],
            arrows = row[Players.arrows],
            planks = row[Players.planks],
            wumpusAlive = row[Players.wumpusAlive],
            hasTreasure = row[Players.hasTreasure],
            gameCompleted = row[Players.gameCompleted],
            death = row[Players.death]
        )
    }

    override fun insert(it: InsertStatement<Number>, creator: CreatePlayer) {
        it[Players.userId] = creator.userId
        it[Players.gameId] = creator.gameId
        it[Players.direction] = EAST
        it[Players.x] = 1
        it[Players.y] = 1
        it[Players.points] = creator.points
        it[Players.arrows] = creator.arrows
        it[Players.planks] = creator.planks
    }

    override fun update(it: UpdateStatement, dto: Player) {
        it[Players.x] = dto.coordinate.x
        it[Players.y] = dto.coordinate.y
        it[Players.direction] = dto.direction
        it[Players.points] = dto.points
        it[Players.arrows] = dto.arrows
        it[Players.planks] = dto.planks
        it[Players.wumpusAlive] = dto.wumpusAlive
        it[Players.hasTreasure] = dto.hasTreasure
        it[Players.gameCompleted] = dto.gameCompleted
        it[Players.death] = dto.death
    }

    override suspend fun add(creator: CreatePlayer) = dbQuery {
        // TODO check whether `reference("game_id", Games, onDelete = CASCADE)` already cover this
        // if (gameRepo.findById(creator.gameId) == null) throw Exception("Cannot add player to non existing game")

        val player = table().select { Players.userId eq creator.userId }.map(::toDto).singleOrNull()

        if (player == null) super.add(creator)
        else if (player.death) edit(player.copy(coordinate = Coordinate(1, 1), wumpusAlive = true, death = false)) // start again, but keeps points
        else player
    }

    override suspend fun findByGameId(gameId: Int) = dbQuery {
        table().select { Players.gameId eq gameId }.map(::toDto)
    }

    override suspend fun findByGameIdAndUserId(gameId: Int, userId: Int) = dbQuery {
        table().select { (Players.gameId eq gameId) and (Players.userId eq userId) }.map(::toDto).singleOrNull()
    }
}

val playerRepo: PlayerRepository = PlayerRepositoryImpl()