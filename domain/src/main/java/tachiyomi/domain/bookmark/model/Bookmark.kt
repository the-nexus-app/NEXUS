package tachiyomi.domain.bookmark.model

data class Bookmark(
    val id: Long,
    val chapterId: Long,
    val pageIndex: Int,
    val scrollPosition: Float?,
    val createdAt: Long,
    val note: String? = null,
)