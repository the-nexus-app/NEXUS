package eu.kanade.tachiyomi.ui.bookmarkedpages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.bookmarkedpages.BookmarkedPagesScreen
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import eu.kanade.tachiyomi.ui.reader.ReaderActivity

class BookmarkedPagesScreen : Screen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { BookmarkedPagesScreenModel() }
        val state by screenModel.state.collectAsState()

        BookmarkedPagesScreen(
            state = state,
            onClickHeader = { mangaId -> navigator.push(MangaScreen(mangaId)) },
            onClickBookmark = { bookmark ->
                // Reuses the exact same mechanism as the per-manga Bookmarks sheet
                // (MangaScreen.openBookmark): passing the page with bookmarkNav = true
                // tells the reader to restore that exact page instead of Auto-Resume.
                val intent = ReaderActivity.newIntent(
                    context,
                    bookmark.mangaId,
                    bookmark.chapterId,
                    bookmark.pageIndex,
                    bookmarkNav = true,
                )
                context.startActivity(intent)
            },
            onDeleteBookmark = { bookmark -> screenModel.removeBookmark(bookmark.id) },
            onUpdateBookmarkNote = screenModel::updateBookmarkNote,
            navigateUp = navigator::pop,
        )
    }
}
