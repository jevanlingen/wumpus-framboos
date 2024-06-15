package framboos.vrolijke.jdriven.com.dao.impl

import framboos.vrolijke.jdriven.com.dao.DatabaseSingleton.dbQuery
import framboos.vrolijke.jdriven.com.dao.PlayerRepository
import framboos.vrolijke.jdriven.com.dao.model.*
import framboos.vrolijke.jdriven.com.dao.model.Direction.EAST
import framboos.vrolijke.jdriven.com.dao.model.Users.shirtColor
import framboos.vrolijke.jdriven.com.dao.model.Users.skinColor
import framboos.vrolijke.jdriven.com.dao.model.Users.trouserColor
import framboos.vrolijke.jdriven.com.utils.Color
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

class PlayerRepositoryImpl : CrudRepositoryImpl<CreatePlayer, Player>(Players), PlayerRepository {
    override fun table() = (Players innerJoin Users).slice(Players.columns + Users.id  + Users.name + shirtColor + trouserColor + skinColor)

    override fun toDto(row: ResultRow) =
        Player(
            id = row[Players.id].value,
            // N.B. When a new player is created, the user info in not retrieved
            user = row.getOrNull(Users.id)?.let {
                User(
                    id = row[Users.id].value,
                    name = row[Users.name],
                    password = "******",
                    shirtColor = Color(row[shirtColor]),
                    trouserColor = Color(row[trouserColor]),
                    skinColor = Color(row[skinColor]),
                )
            },
            gameId = row[Players.gameId].value,
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

    override fun insert(it: InsertStatement<Number>, creator: CreatePlayer) {
        it[Players.userId] = creator.userId
        it[Players.gameId] = creator.gameId
        it[Players.direction] = EAST
        it[Players.x] = creator.startingLocation.x
        it[Players.y] = creator.startingLocation.y
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

    override suspend fun findByGameId(gameId: Int) = dbQuery {
        table().selectAll().where { Players.gameId eq gameId }.map(::toDto)
    }

    override suspend fun findByGameIdAndUserId(gameId: Int, userId: Int) = dbQuery {
        table().selectAll().where { (Players.gameId eq gameId) and (Players.userId eq userId) }.map(::toDto).singleOrNull()
    }
}

val playerRepo: PlayerRepository = PlayerRepositoryImpl()
