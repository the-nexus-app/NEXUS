package eu.kanade.presentation.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.more.components.NexusDownloadQueueStatus
import eu.kanade.presentation.more.components.NexusFeatureBlock
import eu.kanade.presentation.more.components.NexusFeatureTile
import eu.kanade.presentation.more.components.NexusMoreHeader
import eu.kanade.presentation.more.components.NexusNavigationRow
import eu.kanade.presentation.more.components.NexusSectionLabel
import eu.kanade.presentation.more.components.NexusToggleRow
import eu.kanade.tachiyomi.ui.more.DownloadQueueState
import exh.pref.DelegateSourcePreferences
import exh.source.ExhPreferences
import tachiyomi.core.common.Constants
import tachiyomi.i18n.MR
import tachiyomi.i18n.kmk.KMR
import tachiyomi.i18n.sy.SYMR
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

// NXS -->
enum class BookmarkSelection {
    Chapters,
    Pages,
    ;

    companion object {
        fun fromOrdinal(ordinal: Int): BookmarkSelection = entries.getOrNull(ordinal) ?: Chapters
    }
}

// NXS <--
@Composable
fun MoreScreen(
    downloadQueueStateProvider: () -> DownloadQueueState,
    downloadedOnly: Boolean,
    onDownloadedOnlyChange: (Boolean) -> Unit,
    incognitoMode: Boolean,
    onIncognitoModeChange: (Boolean) -> Unit,
    // SY -->
    showNavUpdates: Boolean,
    showNavHistory: Boolean,
    // SY <--
    onClickDownloadQueue: () -> Unit,
    onClickCategories: () -> Unit,
    onClickStats: () -> Unit,
    onClickDataAndStorage: () -> Unit,
    onClickSettings: () -> Unit,
    onClickAbout: () -> Unit,
    onClickBatchAdd: () -> Unit,
    onClickUpdates: () -> Unit,
    onClickHistory: () -> Unit,
    // NXS -->
    onClickDiscover: () -> Unit,
    extensionUpdatesCount: Int,
    onClickBookmarkedChapters: () -> Unit,
    onClickBookmarkedPages: () -> Unit,
    // NXS <--
    // KMK -->
    onClickLibraryUpdateErrors: () -> Unit,
    // KMK <--
) {
    val uriHandler = LocalUriHandler.current
    // SY -->
    val exhPreferences = remember { Injekt.get<ExhPreferences>() }
    val delegateSourcePreferences = remember { Injekt.get<DelegateSourcePreferences>() }
    // SY <--

    // NXS -->
    // State for the bookmark type selection dialog
    var showBookmarkDialog by remember { mutableStateOf(false) }
    var selectedBookmarkType by remember { mutableStateOf<BookmarkSelection?>(null) }

    // Show dialog when bookmarks block is clicked; navigation happens after confirmation
    if (showBookmarkDialog) {
        AlertDialog(
            onDismissRequest = {
                showBookmarkDialog = false
                selectedBookmarkType = null
            },
            title = { Text(text = stringResource(KMR.strings.bookmarks_title)) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedBookmarkType == BookmarkSelection.Chapters,
                            onClick = { selectedBookmarkType = BookmarkSelection.Chapters },
                        )
                        Text(
                            text = stringResource(MR.strings.label_bookmarked_chapters),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedBookmarkType == BookmarkSelection.Pages,
                            onClick = { selectedBookmarkType = BookmarkSelection.Pages },
                        )
                        Text(
                            text = stringResource(MR.strings.label_bookmarked_pages),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (selectedBookmarkType != null) {
                            showBookmarkDialog = false
                            when (selectedBookmarkType) {
                                BookmarkSelection.Chapters -> onClickBookmarkedChapters()
                                BookmarkSelection.Pages -> onClickBookmarkedPages()
                                else -> { /* null case is guarded above */ }
                            }
                        }
                    },
                    enabled = selectedBookmarkType != null,
                ) {
                    Text(text = stringResource(KMR.strings.action_enter))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBookmarkDialog = false }) {
                    Text(text = stringResource(MR.strings.action_cancel))
                }
            },
        )
    }
    // NXS <--
    Scaffold { contentPadding ->
        ScrollbarLazyColumn(
            // NXS -->
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
                bottom = contentPadding.calculateBottomPadding() + 16.dp,
            ),
            // NXS <--
        ) {
            item {
                // NXS -->
                NexusMoreHeader(
                    title = stringResource(KMR.strings.more_screen_title),
                    subtitle = stringResource(KMR.strings.more_screen_subtitle),
                )
                // NXS <--
            }
            // NXS -->

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // NXS <--
            item {
                // NXS -->
                val (count, statusText, isActive) = when (val state = downloadQueueStateProvider()) {
                    DownloadQueueState.Stopped -> Triple(
                        0,
                        stringResource(KMR.strings.download_queue_stopped),
                        false,
                    )
                    is DownloadQueueState.Paused -> Triple(
                        state.pending,
                        stringResource(KMR.strings.download_queue_paused_count, state.pending),
                        false,
                    )
                    is DownloadQueueState.Downloading -> Triple(
                        state.pending,
                        stringResource(KMR.strings.download_queue_downloading_count, state.pending),
                        true,
                    )
                }
                NexusDownloadQueueStatus(
                    count = count,
                    statusText = statusText,
                    isActive = isActive,
                    onClick = onClickDownloadQueue,
                    // NXS <--
                )
            }
            // NXS -->

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // NXS <--
            item {
                // NXS -->
                NexusSectionLabel(title = stringResource(KMR.strings.library_tools_title))
            }

            item {
                NexusFeatureBlock(
                    title = stringResource(KMR.strings.bookmarks_title),
                    subtitle = stringResource(KMR.strings.bookmarks_subtitle),
                    onClick = { showBookmarkDialog = true },
                    // NXS <--
                )
            }

            // NXS -->
            item { Spacer(modifier = Modifier.height(8.dp)) }
            // NXS <--

            // SY -->
            // NXS -->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NexusFeatureTile(
                        title = stringResource(KMR.strings.categories_title),
                        subtitle = stringResource(KMR.strings.categories_subtitle),
                        onClick = onClickCategories,
                        modifier = Modifier.weight(1f),
                        // NXS <--
                    )
                    // NXS -->
                    NexusFeatureTile(
                        title = stringResource(KMR.strings.statistics_title),
                        subtitle = stringResource(KMR.strings.statistics_subtitle),
                        onClick = onClickStats,
                        modifier = Modifier.weight(1f),
                        // NXS <--
                    )
                }
            }
            // SY <--

            // NXS -->
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // NXS <--
            item {
                // NXS -->
                NexusSectionLabel(title = stringResource(KMR.strings.tools_title))
                // NXS <--
            }

            item {
                // NXS -->
                NexusNavigationRow(
                    title = stringResource(KMR.strings.discover_title),
                    subtitle = stringResource(KMR.strings.discover_subtitle),
                    onClick = onClickDiscover,
                    // NXS <--
                )
            }

            item {
                // NXS -->
                NexusNavigationRow(
                    title = stringResource(KMR.strings.data_storage_title),
                    subtitle = stringResource(KMR.strings.data_storage_subtitle),
                    onClick = onClickDataAndStorage,
                    // NXS <--
                )
            }
            // NXS -->

            if (exhPreferences.isHentaiEnabled().get() ||
                delegateSourcePreferences.delegateSources().get()
            ) {
                item {
                    NexusNavigationRow(
                        title = stringResource(SYMR.strings.eh_batch_add),
                        onClick = onClickBatchAdd,
                    )
                }
                // NXS <--
            }

            item {
                // NXS -->
                NexusNavigationRow(
                    title = stringResource(KMR.strings.option_label_library_update_errors),
                    onClick = onClickLibraryUpdateErrors,
                    // NXS <--
                )
            }
            // SY -->
            // NXS -->

            // ========== RECENT UPDATES/HISTORY (CONDITIONAL) ==========
            if (!showNavUpdates || !showNavHistory) {
                item { Spacer(modifier = Modifier.height(16.dp)) }

                // NXS <--
                item {
                    // NXS -->
                    NexusSectionLabel(title = stringResource(KMR.strings.section_recent))
                }
                if (!showNavUpdates) {
                    item {
                        NexusNavigationRow(
                            title = stringResource(MR.strings.label_recent_updates),
                            onClick = onClickUpdates,
                        )
                    }
                }
                if (!showNavHistory) {
                    item {
                        NexusNavigationRow(
                            title = stringResource(MR.strings.label_recent_manga),
                            onClick = onClickHistory,
                        )
                    }
                    // NXS <--
                }
            }
            // SY <--

            // NXS -->
            item { Spacer(modifier = Modifier.height(16.dp)) }
            // NXS <--

            item {
                // NXS -->
                NexusSectionLabel(title = stringResource(KMR.strings.reading_mode_title))
                // NXS <--
            }

            item {
                // NXS -->
                NexusToggleRow(
                    title = stringResource(MR.strings.label_downloaded_only),
                    subtitle = stringResource(KMR.strings.downloaded_only_subtitle),
                    checked = downloadedOnly,
                    onCheckedChange = onDownloadedOnlyChange,
                    // NXS <--
                )
            }

            item {
                // NXS -->
                NexusToggleRow(
                    title = stringResource(MR.strings.pref_incognito_mode),
                    subtitle = stringResource(KMR.strings.incognito_mode_subtitle),
                    checked = incognitoMode,
                    onCheckedChange = onIncognitoModeChange,
                    // NXS <--
                )
            }
            // NXS -->

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                NexusSectionLabel(title = stringResource(KMR.strings.application_title))
            }

            // NXS <--
            // KMK -->
            item {
                // NXS -->
                NexusNavigationRow(
                    title = stringResource(MR.strings.label_settings),
                    onClick = onClickSettings,
                )
                // NXS <--
            }
            // KMK <--

            // NXS -->
            item {
                NexusNavigationRow(
                    title = stringResource(MR.strings.pref_category_about),
                    onClick = onClickAbout,
                    // NXS <--
                )
                // NXS -->
            }

            item {
                NexusNavigationRow(
                    title = stringResource(MR.strings.label_help),
                    onClick = { uriHandler.openUri(Constants.URL_HELP) },
                    // NXS <--
                )
            }
        }
    }
}
