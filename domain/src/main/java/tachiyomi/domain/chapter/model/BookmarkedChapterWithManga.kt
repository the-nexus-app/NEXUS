package tachiyomi.domain.chapter.model

import tachiyomi.domain.manga.interactor.GetCustomMangaInfo
import tachiyomi.domain.manga.model.MangaCover
import uy.kohesive.injekt.injectLazy

data class BookmarkedChapterWithManga(
    val mangaId: Long,
    val ogMangaTitle: String,
    val chapterId: Long,
    val chapterName: String,
    val chapterNumber: Double,
    val scanlator: String?,
    val read: Boolean,
    val lastPageRead: Long,
    val sourceId: Long,
    val dateUpload: Long,
    val bookmarkedAt: Long,
    val coverData: MangaCover,
) {
    val mangaTitle: String = getCustomMangaInfo.get(mangaId)?.title ?: ogMangaTitle

    companion object {
        private val getCustomMangaInfo: GetCustomMangaInfo by injectLazy()
    }
}