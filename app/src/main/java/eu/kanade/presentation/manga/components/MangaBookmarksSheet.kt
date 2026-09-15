package eu.kanade.presentation.manga.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.BookmarkNoteEditDialog
import eu.kanade.presentation.components.DropdownMenu
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
    onBookmarkNoteUpdate: (Long, String?) -> Unit,
) {
    // The bookmark currently being edited via the note dialog, or null if closed.
    var bookmarkBeingEdited by remember { mutableStateOf<BookmarkWithChapter?>(null) }

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
                    var menuExpanded by remember(bookmark.id) { mutableStateOf(false) }

                    ListItem(
                        headlineContent = {
                            Text("${bookmark.chapterName} — Page ${bookmark.pageIndex + 1}")
                        },
                        supportingContent = {
                            Column {
                                // Captured into a local val: smart-casting a nullable property
                                // declared in a different Gradle module (domain) isn't allowed
                                // directly on bookmark.note, but works fine on a local variable.
                                val note = bookmark.note
                                if (!note.isNullOrBlank()) {
                                    Text(
                                        text = note,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                Text(DateFormat.getDateTimeInstance().format(Date(bookmark.createdAt)))
                            }
                        },
                        trailingContent = {
                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(Icons.Outlined.MoreVert, contentDescription = "More options")
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(if (bookmark.note.isNullOrBlank()) "Add note" else "Edit note")
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            bookmarkBeingEdited = bookmark
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Open bookmark") },
                                        onClick = {
                                            menuExpanded = false
                                            onBookmarkClick(bookmark)
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete bookmark") },
                                        onClick = {
                                            menuExpanded = false
                                            onBookmarkDelete(bookmark.id)
                                        },
                                    )
                                }
                            }
                        },
                        modifier = Modifier.clickable { onBookmarkClick(bookmark) },
                    )
                }
            }
        }
    }

    val editingBookmark = bookmarkBeingEdited
    if (editingBookmark != null) {
        BookmarkNoteEditDialog(
            initialNote = editingBookmark.note.orEmpty(),
            onDismissRequest = { bookmarkBeingEdited = null },
            onSave = { newNote ->
                onBookmarkNoteUpdate(editingBookmark.id, newNote)
                bookmarkBeingEdited = null
            },
        )
    }
}