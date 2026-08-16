package eu.kanade.tachiyomi.util.chapter

import eu.kanade.domain.chapter.model.applyFilters
import eu.kanade.tachiyomi.data.download.DownloadManager
import eu.kanade.tachiyomi.ui.manga.ChapterList
import exh.source.isEhBasedManga
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.manga.model.Manga

/**
 * Gets next unread chapter with filters and sorting applied
 */
fun List<Chapter>.getNextUnread(
    manga: Manga,
    downloadManager: DownloadManager,
     -->
    mergedManga: Map<Long, Manga>,
     <--
): Chapter? {
    return applyFilters(manga, downloadManager/* SY --> */, mergedManga/* SY <-- */).let { chapters ->
         -->
        if (manga.isEhBasedManga()) {
            return@let if (manga.sortDescending()) {
                chapters.firstOrNull()?.takeUnless { it.read }
            } else {
                chapters.lastOrNull()?.takeUnless { it.read }
            }
        }
         <--
        if (manga.sortDescending()) {
            chapters.findLast { !it.read }
        } else {
            chapters.find { !it.read }
        }
    }
}

/**
 * Gets next unread chapter with filters and sorting applied
 */
fun List<ChapterList.Item>.getNextUnread(manga: Manga): Chapter? {
    return applyFilters(manga).let { chapters ->
         -->
        if (manga.isEhBasedManga()) {
            return@let if (manga.sortDescending()) {
                chapters.firstOrNull()?.takeUnless { it.chapter.read }
            } else {
                chapters.lastOrNull()?.takeUnless { it.chapter.read }
            }
        }
         <--
        if (manga.sortDescending()) {
            chapters.findLast { !it.chapter.read }
        } else {
            chapters.find { !it.chapter.read }
        }
    }?.chapter
}
