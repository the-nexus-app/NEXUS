package tachiyomi.domain.release.interactor

import logcat.LogPriority
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.release.model.Release
import tachiyomi.domain.release.service.AppUpdatePolicy
import tachiyomi.domain.release.service.ReleaseService
import java.time.Instant
import java.time.temporal.ChronoUnit

class GetApplicationRelease(
    private val service: ReleaseService,
    private val preferenceStore: PreferenceStore,
) {

    private val lastChecked: Preference<Long> by lazy {
        preferenceStore.getLong(Preference.appStateKey("last_app_check"), 0)
    }

    suspend fun await(arguments: Arguments): Result {
        val now = Instant.now()

        // Limit automatic checks (foreground app-open, background WorkManager job) to once
        // every CHECK_INTERVAL_HOURS. A user-initiated "Check for updates" always bypasses
        // this via [Arguments.forceCheck].
        val nextCheckTime = Instant.ofEpochMilli(lastChecked.get())
            .plus(AppUpdatePolicy.CHECK_INTERVAL_HOURS, ChronoUnit.HOURS)
        if (!arguments.forceCheck && now.isBefore(nextCheckTime)) {
            return Result.NoNewUpdate
        }

        val releases = try {
            service.releaseNotes(arguments)
        } catch (e: Exception) {
            // Network failure, GitHub outage/rate-limit, malformed JSON, etc. Never let an
            // update check crash the app or the background job - just report "no update" and
            // try again on the next scheduled/foreground check.
            logcat(LogPriority.ERROR, e) { "Failed to fetch releases from ${arguments.repository}" }
            return Result.NoNewUpdate
        }

        val eligibleReleases = releases
            // Never offer a release that is still a draft (not publicly visible/intended yet)
            // or a prerelease to a stable build.
            .filterNot { it.draft }
            .filter { !it.preRelease }
            .filter {
                isNewVersion(
                    arguments.isPreview,
                    arguments.commitCount,
                    arguments.versionName,
                    it.version,
                )
            }

        val latest = eligibleReleases.getLatest() ?: return Result.NoNewUpdate

        lastChecked.set(now.toEpochMilli())

        return Result.NewUpdate(latest)
    }

    suspend fun awaitReleaseNotes(arguments: Arguments): Result {
        val releases = try {
            service.releaseNotes(arguments)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "Failed to fetch release notes from ${arguments.repository}" }
            return Result.NoNewUpdate
        }
        val latest = releases
            .filterNot { it.draft }
            .filter { !it.preRelease }
            .getLatest() ?: return Result.NoNewUpdate
        return Result.NewUpdate(latest)
    }

    /**
     * [isPreview] is if current version is Preview (beta) build
     *
     * [versionTag] is the version of new release
     *
     * Release (stable) version will compare with current's [versionName] ("v0.1.2")
     *
     * Preview (beta) version will compare with current's [commitCount] ("r1234")
     *
     * Never throws: any malformed/unparseable tag is treated as "not a new version" so a bad
     * GitHub release can't crash the update check.
     */
    private fun isNewVersion(
        isPreview: Boolean,
        commitCount: Int,
        versionName: String,
        versionTag: String,
    ): Boolean {
        return try {
            if (isPreview) {
                // Preview builds: based on releases in the preview repo tagged like "r1234"
                val newCommitCount = versionTag.trimStart('r', 'R').toIntOrNull() ?: return false
                newCommitCount > commitCount
            } else {
                // Release builds: based on releases in the stable repo tagged like "v0.1.2"
                val newSemVer = parseVersionParts(versionTag) ?: return false
                val oldSemVer = parseVersionParts(versionName) ?: return false
                compareVersionParts(newSemVer, oldSemVer) > 0
            }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "Failed to parse version tag \"$versionTag\"" }
            false
        }
    }

    companion object {
        /**
         * Parses a version string such as "v1.2.3", "1.2.3", "1.2" or "1.2.0" into its numeric
         * components ([1, 2, 3]). Any pre-release/build suffix (e.g. "-beta2", "-rc1") is
         * dropped rather than merged into the preceding number. Returns null - rather than
         * throwing - for anything that can't be parsed as a dotted numeric version, so a
         * malformed GitHub tag never crashes the update check.
         */
        internal fun parseVersionParts(raw: String): List<Int>? {
            val parts = raw
                .trim()
                .removePrefix("v")
                .removePrefix("V")
                .substringBefore('-')
                .substringBefore('+')
                .split(".")
                .map { it.trim() }

            if (parts.isEmpty() || parts.any { it.isEmpty() }) return null

            return parts.map { it.toIntOrNull() ?: return null }
        }

        /**
         * Compares two version part lists of potentially different lengths (e.g. "1.2" vs
         * "1.2.0"), treating missing trailing components as 0. Returns >0 if [new] is newer
         * than [old], 0 if equal, <0 if older.
         */
        internal fun compareVersionParts(new: List<Int>, old: List<Int>): Int {
            val maxLength = maxOf(new.size, old.size)
            for (index in 0 until maxLength) {
                val newPart = new.getOrElse(index) { 0 }
                val oldPart = old.getOrElse(index) { 0 }
                if (newPart != oldPart) return newPart.compareTo(oldPart)
            }
            return 0
        }
    }

    data class Arguments(
        val isFoss: Boolean,
        /** If current version is Preview (beta) build */
        val isPreview: Boolean,
        /** Commit count of current version */
        val commitCount: Int,
        /** Current version name, could be version tag (v0.1.2) or commit count (r1234) */
        val versionName: String,
        /** Repository name */
        val repository: String,
        /** Force check for new update */
        val forceCheck: Boolean = false,
    )

    sealed interface Result {
        data class NewUpdate(val release: Release) : Result
        data object NoNewUpdate : Result
        data object OsTooOld : Result
    }
}

internal fun List<Release>.getLatest(): Release? {
    return firstOrNull()
        ?.copy(
            info = joinToString("\r-----\r") {
                "## ${it.version}\r\r" +
                    it.info
            },
        )
}
