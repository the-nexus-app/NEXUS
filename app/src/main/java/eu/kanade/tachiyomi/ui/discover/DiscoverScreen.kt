package eu.kanade.tachiyomi.ui.discover

import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.model.rememberScreenModel
import eu.kanade.presentation.components.TabbedScreen
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.data.connections.discord.DiscordRPCService
import eu.kanade.tachiyomi.data.connections.discord.DiscordScreen
import eu.kanade.tachiyomi.ui.browse.BulkFavoriteScreenModel
import eu.kanade.tachiyomi.ui.browse.extension.ExtensionsScreenModel
import eu.kanade.tachiyomi.ui.browse.extension.extensionsTab
import eu.kanade.tachiyomi.ui.browse.feed.FeedScreenModel
import eu.kanade.tachiyomi.ui.browse.migration.sources.migrateSourceTab
import eu.kanade.tachiyomi.ui.browse.source.sourcesTab
import eu.kanade.tachiyomi.ui.main.MainActivity
import kotlinx.collections.immutable.persistentListOf
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.i18n.MR

/**
 * Groups the former Browse tab's functionality - Sources, Extensions, Migrate - as a
 * screen pushed from Home/More rather than a permanent bottom-nav tab.
 *
 * Feed is deliberately NOT included here anymore - it now lives on the Home dashboard
 * instead (see [eu.kanade.tachiyomi.ui.dashboard.DashboardTab] and
 * [eu.kanade.tachiyomi.ui.browse.feed.FeedTabScreen] for its standalone view).
 *
 * Deliberately reuses [sourcesTab], [extensionsTab], [migrateSourceTab] and their
 * screen models exactly as [eu.kanade.tachiyomi.ui.browse.BrowseTab] did, so none of the
 * source-loading, extension-management, or migration logic is duplicated.
 *
 * [TabbedScreen] still requires a [FeedScreenModel] and [BulkFavoriteScreenModel]
 * unconditionally - its bulk "Select all" / "Reverse selection" actions read from
 * `feedScreenModel.state` regardless of which tab is active. Since Feed is no longer one
 * of [tabs], those two actions have no meaningful selection source while a Sources,
 * Extensions, or Migrate multi-select is active here. That's an existing coupling inside
 * [TabbedScreen] itself, not something introduced by removing the Feed tab, and fixing
 * it would mean changing shared code used by every tab, not just Discover - flagging
 * rather than changing it silently.
 */
class DiscoverScreen(private val openExtensions: Boolean = false) : Screen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current

        val extensionsScreenModel = rememberScreenModel { ExtensionsScreenModel() }
        val extensionsState by extensionsScreenModel.state.collectAsState()

        val feedScreenModel = rememberScreenModel { FeedScreenModel() }
        val bulkFavoriteScreenModel = rememberScreenModel { BulkFavoriteScreenModel() }

        val tabs = persistentListOf(
            sourcesTab(),
            extensionsTab(extensionsScreenModel),
            migrateSourceTab(),
        )

        val extensionsTabIndex = tabs.size - 2 // Extensions always sits before Migrate
        val pagerState = rememberPagerState { tabs.size }

        TabbedScreen(
            titleRes = MR.strings.browse,
            tabs = tabs,
            state = pagerState,
            searchQuery = extensionsState.searchQuery,
            onChangeSearchQuery = extensionsScreenModel::search,
            feedScreenModel = feedScreenModel,
            bulkFavoriteScreenModel = bulkFavoriteScreenModel,
        )

        LaunchedEffect(openExtensions) {
            if (openExtensions) {
                pagerState.scrollToPage(extensionsTabIndex)
            }
        }

        LaunchedEffect(Unit) {
            (context as? MainActivity)?.ready = true

            // AM (DISCORD)
            with(DiscordRPCService) {
                discordScope.launchIO { setScreen(context, DiscordScreen.BROWSE) }
            }
            // AM (DISCORD)
        }
    }
}
