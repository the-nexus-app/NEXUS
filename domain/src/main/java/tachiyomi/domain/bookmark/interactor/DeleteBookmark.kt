package tachiyomi.domain.bookmark.interactor

import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.bookmark.repository.BookmarkRepository

class DeleteBookmark(
    private val bookmarkRepository: BookmarkRepository,
) {
    suspend fun await(id: Long) {
        try {
            bookmarkRepository.deleteBookmark(id)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
        }
    }
}