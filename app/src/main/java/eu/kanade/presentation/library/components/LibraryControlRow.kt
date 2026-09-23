package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tachiyomi.domain.library.model.LibraryDisplayMode
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

/**
 * Compact [Filter] [Sort] [Grid/List] control row shown below the Library
 * header, above the category bar. All three actions reuse the existing
 * settings-sheet dialog (via [onClickFilter]/[onClickSort]/[onClickDisplayMode],
 * each opening [eu.kanade.presentation.library.LibrarySettingsDialog] to a
 * different tab) - no new filter/sort/display state is introduced here.
 */
@Composable
fun LibraryControlRow(
    hasActiveFilters: Boolean,
    displayMode: LibraryDisplayMode,
    onClickFilter: () -> Unit,
    onClickSort: () -> Unit,
    onClickDisplayMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val filterTint = if (hasActiveFilters) MaterialTheme.colorScheme.primary else LocalContentColor.current
        ControlChip(
            icon = Icons.Outlined.FilterList,
            label = stringResource(MR.strings.action_filter),
            tint = filterTint,
            onClick = onClickFilter,
        )
        ControlChip(
            icon = Icons.AutoMirrored.Outlined.Sort,
            label = stringResource(MR.strings.action_sort),
            onClick = onClickSort,
        )
        ControlChip(
            icon = if (displayMode == LibraryDisplayMode.List) {
                Icons.AutoMirrored.Filled.ViewList
            } else {
                Icons.Filled.ViewModule
            },
            label = stringResource(MR.strings.action_display_mode),
            onClick = onClickDisplayMode,
        )
    }
}

@Composable
private fun ControlChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = LocalContentColor.current,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    ) {
        Row(
            modifier = Modifier
                .height(36.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp),
                tint = tint,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                ),
                color = tint,
            )
        }
    }
}
