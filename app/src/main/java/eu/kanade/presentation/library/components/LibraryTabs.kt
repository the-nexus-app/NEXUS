package eu.kanade.presentation.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import eu.kanade.presentation.category.visualName
import tachiyomi.domain.category.model.Category
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

/**
 * NEXUS CATEGORY section for Library.
 *
 * Displays the CATEGORY heading, Manage > action, and user-created
 * category chips for navigation between visible categories.
 */
@Composable
internal fun LibraryTabs(
    categories: List<Category>,
    pagerState: PagerState,
    getItemCountForCategory: (Category) -> Int?,
    onTabItemClick: (Int) -> Unit,
    onManageClick: () -> Unit,
) {
    val currentPageIndex = pagerState.currentPage.coerceAtMost(categories.lastIndex)
    val scrollState = rememberScrollState()

    // Filter to only visible user-created categories (non-system, non-hidden)
    val visibleCategories = categories.filterNot { it.isSystemCategory }.filterNot { it.hidden }

    Column(
        modifier = Modifier
            .zIndex(2f)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        // CATEGORY header row: heading + Manage action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
            Text(
                text = "Manage  >",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onManageClick),
            )
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        )

        if (visibleCategories.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                visibleCategories.forEachIndexed { index, category ->
                    LibraryCategoryChip(
                        label = category.visualName,
                        count = getItemCountForCategory(category),
                        isSelected = currentPageIndex == index,
                        onClick = { onTabItemClick(index) },
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        )
    }

    // Auto-scroll to selected chip
    LaunchedEffect(currentPageIndex) {
        val targetScroll = (currentPageIndex * 110).coerceAtLeast(0)
        scrollState.animateScrollTo(targetScroll)
    }
}

@Composable
private fun LibraryCategoryChip(
    label: String,
    count: Int?,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }

    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(17.dp))
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(17.dp),
        tonalElevation = if (isSelected) 2.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 13.sp,
                ),
                color = contentColor,
                maxLines = 1,
            )

            if (count != null && count > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                            },
                        )
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                ) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                        ),
                        color = contentColor.copy(alpha = 0.9f),
                    )
                }
            }
        }
    }
}
