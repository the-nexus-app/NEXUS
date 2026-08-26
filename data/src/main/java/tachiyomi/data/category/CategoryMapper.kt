package tachiyomi.data.category

import tachiyomi.domain.category.model.Category

object CategoryMapper {
    fun mapCategory(
        id: Long,
        name: String,
        order: Long,
        flags: Long,
        hidden: Long,
    ): Category {
        return Category(
            id = id,
            name = name,
            order = order,
            flags = flags,
            hidden = hidden == 1L,
        )
    }
}
