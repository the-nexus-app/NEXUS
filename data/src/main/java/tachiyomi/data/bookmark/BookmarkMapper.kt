package tachiyomi.data.bookmark

import tachiyomi.domain.bookmark.model.Bookmark
import tachiyomi.domain.bookmark.model.BookmarkWithChapter

object BookmarkMapper {

    fun mapBookmark(
        id: Long,
        chapterId: Long,
        pageIndex: Long,
        scrollPosition: Double?,
        createdAt: Long,
    ): Bookmark {
        return Bookmark(
            id = id,
            chapterId = chapterId,
            pageIndex = pageIndex.toInt(),
            scrollPosition = scrollPosition?.toFloat(),
            createdAt = createdAt,
        )
    }

    fun mapBookmarkWithChapter(
        id: Long,
        chapterId: Long,
        pageIndex: Long,
        scrollPosition: Double?,
        createdAt: Long,
        mangaId: Long,
        chapterName: String,
        chapterNumber: Double,
    ): BookmarkWithChapter {
        return BookmarkWithChapter(
            id = id,
            chapterId = chapterId,
            mangaId = mangaId,
            pageIndex = pageIndex.toInt(),
            scrollPosition = scrollPosition?.toFloat(),
            createdAt = createdAt,
            chapterName = chapterName,
            chapterNumber = chapterNumber,
        )
    }
}