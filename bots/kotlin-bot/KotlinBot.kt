const val BACKEND = "http://0.0.0.0:8080"
const val AMOUNT_OF_BOTS = 5 // Don't set this to high, otherwise you will get OOM errors

fun main() {
    List(AMOUNT_OF_BOTS) { Thread.ofVirtual().start { createBotAndPlayGame(it + 1) } }
        .onEach { it.join() }
}

fun createBotAndPlayGame(idx: Int) {
    val api = WumpusApi(BACKEND)
    val username = "random-bot-$idx"

    if (!api.createAccount(username, "my-precious")) {
        println("Failed to create account for $username")
        return
    }

    val competitions = api.getCompetitions() ?: return
    for (competitionId in competitions) {
        val competition = api.getCompetition(competitionId) ?: continue
        val gameId = competition.currentGameId
        val game = api.getGame(gameId) ?: continue

        WumpusGame(api, game).playGame()
    }
}
