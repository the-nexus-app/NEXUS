package eu.kanade.presentation.bookmarkedpages

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import eu.kanade.presentation.bookmarkedpages.components.BookmarkedPagesUiModel
import eu.kanade.presentation.bookmarkedpages.components.bookmarkedPagesUiItems
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.BookmarkNoteEditDialog
import eu.kanade.tachiyomi.ui.bookmarkedpages.BookmarkedPagesScreenModel
import tachiyomi.domain.bookmark.model.BookmarkWithManga
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.LoadingScreen

@Composable
fun BookmarkedPagesScreen(
    state: BookmarkedPagesScreenModel.State,
    onClickHeader: (Long) -> Unit,
    onClickBookmark: (BookmarkWithManga) -> Unit,
    onDeleteBookmark: (BookmarkWithManga) -> Unit,
    onUpdateBookmarkNote: (Long, String?) -> Unit,
    navigateUp: () -> Unit,
) {
    // The bookmark currently being edited via the note dialog, or null if closed.
    var bookmarkBeingEdited by remember { mutableStateOf<BookmarkWithManga?>(null) }

    Scaffold(
        topBar = { scrollBehavior ->
            AppBar(
                title = stringResource(MR.strings.label_bookmarked_pages),
                navigateUp = navigateUp,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { contentPadding ->
        when {
            state.isLoading -> LoadingScreen(modifier = Modifier.padding(contentPadding))
            state.isEmpty -> EmptyScreen(
                message = stringResource(MR.strings.info_empty_bookmarked_pages),
                modifier = Modifier.padding(contentPadding),
            )
            else -> {
                val uiModels = state.getUiModels()
                val listState = rememberLazyListState()
                FastScrollLazyColumn(
                    modifier = Modifier.padding(contentPadding),
                    state = listState,
                ) {
                    bookmarkedPagesUiItems(
                        uiModels = uiModels,
                        onClickHeader = { header: BookmarkedPagesUiModel.Header ->
                            onClickHeader(header.mangaId)
                        },
                        onClickBookmark = onClickBookmark,
                        onDeleteBookmark = onDeleteBookmark,
                        onEditNote = { bookmark -> bookmarkBeingEdited = bookmark },
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
                onUpdateBookmarkNote(editingBookmark.id, newNote)
                bookmarkBeingEdited = null
            },
        )
    }
}