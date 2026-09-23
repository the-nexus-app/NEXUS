package eu.kanade.tachiyomi.ui.browse.feed

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.browse.BrowseTabWrapper
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.ui.browse.BulkFavoriteScreenModel

/**
 * Full Feed screen, pushed from Home's "See all" action (Feed is no longer a Discover
 * sub-tab). Reuses [feedTab] and [FeedScreenModel] exactly as the former Browse tab did
 * - same dialogs, add-source flow, sort order screen, and bulk-select - none of that is
 * duplicated here.
 */
class FeedTabScreen : Screen() {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val feedScreenModel = rememberScreenModel { FeedScreenModel() }
        val bulkFavoriteScreenModel = rememberScreenModel { BulkFavoriteScreenModel() }
        BrowseTabWrapper(
            feedTab(feedScreenModel, bulkFavoriteScreenModel),
            onBackPressed = navigator::pop,
        )
    }
}
