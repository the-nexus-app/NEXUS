package tachiyomi.data.source

import androidx.paging.PagingState
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.MetadataMangasPage
import exh.log.xLogE
import exh.metadata.metadata.RaisedSearchMetadata
import mihon.domain.manga.model.toDomainManga
import tachiyomi.core.common.util.QuerySanitizer.sanitize
import tachiyomi.core.common.util.lang.withIOContext
import tachiyomi.domain.manga.interactor.NetworkToLocalManga
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.repository.SourcePagingSource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class SourceSearchPagingSource(
    source: Source,
    private val query: String,
    private val filters: FilterList,
) : BaseSourcePagingSource(source) {
    override suspend fun requestNextPage(currentPage: Int): MangasPage {
        return source.getSearchManga(currentPage, query.sanitize(), filters)
    }
}

class SourcePopularPagingSource(source: Source) : BaseSourcePagingSource(source) {
    override suspend fun requestNextPage(currentPage: Int): MangasPage {
        return source.getPopularManga(currentPage)
    }
}

class SourceLatestPagingSource(source: Source) : BaseSourcePagingSource(source) {
    override suspend fun requestNextPage(currentPage: Int): MangasPage {
        return source.getLatestUpdates(currentPage)
    }
}

abstract class BaseSourcePagingSource(
    protected val source: Source,
    protected val networkToLocalManga: NetworkToLocalManga = Injekt.get(),
) : SourcePagingSource() {

    protected val seenManga = hashSetOf<String>()

    abstract suspend fun requestNextPage(currentPage: Int): MangasPage

    override suspend fun load(
        params: LoadParams<Long>,
    ): LoadResult<Long, Pair<Manga, RaisedSearchMetadata?>> {
        val page = params.key ?: 1

        return try {
            val mangasPage = withIOContext {
                requestNextPage(page.toInt())
                    .takeIf { it.mangas.isNotEmpty() }
                    ?: throw NoResultsException()
            }

            getPageLoadResult(params, mangasPage)
        } catch (e: Exception) {
            xLogE("${this::class.simpleName}: Failed to load paging source", e)
            LoadResult.Error(e)
        }
    }

    open suspend fun getPageLoadResult(
        params: LoadParams<Long>,
        mangasPage: MangasPage,
    ): LoadResult.Page<Long, Pair<Manga, RaisedSearchMetadata?>> {
        val page = params.key ?: 1

        val metadata = if (mangasPage is MetadataMangasPage) {
            mangasPage.mangasMetadata
        } else {
            emptyList()
        }

        val manga = mangasPage.mangas
            .mapIndexed { index, sManga -> sManga.toDomainManga(source.id) to metadata.getOrNull(index) }
            .filter { seenManga.add(it.first.url) }
            .let { pairs -> networkToLocalManga(pairs.map { it.first }).zip(pairs.map { it.second }) }

        return LoadResult.Page(
            data = manga,
            prevKey = null,
            nextKey = if (mangasPage.hasNextPage) page + 1 else null,
        )
    }

    override fun getRefreshKey(
        state: PagingState<Long, Pair<Manga, RaisedSearchMetadata?>>,
    ): Long? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }
}

class NoResultsException : Exception()
