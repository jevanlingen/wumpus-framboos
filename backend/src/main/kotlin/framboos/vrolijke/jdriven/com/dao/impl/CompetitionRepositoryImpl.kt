package framboos.vrolijke.jdriven.com.dao.impl

import framboos.vrolijke.jdriven.com.dao.CompetitionRepository
import framboos.vrolijke.jdriven.com.dao.model.*
import org.jetbrains.exposed.sql.ResultRow

class CompetitionRepositoryImpl : ReadRepositoryImpl<Competition>(Competitions), CompetitionRepository {
    override fun toDto(row: ResultRow) = Competition(
        id = row[Competitions.id].value,
        currentGame = row[Competitions.currentGameId],
        games = row[Competitions.gameIds],
    )

    override suspend fun allIds() =
        Competitions
            .select(Competitions.id)
            .map { it[Competitions.id].value }
}

val competitionRepo: CompetitionRepository = CompetitionRepositoryImpl()
