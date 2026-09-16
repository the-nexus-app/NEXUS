package tachiyomi.data.chapter

import kotlinx.serialization.json.JsonObject
import tachiyomi.domain.chapter.model.BookmarkedChapterWithManga
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.manga.model.MangaCover

object ChapterMapper {
    fun mapChapter(
        id: Long,
        mangaId: Long,
        url: String,
        name: String,
        scanlator: String?,
        read: Boolean,
        bookmark: Boolean,
        lastPageRead: Long,
        chapterNumber: Double,
        sourceOrder: Long,
        dateFetch: Long,
        dateUpload: Long,
        lastModifiedAt: Long,
        version: Long,
        @Suppress("UNUSED_PARAMETER")
        isSyncing: Long,
        memo: JsonObject,
    ): Chapter = Chapter(
        id = id,
        mangaId = mangaId,
        read = read,
        bookmark = bookmark,
        lastPageRead = lastPageRead,
        dateFetch = dateFetch,
        sourceOrder = sourceOrder,
        url = url,
        name = name,
        dateUpload = dateUpload,
        chapterNumber = chapterNumber,
        scanlator = scanlator,
        lastModifiedAt = lastModifiedAt,
        version = version,
        memo = memo,
    )

    fun mapBookmarkedChapterWithManga(
        mangaId: Long,
        mangaTitle: String,
        source: Long,
        favorite: Boolean,
        thumbnailUrl: String?,
        coverLastModified: Long,
        chapterId: Long,
        chapterName: String,
        chapterNumber: Double,
        scanlator: String?,
        read: Boolean,
        lastPageRead: Long,
        dateUpload: Long,
        bookmarkedAt: Long,
    ): BookmarkedChapterWithManga = BookmarkedChapterWithManga(
        mangaId = mangaId,
        ogMangaTitle = mangaTitle,
        chapterId = chapterId,
        chapterName = chapterName,
        chapterNumber = chapterNumber,
        scanlator = scanlator,
        read = read,
        lastPageRead = lastPageRead,
        sourceId = source,
        dateUpload = dateUpload,
        bookmarkedAt = bookmarkedAt,
        coverData = MangaCover(
            mangaId = mangaId,
            sourceId = source,
            isMangaFavorite = favorite,
            ogUrl = thumbnailUrl,
            lastModified = coverLastModified,
        ),
    )
}
