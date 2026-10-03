// NXS -->
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
import tachiyomi.i18n.MR
import tachiyomi.i18n.kmk.KMR
import tachiyomi.presentation.core.i18n.stringResource

/**
 * Small note editor dialog for a manual page bookmark, following the same AlertDialog +
 * OutlinedTextField pattern used by CategoryDialogs.kt's rename dialog.
 *
 * Shared between the manga Bookmarks sheet (editing a note on an existing bookmark), the
 * global Bookmarks screen, and the Reader (adding a note immediately after creating a bookmark).
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
                Text(text = stringResource(MR.strings.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(MR.strings.action_cancel))
            }
        },
        title = {
            Text(
                text = stringResource(
                    if (initialNote.isBlank()) {
                        KMR.strings.bookmark_add_note_title
                    } else {
                        KMR.strings.bookmark_edit_note_title
                    },
                ),
            )
        },
        text = {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(text = stringResource(KMR.strings.bookmark_note_label)) },
                singleLine = false,
            )
        },
    )
}
// NXS <--
