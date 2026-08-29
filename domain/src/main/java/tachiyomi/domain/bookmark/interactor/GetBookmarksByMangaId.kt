package tachiyomi.domain.bookmark.interactor

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.bookmark.model.BookmarkWithChapter
import tachiyomi.domain.bookmark.repository.BookmarkRepository

class GetBookmarksByMangaId(
    private val bookmarkRepository: BookmarkRepository,
) {
    fun subscribe(mangaId: Long): Flow<List<BookmarkWithChapter>> {
        return bookmarkRepository.getBookmarksByMangaIdAsFlow(mangaId)
    }

    suspend fun await(mangaId: Long): List<BookmarkWithChapter> {
        return bookmarkRepository.getBookmarksByMangaId(mangaId)
    }
}