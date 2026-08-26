package tachiyomi.domain.history.model

import tachiyomi.domain.manga.interactor.GetCustomMangaInfo
import tachiyomi.domain.manga.model.MangaCover
import uy.kohesive.injekt.injectLazy
import java.util.Date

data class HistoryWithRelations(
    val id: Long,
    val chapterId: Long,
    val mangaId: Long,
    val ogTitle: String,
    val chapterNumber: Double,
    val read: Boolean,
    val lastPageRead: Long,
    val totalCountCalculated: Long,
    val readCountCalculated: Long,
    val readAt: Date?,
    val readDuration: Long,
    val coverData: MangaCover,
) {
    val title: String = customMangaManager.get(mangaId)?.title ?: ogTitle

    companion object {
        private val customMangaManager: GetCustomMangaInfo by injectLazy()
    }

    val unreadCount
        get() = totalCountCalculated - readCountCalculated
}
