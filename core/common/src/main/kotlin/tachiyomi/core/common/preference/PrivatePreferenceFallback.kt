// NXS -->
// Fallback reader for credentials promoted to Preference.privateKey.
// NXS <--
package tachiyomi.core.common.preference

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn

/**
 * Backs a credential that must never be exported: the value is stored under
 * [Preference.privateKey], which keeps it out of backups and sync unless the user
 * explicitly opts into private preferences.
 *
 * Reads still fall back to the legacy plain key while one exists. A restore writes any
 * key it is handed, including ones that do not exist locally, so restoring a backup
 * created before the key was made private can repopulate the plain value with no private
 * counterpart; without the fallback the credential would read back as blank and silently
 * break whatever it protects. Writing always targets the private key and clears the plain
 * one, so the plain value cannot survive the next write.
 *
 * Anything left behind between releases is swept up by
 * `mihon.core.migration.migrations.PrivateSyncCredentialMigration`, which runs on every
 * version bump and again after a restore.
 */
fun PreferenceStore.privateStringWithLegacyFallback(key: String): Preference<String> {
    val privatePreference = getString(Preference.privateKey(key), "")
    val legacyPreference = getString(key, "")
    return object : Preference<String> {
        override fun key(): String = privatePreference.key()

        override fun get(): String = privatePreference.get().ifBlank { legacyPreference.get() }

        override fun set(value: String) {
            privatePreference.set(value)
            legacyPreference.delete()
        }

        override fun isSet(): Boolean = privatePreference.isSet() || legacyPreference.isSet()

        override fun delete() {
            privatePreference.delete()
            legacyPreference.delete()
        }

        override fun defaultValue(): String = ""

        override fun changes(): Flow<String> = merge(privatePreference.changes(), legacyPreference.changes())
            .map { get() }

        override fun stateIn(scope: CoroutineScope): StateFlow<String> =
            changes().stateIn(scope, SharingStarted.Eagerly, get())
    }
}
