package framboos.vrolijke.jdriven.com.service

import framboos.vrolijke.jdriven.com.dao.impl.gameRepo
import framboos.vrolijke.jdriven.com.dao.impl.playerRepo
import framboos.vrolijke.jdriven.com.dao.model.*
import framboos.vrolijke.jdriven.com.dao.model.Perception.BUMP
import framboos.vrolijke.jdriven.com.dao.model.Perception.SCREAM
import framboos.vrolijke.jdriven.com.dao.model.Perception.GLITTER
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

suspend fun doGameAction(gameId: Int, action: String?, userId: Int): Player? {
    if (action == "enter") {
        return addPlayer(userId, gameId)
    }

    val (game, player) = retrieveData(gameId, userId)

    if (player != null && game != null) {
        val p = when (action) {
            "turn-left" -> turnLeft(player)
            "turn-right" -> turnRight(player)
            "move-forward" -> TODO()
            "grab" -> TODO()
            "release" -> TODO()
            "shoot" -> TODO()
            else -> null
        }

        return p?.copy(perceptions = getPerceptions(player, p, game))
    }

    return null
}

private suspend fun addPlayer(userId: Int, gameId: Int) =
    playerRepo.add(CreatePlayer(userId, gameId))


private suspend fun turnLeft(player: Player): Player? {
    val newDirection = if (player.direction == Direction.entries.first()) Direction.entries.last() else Direction.entries[player.direction.ordinal - 1]
    return playerRepo.edit(player.copy(direction = newDirection))
}

private suspend fun turnRight(player: Player): Player? {
    val newDirection = if (player.direction == Direction.entries.last()) Direction.entries.first() else Direction.entries[player.direction.ordinal + 1]
    return playerRepo.edit(player.copy(direction = newDirection))
}

private fun getPerceptions(before: Player, after: Player, game: Game): List<Perception> {
    val list = mutableListOf<Perception>()

    // The agent will perceive the stench if he is in the room adjacent to the Wumpus. (Not diagonally).
    // -> Check `afterAction` coordinates with wumpus

    // The agent will perceive breeze if he is in the room directly adjacent to the Pit.
    // -> Check `afterAction` coordinates with pits

    // The agent will perceive the glitter in the room where the gold is present.
    if (after.coordinate == game.treasure.coordinate) list.add(GLITTER)

    // The agent will perceive the bump if he walks into a wall.
    if (before.coordinate == after.coordinate) list.add(BUMP)

    // When the Wumpus is shot, it emits a horrible scream which can be perceived anywhere in the cave.
    if (before.wumpusAlive && !after.wumpusAlive) list.add(SCREAM)

    return list.toList()
}

private suspend fun retrieveData(gameId: Int, userId: Int) = coroutineScope {
    val game = async { gameRepo.findById(gameId) }
    val player = async { playerRepo.findByGameIdAndUserId(gameId, userId) }
    Pair(game.await(), player.await())
}