package mihon.core.migration.migrations

import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore

/**
 * Moves credentials that were historically stored under plain keys into
 * [Preference.privateKey], so that they are excluded from backups and sync unless the
 * user explicitly opts into private preferences.
 *
 * This runs as [Migration.ALWAYS] and is idempotent: it only ever moves a value that
 * still sits under its legacy key, so running it again once there is nothing left to
 * move is a no-op, as it is on a fresh install.
 */
class PrivateSyncCredentialMigration : Migration {
    override val version: Float = Migration.ALWAYS

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val preferenceStore = migrationContext.get<PreferenceStore>() ?: return false
        moveLegacyCredentialsToPrivateKeys(preferenceStore)
        return true
    }

    companion object {

        /** Plain keys that have since been wrapped with [Preference.privateKey]. */
        val legacyPrivateKeys = listOf(
            "connection_webdav_password",
            "connection_sync_client_api_key",
            "discord_accounts",
        )

        /**
         * Prefixes of keys that vary per service id and so cannot be listed by name.
         * The private counterpart is `__PRIVATE_` + the legacy key, which never starts
         * with these prefixes, so an already-moved key is never matched twice.
         */
        val legacyPrivateKeyPrefixes = listOf(
            "pref_connections_password_",
        )

        /**
         * Copies each legacy value onto its private key when that key is still unset,
         * then removes the legacy key. Safe to call repeatedly.
         *
         * [eu.kanade.tachiyomi.data.backup.restore.restorers.PreferenceRestorer] calls this
         * as well, because a restore writes keys that do not exist locally and can therefore
         * reintroduce a legacy key after this migration has already run for the current
         * version.
         */
        fun moveLegacyCredentialsToPrivateKeys(preferenceStore: PreferenceStore) {
            val legacyKeys = buildList {
                addAll(legacyPrivateKeys)
                addAll(
                    preferenceStore.getAll().keys.filter { key ->
                        legacyPrivateKeyPrefixes.any { prefix -> key.startsWith(prefix) }
                    },
                )
            }

            legacyKeys.forEach { legacyKey ->
                val legacy = preferenceStore.getString(legacyKey, "")
                if (!legacy.isSet()) return@forEach

                val privatePref = preferenceStore.getString(Preference.privateKey(legacyKey), "")
                if (!privatePref.isSet()) {
                    privatePref.set(legacy.get())
                }
                legacy.delete()
            }
        }
    }
}
