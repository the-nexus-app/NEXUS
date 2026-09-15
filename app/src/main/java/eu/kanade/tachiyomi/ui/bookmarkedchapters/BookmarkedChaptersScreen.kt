package eu.kanade.tachiyomi.ui.bookmarkedchapters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.bookmarkedchapters.BookmarkedChaptersScreen
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import eu.kanade.tachiyomi.ui.reader.ReaderActivity

class BookmarkedChaptersScreen : Screen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { BookmarkedChaptersScreenModel() }
        val state by screenModel.state.collectAsState()

        BookmarkedChaptersScreen(
            state = state,
            onClickHeader = { mangaId -> navigator.push(MangaScreen(mangaId)) },
            onClickChapter = { item ->
                val intent = ReaderActivity.newIntent(context, item.mangaId, item.chapterId)
                context.startActivity(intent)
            },
            onUnbookmarkChapter = screenModel::unbookmarkChapter,
            navigateUp = navigator::pop,
        )
    }
}