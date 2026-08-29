package eu.kanade.presentation.manga.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tachiyomi.domain.bookmark.model.BookmarkWithChapter
import java.text.DateFormat
import java.util.Date

/**
 * Per-title list of manual page bookmarks. Reuses the existing ModalBottomSheet pattern
 * rather than introducing a new screen/dialog affordance.
 */
@Composable
fun MangaBookmarksSheet(
    bookmarks: List<BookmarkWithChapter>,
    onDismissRequest: () -> Unit,
    onBookmarkClick: (BookmarkWithChapter) -> Unit,
    onBookmarkDelete: (Long) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (bookmarks.isEmpty()) {
                Text(
                    text = "No bookmarks yet",
                    modifier = Modifier.padding(16.dp),
                )
                return@Column
            }
            LazyColumn {
                items(bookmarks, key = { it.id }) { bookmark ->
                    ListItem(
                        headlineContent = {
                            Text("${bookmark.chapterName} — Page ${bookmark.pageIndex + 1}")
                        },
                        supportingContent = {
                            Text(DateFormat.getDateTimeInstance().format(Date(bookmark.createdAt)))
                        },
                        trailingContent = {
                            IconButton(onClick = { onBookmarkDelete(bookmark.id) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Remove bookmark")
                            }
                        },
                        modifier = Modifier.clickable { onBookmarkClick(bookmark) },
                    )
                }
            }
        }
    }
}