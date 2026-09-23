package eu.kanade.tachiyomi.ui.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import eu.kanade.presentation.dashboard.DashboardScreen
import eu.kanade.presentation.util.Tab
import eu.kanade.tachiyomi.data.connections.discord.DiscordRPCService
import eu.kanade.tachiyomi.data.connections.discord.DiscordScreen
import eu.kanade.tachiyomi.ui.browse.feed.FeedScreenModel
import eu.kanade.tachiyomi.ui.browse.feed.FeedTabScreen
import eu.kanade.tachiyomi.ui.browse.source.browse.BrowseSourceScreen
import eu.kanade.tachiyomi.ui.browse.source.globalsearch.GlobalSearchScreen
import eu.kanade.tachiyomi.ui.discover.DiscoverScreen
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import eu.kanade.tachiyomi.ui.setting.SettingsScreen
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.domain.source.interactor.GetRemoteManga
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

data object DashboardTab : Tab {
    private fun readResolve(): Any = DashboardTab

    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current.key == key
            return TabOptions(
                index = 0u,
                title = stringResource(MR.strings.label_home),
                icon = rememberVectorPainter(if (isSelected) Icons.Filled.Home else Icons.Outlined.Home),
            )
        }

    override suspend fun onReselect(navigator: Navigator) {
        navigator.push(GlobalSearchScreen())
    }

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { DashboardScreenModel() }
        val state by screenModel.state.collectAsState()

        val feedScreenModel = rememberScreenModel { FeedScreenModel() }
        val feedState by feedScreenModel.state.collectAsState()

        DashboardScreen(
            state = state,
            feedState = feedState,
            getMangaState = { manga -> feedScreenModel.getManga(initialManga = manga) },
            onClickSearch = { navigator.push(GlobalSearchScreen()) },
            onClickManga = { manga -> navigator.push(MangaScreen(manga.id, fromSource = true)) },
            onClickSource = { source ->
                feedScreenModel.sourcePreferences.lastUsedSource().set(source.id)
                navigator.push(
                    BrowseSourceScreen(
                        source.id,
                        listingQuery = if (!source.supportsLatest) {
                            GetRemoteManga.QUERY_POPULAR
                        } else {
                            GetRemoteManga.QUERY_LATEST
                        },
                    ),
                )
            },
            onClickSavedSearch = { savedSearch, source ->
                feedScreenModel.sourcePreferences.lastUsedSource().set(savedSearch.source)
                navigator.push(
                    BrowseSourceScreen(
                        source.id,
                        listingQuery = null,
                        savedSearch = savedSearch.id,
                    ),
                )
            },
            onManageFeed = { navigator.push(FeedTabScreen()) },
            onQuickActionDiscover = { navigator.push(DiscoverScreen()) },
            onQuickActionDataStorage = {
                navigator.push(SettingsScreen(SettingsScreen.Destination.DataAndStorage))
            },
        )

        // AM (DISCORD)
        LaunchedEffect(Unit) {
            with(DiscordRPCService) {
                discordScope.launchIO { setScreen(context, DiscordScreen.APP) }
            }
        }
        // AM (DISCORD)
    }
}
