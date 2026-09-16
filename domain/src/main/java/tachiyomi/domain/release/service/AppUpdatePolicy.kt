package tachiyomi.domain.release.service

class AppUpdatePolicy {
    companion object {
        const val DEVICE_ONLY_ON_WIFI = "wifi"
        const val DEVICE_NETWORK_NOT_METERED = "network_not_metered"
        const val DEVICE_CHARGING = "ac"
        const val DISABLE_AUTO_DOWNLOAD = "disable"

        /**
         * How often an update check against GitHub is allowed to actually hit the network,
         * outside of a user-initiated "Check for updates" (which always bypasses this via
         * [tachiyomi.domain.release.interactor.GetApplicationRelease.Arguments.forceCheck]).
         *
         * This value is shared by:
         * - [tachiyomi.domain.release.interactor.GetApplicationRelease], which throttles
         *   foreground/automatic checks (e.g. on app launch) so we don't call the GitHub API
         *   on every single app open.
         * - The periodic WorkManager job that checks for updates in the background, so that a
         *   background run is never silently skipped by the same throttle it's meant to satisfy.
         *
         * 24 hours balances "don't hammer GitHub's API" against "don't make users wait several
         * days to hear about a new release" - a released update is discoverable within at most
         * ~1 day via the background job even if the app is never opened, and immediately via the
         * in-app "Check for updates" action.
         */
        const val CHECK_INTERVAL_HOURS = 24L
    }
}
