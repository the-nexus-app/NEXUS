package tachiyomi.domain.bookmark.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.bookmark.model.BookmarkWithManga
import tachiyomi.domain.bookmark.repository.BookmarkRepository

/**
 * Global (cross-manga) manual page bookmark subscription, used by the Bookmarked Pages
 * screen. Mirrors GetAllBookmarkedChapters, which does the same for chapter bookmarks.
 */
class GetAllBookmarks(
    private val bookmarkRepository: BookmarkRepository,
) {

    fun subscribe(): Flow<List<BookmarkWithManga>> {
        return bookmarkRepository.getAllBookmarksAsFlow()
            .catch {
                logcat(LogPriority.ERROR, it)
                emit(emptyList())
            }
    }
}
