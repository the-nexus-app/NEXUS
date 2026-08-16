package eu.kanade.tachiyomi.data.updater

import android.content.Context
import android.os.Build
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.util.system.isFossBuildType
import eu.kanade.tachiyomi.util.system.isPreviewBuildType
import exh.source.ExhPreferences
import tachiyomi.core.common.util.lang.withIOContext
import tachiyomi.domain.release.interactor.GetApplicationRelease
import tachiyomi.domain.release.service.AppUpdatePolicy
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import uy.kohesive.injekt.injectLazy

class AppUpdateChecker(
     -->
    private val peekIntoPreview: Boolean = false,
     <--
) {

    private val getApplicationRelease: GetApplicationRelease by injectLazy()

     -->
    private val exhPreferences by lazy { Injekt.get<ExhPreferences>() }
     <--

    suspend fun checkForUpdate(
        context: Context,
        forceCheck: Boolean = false,
         -->
        autoUpdate: Boolean = AppUpdatePolicy.DISABLE_AUTO_DOWNLOAD !in exhPreferences.appShouldAutoUpdate().get(),
         <--
    ): GetApplicationRelease.Result {
        return withIOContext {
            val result = getApplicationRelease.await(
                GetApplicationRelease.Arguments(
                    isFoss = isFossBuildType,
                    isPreview = isPreviewBuildType || peekIntoPreview,
                    commitCount = BuildConfig.COMMIT_COUNT.toInt(),
                    versionName = BuildConfig.VERSION_NAME,
                    repository = getGithubRepo(peekIntoPreview),
                    forceCheck = forceCheck,
                ),
            )

             -->
            if (!peekIntoPreview) {
                 <--
                when (result) {
                    is GetApplicationRelease.Result.NewUpdate -> {
                         -->
                        AppUpdateNotifier.releasePageUrl = result.release.releaseLink
                         <--
                        AppUpdateNotifier(context).promptUpdate(result.release)
                    }

                    else -> {}
                }

                 -->
                if (autoUpdate && result is GetApplicationRelease.Result.NewUpdate) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        AppUpdateDownloadJob.start(
                            context = context,
                            url = result.release.downloadLink,
                            title = result.release.version,
                            scheduled = true,
                        )
                    }
                }
                 <--
            }

            result
        }
    }

     -->
    suspend fun getReleaseNotes(): GetApplicationRelease.Result {
        return withIOContext {
            getApplicationRelease.awaitReleaseNotes(
                GetApplicationRelease.Arguments(
                    isFoss = isFossBuildType,
                    isPreview = isPreviewBuildType || peekIntoPreview,
                    commitCount = BuildConfig.COMMIT_COUNT.toInt(),
                    versionName = BuildConfig.VERSION_NAME,
                    repository = getGithubRepo(peekIntoPreview),
                ),
            )
        }
    }
     <--
}

val GITHUB_REPO: String by lazy { getGithubRepo() }

fun getGithubRepo(peekIntoPreview: Boolean = false): String =
    if (isPreviewBuildType || peekIntoPreview) {
        "the-nexus-app/NEXUS-preview"
    } else {
        "the-nexus-app/NEXUS"
    }

val RELEASE_TAG: String by lazy { getReleaseTag() }

fun getReleaseTag(peekIntoPreview: Boolean = false): String =
    if (isPreviewBuildType || peekIntoPreview) {
        "r${BuildConfig.COMMIT_COUNT}"
    } else {
        "v${BuildConfig.VERSION_NAME}"
    }

val RELEASE_URL = "https://github.com/$GITHUB_REPO/releases/tag/$RELEASE_TAG"
