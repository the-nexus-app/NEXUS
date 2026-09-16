package tachiyomi.data.bookmark

import tachiyomi.domain.bookmark.model.Bookmark
import tachiyomi.domain.bookmark.model.BookmarkWithChapter
import tachiyomi.domain.bookmark.model.BookmarkWithManga
import tachiyomi.domain.manga.model.MangaCover

object BookmarkMapper {

    fun mapBookmark(
        id: Long,
        chapterId: Long,
        pageIndex: Long,
        scrollPosition: Double?,
        createdAt: Long,
        note: String?,
    ): Bookmark {
        return Bookmark(
            id = id,
            chapterId = chapterId,
            pageIndex = pageIndex.toInt(),
            scrollPosition = scrollPosition?.toFloat(),
            createdAt = createdAt,
            note = note,
        )
    }

    fun mapBookmarkWithChapter(
        id: Long,
        chapterId: Long,
        pageIndex: Long,
        scrollPosition: Double?,
        createdAt: Long,
        note: String?,
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
            note = note,
        )
    }

    fun mapBookmarkWithManga(
        id: Long,
        chapterId: Long,
        pageIndex: Long,
        scrollPosition: Double?,
        createdAt: Long,
        note: String?,
        mangaId: Long,
        chapterName: String,
        chapterNumber: Double,
        mangaTitle: String,
        sourceId: Long,
        favorite: Boolean,
        thumbnailUrl: String?,
        coverLastModified: Long,
    ): BookmarkWithManga {
        return BookmarkWithManga(
            id = id,
            chapterId = chapterId,
            mangaId = mangaId,
            pageIndex = pageIndex.toInt(),
            scrollPosition = scrollPosition?.toFloat(),
            createdAt = createdAt,
            chapterName = chapterName,
            chapterNumber = chapterNumber,
            mangaTitle = mangaTitle,
            coverData = MangaCover(
                mangaId = mangaId,
                sourceId = sourceId,
                isMangaFavorite = favorite,
                ogUrl = thumbnailUrl,
                lastModified = coverLastModified,
            ),
            note = note,
        )
    }
}
