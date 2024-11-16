# wumpus-framboos

Application for the `vrolijke framboos`. It contains a server to interact with the Wumpus world, a frontend to show the world and two bots to show how you could implement it as a player.
The player can go to http://localhost:80/swagger for actions.

## Run 
Backend:

```shell
> cd backend
> ./gradlew run

# Remove database
# > rm -r build
```

Frontend:

```shell
> cd frontend
> ng serve
```

You can also run the `./build-frontend` command to give the players the option to see the overview interface. The admin button will not be available for them.

Bots:
```shell
> cd bots

# node bot (complete working bot)
> cd node-bots
> npm start

# Kotlin bot (not complete, used to do stress tests)
# Just run from IntelliJ
```

## Game night
The game exists of two parts, a hacking/development phase and a contest phase. The first phase is meant for the players to learn the game and code a bot. The second phase is the competition, where players let their bots go through the levels without any more changes to their code. After all levels are completed the player with the most points wins the game.

As an host, you need to manually change the mode if you want to go to contest phase, by stopping the application, changing the mode to CONTEST (in Application.kt) and starting the application again. You probably also want to remove the database to give each player a fresh start. Once the game is booted in CONTEST mode, the overview interface for the players will no longer work nor can the admin calls be made from anywhere else than localhost. So the host needs to present its screen and show the game with `ng serve`.

_The reason for this complication is just to ensure that players do not cheat by using the admin password to retrieve game data._ 
