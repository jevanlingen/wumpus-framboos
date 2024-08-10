fun main() {
    val apiUrl = "http://localhost.com:8080"
    val api = WumpusApi(apiUrl)

    if (!api.createAccount("jacob", "my-precious")) {
        println("Failed to create account")
        return
    }

    val game = WumpusGame(api)
    game.playCompetitions()
}
