package eu.kanade.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Small note editor dialog for a manual page bookmark, following the same AlertDialog +
 * OutlinedTextField pattern used by CategoryDialogs.kt's rename dialog.
 *
 * Shared between the manga Bookmarks sheet (editing a note on an existing bookmark) and the
 * Reader (adding a note immediately after creating a bookmark), so there is exactly one
 * bookmark-note UI in the app.
 *
 * An empty/whitespace-only save is normalized to null further down in UpdateBookmarkNote, so
 * this dialog just passes the raw text through.
 */
@Composable
fun BookmarkNoteEditDialog(
    initialNote: String,
    onDismissRequest: () -> Unit,
    onSave: (String?) -> Unit,
) {
    var note by remember { mutableStateOf(initialNote) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onSave(note) }) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "Cancel")
            }
        },
        title = {
            Text(text = if (initialNote.isBlank()) "Add bookmark note" else "Edit bookmark note")
        },
        text = {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(text = "Note") },
                singleLine = false,
            )
        },
    )
}