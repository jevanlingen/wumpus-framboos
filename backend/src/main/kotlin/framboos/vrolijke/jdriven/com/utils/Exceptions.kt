package framboos.vrolijke.jdriven.com.utils

class PlayerNotRegisteredException: Exception("You need to use the `enter` action to start playing this game.")

class NotAnActionException(action: String?): Exception("The `$action` action does not exists. Use a valid action.")

class GameDoesNotExists(gameId: Int): Exception("Game #$gameId does not exist, try another <id>.")

class GameIsNotCurrentException: Exception("This game is not currently being played in the competition.")
