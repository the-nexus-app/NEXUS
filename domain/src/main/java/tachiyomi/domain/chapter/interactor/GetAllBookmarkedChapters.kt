package tachiyomi.domain.chapter.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.chapter.model.BookmarkedChapterWithManga
import tachiyomi.domain.chapter.repository.ChapterRepository

class GetAllBookmarkedChapters(
    private val chapterRepository: ChapterRepository,
) {

    fun subscribe(): Flow<List<BookmarkedChapterWithManga>> {
        return chapterRepository.subscribeAllBookmarkedChapters()
            .catch {
                logcat(LogPriority.ERROR, it)
                emit(emptyList())
            }
    }
}