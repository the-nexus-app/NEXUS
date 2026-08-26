package tachiyomi.domain.library.model

sealed interface LibraryDisplayMode {

    data object CompactGrid : LibraryDisplayMode
    data object ComfortableGrid : LibraryDisplayMode

    data object ComfortableGridPanorama : LibraryDisplayMode

    data object List : LibraryDisplayMode
    data object CoverOnlyGrid : LibraryDisplayMode

    object Serializer {
        fun deserialize(serialized: String): LibraryDisplayMode {
            return LibraryDisplayMode.deserialize(serialized)
        }

        fun serialize(value: LibraryDisplayMode): String {
            return value.serialize()
        }
    }

    companion object {
        val values by lazy { setOf(CompactGrid, ComfortableGrid, ComfortableGridPanorama, List, CoverOnlyGrid) }
        val default = CompactGrid

        fun deserialize(serialized: String): LibraryDisplayMode {
            return when (serialized) {
                "COMFORTABLE_GRID" -> ComfortableGrid
                "COMFORTABLE_GRID_PANORAMA" -> ComfortableGridPanorama
                "COMPACT_GRID" -> CompactGrid
                "COVER_ONLY_GRID" -> CoverOnlyGrid
                "LIST" -> List
                else -> default
            }
        }
    }

    fun serialize(): String {
        return when (this) {
            ComfortableGrid -> "COMFORTABLE_GRID"
            ComfortableGridPanorama -> "COMFORTABLE_GRID_PANORAMA"
            CompactGrid -> "COMPACT_GRID"
            CoverOnlyGrid -> "COVER_ONLY_GRID"
            List -> "LIST"
        }
    }
}
