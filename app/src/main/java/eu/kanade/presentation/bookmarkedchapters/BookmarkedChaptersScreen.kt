package eu.kanade.presentation.bookmarkedchapters

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.kanade.presentation.bookmarkedchapters.components.BookmarkedChaptersUiModel
import eu.kanade.presentation.bookmarkedchapters.components.bookmarkedChaptersUiItems
import eu.kanade.presentation.components.AppBar
import eu.kanade.tachiyomi.ui.bookmarkedchapters.BookmarkedChaptersScreenModel
import tachiyomi.domain.chapter.model.BookmarkedChapterWithManga
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.LoadingScreen

@Composable
fun BookmarkedChaptersScreen(
    state: BookmarkedChaptersScreenModel.State,
    onClickHeader: (Long) -> Unit,
    onClickChapter: (BookmarkedChapterWithManga) -> Unit,
    onUnbookmarkChapter: (BookmarkedChapterWithManga) -> Unit,
    navigateUp: () -> Unit,
) {
    Scaffold(
        topBar = { scrollBehavior ->
            AppBar(
                title = stringResource(MR.strings.label_bookmarked_chapters),
                navigateUp = navigateUp,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { contentPadding ->
        when {
            state.isLoading -> LoadingScreen(modifier = Modifier.padding(contentPadding))
            state.isEmpty -> EmptyScreen(
                message = stringResource(MR.strings.info_empty_bookmarked_chapters),
                modifier = Modifier.padding(contentPadding),
            )
            else -> {
                val uiModels = state.getUiModels()
                val listState = rememberLazyListState()
                FastScrollLazyColumn(
                    modifier = Modifier.padding(contentPadding),
                    state = listState,
                ) {
                    bookmarkedChaptersUiItems(
                        uiModels = uiModels,
                        onClickHeader = { header: BookmarkedChaptersUiModel.Header ->
                            onClickHeader(header.mangaId)
                        },
                        onClickChapter = onClickChapter,
                        onUnbookmarkChapter = onUnbookmarkChapter,
                    )
                }
            }
        }
    }
}
