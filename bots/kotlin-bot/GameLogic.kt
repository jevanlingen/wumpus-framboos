class WumpusGame(
    private val api: WumpusApi,
) {
    fun playCompetitions() {
        val competitions = api.getCompetitions() ?: return

        for (competitionId in competitions) {
            val competition = api.getCompetition(competitionId) ?: continue
            val gameId = competition.currentGameId
            val player = api.performAction(gameId, "enter") ?: continue
            playGame(player, gameId)
        }
    }

    private fun playGame(
        player: Player,
        gameId: Int,
    ) {
        var playerX = player
        var deaths = 0

        while (!playerX.gameCompleted) {
            if (playerX.death) {
                deaths++
                if (deaths > 3) break
            }

            playerX = takeNextAction(playerX, gameId)
        }

        if (playerX.gameCompleted) {
            println("You won the game with ${playerX.points} points!")
        } else {
            println("You could not complete this game (you have ${playerX.points} points.")
        }
    }

    private fun takeNextAction(
        player: Player,
        gameId: Int,
    ): Player {
        val action =
            when {
                "BREEZE" in player.perceptions -> "turn-right"
                "STENCH" in player.perceptions -> "turn-right"
                else -> "move-forward"
            }
        return api.performAction(gameId, action) ?: player
    }
}
