package tachiyomi.domain.bookmark.model

/**
 * A manual bookmark joined with just enough chapter info to render it in a list
 * without a second query per row (mirrors HistoryWithRelations' shape/purpose).
 */
data class BookmarkWithChapter(
    val id: Long,
    val chapterId: Long,
    val mangaId: Long,
    val pageIndex: Int,
    val scrollPosition: Float?,
    val createdAt: Long,
    val chapterName: String,
    val chapterNumber: Double,
    val note: String? = null,
)