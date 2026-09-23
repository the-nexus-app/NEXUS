package eu.kanade.tachiyomi.ui.dashboard

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.domain.category.interactor.GetCategories
import tachiyomi.domain.history.interactor.GetHistory
import tachiyomi.domain.manga.interactor.GetLibraryManga
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Backs only the Welcome Back card's Reading Insights now - Home's Feed section owns
 * its own data via [eu.kanade.tachiyomi.ui.browse.feed.FeedScreenModel] directly
 * (created in [DashboardTab]), reusing it rather than funneling Feed data through here.
 */
class DashboardScreenModel(
    private val getHistory: GetHistory = Injekt.get(),
    private val getLibraryManga: GetLibraryManga = Injekt.get(),
    private val getCategories: GetCategories = Injekt.get(),
) : StateScreenModel<DashboardScreenModel.State>(State()) {

    init {
        screenModelScope.launchIO {
            combine(
                libraryVisibilityFlow(),
                // History is only read here to compute "Chapters Completed This Week" -
                // it is never surfaced as a Continue Reading UI on Home. The reader and
                // History tab keep using GetHistory/HistoryRepository exactly as before;
                // nothing about that underlying logic is touched or removed.
                getHistory.subscribe(
                    query = "",
                    unfinishedManga = null,
                    unfinishedChapter = null,
                    nonLibraryEntries = true,
                ),
            ) { visibility, history ->
                // "Chapters Completed This Week" only counts history rows that are both
                // (a) actually marked read - history is written on any progress, not
                // just completion - and (b) last touched within the last 7 days. No
                // approximation: HistoryWithRelations already carries the chapter's
                // real `read` flag, so this is an exact count, not an estimate. Hidden
                // categories are excluded the same way Library Titles are.
                val weekAgo = Instant.now().minus(7, ChronoUnit.DAYS)
                val chaptersCompletedThisWeek = history.count { entry ->
                    val readAt = entry.readAt
                    entry.mangaId !in visibility.hiddenMangaIds &&
                        entry.read &&
                        readAt != null &&
                        readAt.toInstant().isAfter(weekAgo)
                }

                val insights = ReadingInsights(
                    libraryTitles = visibility.visibleLibraryTitleCount,
                    chaptersCompletedThisWeek = chaptersCompletedThisWeek,
                )
                insights to visibility.hiddenMangaIds
            }.collectLatest { (insights, hiddenMangaIds) ->
                mutableState.update {
                    it.copy(isLoading = false, readingInsights = insights, hiddenMangaIds = hiddenMangaIds)
                }
            }
        }
    }

    /**
     * Combines hidden-category manga IDs with the total *visible* library title count in
     * one pass, so "Library Titles" and the "Chapters Completed" filtering share exactly
     * one source of truth for what's hidden.
     */
    private fun libraryVisibilityFlow() = combine(
        getCategories.subscribe(),
        getLibraryManga.subscribe(),
    ) { categories, libraryManga ->
        val hiddenCategoryIds = categories.filter { it.hidden }.map { it.id }.toSet()
        val hiddenMangaIds: Set<Long> = if (hiddenCategoryIds.isEmpty()) {
            emptySet()
        } else {
            libraryManga
                .filter { it.categories.any { catId -> catId in hiddenCategoryIds } }
                .map { it.manga.id }
                .toSet()
        }
        LibraryVisibility(
            hiddenMangaIds = hiddenMangaIds,
            visibleLibraryTitleCount = libraryManga.count { it.manga.id !in hiddenMangaIds },
        )
    }

    @Immutable
    data class State(
        val isLoading: Boolean = true,
        val readingInsights: ReadingInsights = ReadingInsights(),
        val hiddenMangaIds: Set<Long> = emptySet(),
    )

    /**
     * Compact, privacy-safe stats for the Welcome Back card. Deliberately does not
     * include "reading sessions" or "reading time" - the `history` table stores one
     * cumulative row per chapter with a single last-read timestamp, not a session log
     * or a per-week time breakdown, so neither stat can be computed reliably.
     */
    @Immutable
    data class ReadingInsights(
        val libraryTitles: Int = 0,
        val chaptersCompletedThisWeek: Int = 0,
    )

    private data class LibraryVisibility(
        val hiddenMangaIds: Set<Long>,
        val visibleLibraryTitleCount: Int,
    )
}
