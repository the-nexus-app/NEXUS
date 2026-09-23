package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tachiyomi.domain.library.model.LibrarySearchScope
import tachiyomi.i18n.MR
import tachiyomi.i18n.kmk.KMR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun LibrarySearchBar(
    searchQuery: String?,
    onSearchQueryChange: (String?) -> Unit,
    searchScope: LibrarySearchScope,
    onScopeSelected: (LibrarySearchScope) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Scope dropdown on the LEFT
        Box {
            FilterChip(
                selected = false,
                onClick = { expanded = true },
                label = {
                    Text(
                        text = when (searchScope) {
                            LibrarySearchScope.ALL_CATEGORIES -> stringResource(
                                KMR.strings.search_scope_all_categories,
                            )
                            LibrarySearchScope.CURRENT_CATEGORY -> stringResource(
                                KMR.strings.search_scope_current_category,
                            )
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                        ),
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                ),
                shape = RoundedCornerShape(16.dp),
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                LibrarySearchScope.entries.forEach { scope ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = when (scope) {
                                    LibrarySearchScope.ALL_CATEGORIES -> stringResource(
                                        KMR.strings.search_scope_all_categories,
                                    )
                                    LibrarySearchScope.CURRENT_CATEGORY -> stringResource(
                                        KMR.strings.search_scope_current_category,
                                    )
                                },
                            )
                        },
                        onClick = {
                            onScopeSelected(scope)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Search input field in the CENTER
        TextField(
            value = searchQuery ?: "",
            onValueChange = { onSearchQueryChange(it) },
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    text = stringResource(MR.strings.action_search_hint),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            singleLine = true,
            maxLines = 1,
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
            ),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Search button on the RIGHT
        IconButton(
            onClick = {
                // Trigger search when button is clicked
                // If searchQuery is not null/empty, it's already being searched
                // This button confirms/triggers the search
                if (!searchQuery.isNullOrEmpty()) {
                    // Re-trigger search by setting the same query
                    onSearchQueryChange(searchQuery)
                }
            },
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = stringResource(MR.strings.action_search),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
