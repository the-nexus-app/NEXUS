package tachiyomi.domain.category.interactor

import tachiyomi.domain.category.model.Category
import tachiyomi.domain.category.model.CategoryUpdate
import tachiyomi.domain.category.repository.CategoryRepository
import tachiyomi.domain.library.model.LibraryGroup
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.domain.library.model.plus
import tachiyomi.domain.library.service.LibraryPreferences
import kotlin.random.Random

class SetSortModeForCategory(
    private val preferences: LibraryPreferences,
    private val categoryRepository: CategoryRepository,
) {

    suspend fun await(categoryId: Long?, type: LibrarySort.Type, direction: LibrarySort.Direction) {
        if (preferences.groupLibraryBy().get() != LibraryGroup.BY_DEFAULT) {
            preferences.sortingMode().set(LibrarySort(type, direction))
            return
        }
        val category = categoryId?.let { categoryRepository.get(it) }
        val flags = (category?.flags ?: 0) + type + direction
        if (type == LibrarySort.Type.Random) {
            preferences.randomSortSeed().set(Random.nextInt())
        }
        val categorizedSettings = preferences.categorizedDisplaySettings().get()
        if (category != null && categorizedSettings) {
            categoryRepository.updatePartial(
                CategoryUpdate(
                    id = category.id,
                    flags = flags,
                ),
            )
        } else {
            preferences.sortingMode().set(LibrarySort(type, direction))
            // NEXUS: with per-category sort settings enabled, a null category means there is
            // no row to write to (the merged "All Categories" search view has no category).
            // Blasting every category's flags here would silently reorder the whole library,
            // so only the global sort preference is updated.
            if (!categorizedSettings) {
                categoryRepository.updateAllFlags(flags)
            }
        }
    }

    suspend fun await(
        category: Category?,
        type: LibrarySort.Type,
        direction: LibrarySort.Direction,
    ) {
        await(category?.id, type, direction)
    }
}
