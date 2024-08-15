fun main() {
    val apiUrl = "http://0.0.0.0:8080"
    val api = WumpusApi(apiUrl)

    if (!api.createAccount("random-bot", "my-precious")) {
        println("Failed to create account")
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
