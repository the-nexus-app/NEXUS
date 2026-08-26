package eu.kanade.presentation.history.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import tachiyomi.domain.history.model.HistoryWithRelations
import java.util.Date

internal class HistoryWithRelationsProvider : PreviewParameterProvider<HistoryWithRelations> {

    private val simple = HistoryWithRelations(
        id = 1L,
        chapterId = 2L,
        mangaId = 3L,
        ogTitle = "Test Title",
        chapterNumber = 10.2,
        read = true,
        lastPageRead = 5,
        totalCountCalculated = 5L,
        readCountCalculated = 3L,
        readAt = Date(1697247357L),
        readDuration = 123L,
        coverData = tachiyomi.domain.manga.model.MangaCover(
            mangaId = 3L,
            sourceId = 4L,
            isMangaFavorite = false,
            ogUrl = "https://example.com/cover.png",
            lastModified = 5L,
        ),
    )

    private val historyWithoutReadAt = HistoryWithRelations(
        id = 1L,
        chapterId = 2L,
        mangaId = 3L,
        ogTitle = "Test Title",
        chapterNumber = 10.2,
        read = false,
        lastPageRead = 5,
        totalCountCalculated = 5L,
        readCountCalculated = 3L,
        readAt = null,
        readDuration = 123L,
        coverData = tachiyomi.domain.manga.model.MangaCover(
            mangaId = 3L,
            sourceId = 4L,
            isMangaFavorite = false,
            ogUrl = "https://example.com/cover.png",
            lastModified = 5L,
        ),
    )

    private val historyWithNegativeChapterNumber = HistoryWithRelations(
        id = 1L,
        chapterId = 2L,
        mangaId = 3L,
        ogTitle = "Test Title",
        chapterNumber = -2.0,
        read = true,
        lastPageRead = 5,
        totalCountCalculated = 5L,
        readCountCalculated = 3L,
        readAt = Date(1697247357L),
        readDuration = 123L,
        coverData = tachiyomi.domain.manga.model.MangaCover(
            mangaId = 3L,
            sourceId = 4L,
            isMangaFavorite = false,
            ogUrl = "https://example.com/cover.png",
            lastModified = 5L,
        ),
    )

    override val values: Sequence<HistoryWithRelations>
        get() = sequenceOf(simple, historyWithoutReadAt, historyWithNegativeChapterNumber)
}
