import Direction.EAST
import Direction.NORTH
import Direction.SOUTH
import Direction.WEST

val startingLocation = Coordinate(1, 1)

private const val GIVE_UP = 3000

class WumpusGame(
    private val api: WumpusApi,
    private val game: Game,
) {
    private var grid: Array<Array<Tile>> = Array(game.gridSize) { Array(game.gridSize) { Tile() } }
    private var deaths = 0

    fun playGame() {
        var player = api.performAction(game.id, "enter")!!

        while (!player.gameCompleted) {
            if (player.death) {
                deaths++
                if (deaths > GIVE_UP) break else api.performAction(game.id, "restart")
            }

            player = player.takeNextAction()
        }

        if (player.gameCompleted) {
            println("You won the game with ${player.points} points!")
        } else {
            println("You could not complete this game (you have ${player.points} points).")
        }
    }

    private fun Player.takeNextAction(): Player {
        if (hasTreasure && coordinate == startingLocation) {
            return api.performAction(this@WumpusGame.game.id, "climb") ?: this
        }

        val nextMove = if (hasTreasure) moveToStart() else exploreNextTile()
        return (perform(nextMove) ?: this)
            .also { if ("GLITTER" in this.perceptions) api.performAction(game.id, "grab") }
            .also { it.markTile() }
    }

    // Dumb implementation, will walk straight to pits and wumpus :(
    private fun Player.moveToStart() =
        when {
            coordinate.x > 1 -> Move(WEST)
            coordinate.y > 1 -> Move(SOUTH)
            else -> Move(direction) // fallback
        }

    private fun Player.exploreNextTile(): Move {
        val (x, y) = coordinate
        val moves = mutableListOf<Move>()

        if (x > 1 && grid[x - 2][y - 1].isSafe()) moves.add(Move(WEST))
        if (x < grid.size && grid[x][y - 1].isSafe()) moves.add(Move(EAST))
        if (y > 1 && grid[x - 1][y - 2].isSafe()) moves.add(Move(SOUTH))
        if (y < grid.size && grid[x - 1][y].isSafe()) moves.add(Move(NORTH))

        // If all options are dangerous, just move forward
        if (moves.isEmpty()) {
            return Move(direction)
        }

        return moves.random()
    }

    private fun Player.perform(move: Move): Player? {
        fun turnRight() = api.performAction(game.id, "turn-right")

        fun turnLeft() = api.performAction(game.id, "turn-left")

        fun moveForward() = api.performAction(game.id, "move-forward")

        val to = move.direction
        return when {
            (direction == NORTH && to == EAST) ||
                (direction == EAST && to == SOUTH) ||
                (direction == SOUTH && to == WEST) ||
                (direction == WEST && to == NORTH) -> turnRight()?.let { moveForward() }

            (direction == NORTH && to == WEST) ||
                (direction == WEST && to == SOUTH) ||
                (direction == SOUTH && to == EAST) ||
                (direction == EAST && to == NORTH) -> turnLeft()?.let { moveForward() }

            else -> moveForward()
        }
    }

    private fun Player.markTile() {
        val (x, y) = coordinate
        val tile = grid[x - 1][y - 1]
        tile.visited = true
        tile.perceptions = perceptions

        if ("BREEZE" in perceptions || "STENCH" in perceptions) {
            /* Not implemented right, could also be dangerous somewhere other than the direction you are walking...
            when (direction) {
                NORTH -> grid[x][y + 1].dangerous = true
                EAST -> grid[x + 1][y].dangerous = true
                SOUTH -> grid[x][y - 1].dangerous = true
                WEST -> grid[x - 1][y].dangerous = true
            }*/
        }
    }
}

private data class Tile(
    var visited: Boolean = false,
    var dangerous: Boolean = false,
    var perceptions: List<String> = emptyList(),
) {
    fun isSafe(): Boolean = visited || !dangerous
}

data class Move(
    val direction: Direction,
)
