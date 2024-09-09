# wumpus-framboos

Application for the `vrolijke framboos`. It contains a server to interact with the Wmpus world, a frontend to show the world and two bots to show how you could implement it as a player.
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

Bots:
```shell
> cd bots

# node bot (complete working bot)
> cd node-bots
> npm start

# Kotlin bot (not complete, used to do stress tests)
# Just run from IntelliJ
```
