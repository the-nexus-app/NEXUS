package tachiyomi.domain.updates.model

import tachiyomi.domain.manga.interactor.GetCustomMangaInfo
import tachiyomi.domain.manga.model.MangaCover
import uy.kohesive.injekt.injectLazy

data class UpdatesWithRelations(
    val mangaId: Long,
    val ogMangaTitle: String,
    val chapterId: Long,
    val chapterName: String,
    val scanlator: String?,
    val chapterUrl: String,
    val read: Boolean,
    val bookmark: Boolean,
    val lastPageRead: Long,
    val sourceId: Long,
    val dateFetch: Long,
    val coverData: MangaCover,
) {
    val mangaTitle: String = getCustomMangaInfo.get(mangaId)?.title ?: ogMangaTitle

    companion object {
        private val getCustomMangaInfo: GetCustomMangaInfo by injectLazy()
    }
}
