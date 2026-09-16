package eu.kanade.presentation.bookmarkedpages.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.DropdownMenu
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.presentation.util.animateItemFastScroll
import tachiyomi.domain.bookmark.model.BookmarkWithManga
import tachiyomi.presentation.core.components.ListGroupHeader
import tachiyomi.presentation.core.components.Scroller.STICKY_HEADER_KEY_PREFIX
import tachiyomi.presentation.core.components.material.padding
import java.text.DateFormat
import java.util.Date

private val BookmarkedPageRowHeight = 96.dp

internal fun LazyListScope.bookmarkedPagesUiItems(
    uiModels: List<BookmarkedPagesUiModel>,
    onClickHeader: (BookmarkedPagesUiModel.Header) -> Unit,
    onClickBookmark: (BookmarkWithManga) -> Unit,
    onDeleteBookmark: (BookmarkWithManga) -> Unit,
    onEditNote: (BookmarkWithManga) -> Unit,
) {
    uiModels.forEach { uiModel ->
        when (uiModel) {
            is BookmarkedPagesUiModel.Header -> {
                stickyHeader(
                    key = "$STICKY_HEADER_KEY_PREFIX-bookmarkedPagesHeader-${uiModel.mangaId}",
                    contentType = "header",
                ) {
                    BookmarkedPagesMangaHeader(
                        modifier = Modifier.animateItemFastScroll(),
                        header = uiModel,
                        onClick = { onClickHeader(uiModel) },
                    )
                }
            }
            is BookmarkedPagesUiModel.Item -> {
                item(
                    key = "bookmarked-page-${uiModel.bookmark.id}",
                    contentType = "item",
                ) {
                    BookmarkedPageRow(
                        modifier = Modifier.animateItemFastScroll(),
                        bookmark = uiModel.bookmark,
                        onClick = { onClickBookmark(uiModel.bookmark) },
                        onDelete = { onDeleteBookmark(uiModel.bookmark) },
                        onEditNote = { onEditNote(uiModel.bookmark) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BookmarkedPagesMangaHeader(
    modifier: Modifier,
    header: BookmarkedPagesUiModel.Header,
    onClick: () -> Unit,
) {
    // Text-only sticky header, same convention as Bookmarked Chapters: tapping it
    // navigates to the manga screen. The cover itself is shown per-row below, since a
    // manga can have bookmarks spread across several different chapters.
    ListGroupHeader(
        modifier = modifier.clickable(onClick = onClick),
        text = header.mangaTitle,
        tonalElevation = 1.dp,
        count = header.bookmarkCount,
    )
}

@Composable
private fun BookmarkedPageRow(
    modifier: Modifier,
    bookmark: BookmarkWithManga,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onEditNote: () -> Unit,
) {
    var menuExpanded by remember(bookmark.id) { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(BookmarkedPageRowHeight)
            .padding(horizontal = MaterialTheme.padding.medium, vertical = MaterialTheme.padding.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Same cover thumbnail component used by History/Updates/Library rows.
        MangaCover.Book(
            modifier = Modifier.fillMaxHeight(),
            data = bookmark.coverData,
            size = MangaCover.Size.Medium,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = MaterialTheme.padding.medium, end = MaterialTheme.padding.small),
        ) {
            Text(
                text = bookmark.mangaTitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            Text(
                text = pageBookmarkDisplayName(bookmark),
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            // Captured into a local val: smart-casting a nullable property declared in
            // a different Gradle module (domain) isn't allowed directly on
            // bookmark.note, but works fine on a local variable.
            val note = bookmark.note
            if (!note.isNullOrBlank()) {
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.primary,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
            Text(
                text = DateFormat.getDateTimeInstance().format(Date(bookmark.createdAt)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

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
                        onEditNote()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Delete bookmark") },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    },
                )
            }
        }
    }
}

private fun pageBookmarkDisplayName(bookmark: BookmarkWithManga): String {
    val chapterLabel = bookmark.chapterName.ifBlank {
        val isWholeNumber = bookmark.chapterNumber == bookmark.chapterNumber.toLong().toDouble()
        val number = if (isWholeNumber) {
            bookmark.chapterNumber.toLong().toString()
        } else {
            bookmark.chapterNumber.toString()
        }
        "Chapter $number"
    }
    return "$chapterLabel · Page ${bookmark.pageIndex + 1}"
}

sealed class BookmarkedPagesUiModel {

    data class Header(
        val mangaId: Long,
        val mangaTitle: String,
        val bookmarkCount: Int,
    ) : BookmarkedPagesUiModel()

    data class Item(val bookmark: BookmarkWithManga) : BookmarkedPagesUiModel()
}
