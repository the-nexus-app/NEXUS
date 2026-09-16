package tachiyomi.domain.bookmark.model

import tachiyomi.domain.manga.model.MangaCover

/**
 * A manual page bookmark joined with enough chapter + manga info to render it in the
 * GLOBAL Bookmarked Pages list (across every manga, favorited or not).
 *
 * This is distinct from [BookmarkWithChapter], which is used by the per-manga bookmark
 * sheet and does not need a manga title/cover since it's always shown in the context of
 * a single, already-known manga.
 */
data class BookmarkWithManga(
    val id: Long,
    val chapterId: Long,
    val mangaId: Long,
    val pageIndex: Int,
    val scrollPosition: Float?,
    val createdAt: Long,
    val chapterName: String,
    val chapterNumber: Double,
    val mangaTitle: String,
    val coverData: MangaCover,
    val note: String? = null,
)
