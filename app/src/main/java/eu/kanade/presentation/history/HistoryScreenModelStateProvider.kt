package eu.kanade.presentation.history

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import eu.kanade.tachiyomi.ui.history.HistoryScreenModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tachiyomi.domain.history.model.HistoryWithRelations
import tachiyomi.domain.manga.model.MangaCover
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlin.random.Random

class HistoryScreenModelStateProvider : PreviewParameterProvider<HistoryScreenModel.State> {

    private val multiPage = HistoryScreenModel.State(
        searchQuery = null,
        list =
        persistentListOf(HistoryWithRelationExamples.headerToday)
            .asSequence()
            .plus(HistoryWithRelationExamples.items().take(3))
            .plus(HistoryWithRelationExamples.header { it.minus(1, ChronoUnit.DAYS) })
            .plus(HistoryWithRelationExamples.items().take(1))
            .plus(HistoryWithRelationExamples.header { it.minus(2, ChronoUnit.DAYS) })
            .plus(HistoryWithRelationExamples.items().take(7))
            .toImmutableList(),
        dialog = null,
    )

    private val shortRecent = HistoryScreenModel.State(
        searchQuery = null,
        list = persistentListOf(
            HistoryWithRelationExamples.headerToday,
            HistoryWithRelationExamples.items().first(),
        ),
        dialog = null,
    )

    private val shortFuture = HistoryScreenModel.State(
        searchQuery = null,
        list = persistentListOf(
            HistoryWithRelationExamples.headerTomorrow,
            HistoryWithRelationExamples.items().first(),
        ),
        dialog = null,
    )

    private val empty = HistoryScreenModel.State(
        searchQuery = null,
        list = persistentListOf(),
        dialog = null,
    )

    private val loadingWithSearchQuery = HistoryScreenModel.State(
        searchQuery = "Example Search Query",
    )

    private val loading = HistoryScreenModel.State(
        searchQuery = null,
        isLoading = true,
        dialog = null,
    )

    override val values: Sequence<HistoryScreenModel.State> = sequenceOf(
        multiPage,
        shortRecent,
        shortFuture,
        empty,
        loadingWithSearchQuery,
        loading,
    )

    private object HistoryWithRelationExamples {
        val headerToday = randItem()
        val headerTomorrow = randItem(LocalDate.now().plusDays(1).toDate())

        fun header(instantBuilder: (Instant) -> Instant = { it }) =
            randItem(LocalDate.from(instantBuilder(Instant.now())).toDate())

        fun LocalDate.toDate(zone: ZoneId = ZoneId.systemDefault()): Date =
            Date.from(atStartOfDay(zone).toInstant())

        fun items() = sequence {
            var count = 1
            while (true) {
                yield(randItem { it.copy(ogTitle = "Example Title $count") })
                count += 1
            }
        }

        fun randItem(
            readAt: Date = Date.from(Instant.now()),
            historyBuilder: (HistoryWithRelations) -> HistoryWithRelations = { it },
        ) =
            historyBuilder(
                HistoryWithRelations(
                    id = Random.nextLong(),
                    chapterId = Random.nextLong(),
                    mangaId = Random.nextLong(),
                    ogTitle = "Test Title",
                    chapterNumber = Random.nextDouble(),
                    read = Random.nextBoolean(),
                    lastPageRead = Random.nextLong(1, 10),
                    totalCountCalculated = Random.nextLong(1, 100),
                    readCountCalculated = 1,
                    readAt = readAt,
                    readDuration = Random.nextLong(),
                    coverData = MangaCover(
                        mangaId = Random.nextLong(),
                        sourceId = Random.nextLong(),
                        isMangaFavorite = Random.nextBoolean(),
                        ogUrl = "https://example.com/cover.png",
                        lastModified = Random.nextLong(),
                    ),
                ),
            )
    }
}
