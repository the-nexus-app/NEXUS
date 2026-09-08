package tachiyomi.domain.bookmark.repository

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.bookmark.model.Bookmark
import tachiyomi.domain.bookmark.model.BookmarkWithChapter

interface BookmarkRepository {

    fun getBookmarksByMangaIdAsFlow(mangaId: Long): Flow<List<BookmarkWithChapter>>

    suspend fun getBookmarksByMangaId(mangaId: Long): List<BookmarkWithChapter>

    suspend fun getBookmark(chapterId: Long, pageIndex: Int): Bookmark?

    suspend fun insertBookmark(chapterId: Long, pageIndex: Int, scrollPosition: Float?): Long

    suspend fun deleteBookmark(id: Long)

    suspend fun deleteBookmark(chapterId: Long, pageIndex: Int)

    suspend fun updateBookmarkNote(id: Long, note: String?)
}