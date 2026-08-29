package tachiyomi.data.bookmark

import kotlinx.coroutines.flow.Flow
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.data.DatabaseHandler
import tachiyomi.domain.bookmark.model.Bookmark
import tachiyomi.domain.bookmark.model.BookmarkWithChapter
import tachiyomi.domain.bookmark.repository.BookmarkRepository
import java.time.Instant

class BookmarkRepositoryImpl(
    private val handler: DatabaseHandler,
) : BookmarkRepository {

    override fun getBookmarksByMangaIdAsFlow(mangaId: Long): Flow<List<BookmarkWithChapter>> {
        return handler.subscribeToList {
            bookmarksQueries.getBookmarksByMangaId(mangaId, BookmarkMapper::mapBookmarkWithChapter)
        }
    }

    override suspend fun getBookmarksByMangaId(mangaId: Long): List<BookmarkWithChapter> {
        return handler.awaitList {
            bookmarksQueries.getBookmarksByMangaId(mangaId, BookmarkMapper::mapBookmarkWithChapter)
        }
    }

    override suspend fun getBookmark(chapterId: Long, pageIndex: Int): Bookmark? {
        return handler.awaitOneOrNull {
            bookmarksQueries.getBookmarkByChapterAndPage(
                chapterId,
                pageIndex.toLong(),
                BookmarkMapper::mapBookmark,
            )
        }
    }

    override suspend fun insertBookmark(chapterId: Long, pageIndex: Int, scrollPosition: Float?): Long {
        return try {
            handler.await(inTransaction = true) {
                bookmarksQueries.insert(
                    chapterId,
                    pageIndex.toLong(),
                    scrollPosition?.toDouble(),
                    Instant.now().toEpochMilli(),
                )
                bookmarksQueries.selectLastInsertedRowId().executeAsOne()
            }
        } catch (e: Exception) {
            // Most likely the unique(chapter_id, page_index) index rejecting a duplicate
            // created by a race (double-tap on the bookmark button); not fatal.
            logcat(LogPriority.ERROR, e)
            -1L
        }
    }

    override suspend fun deleteBookmark(id: Long) {
        try {
            handler.await { bookmarksQueries.deleteById(id) }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
        }
    }

    override suspend fun deleteBookmark(chapterId: Long, pageIndex: Int) {
        try {
            handler.await { bookmarksQueries.deleteByChapterAndPage(chapterId, pageIndex.toLong()) }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
        }
    }
}