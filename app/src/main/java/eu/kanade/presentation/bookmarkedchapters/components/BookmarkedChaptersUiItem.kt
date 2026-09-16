package eu.kanade.presentation.bookmarkedchapters.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkRemove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue.EndToStart
import androidx.compose.material3.SwipeToDismissBoxValue.Settled
import androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.presentation.util.animateItemFastScroll
import tachiyomi.domain.chapter.model.BookmarkedChapterWithManga
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.ListGroupHeader
import tachiyomi.presentation.core.components.Scroller.STICKY_HEADER_KEY_PREFIX
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.secondaryItemAlpha

private val BookmarkedChapterRowHeight = 72.dp

internal fun LazyListScope.bookmarkedChaptersUiItems(
    uiModels: List<BookmarkedChaptersUiModel>,
    onClickHeader: (BookmarkedChaptersUiModel.Header) -> Unit,
    onClickChapter: (BookmarkedChapterWithManga) -> Unit,
    onUnbookmarkChapter: (BookmarkedChapterWithManga) -> Unit,
) {
    uiModels.forEach { uiModel ->
        when (uiModel) {
            is BookmarkedChaptersUiModel.Header -> {
                stickyHeader(
                    key = "$STICKY_HEADER_KEY_PREFIX-bookmarkedChaptersHeader-${uiModel.mangaId}",
                    contentType = "header",
                ) {
                    BookmarkedChaptersMangaHeader(
                        modifier = Modifier.animateItemFastScroll(),
                        header = uiModel,
                        onClick = { onClickHeader(uiModel) },
                    )
                }
            }
            is BookmarkedChaptersUiModel.Item -> {
                item(
                    key = "bookmarked-chapter-${uiModel.chapter.chapterId}",
                    contentType = "item",
                ) {
                    BookmarkedChapterRow(
                        modifier = Modifier.animateItemFastScroll(),
                        chapter = uiModel.chapter,
                        onClick = { onClickChapter(uiModel.chapter) },
                        onSwipe = { onUnbookmarkChapter(uiModel.chapter) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BookmarkedChaptersMangaHeader(
    modifier: Modifier,
    header: BookmarkedChaptersUiModel.Header,
    onClick: () -> Unit,
) {
    // Text-only sticky header, matching the existing grouping convention used by
    // History (date headers) and LibraryUpdateError (message headers). Tapping it
    // navigates to the manga, same as tapping a manga row elsewhere in the app.
    ListGroupHeader(
        modifier = modifier.clickable(onClick = onClick),
        text = header.mangaTitle,
        tonalElevation = 1.dp,
        count = header.chapterCount,
    )
}

@Composable
private fun BookmarkedChapterRow(
    modifier: Modifier,
    chapter: BookmarkedChapterWithManga,
    onClick: () -> Unit,
    onSwipe: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                StartToEnd, EndToStart -> onSwipe()
                else -> {}
            }
            return@rememberSwipeToDismissBoxState true
        },
        // Set threshold to 25% of the width
        positionalThreshold = { totalDistance -> totalDistance * 0.25f },
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = { UnbookmarkDismissBackground(dismissState) },
    ) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClick)
                .height(BookmarkedChapterRowHeight)
                .padding(horizontal = MaterialTheme.padding.medium, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Same cover thumbnail component used by Bookmarked Pages/History/Library rows.
            MangaCover.Book(
                modifier = Modifier.fillMaxHeight(),
                data = chapter.coverData,
                size = MangaCover.Size.Medium,
            )

            Column(
                modifier = Modifier
                    .padding(start = MaterialTheme.padding.medium)
                    .weight(1f),
            ) {
                Text(
                    text = chapterDisplayName(chapter),
                    style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
                val scanlator = chapter.scanlator
                if (scanlator != null) {
                    Text(
                        text = scanlator,
                        style = MaterialTheme.typography.bodySmall,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.secondaryItemAlpha(),
                    )
                }
            }
        }
    }
}

private fun chapterDisplayName(chapter: BookmarkedChapterWithManga): String {
    return chapter.chapterName.ifBlank {
        val isWholeNumber = chapter.chapterNumber == chapter.chapterNumber.toLong().toDouble()
        val number = if (isWholeNumber) {
            chapter.chapterNumber.toLong().toString()
        } else {
            chapter.chapterNumber.toString()
        }
        "Chapter $number"
    }
}

@Composable
private fun UnbookmarkDismissBackground(dismissState: SwipeToDismissBoxState) {
    val direction = dismissState.dismissDirection
    val targetState = dismissState.targetValue

    val backgroundColor by animateColorAsState(
        when (direction) {
            Settled -> MaterialTheme.colorScheme.surface
            StartToEnd, EndToStart ->
                MaterialTheme.colorScheme.errorContainer
                    .copy(alpha = if (targetState == Settled) 0.45f else 1f)
        },
    )
    val alignment = when (direction) {
        StartToEnd -> Alignment.CenterStart
        EndToStart -> Alignment.CenterEnd
        else -> Alignment.Center
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment,
    ) {
        Icon(
            imageVector = Icons.Outlined.BookmarkRemove,
            contentDescription = stringResource(MR.strings.action_remove_bookmark),
            tint = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

sealed class BookmarkedChaptersUiModel {

    data class Header(
        val mangaId: Long,
        val mangaTitle: String,
        val chapterCount: Int,
    ) : BookmarkedChaptersUiModel()

    data class Item(val chapter: BookmarkedChapterWithManga) : BookmarkedChaptersUiModel()
}
