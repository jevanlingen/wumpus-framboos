package framboos.vrolijke.jdriven.com.dao.impl

import framboos.vrolijke.jdriven.com.dao.CompetitionRepository
import framboos.vrolijke.jdriven.com.dao.DatabaseSingleton.dbQuery
import framboos.vrolijke.jdriven.com.dao.model.*
import org.jetbrains.exposed.sql.ResultRow

class CompetitionRepositoryImpl : ReadRepositoryImpl<Competition>(Competitions), CompetitionRepository {
    override fun toDto(row: ResultRow) = Competition(
        id = row[Competitions.id].value,
        currentGameId = row[Competitions.currentGameId],
        gameIds = row[Competitions.gameIds],
    )

    override suspend fun allIds() = dbQuery {
        Competitions
            .select(Competitions.id)
            .map { it[Competitions.id].value }
    }

    override suspend fun findById(id: Int): Competition? {
        val competition = super.findById(id)
        val score = (competition?.gameIds ?: emptyList())
            .flatMap { g -> playerRepo.findByGameId(g).map { Score(it.userId, it.user, it.points) } }
            .groupBy { it.userId }
            .map { (userId, scoreList) -> Score(userId, scoreList.first().username, scoreList.sumOf { it.points }) }

        return competition?.copy(score = score)
    }
}

val competitionRepo: CompetitionRepository = CompetitionRepositoryImpl()
