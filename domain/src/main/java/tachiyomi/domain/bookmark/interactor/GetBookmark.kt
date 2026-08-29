package tachiyomi.domain.bookmark.interactor

import tachiyomi.domain.bookmark.model.Bookmark
import tachiyomi.domain.bookmark.repository.BookmarkRepository

class GetBookmark(
    private val bookmarkRepository: BookmarkRepository,
) {
    suspend fun await(chapterId: Long, pageIndex: Int): Bookmark? {
        return bookmarkRepository.getBookmark(chapterId, pageIndex)
    }
}