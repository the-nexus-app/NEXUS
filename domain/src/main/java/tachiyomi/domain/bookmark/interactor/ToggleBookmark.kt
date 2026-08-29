package tachiyomi.domain.bookmark.interactor

import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.bookmark.repository.BookmarkRepository

/**
 * Creates a manual bookmark at (chapterId, pageIndex) if one doesn't already exist there,
 * or removes it if it does. The unique index on (chapter_id, page_index) is the source of
 * truth for "same exact location"; this class just decides create-vs-remove.
 *
 * Returns true if a bookmark now exists at that location, false if it was removed.
 */
class ToggleBookmark(
    private val bookmarkRepository: BookmarkRepository,
) {
    suspend fun await(chapterId: Long, pageIndex: Int, scrollPosition: Float?): Boolean {
        return try {
            val existing = bookmarkRepository.getBookmark(chapterId, pageIndex)
            if (existing != null) {
                bookmarkRepository.deleteBookmark(existing.id)
                false
            } else {
                bookmarkRepository.insertBookmark(chapterId, pageIndex, scrollPosition)
                true
            }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            existingStateFallback(chapterId, pageIndex)
        }
    }

    private suspend fun existingStateFallback(chapterId: Long, pageIndex: Int): Boolean {
        return bookmarkRepository.getBookmark(chapterId, pageIndex) != null
    }
}