package eu.kanade.tachiyomi.ui.more

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.more.NewUpdateScreen
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.data.updater.AppUpdateDownloadJob
import eu.kanade.tachiyomi.util.system.openInBrowser
import tachiyomi.domain.release.service.AppUpdatePreferences
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class NewUpdateScreen(
    private val versionName: String,
    private val changelogInfo: String,
    private val releaseLink: String,
    private val downloadLink: String,
) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val changelogInfoNoChecksum = remember {
            changelogInfo
        }

        // Record that the user has now been shown this release, however it closes (Update Now
        // or Later), so an automatic future check doesn't push this same dialog again on top of
        // whatever the user is doing - see [AppUpdatePreferences] and
        // [eu.kanade.tachiyomi.ui.main.MainActivity.CheckForUpdates].
        LaunchedEffect(versionName) {
            Injekt.get<AppUpdatePreferences>().lastPromptedVersion().set(versionName)
        }

        NewUpdateScreen(
            versionName = versionName,
            changelogInfo = changelogInfoNoChecksum,
            onOpenInBrowser = { context.openInBrowser(releaseLink) },
            onRejectUpdate = navigator::pop,
            onAcceptUpdate = {
                AppUpdateDownloadJob.start(
                    context = context,
                    url = downloadLink,
                    title = versionName,
                )
                navigator.pop()
            },
        )
    }
}
