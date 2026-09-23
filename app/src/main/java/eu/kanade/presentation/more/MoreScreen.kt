package eu.kanade.presentation.more

import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.GetApp
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.more.settings.widget.SwitchPreferenceWidget
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.more.DownloadQueueState
import eu.kanade.tachiyomi.util.system.openInBrowser
import exh.pref.DelegateSourcePreferences
import exh.source.ExhPreferences
import tachiyomi.core.common.Constants
import tachiyomi.i18n.MR
import tachiyomi.i18n.kmk.KMR
import tachiyomi.i18n.sy.SYMR
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.TextButton
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

@Composable
fun MoreScreen(
    downloadQueueStateProvider: () -> DownloadQueueState,
    downloadedOnly: Boolean,
    onDownloadedOnlyChange: (Boolean) -> Unit,
    incognitoMode: Boolean,
    onIncognitoModeChange: (Boolean) -> Unit,
    showNavUpdates: Boolean,
    showNavHistory: Boolean,
    onClickDownloadQueue: () -> Unit,
    onClickCategories: () -> Unit,
    onClickStats: () -> Unit,
    onClickDataAndStorage: () -> Unit,
    onClickSettings: () -> Unit,
    onClickAbout: () -> Unit,
    onClickBatchAdd: () -> Unit,
    onClickUpdates: () -> Unit,
    onClickHistory: () -> Unit,
    onClickDiscover: () -> Unit,
    extensionUpdatesCount: Int,
    onClickBookmarkedChapters: () -> Unit,
    onClickBookmarkedPages: () -> Unit,
    onClickLibraryUpdateErrors: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val exhPreferences = remember { Injekt.get<ExhPreferences>() }
    val delegateSourcePreferences = remember { Injekt.get<DelegateSourcePreferences>() }

    Scaffold { contentPadding ->
        ScrollbarLazyColumn(
            // use contentPadding as preferable padding for ScrollbarLazyColumn when not using stickyHeader
            contentPadding = contentPadding,
        ) {
            // ========== HEADER ==========
            item {
                LogoHeader()
            }

            // ========== READING & DOWNLOADS SECTION ==========
            item {
                MoreSectionHeader(title = stringResource(KMR.strings.section_reading_downloads))
            }
            item {
                SwitchPreferenceWidget(
                    title = stringResource(MR.strings.label_downloaded_only),
                    subtitle = stringResource(MR.strings.downloaded_only_summary),
                    icon = Icons.Outlined.CloudOff,
                    checked = downloadedOnly,
                    onCheckedChanged = onDownloadedOnlyChange,
                )
            }
            item {
                SwitchPreferenceWidget(
                    title = stringResource(MR.strings.pref_incognito_mode),
                    subtitle = stringResource(MR.strings.pref_incognito_mode_summary),
                    icon = rememberAnimatedVectorPainter(
                        AnimatedImageVector.animatedVectorResource(R.drawable.anim_incognito),
                        incognitoMode,
                    ),
                    checked = incognitoMode,
                    onCheckedChanged = onIncognitoModeChange,
                )
            }
            item {
                val downloadQueueState = downloadQueueStateProvider()
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_download_queue),
                    subtitle = when (downloadQueueState) {
                        DownloadQueueState.Stopped -> null
                        is DownloadQueueState.Paused -> {
                            val pending = downloadQueueState.pending
                            if (pending == 0) {
                                stringResource(MR.strings.paused)
                            } else {
                                "${stringResource(MR.strings.paused)} • ${
                                    pluralStringResource(
                                        MR.plurals.download_queue_summary,
                                        count = pending,
                                        pending,
                                    )
                                }"
                            }
                        }
                        is DownloadQueueState.Downloading -> {
                            val pending = downloadQueueState.pending
                            pluralStringResource(MR.plurals.download_queue_summary, count = pending, pending)
                        }
                    },
                    icon = Icons.Outlined.GetApp,
                    onPreferenceClick = onClickDownloadQueue,
                )
            }

            // ========== LIBRARY SECTION ==========
            item {
                MoreSectionHeader(title = stringResource(KMR.strings.section_library))
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_bookmarked_chapters),
                    icon = Icons.Outlined.CollectionsBookmark,
                    onPreferenceClick = onClickBookmarkedChapters,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_bookmarked_pages),
                    icon = Icons.Outlined.Bookmarks,
                    onPreferenceClick = onClickBookmarkedPages,
                )
            }

            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.categories),
                    icon = Icons.AutoMirrored.Outlined.Label,
                    onPreferenceClick = onClickCategories,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_stats),
                    icon = Icons.Outlined.QueryStats,
                    onPreferenceClick = onClickStats,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(KMR.strings.option_label_library_update_errors),
                    icon = Icons.Outlined.NewReleases,
                    onPreferenceClick = onClickLibraryUpdateErrors,
                )
            }

            // ========== TOOLS & DATA SECTION ==========
            item {
                MoreSectionHeader(title = stringResource(KMR.strings.section_tools_data))
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(KMR.strings.label_discover),
                    subtitle = if (extensionUpdatesCount > 0) {
                        pluralStringResource(
                            MR.plurals.update_check_notification_ext_updates,
                            count = extensionUpdatesCount,
                            extensionUpdatesCount,
                        )
                    } else {
                        null
                    },
                    icon = Icons.Outlined.Explore,
                    onPreferenceClick = onClickDiscover,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_data_storage),
                    icon = Icons.Outlined.Storage,
                    onPreferenceClick = onClickDataAndStorage,
                )
            }
            if (exhPreferences.isHentaiEnabled().get() || delegateSourcePreferences.delegateSources().get()) {
                item {
                    TextPreferenceWidget(
                        title = stringResource(SYMR.strings.eh_batch_add),
                        icon = Icons.AutoMirrored.Outlined.PlaylistAdd,
                        onPreferenceClick = onClickBatchAdd,
                    )
                }
            }

            // ========== RECENT UPDATES/HISTORY (CONDITIONAL) ==========
            if (!showNavUpdates || !showNavHistory) {
                item {
                    MoreSectionHeader(title = stringResource(KMR.strings.section_recent))
                }
                if (!showNavUpdates) {
                    item {
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_recent_updates),
                            icon = Icons.Outlined.NewReleases,
                            onPreferenceClick = onClickUpdates,
                        )
                    }
                }
                if (!showNavHistory) {
                    item {
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_recent_manga),
                            icon = Icons.Outlined.History,
                            onPreferenceClick = onClickHistory,
                        )
                    }
                }
            }

            // ========== APPLICATION SECTION ==========
            item {
                MoreSectionHeader(title = stringResource(KMR.strings.section_application))
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_settings),
                    icon = Icons.Outlined.Settings,
                    onPreferenceClick = onClickSettings,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.pref_category_about),
                    icon = Icons.Outlined.Info,
                    onPreferenceClick = onClickAbout,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_help),
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    onPreferenceClick = { uriHandler.openUri(Constants.URL_HELP) },
                )
            }
        }
    }
}

/**
 * Section header for More Screen sections.
 * Provides visual separation and organization between logical groupings.
 */
@Composable
private fun MoreSectionHeader(title: String) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
