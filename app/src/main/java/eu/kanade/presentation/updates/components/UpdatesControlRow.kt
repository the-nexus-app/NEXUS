package eu.kanade.presentation.updates.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tachiyomi.i18n.MR
import tachiyomi.i18n.kmk.KMR
import tachiyomi.presentation.core.i18n.stringResource

/**
 * Compact [Filter] [Hidden Updates] control row shown below the Updates
 * header. Matches the pill-chip style of the NEXUS Library control row.
 */
@Composable
fun UpdatesControlRow(
    hasActiveFilters: Boolean,
    onClickFilter: () -> Unit,
    showHiddenUpdates: Boolean,
    onToggleHiddenUpdates: () -> Unit,
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

        val hiddenTint = if (showHiddenUpdates) MaterialTheme.colorScheme.primary else LocalContentColor.current
        ControlChip(
            icon = if (showHiddenUpdates) Icons.Outlined.LockOpen else Icons.Outlined.Lock,
            label = stringResource(
                if (showHiddenUpdates) KMR.strings.action_hidden_unlocked else KMR.strings.action_hidden_updates,
            ),
            tint = hiddenTint,
            onClick = onToggleHiddenUpdates,
        )
    }
}

@Composable
private fun ControlChip(
    icon: ImageVector,
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
