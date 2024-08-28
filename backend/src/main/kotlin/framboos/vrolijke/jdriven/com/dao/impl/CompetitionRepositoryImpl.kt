package framboos.vrolijke.jdriven.com.dao.impl

import framboos.vrolijke.jdriven.com.dao.CompetitionRepository
import framboos.vrolijke.jdriven.com.dao.DatabaseSingleton.dbQuery
import framboos.vrolijke.jdriven.com.dao.model.*
import framboos.vrolijke.jdriven.com.utils.exists
import framboos.vrolijke.jdriven.com.utils.getNextOrNull
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update

class CompetitionRepositoryImpl : ReadRepositoryImpl<Competition>(Competitions), CompetitionRepository {
    override fun toDto(row: ResultRow) = Competition(
        id = row[Competitions.id].value,
        currentGameId = row[Competitions.currentGameId],
        gameIds = row[Competitions.gameIds],
    )

    override suspend fun create() = dbQuery {
        val gameIds = gameRepo.allIds()//.shuffled().take((3..6).random()).sorted()
        Competitions.insert {
            it[currentGameId] = gameIds.first()
            it[this.gameIds] = gameIds
        }
    }

    override suspend fun allIds() = dbQuery {
        Competitions
            .select(Competitions.id)
            .map { it[Competitions.id].value }
    }

    override suspend fun findByIdWithScore(id: Int) =
        super.findById(id)?.let { competition ->
            val score = competition.gameIds
                .flatMap { g -> playerRepo.findByGameId(g).map { Score(it.user!!.id, it.user.name, it.user.trouserColor, it.user.skinColor, it.points) } }
                .groupBy { it.userId }
                .map { (userId, scoreList) -> Score(userId, scoreList[0].username, scoreList[0].trouserColor, scoreList[0].skinColor, scoreList.sumOf { it.points }) }

            CompetitionWithScore(competition.id, competition.currentGameId, competition.gameIds, score)
        }

    override suspend fun isCurrentGame(gameId: Int) = dbQuery {
        Competitions.exists { Competitions.currentGameId eq gameId }
    }

    override suspend fun advance(id: Int) = dbQuery {
        val competition = super.findById(id) ?: return@dbQuery true
        val nextCurrentGame = competition.gameIds.getNextOrNull(competition.currentGameId) ?: return@dbQuery true

        Competitions.update({ Competitions.id eq id }) { it[currentGameId] = nextCurrentGame } > 0
    }
}

val competitionRepo: CompetitionRepository = CompetitionRepositoryImpl()
