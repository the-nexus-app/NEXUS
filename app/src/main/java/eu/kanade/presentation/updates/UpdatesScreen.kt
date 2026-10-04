package eu.kanade.presentation.updates

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.FlipToBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastAll
import androidx.compose.ui.util.fastAny
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.components.AppBarTitle
import eu.kanade.presentation.manga.components.ChapterDownloadAction
import eu.kanade.presentation.manga.components.MangaBottomActionMenu
import eu.kanade.presentation.updates.components.UpdatesControlRow
import eu.kanade.tachiyomi.data.download.model.Download
import eu.kanade.tachiyomi.ui.updates.UpdatesItem
import eu.kanade.tachiyomi.ui.updates.UpdatesScreenModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.components.Pill
import tachiyomi.presentation.core.components.material.PullRefresh
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.theme.active
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

@Composable
fun UpdateScreen(
    state: UpdatesScreenModel.State,
    snackbarHostState: SnackbarHostState,
    lastUpdated: Long,
    // SY -->
    preserveReadingPosition: Boolean,
    // SY <--
    onClickCover: (UpdatesItem) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onInvertSelection: () -> Unit,
    onCalendarClicked: () -> Unit,
    onUpdateLibrary: () -> Boolean,
    onDownloadChapter: (List<UpdatesItem>, ChapterDownloadAction) -> Unit,
    onMultiBookmarkClicked: (List<UpdatesItem>, bookmark: Boolean) -> Unit,
    onMultiMarkAsReadClicked: (List<UpdatesItem>, read: Boolean) -> Unit,
    onMultiDeleteClicked: (List<UpdatesItem>) -> Unit,
    // KMK -->
    updateSwipeStartAction: LibraryPreferences.ChapterSwipeAction,
    updateSwipeEndAction: LibraryPreferences.ChapterSwipeAction,
    onUpdateSwipe: (UpdatesItem, LibraryPreferences.ChapterSwipeAction) -> Unit,
    // KMK <--
    // NXS -->
    showHiddenUpdates: Boolean,
    onToggleHiddenUpdates: () -> Unit,
    onUpdateSelected: (
        UpdatesItem, /* KMK*/
        UpdatesScreenModel.UpdateSelectionOptions, /* KMK*/
    ) -> Unit,
    // NXS <--
    onOpenChapter: (UpdatesItem) -> Unit,
    onFilterClicked: () -> Unit,
    hasActiveFilters: Boolean,
    // KMK -->
    usePanoramaCover: Boolean,
    collapseToggle: (key: String) -> Unit,
    // KMK <--
) {
    BackHandler(enabled = state.selectionMode, onBack = { onSelectAll(false) })

    Scaffold(
        topBar = { scrollBehavior ->
            UpdatesAppBar(
                onCalendarClicked = { onCalendarClicked() },
                onUpdateLibrary = { onUpdateLibrary() },
                onFilterClicked = { onFilterClicked() },
                hasFilters = hasActiveFilters,
                actionModeCounter = state.selected.size,
                onSelectAll = { onSelectAll(true) },
                onInvertSelection = { onInvertSelection() },
                onCancelActionMode = { onSelectAll(false) },
                scrollBehavior = scrollBehavior,
                // NXS -->
                showHiddenUpdates = showHiddenUpdates,
                onToggleHiddenUpdates = onToggleHiddenUpdates,
                // NXS <--
            )
        },
        bottomBar = {
            UpdatesBottomBar(
                selected = state.selected,
                onDownloadChapter = onDownloadChapter,
                onMultiBookmarkClicked = onMultiBookmarkClicked,
                onMultiMarkAsReadClicked = onMultiMarkAsReadClicked,
                onMultiDeleteClicked = onMultiDeleteClicked,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        // NXS -->
        // Recomputed whenever the update items, the hidden manga IDs, or the Hidden Updates
        // lock state change, so toggling the lock immediately reveals/hides hidden-category
        // updates without needing to navigate away and back. When everything visible ends up
        // filtered out (e.g. every update is hidden-category while locked), uiModels is empty
        // and the empty-state screen below is shown instead of a blank list.
        val uiModels = remember(state.items, state.showHiddenUpdates, state.hiddenMangaIds) {
            state.getUiModel()
        }
        // NXS <--
        when {
            state.isLoading -> LoadingScreen(Modifier.padding(contentPadding))
            // NXS -->
            uiModels.isEmpty() -> EmptyScreen(
                // NXS <--
                stringRes = MR.strings.information_no_recent,
                modifier = Modifier.padding(contentPadding),
            )
            else -> {
                val scope = rememberCoroutineScope()
                var isRefreshing by remember { mutableStateOf(false) }

                PullRefresh(
                    refreshing = isRefreshing,
                    onRefresh = {
                        val started = onUpdateLibrary()
                        if (!started) return@PullRefresh
                        scope.launch {
                            // Fake refresh status but hide it after a second as it's a long running task
                            isRefreshing = true
                            delay(1.seconds)
                            isRefreshing = false
                        }
                    },
                    enabled = !state.selectionMode,
                    indicatorPadding = contentPadding,
                ) {
                    FastScrollLazyColumn(
                        contentPadding = contentPadding,
                    ) {
                        updatesUiItems(
                            // KMK -->
                            uiModels = uiModels,
                            expandedState = state.expandedState,
                            collapseToggle = collapseToggle,
                            usePanoramaCover = usePanoramaCover,
                            // KMK <--
                            selectionMode = state.selectionMode,
                            // SY -->
                            preserveReadingPosition = preserveReadingPosition,
                            // SY <--
                            onUpdateSelected = onUpdateSelected,
                            onClickCover = onClickCover,
                            onClickUpdate = onOpenChapter,
                            onDownloadChapter = onDownloadChapter,
                            // KMK -->
                            updateSwipeStartAction = updateSwipeStartAction,
                            updateSwipeEndAction = updateSwipeEndAction,
                            onUpdateSwipe = onUpdateSwipe,
                            // KMK <--
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdatesAppBar(
    onCalendarClicked: () -> Unit,
    onUpdateLibrary: () -> Unit,
    onFilterClicked: () -> Unit,
    hasFilters: Boolean,
    // For action mode
    actionModeCounter: Int,
    onSelectAll: () -> Unit,
    onInvertSelection: () -> Unit,
    onCancelActionMode: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    // NXS -->
    showHiddenUpdates: Boolean,
    onToggleHiddenUpdates: () -> Unit,
    // NXS <--
    modifier: Modifier = Modifier,
) {
    // NXS -->
    val isActionMode = actionModeCounter > 0
    // NXS <--
    AppBar(
        modifier = modifier,
        // NXS -->
        titleContent = {
            if (isActionMode) {
                AppBarTitle(actionModeCounter.toString())
            } else {
                AppBarTitle(
                    title = stringResource(MR.strings.label_recent_updates),
                    titleStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        // NXS <--
                    ),
                    // NXS -->
                    titleColor = MaterialTheme.colorScheme.primary,
                )
            }
            // NXS <--
        },
        // NXS -->
        actions = {
            if (isActionMode) {
                AppBarActions(
                    persistentListOf(
                        AppBar.Action(
                            title = stringResource(MR.strings.action_select_all),
                            icon = Icons.Outlined.SelectAll,
                            onClick = onSelectAll,
                        ),
                        AppBar.Action(
                            title = stringResource(MR.strings.action_select_inverse),
                            icon = Icons.Outlined.FlipToBack,
                            onClick = onInvertSelection,
                        ),
                        // NXS <--
                    ),
                    // NXS -->
                )
            } else {
                AppBarActions(
                    persistentListOf(
                        AppBar.Action(
                            title = stringResource(MR.strings.action_filter),
                            icon = Icons.Outlined.FilterList,
                            iconTint = if (hasFilters) MaterialTheme.colorScheme.active else LocalContentColor.current,
                            onClick = onFilterClicked,
                        ),
                        AppBar.Action(
                            title = stringResource(MR.strings.action_view_upcoming),
                            icon = Icons.Outlined.CalendarMonth,
                            onClick = onCalendarClicked,
                        ),
                        AppBar.Action(
                            title = "Show hidden updates",
                            icon = if (showHiddenUpdates) Icons.Outlined.LockOpen else Icons.Outlined.Lock,
                            onClick = onToggleHiddenUpdates,
                        ),
                        AppBar.Action(
                            title = stringResource(MR.strings.action_update_library),
                            icon = Icons.Outlined.Refresh,
                            onClick = onUpdateLibrary,
                        ),
                        // NXS <--
                    ),
                    // NXS -->
                )
            }
            // NXS <--
        },
        // NXS -->
        isActionMode = isActionMode,
        onCancelActionMode = onCancelActionMode,
        // NXS <--
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun UpdatesBottomBar(
    selected: List<UpdatesItem>,
    onDownloadChapter: (List<UpdatesItem>, ChapterDownloadAction) -> Unit,
    onMultiBookmarkClicked: (List<UpdatesItem>, bookmark: Boolean) -> Unit,
    onMultiMarkAsReadClicked: (List<UpdatesItem>, read: Boolean) -> Unit,
    onMultiDeleteClicked: (List<UpdatesItem>) -> Unit,
) {
    MangaBottomActionMenu(
        visible = selected.isNotEmpty(),
        modifier = Modifier.fillMaxWidth(),
        onBookmarkClicked = {
            onMultiBookmarkClicked.invoke(selected, true)
        }.takeIf { selected.fastAny { !it.update.bookmark } },
        onRemoveBookmarkClicked = {
            onMultiBookmarkClicked.invoke(selected, false)
        }.takeIf { selected.fastAll { it.update.bookmark } },
        onMarkAsReadClicked = {
            onMultiMarkAsReadClicked(selected, true)
        }.takeIf { selected.fastAny { !it.update.read } },
        onMarkAsUnreadClicked = {
            onMultiMarkAsReadClicked(selected, false)
        }.takeIf { selected.fastAny { it.update.read || it.update.lastPageRead > 0L } },
        onDownloadClicked = {
            onDownloadChapter(selected, ChapterDownloadAction.START)
        }.takeIf {
            selected.fastAny { it.downloadStateProvider() != Download.State.DOWNLOADED }
        },
        onDeleteClicked = {
            onMultiDeleteClicked(selected)
        }.takeIf { selected.fastAny { it.downloadStateProvider() == Download.State.DOWNLOADED } },
    )
}

sealed interface UpdatesUiModel {
    data class Header(val date: LocalDate, val mangaCount: Int) : UpdatesUiModel
    open class Item(open val item: UpdatesItem, open val isExpandable: Boolean = false) : UpdatesUiModel

    // KMK -->
    /** The first [Item] in a group of chapters from same manga */
    data class Leader(override val item: UpdatesItem, override val isExpandable: Boolean) : Item(item)
    // KMK <--
}
