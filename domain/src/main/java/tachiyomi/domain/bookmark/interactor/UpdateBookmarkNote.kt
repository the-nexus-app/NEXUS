package tachiyomi.domain.bookmark.interactor

import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.bookmark.repository.BookmarkRepository

/**
 * Updates the optional personal note attached to an existing bookmark. A blank or
 * whitespace-only note is normalized to null so empty notes are never persisted or
 * displayed. Does not affect bookmark identity (chapter + page) or navigation.
 */
class UpdateBookmarkNote(
    private val bookmarkRepository: BookmarkRepository,
) {
    suspend fun await(id: Long, note: String?) {
        try {
            bookmarkRepository.updateBookmarkNote(id, note?.trim()?.ifBlank { null })
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
        }
    }
}