package tachiyomi.domain.release.service

import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore

/**
 * Tracks which app-update release has already been shown to the user via the in-app
 * "NEXUS Update Available" popup ([eu.kanade.presentation.more.NewUpdateScreen]).
 *
 * This exists so an *automatic* update check (app launch, background WorkManager job) doesn't
 * re-interrupt the user with a popup for a release they've already seen and dismissed with
 * "Later" - it only pops up again once a *newer* release is published. A user-initiated manual
 * "Check for updates" (in About) always bypasses this and shows the dialog regardless, since
 * that's an explicit action - see [eu.kanade.presentation.more.settings.screen.about.AboutScreen].
 *
 * This intentionally only gates the in-app popup, not the update notification
 * ([eu.kanade.tachiyomi.data.updater.AppUpdateNotifier.promptUpdate]), which is a low-friction,
 * single, replaceable notification (same notification ID every time) rather than an interruption.
 */
class AppUpdatePreferences(
    private val preferenceStore: PreferenceStore,
) {
    fun lastPromptedVersion(): Preference<String> = preferenceStore.getString(
        Preference.appStateKey("last_app_update_prompted_version"),
        "",
    )
}
