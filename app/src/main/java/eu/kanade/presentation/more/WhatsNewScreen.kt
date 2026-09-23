package eu.kanade.presentation.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import eu.kanade.presentation.manga.components.MarkdownRender
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import tachiyomi.i18n.MR
import tachiyomi.i18n.kmk.KMR
import tachiyomi.i18n.sy.SYMR
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.InfoScreen

@Composable
fun WhatsNewScreen(
    currentVersion: String,
    versionName: String,
    changelogInfo: String,
    isUpdateAvailable: Boolean = true,
    onOpenInBrowser: () -> Unit,
    onDownloadUpdate: () -> Unit,
) {
    InfoScreen(
        icon = Icons.Outlined.NewReleases,
        headingText = stringResource(MR.strings.whats_new),
        subtitleText = stringResource(SYMR.strings.latest_, versionName) +
            " - " + stringResource(KMR.strings.current_, currentVersion),
        acceptText = if (isUpdateAvailable) {
            stringResource(MR.strings.whats_new_download_update)
        } else {
            stringResource(KMR.strings.up_to_date)
        },
        canAccept = isUpdateAvailable,
        onAcceptClick = onDownloadUpdate,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.padding.large),
        ) {
            MarkdownRender(
                content = changelogInfo.trimIndent(),
                flavour = GFMFlavourDescriptor(),
            )

            TextButton(
                onClick = onOpenInBrowser,
                modifier = Modifier.padding(top = MaterialTheme.padding.small),
            ) {
                Text(text = stringResource(MR.strings.update_check_open))
                Spacer(modifier = Modifier.width(MaterialTheme.padding.extraSmall))
                Icon(imageVector = Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun WhatsNewScreenPreview() {
    TachiyomiPreviewTheme {
        WhatsNewScreen(
            currentVersion = "v1.2.1",
            versionName = "v1.2.2",
            changelogInfo = """
                ## v1.2.2


                #### What's Changed
                ##### Fix

                - Fix mark existing duplicate read chapters as read option not working in some cases ([@octocat](https://github.com/octocat))
                - Fix: NaN when dragging `Start/Resume` reading button in MangaScreen ([@octocat](https://github.com/octocat))


                **Full Changelog**: [the-nexus-app/NEXUS@v1.2.1...v1.2.2](https://github.com/the-nexus-app/NEXUS/compare/v1.2.1...v1.2.2)


                -----
                ## v1.2.1


                #### What's Changed
                ##### Fix
                - bump version ([@octocat](https://github.com/octocat))
                - rename repo ([@octocat](https://github.com/octocat))

                **Full Changelog**: [the-nexus-app/NEXUS@v1.2.0...v1.2.1](https://github.com/the-nexus-app/NEXUS/compare/v1.2.0...v1.2.1)


                -----
                ## v1.2.0



                #### What's Changed
                ##### Fix

                - Fix (MangasPage): crash when extensions trying to destructuring MangasPage ([@octocat](https://github.com/octocat))

                **Full Changelog**: [the-nexus-app/NEXUS@v1.1.9...v1.2.0](https://github.com/the-nexus-app/NEXUS/compare/v1.1.9...v1.2.0)
            """,
            isUpdateAvailable = true,
            onOpenInBrowser = {},
            onDownloadUpdate = {},
        )
    }
}
