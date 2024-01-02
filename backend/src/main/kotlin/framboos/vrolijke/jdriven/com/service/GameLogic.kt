package framboos.vrolijke.jdriven.com.service

import framboos.vrolijke.jdriven.com.dao.impl.gameRepo
import framboos.vrolijke.jdriven.com.dao.impl.playerRepo
import framboos.vrolijke.jdriven.com.dao.model.*
import framboos.vrolijke.jdriven.com.dao.model.Direction.*
import framboos.vrolijke.jdriven.com.dao.model.Perception.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.math.abs

/*
+1000 reward points if the player comes out of the cave with the gold.
-1000 points penalty for being eaten by the Wumpus or falling into the pit.
-1 for each action, and -10 for using an arrow.
The game ends if the player comes out of the cave.
The game kind of ends when the player dies, but player can restart by `enter`ing the cave again (player keeps its current points)
 */
suspend fun doGameAction(gameId: Int, action: String?, userId: Int): Player? {
    var (game, player) = retrieveData(gameId, userId)
    if (game == null) return null

    if (action == "enter") {
        player =
            if (player == null) addPlayer(userId, game)?.copy(perceptions = listOf(LADDER))
            else if (player.death) startAgain(player)
            else player
    }

    if (player == null) return null
    if (player.death || player.gameCompleted) return player

    val p = when (action) {
        "enter" -> player // do nothing else
        "turn-left" -> turnLeft(player)
        "turn-right" -> turnRight(player)
        "move-forward" -> moveForward(player, game)
        "grab" -> grab(player, game)
        // "release" -> TODO() // it can release an object that it is holding; I don't see o need to implement this for now
        "shoot" -> shoot(player, game)
        "climb" -> climb(player, game)
        else -> null
    }

    return p?.copy(perceptions = getPerceptions(action, player, p, game))
}

private suspend fun startAgain(player: Player) =
    player.copy(
        direction = EAST,
        coordinate = Coordinate(1, 1),
        arrows = player.arrows,
        planks = player.planks,
        wumpusAlive = true,
        hasTreasure = false,
        death = false
    ).process()

private suspend fun addPlayer(userId: Int, game: Game) =
    playerRepo.add(CreatePlayer(userId, game.id, game.startingLocation))

/**
 * The player can turn left.
 */
private suspend fun turnLeft(player: Player): Player? {
    val newDirection = if (player.direction == Direction.entries.first()) Direction.entries.last() else Direction.entries[player.direction.ordinal - 1]
    return player.copy(points = player.points - 1, direction = newDirection).process()
}

/**
 * The player can turn right.
 */
private suspend fun turnRight(player: Player): Player? {
    val newDirection = if (player.direction == Direction.entries.last()) Direction.entries.first() else Direction.entries[player.direction.ordinal + 1]
    return player.copy(points = player.points - 1, direction = newDirection).process()
}

/**
 * The player can go forward in the direction it is currently facing, going forward into a wall will generate a Bump perception.
 */
private suspend fun moveForward(player: Player, game: Game): Player? {
    val newCoordinate = when (player.direction) {
        NORTH -> Coordinate(player.coordinate.x, player.coordinate.y + 1)
        EAST -> Coordinate(player.coordinate.x + 1, player.coordinate.y)
        SOUTH -> Coordinate(player.coordinate.x, player.coordinate.y - 1)
        WEST -> Coordinate(player.coordinate.x - 1, player.coordinate.y)
    }

    // Player walks outside grid
    if (newCoordinate.x < 1 || newCoordinate.y < 1 || newCoordinate.x > game.gridSize || newCoordinate.y > game.gridSize)
        return player.copy(points = player.points - 1).process()

    // Player encounters Wumpus OR falls in a pit
    if (player.wumpusAlive && game.wumpus.coordinate == newCoordinate || game.pits.any { it.coordinate == newCoordinate })
        return player.copy(points = player.points - 1000, coordinate = newCoordinate, death = true).process()

    return player.copy(points = player.points - 1, coordinate = newCoordinate).process()
}

/**
 * The player can grab a portable object at the current square.
 */
private suspend fun grab(player: Player, game: Game) =
    if (!player.hasTreasure && game.treasure.coordinate == player.coordinate)
         player.copy(points = player.points - 1, hasTreasure = true).process()
    else
        player.copy(points = player.points - 1).process()

/**
 * /The player has a single arrow that it can shoot.
 * It will go straight in the direction faced by the player until it hits (and kills) the wumpus, or hits (and is absorbed by) a wall.
 */
private suspend fun shoot(player: Player, game: Game): Player? {
    if (player.arrows < 1) return player.copy(points = player.points - 1).process()
    if (!player.wumpusAlive) return player.copy(points = player.points - 10, arrows = player.arrows - 1).process()

    val hitWumpus = when (player.direction) {
        NORTH -> player.coordinate.x == game.wumpus.coordinate.x && player.coordinate.y < game.wumpus.coordinate.y
        EAST -> player.coordinate.x < game.wumpus.coordinate.x && player.coordinate.y == game.wumpus.coordinate.y
        SOUTH -> player.coordinate.x == game.wumpus.coordinate.x && player.coordinate.y > game.wumpus.coordinate.y
        WEST -> player.coordinate.x > game.wumpus.coordinate.x && player.coordinate.y == game.wumpus.coordinate.y
    }

    return player.copy(points = player.points - 10, arrows = player.arrows - 1, wumpusAlive = !hitWumpus).process()
}

/**
 * The player can climb out of the cave if it is at the start square and has retrieved the treasure
 */
private suspend fun climb(player: Player, game: Game) =
    if (player.coordinate == game.startingLocation && player.hasTreasure)
        player.copy(points = player.points + 1000, gameCompleted = true).process()
    else
        player.copy(points = player.points - 1).process()

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

private suspend fun retrieveData(gameId: Int, userId: Int) = coroutineScope {
    val game = async { gameRepo.findById(gameId) }
    val player = async { playerRepo.findByGameIdAndUserId(gameId, userId) }
    Pair(game.await(), player.await())
}