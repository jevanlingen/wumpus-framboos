package framboos.vrolijke.jdriven.com.service

import framboos.vrolijke.jdriven.com.dao.impl.competitionRepo
import framboos.vrolijke.jdriven.com.dao.impl.gameRepo
import framboos.vrolijke.jdriven.com.dao.impl.playerRepo
import framboos.vrolijke.jdriven.com.dao.model.*
import framboos.vrolijke.jdriven.com.dao.model.Direction.*
import framboos.vrolijke.jdriven.com.dao.model.Perception.*
import framboos.vrolijke.jdriven.com.utils.GameDoesNotExists
import framboos.vrolijke.jdriven.com.utils.GameIsNotCurrentException
import framboos.vrolijke.jdriven.com.utils.NotAnActionException
import framboos.vrolijke.jdriven.com.utils.PlayerNotRegisteredException
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlin.math.abs

/**
 * Let the player do an action at the game. Following rules apply:
 * - Plus 1000 reward points if the player comes out of the cave with the gold.
 * - Minus 150 points penalty for being eaten by the Wumpus or falling into the pit.
 * - Minus 1 point for each action, and minus 10 points for using an arrow.
 * - The game ends if the player comes out of the cave.
 * - The game kind of ends when the player dies, but player can restart to try again (player keeps its current points)
 */
suspend fun doGameAction(gameId: Int, action: String?, userId: Int): Result<Player> {
    var (game, player) = retrieveData(gameId, userId)
    if (game == null) return failure(GameDoesNotExists(gameId))

    if (!competitionRepo.isCurrentGame(gameId)) return failure(GameIsNotCurrentException())

    if (action == "enter") {
        player = player ?: addPlayer(userId, game)
    }

    if (player == null) return failure(PlayerNotRegisteredException())
    if ((player.death && action != "restart") || player.gameCompleted) return success(player)

    return when (action) {
        "enter" -> player // do nothing else
        "restart" -> player.retry()
        "turn-left" -> player.turnLeft()
        "turn-right" -> player.turnRight()
        "move-forward" -> player.moveForward(game)
        "grab" -> player.grab(game)
        // "release" -> TODO() // release a captured object; I don't see o need to implement this for now
        "shoot" -> player.shoot(game)
        "climb" -> player.climb(game)
        else -> null
    }
        ?.let { success(it.copy(perceptions = getPerceptions(action, player, it, game))) }
        ?: failure(NotAnActionException(action))
}

private suspend fun addPlayer(userId: Int, game: Game) =
    playerRepo.add(CreatePlayer(userId, game.id, game.startingLocation))

/**
 * The player can go back to the starting coordinate point to try it again.
 */
private suspend fun Player.retry() =
    copy(
        direction = EAST,
        coordinate = Coordinate(1, 1),
        arrows = 1,
        planks = 0,
        wumpusAlive = true,
        hasTreasure = false,
        death = false
    ).process()

/**
 * The player can turn left.
 */
private suspend fun Player.turnLeft(): Player? {
    val newDirection =
        if (direction == Direction.entries.first()) Direction.entries.last() else Direction.entries[direction.ordinal - 1]
    return copy(points = points - 1, direction = newDirection).process()
}

/**
 * The player can turn right.
 */
private suspend fun Player.turnRight(): Player? {
    val newDirection =
        if (direction == Direction.entries.last()) Direction.entries.first() else Direction.entries[direction.ordinal + 1]
    return copy(points = points - 1, direction = newDirection).process()
}

/**
 * The player can go forward in the direction it is currently facing, going forward into a wall will generate a Bump perception.
 */
private suspend fun Player.moveForward(game: Game): Player? {
    val newCoordinate = when (direction) {
        NORTH -> Coordinate(coordinate.x, coordinate.y + 1)
        EAST -> Coordinate(coordinate.x + 1, coordinate.y)
        SOUTH -> Coordinate(coordinate.x, coordinate.y - 1)
        WEST -> Coordinate(coordinate.x - 1, coordinate.y)
    }

    // Player walks outside grid
    if (newCoordinate.x < 1 || newCoordinate.y < 1 || newCoordinate.x > game.gridSize || newCoordinate.y > game.gridSize)
        return copy(points = points - 1).process()

    // Player encounters Wumpus OR falls in a pit
    if (wumpusAlive && game.wumpus.coordinate == newCoordinate || game.pits.any { it.coordinate == newCoordinate })
        return copy(points = points - 150, coordinate = newCoordinate, death = true).process()

    return copy(points = points - 1, coordinate = newCoordinate).process()
}

/**
 * The player can grab a portable object at the current square.
 */
private suspend fun Player.grab(game: Game) =
    if (!hasTreasure && game.treasure.coordinate == coordinate)
        copy(points = points - 1, hasTreasure = true).process()
    else
        copy(points = points - 1).process()

/**
 * The player has a single arrow that it can shoot.
 * It will go straight in the direction faced by the player until it hits (and kills) the wumpus, or hits (and is absorbed by) a wall.
 */
private suspend fun Player.shoot(game: Game): Player? {
    if (arrows < 1) return copy(points = points - 1).process()
    if (!wumpusAlive) return copy(points = points - 10, arrows = arrows - 1).process()

    val hitWumpus = when (direction) {
        NORTH -> coordinate.x == game.wumpus.coordinate.x && coordinate.y < game.wumpus.coordinate.y
        EAST -> coordinate.x < game.wumpus.coordinate.x && coordinate.y == game.wumpus.coordinate.y
        SOUTH -> coordinate.x == game.wumpus.coordinate.x && coordinate.y > game.wumpus.coordinate.y
        WEST -> coordinate.x > game.wumpus.coordinate.x && coordinate.y == game.wumpus.coordinate.y
    }

    return copy(points = points - 10, arrows = arrows - 1, wumpusAlive = !hitWumpus).process()
}

/**
 * The player can climb out of the cave if it is at the start square and has retrieved the treasure
 */
private suspend fun Player.climb(game: Game) =
    if (coordinate == game.startingLocation && hasTreasure)
        copy(points = points + 1000, gameCompleted = true).process()
    else
        copy(points = points - 1).process()

private suspend fun Player.process() =
    playerRepo.edit(this)

private fun getPerceptions(action: String?, before: Player, after: Player, game: Game): List<Perception> {
    val list = mutableListOf<Perception>()

    // The player will perceive the stench if he is in the room adjacent to the Wumpus.
    if (after.wumpusAlive && areAdjacent(after.coordinate, game.wumpus.coordinate)) list.add(STENCH)

    // The player will perceive breeze if he is in the room directly adjacent to a pit.
    if (game.pits.any { areAdjacent(after.coordinate, it.coordinate) }) list.add(BREEZE)

    // The player will perceive the glitter in the room where the gold is present.
    if (!after.hasTreasure && after.coordinate == game.treasure.coordinate) list.add(GLITTER)

    // The player will perceive the bump if he walks into a wall.
    if (action == "move-forward" && before.coordinate == after.coordinate) list.add(BUMP)

    // When the Wumpus is shot, it emits a horrible scream which can be perceived anywhere in the cave.
    if (before.wumpusAlive && !after.wumpusAlive) list.add(SCREAM)

    // When player walks at the starting tile, it perceives the entrance
    if (after.coordinate == game.startingLocation) list.add(LADDER)

    return list.toList()
}

private fun areAdjacent(point1: Coordinate, point2: Coordinate): Boolean {
    val (x1, y1) = point1
    val (x2, y2) = point2

    // Check if the points are the same, which don't count as adjacent
    if (x1 == x2 && y1 == y2) {
        return false
    }

    // Check if the points are on the same row or column and are adjacent
    return (x1 == x2 && abs(y1 - y2) == 1) || (y1 == y2 && abs(x1 - x2) == 1)
}

private suspend fun retrieveData(gameId: Int, userId: Int): Pair<Game?, Player?> {
    val game = gameRepo.findById(gameId)
    val player = game?.players?.find { it.user!!.id == userId }
    return Pair(game, player)
}
