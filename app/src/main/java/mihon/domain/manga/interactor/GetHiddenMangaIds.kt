package mihon.domain.manga.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import tachiyomi.domain.category.interactor.GetCategories
import tachiyomi.domain.manga.interactor.GetLibraryManga
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/**
 * Resolves the set of manga ids that belong exclusively to hidden categories.
 *
 * Any screen that surfaces library or remote manga outside of the Library tab itself
 * (Dashboard's Continue Reading / Recently Read / fallback feed, and the Feed section
 * reused on both Discover and the Home dashboard) must exclude these ids before the
 * content is emitted to the UI, matching how the Library tab itself hides them.
 */
class GetHiddenMangaIds(
    private val getCategories: GetCategories = Injekt.get(),
    private val getLibraryManga: GetLibraryManga = Injekt.get(),
) {

    fun subscribe(): Flow<Set<Long>> {
        return combine(getCategories.subscribe(), getLibraryManga.subscribe()) { categories, libraryManga ->
            val hiddenCategoryIds = categories.filter { it.hidden }.map { it.id }.toSet()
            if (hiddenCategoryIds.isEmpty()) {
                emptySet()
            } else {
                libraryManga
                    .filter { it.categories.any { catId -> catId in hiddenCategoryIds } }
                    .map { it.manga.id }
                    .toSet()
            }
        }
    }

    suspend fun await(): Set<Long> = subscribe().first()
}
