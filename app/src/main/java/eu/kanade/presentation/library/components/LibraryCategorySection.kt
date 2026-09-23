package eu.kanade.presentation.library.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.category.visualName
import tachiyomi.domain.category.model.Category
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

/**
 * Category section displayed between the search bar and the library collection.
 * Shows "CATEGORY" heading with "Manage >" button, followed by a horizontal
 * scrollable row of category chips for quick navigation.
 *
 * Only displays user-created categories (not the "All" system category).
 */
@Composable
fun LibraryCategorySection(
    categories: List<Category>,
    selectedCategoryIndex: Int,
    onCategorySelected: (Int) -> Unit,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Filter out system categories and only show user categories
    val userCategories = categories.filterNot { it.isSystemCategory }

    // Don't show the section if there are no user categories
    if (userCategories.isEmpty()) {
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        // Header: "CATEGORY" + "Manage >"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(MR.strings.categories).uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onManageClick) {
                Text(
                    text = stringResource(MR.strings.action_edit),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

        // Category chips row - only user categories
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            userCategories.forEachIndexed { index, category ->
                // Find the original index in the full categories list
                val originalIndex = categories.indexOf(category)
                FilterChip(
                    selected = originalIndex == selectedCategoryIndex,
                    onClick = { onCategorySelected(originalIndex) },
                    label = {
                        Text(
                            text = category.visualName,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                )
            }
        }
    }
}
