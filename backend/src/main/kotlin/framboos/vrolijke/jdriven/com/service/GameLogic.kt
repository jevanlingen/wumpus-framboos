package framboos.vrolijke.jdriven.com.service

import framboos.vrolijke.jdriven.com.dao.impl.gameRepo
import framboos.vrolijke.jdriven.com.dao.impl.playerRepo
import framboos.vrolijke.jdriven.com.dao.model.*
import framboos.vrolijke.jdriven.com.dao.model.Perception.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.math.abs

suspend fun doGameAction(gameId: Int, action: String?, userId: Int): Player? {
    if (action == "enter") {
        return addPlayer(userId, gameId)
    }

    val (game, player) = retrieveData(gameId, userId)
    if (player == null || game == null) return null

    val p = when (action) {
        "turn-left" -> turnLeft(player)
        "turn-right" -> turnRight(player)
        "move-forward" -> moveForward(player, game)
        "grab" -> grab(player, game)
        "release" -> TODO() // or it can release an object that it is holding.
        "shoot" -> TODO() // The agent has a single arrow that it can shoot. It will go straight in the direction faced by the agent until it hits (and kills) the wumpus, or hits (and is absorbed by) a wall.
        "climb" -> TODO() //The agent can climb out of the cave if it is at the start square (x=1, y=1) + has retrieved the treasure
        else -> null
    }

    return p?.copy(perceptions = getPerceptions(action, player, p, game))
}

/*
+1000 reward points if the agent comes out of the cave with the gold.
-1000 points penalty for being eaten by the Wumpus or falling into the pit.
-1 for each action, and -10 for using an arrow.
The game ends if either agent dies or came out of the cave.
 */

private suspend fun addPlayer(userId: Int, gameId: Int) =
    playerRepo.add(CreatePlayer(userId, gameId))


/**
 * The agent can turn left.
 */
private suspend fun turnLeft(player: Player): Player? {
    val newDirection = if (player.direction == Direction.entries.first()) Direction.entries.last() else Direction.entries[player.direction.ordinal - 1]
    return playerRepo.edit(player.copy(points = player.points - 1, direction = newDirection))
}

/**
 * The agent can turn right.
 */
private suspend fun turnRight(player: Player): Player? {
    val newDirection = if (player.direction == Direction.entries.last()) Direction.entries.first() else Direction.entries[player.direction.ordinal + 1]
    return playerRepo.edit(player.copy(points = player.points - 1, direction = newDirection))
}

/**
 * The agent can go forward in the direction it is currently facing, going forward into a wall will generate a Bump perception.
 */
private suspend fun moveForward(player: Player, game: Game): Player? {
    val newCoordinate = when (player.direction) {
        Direction.NORTH -> Coordinate(player.coordinate.x, player.coordinate.y + 1)
        Direction.EAST -> Coordinate(player.coordinate.x + 1, player.coordinate.y)
        Direction.SOUTH -> Coordinate(player.coordinate.x, player.coordinate.y - 1)
        Direction.WEST -> Coordinate(player.coordinate.x - 1, player.coordinate.y)
    }

    val (points, coordinate, hasTreasure) =
        if (newCoordinate.x < 1 || newCoordinate.y < 1 || newCoordinate.x >= game.gridSize || newCoordinate.y >= game.gridSize) // outside grid
            Triple(player.points - 1, player.coordinate, player.hasTreasure)
        else if (player.wumpusAlive && game.wumpus.coordinate == newCoordinate) // Wumpus encountered
            Triple(player.points - 1000, Coordinate(1, 1), false)
        else if (game.pits.any { it.coordinate == newCoordinate }) // fall in pit
            Triple(player.points - 1000, Coordinate(1, 1), false)
        else
            Triple(player.points - 1, newCoordinate, player.hasTreasure)

    return playerRepo.edit(player.copy(points = points, coordinate = coordinate, hasTreasure = hasTreasure))
}

/**
 * The agent can grab a portable object at the current square.
 */
private suspend fun grab(player: Player, game: Game) =
    if (!player.hasTreasure && game.treasure.coordinate == player.coordinate)
         playerRepo.edit(player.copy(points = player.points - 1, hasTreasure = true))
    else
        playerRepo.edit(player.copy(points = player.points - 1))

private fun getPerceptions(action: String?, before: Player, after: Player, game: Game): List<Perception> {
    val list = mutableListOf<Perception>()

    // The agent will perceive the stench if he is in the room adjacent to the Wumpus.
    if (areAdjacent(after.coordinate, game.wumpus.coordinate)) list.add(STENCH)

    // The agent will perceive breeze if he is in the room directly adjacent to a pit.
    if (game.pits.any { areAdjacent(after.coordinate, it.coordinate) }) list.add(BREEZE)

    // The agent will perceive the glitter in the room where the gold is present.
    if (after.coordinate == game.treasure.coordinate) list.add(GLITTER)

    // The agent will perceive the bump if he walks into a wall.
    if (action == "move-forward" && before.coordinate == after.coordinate) list.add(BUMP)

    // When the Wumpus is shot, it emits a horrible scream which can be perceived anywhere in the cave.
    if (before.wumpusAlive && !after.wumpusAlive) list.add(SCREAM)

    return list.toList()
}

private fun areAdjacent(point1: Coordinate, point2: Coordinate): Boolean {
    val (x1, y1) = point1
    val (x2, y2) = point2

    // Check if the points are the same, which we don't count as adjacent
    if (x1 == x2 && y1 == y2) {
        return false
    }

    // Check if the points are on the same row or column and are adjacent
    return (x1 == x2 && abs(y1 - y2) == 1) || (y1 == y2 && abs(x1 - x2) == 1)
}

private suspend fun retrieveData(gameId: Int, userId: Int) = coroutineScope {
    val game = async { gameRepo.findById(gameId) }
    val player = async { playerRepo.findByGameIdAndUserId(gameId, userId) }
    Pair(game.await(), player.await())
}