package exh.ui.metadata.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import eu.kanade.presentation.theme.colorscheme.AndroidViewColorScheme
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.databinding.DescriptionAdapterEhBinding
import eu.kanade.tachiyomi.ui.manga.MangaScreenModel.State
import eu.kanade.tachiyomi.util.system.copyToClipboard
import eu.kanade.tachiyomi.util.system.dpToPx
import exh.metadata.MetadataUtil
import exh.metadata.metadata.EHentaiSearchMetadata
import exh.ui.metadata.adapters.MetadataUIUtil.bindDrawable
import exh.util.SourceTagsUtil
import exh.util.SourceTagsUtil.genreTextColor
import tachiyomi.core.common.i18n.pluralStringResource
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.i18n.MR
import tachiyomi.i18n.sy.SYMR
import tachiyomi.presentation.core.icons.FlagEmoji.Companion.getEmojiLangFlag

@Composable
fun EHentaiDescription(
    state: State.Success,
    openMetadataViewer: () -> Unit,
    search: (String) -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = AndroidViewColorScheme(MaterialTheme.colorScheme)
    val iconColor = colorScheme.iconColor
    val ratingBarColor = colorScheme.ratingBarColor
    val ratingBarSecondaryColor = colorScheme.ratingBarSecondaryColor
    val textColor = LocalContentColor.current.toArgb()
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { factoryContext ->
            DescriptionAdapterEhBinding.inflate(LayoutInflater.from(factoryContext)).root
        },
        update = {
            val meta = state.meta
            if (meta == null || meta !is EHentaiSearchMetadata) return@AndroidView
            val binding = DescriptionAdapterEhBinding.bind(it)

            binding.genre.text =
                meta.genre?.let { genre -> MetadataUIUtil.getGenreAndColour(context, genre) }
                    ?.let { (genre, name) ->
                        binding.genre.setBackgroundColor(genre.color)
                        binding.genre.setTextColor(genreTextColor(genre))
                        name
                    }
                    ?: meta.genre
                    ?: context.stringResource(MR.strings.unknown)

            binding.visible.text =
                context.stringResource(
                    SYMR.strings.is_visible,
                    meta.visible ?: context.stringResource(MR.strings.unknown),
                )
            binding.visible.setTextColor(textColor)

            binding.favorites.text = (meta.favorites ?: 0).toString()
            binding.favorites.bindDrawable(context, R.drawable.ic_book_24dp, iconColor)
            binding.favorites.setTextColor(textColor)

            binding.uploader.text = meta.uploader ?: context.stringResource(MR.strings.unknown)
            binding.uploader.setTextColor(textColor)

            binding.size.text = MetadataUtil.humanReadableByteCount(meta.size ?: 0, true)
            binding.size.bindDrawable(context, R.drawable.ic_outline_sd_card_24, iconColor)
            binding.size.setTextColor(textColor)

            val length = meta.length ?: 0
            binding.pages.text = context.pluralStringResource(SYMR.plurals.num_pages, length, length)
            binding.pages.bindDrawable(context, R.drawable.ic_baseline_menu_book_24, iconColor, 4.dpToPx)
            binding.pages.setTextColor(textColor)

            val language = (meta.language ?: context.stringResource(MR.strings.unknown))
                .let { lang ->
                    getEmojiLangFlag(
                        SourceTagsUtil.getLocaleSourceUtil(lang.lowercase())?.toLanguageTag().orEmpty(),
                    ) + " " + lang
                }
            binding.language.text = if (meta.translated == true) {
                context.stringResource(SYMR.strings.language_translated, language)
            } else {
                language
            }
            binding.language.setTextColor(textColor)

            val ratingFloat = meta.averageRating?.toFloat()
            binding.ratingBar.rating = ratingFloat ?: 0F
            @SuppressLint("SetTextI18n")
            binding.rating.text =
                (ratingFloat ?: 0F).toString() + " - " + MetadataUIUtil.getRatingString(context, ratingFloat?.times(2))
            binding.ratingBar.supportProgressTintList = ColorStateList.valueOf(ratingBarColor)
            binding.ratingBar.supportSecondaryProgressTintList = ColorStateList.valueOf(ratingBarSecondaryColor)
            binding.rating.setTextColor(textColor)

            binding.moreInfo.bindDrawable(context, R.drawable.ic_info_24dp, iconColor)
            binding.moreInfo.text = context.stringResource(SYMR.strings.more_info)
            binding.moreInfo.setTextColor(iconColor)

            listOf(
                binding.favorites,
                binding.genre,
                binding.language,
                binding.pages,
                binding.rating,
                binding.uploader,
                binding.visible,
            ).forEach { textView ->
                textView.setOnLongClickListener {
                    context.copyToClipboard(
                        textView.text.toString(),
                        textView.text.toString(),
                    )
                    true
                }
            }

            binding.uploader.setOnClickListener {
                meta.uploader?.let { search("uploader:\"$it\"") }
            }

            binding.moreInfo.setOnClickListener {
                openMetadataViewer()
            }
        },
    )
}
