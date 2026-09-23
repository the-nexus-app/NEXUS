package eu.kanade.presentation.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.browse.FeedItem
import eu.kanade.presentation.browse.components.GlobalSearchResultItem
import eu.kanade.presentation.browse.key
import eu.kanade.presentation.dashboard.components.DashboardHeader
import eu.kanade.presentation.dashboard.components.DashboardSearchBar
import eu.kanade.presentation.dashboard.components.DashboardSectionHeader
import eu.kanade.presentation.dashboard.components.QuickActionsRow
import eu.kanade.presentation.dashboard.components.WelcomeBackCard
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.ui.browse.feed.FeedScreenState
import eu.kanade.tachiyomi.ui.dashboard.DashboardScreenModel
import kotlinx.collections.immutable.persistentListOf
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.model.SavedSearch
import tachiyomi.i18n.kmk.KMR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.EmptyScreenAction
import tachiyomi.presentation.core.screens.LoadingScreen

@Composable
fun DashboardScreen(
    state: DashboardScreenModel.State,
    feedState: FeedScreenState,
    getMangaState: @Composable (Manga) -> State<Manga>,
    onClickSearch: () -> Unit,
    onClickManga: (Manga) -> Unit,
    onClickSource: (Source) -> Unit,
    onClickSavedSearch: (SavedSearch, Source) -> Unit,
    onManageFeed: () -> Unit,
    onQuickActionDiscover: () -> Unit,
    onQuickActionDataStorage: () -> Unit,
) {
    // Wrapping in the shared Scaffold (no topBar/bottomBar of its own — HomeScreen's
    // outer Scaffold already provides the bottom nav) is what applies real status-bar
    // insets to contentPadding. Every other tab gets this for free through its own
    // AppBar; Dashboard has a custom header instead of an AppBar, so without this
    // Scaffold here, nothing ever accounted for the status bar and the header rendered
    // underneath it.
    Scaffold { contentPadding ->
        if (state.isLoading) {
            LoadingScreen(Modifier.fillMaxSize())
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
        ) {
            item(key = "header") {
                DashboardHeader()
            }

            item(key = "search") {
                DashboardSearchBar(onClick = onClickSearch)
                Spacer(Modifier.height(12.dp))
            }

            item(key = "welcome_back") {
                WelcomeBackCard(insights = state.readingInsights)
                Spacer(Modifier.height(20.dp))
            }

            item(key = "quick_actions") {
                QuickActionsRow(
                    onDiscover = onQuickActionDiscover,
                    onDataStorage = onQuickActionDataStorage,
                )
                Spacer(Modifier.height(20.dp))
            }

            item(key = "feed_header") {
                DashboardSectionHeader(
                    titleRes = KMR.strings.dashboard_feed,
                    actionLabelRes = KMR.strings.dashboard_feed_manage,
                    onAction = onManageFeed,
                )
                Spacer(Modifier.height(8.dp))
            }

            when {
                feedState.isLoading -> {
                    item(key = "feed_loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                feedState.isEmpty -> {
                    item(key = "feed_empty") {
                        EmptyScreen(
                            stringRes = KMR.strings.dashboard_feed_none_configured,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            actions = persistentListOf(
                                EmptyScreenAction(
                                    stringRes = KMR.strings.dashboard_feed_manage,
                                    icon = Icons.Outlined.Add,
                                    onClick = onManageFeed,
                                ),
                            ),
                        )
                    }
                }

                else -> {
                    // Each FeedItemUI is already exactly one source's configured feed -
                    // grouping by source falls straight out of reusing this model as-is,
                    // no separate grouping step needed. Hidden-category manga are
                    // stripped from each source's results before FeedItem ever renders
                    // them, so nothing hidden can flash in while scrolling.
                    items(
                        items = feedState.items.orEmpty(),
                        key = { it.feed.key },
                    ) { rawItem ->
                        val item = remember(rawItem, state.hiddenMangaIds) {
                            rawItem.copy(
                                results = rawItem.results?.filterNot { it.id in state.hiddenMangaIds },
                            )
                        }
                        GlobalSearchResultItem(
                            title = item.title,
                            subtitle = item.subtitle,
                            onClick = {
                                val savedSearch = item.savedSearch
                                val source = item.source
                                if (savedSearch != null && source != null) {
                                    onClickSavedSearch(savedSearch, source)
                                } else if (source != null) {
                                    onClickSource(source)
                                }
                            },
                        ) {
                            FeedItem(
                                item = item,
                                getMangaState = getMangaState,
                                onClickManga = onClickManga,
                                onLongClickManga = onClickManga,
                                selection = emptyList(),
                            )
                        }
                    }
                }
            }

            item(key = "bottom_spacer") {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
