package tachiyomi.domain.history.repository

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.history.model.History
import tachiyomi.domain.history.model.HistoryUpdate
import tachiyomi.domain.history.model.HistoryWithRelations

interface HistoryRepository {

    fun getHistory(
        query: String,
         -->
        unfinishedManga: Boolean?,
        unfinishedChapter: Boolean?,
        nonLibraryEntries: Boolean?,
         <--
    ): Flow<List<HistoryWithRelations>>

    suspend fun getLastHistory(): HistoryWithRelations?

    suspend fun getTotalReadDuration(): Long

    suspend fun getHistoryByMangaId(mangaId: Long): List<History>

     -->
    suspend fun resetHistory(historyIds: List<Long>)

    suspend fun resetHistoryByMangaIds(mangaIds: List<Long>)
     <--

    suspend fun deleteAllHistory(): Boolean

    suspend fun upsertHistory(historyUpdate: HistoryUpdate)

     -->
    suspend fun upsertHistory(historyUpdates: List<HistoryUpdate>)
     <--
}
