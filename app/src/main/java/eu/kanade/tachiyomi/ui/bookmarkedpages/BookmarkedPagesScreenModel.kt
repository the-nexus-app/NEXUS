package eu.kanade.tachiyomi.ui.bookmarkedpages

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.presentation.bookmarkedpages.components.BookmarkedPagesUiModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.core.common.util.lang.launchNonCancellable
import tachiyomi.domain.bookmark.interactor.DeleteBookmark
import tachiyomi.domain.bookmark.interactor.GetAllBookmarks
import tachiyomi.domain.bookmark.interactor.UpdateBookmarkNote
import tachiyomi.domain.bookmark.model.BookmarkWithManga
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class BookmarkedPagesScreenModel(
    private val getAllBookmarks: GetAllBookmarks = Injekt.get(),
    private val deleteBookmark: DeleteBookmark = Injekt.get(),
    private val updateBookmarkNoteInteractor: UpdateBookmarkNote = Injekt.get(),
) : StateScreenModel<BookmarkedPagesScreenModel.State>(State()) {

    init {
        screenModelScope.launchIO {
            getAllBookmarks.subscribe().collectLatest { bookmarks ->
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        items = bookmarks,
                    )
                }
            }
        }
    }

    fun removeBookmark(id: Long) {
        screenModelScope.launchNonCancellable { deleteBookmark.await(id) }
    }

    fun updateBookmarkNote(id: Long, note: String?) {
        screenModelScope.launchNonCancellable { updateBookmarkNoteInteractor.await(id, note) }
    }

    @Immutable
    data class State(
        val isLoading: Boolean = true,
        val items: List<BookmarkWithManga> = emptyList(),
    ) {
        val isEmpty: Boolean = items.isEmpty()

        fun getUiModels(): List<BookmarkedPagesUiModel> {
            val uiModels = mutableListOf<BookmarkedPagesUiModel>()
            items.groupBy { it.mangaId }.forEach { (mangaId, bookmarks) ->
                val representative = bookmarks.first()
                uiModels.add(
                    BookmarkedPagesUiModel.Header(
                        mangaId = mangaId,
                        mangaTitle = representative.mangaTitle,
                        bookmarkCount = bookmarks.size,
                    ),
                )
                uiModels.addAll(
                    bookmarks
                        .sortedByDescending { it.createdAt }
                        .map(BookmarkedPagesUiModel::Item),
                )
            }
            return uiModels
        }
    }
}