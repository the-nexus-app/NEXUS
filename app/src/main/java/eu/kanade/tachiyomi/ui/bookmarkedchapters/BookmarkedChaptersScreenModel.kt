package eu.kanade.tachiyomi.ui.bookmarkedchapters

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.presentation.bookmarkedchapters.components.BookmarkedChaptersUiModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.domain.chapter.interactor.GetAllBookmarkedChapters
import tachiyomi.domain.chapter.interactor.UpdateChapter
import tachiyomi.domain.chapter.model.BookmarkedChapterWithManga
import tachiyomi.domain.chapter.model.ChapterUpdate
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class BookmarkedChaptersScreenModel(
    private val getAllBookmarkedChapters: GetAllBookmarkedChapters = Injekt.get(),
    private val updateChapter: UpdateChapter = Injekt.get(),
) : StateScreenModel<BookmarkedChaptersScreenModel.State>(State()) {

    init {
        screenModelScope.launchIO {
            getAllBookmarkedChapters.subscribe().collectLatest { bookmarkedChapters ->
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        items = bookmarkedChapters,
                    )
                }
            }
        }
    }

    fun unbookmarkChapter(item: BookmarkedChapterWithManga) {
        screenModelScope.launchIO {
            updateChapter.await(ChapterUpdate(id = item.chapterId, bookmark = false))
        }
    }

    @Immutable
    data class State(
        val isLoading: Boolean = true,
        val items: List<BookmarkedChapterWithManga> = emptyList(),
    ) {
        val isEmpty: Boolean = items.isEmpty()

        fun getUiModels(): List<BookmarkedChaptersUiModel> {
            val uiModels = mutableListOf<BookmarkedChaptersUiModel>()
            items.groupBy { it.mangaId }.forEach { (mangaId, chapters) ->
                val representative = chapters.first()
                uiModels.add(
                    BookmarkedChaptersUiModel.Header(
                        mangaId = mangaId,
                        mangaTitle = representative.mangaTitle,
                        chapterCount = chapters.size,
                    ),
                )
                uiModels.addAll(
                    chapters
                        .sortedByDescending { it.chapterNumber }
                        .map(BookmarkedChaptersUiModel::Item),
                )
            }
            return uiModels
        }
    }
}
