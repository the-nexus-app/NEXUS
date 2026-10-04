package eu.kanade.presentation.library.components

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.FlipToBack
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.components.AppBarTitle
import kotlinx.collections.immutable.persistentListOf
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun LibraryToolbar(
    selectedCount: Int,
    onClickUnselectAll: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
    // NXS -->
    onClickControls: () -> Unit,
    // NXS <--
    onClickRefresh: () -> Unit,
    onClickGlobalUpdate: () -> Unit,
    onClickOpenRandomManga: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
    onInvalidateDownloadCache: (Context) -> Unit,
) = when {
    selectedCount > 0 -> LibrarySelectionToolbar(
        selectedCount = selectedCount,
        onClickUnselectAll = onClickUnselectAll,
        onClickSelectAll = onClickSelectAll,
        onClickInvertSelection = onClickInvertSelection,
    )
    else -> LibraryRegularToolbar(
        // NXS -->
        onClickControls = onClickControls,
        // NXS <--
        onClickRefresh = onClickRefresh,
        onClickGlobalUpdate = onClickGlobalUpdate,
        onClickOpenRandomManga = onClickOpenRandomManga,
        scrollBehavior = scrollBehavior,
        onInvalidateDownloadCache = onInvalidateDownloadCache,
    )
}

@Composable
private fun LibraryRegularToolbar(
    // NXS -->
    onClickControls: () -> Unit,
    // NXS <--
    onClickRefresh: () -> Unit,
    onClickGlobalUpdate: () -> Unit,
    onClickOpenRandomManga: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
    onInvalidateDownloadCache: (Context) -> Unit,
) {
    val context = LocalContext.current
    // NXS -->
    AppBar(
        // NXS <--
        titleContent = {
            // NXS -->
            AppBarTitle(
                title = stringResource(MR.strings.label_library),
                titleStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
                titleColor = MaterialTheme.colorScheme.primary,
            )
            // NXS <--
        },
        actions = {
            AppBarActions(
                persistentListOf(
                    AppBar.Action(
                        title = stringResource(MR.strings.action_filter),
                        icon = Icons.Outlined.FilterList,
                        // NXS -->
                        onClick = onClickControls,
                        // NXS <--
                    ),
                    AppBar.OverflowAction(
                        title = stringResource(MR.strings.action_update_library),
                        onClick = onClickGlobalUpdate,
                    ),
                    AppBar.OverflowAction(
                        title = stringResource(MR.strings.action_update_category),
                        onClick = onClickRefresh,
                    ),
                    AppBar.OverflowAction(
                        title = stringResource(MR.strings.action_open_random_manga),
                        onClick = onClickOpenRandomManga,
                    ),
                    AppBar.OverflowAction(
                        title = stringResource(MR.strings.pref_invalidate_download_cache),
                        // NXS -->
                        onClick = { onInvalidateDownloadCache(context) },
                        // NXS <--
                    ),
                    // NXS -->
                ),
                // NXS <--
            )
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun LibrarySelectionToolbar(
    selectedCount: Int,
    onClickUnselectAll: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
) {
    AppBar(
        titleContent = { Text(text = "$selectedCount") },
        actions = {
            AppBarActions(
                persistentListOf(
                    AppBar.Action(
                        title = stringResource(MR.strings.action_select_all),
                        icon = Icons.Outlined.SelectAll,
                        onClick = onClickSelectAll,
                    ),
                    AppBar.Action(
                        title = stringResource(MR.strings.action_select_inverse),
                        icon = Icons.Outlined.FlipToBack,
                        onClick = onClickInvertSelection,
                    ),
                ),
            )
        },
        isActionMode = true,
        onCancelActionMode = onClickUnselectAll,
    )
}
