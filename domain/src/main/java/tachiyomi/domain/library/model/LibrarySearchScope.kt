package tachiyomi.domain.library.model

/**
 * Determines which categories a Library search query is matched against.
 *
 * [ALL_CATEGORIES] is the default: search results from every visible category are
 * merged into a single, deduplicated list (see `LibraryScreenModel`'s grouping
 * pipeline). Hidden categories are unaffected by this - they are excluded the same
 * way they already are for normal, non-search browsing.
 *
 * [CURRENT_CATEGORY] preserves the pre-existing behavior: the search query only
 * narrows down whichever category tab is currently active, and other tabs keep
 * showing their own (still search-filtered) results independently.
 */
enum class LibrarySearchScope {
    ALL_CATEGORIES,
    CURRENT_CATEGORY,
}
