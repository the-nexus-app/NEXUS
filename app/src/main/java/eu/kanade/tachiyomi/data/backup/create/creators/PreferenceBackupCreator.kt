package eu.kanade.tachiyomi.data.backup.create.creators

import eu.kanade.tachiyomi.data.backup.models.BackupPreference
import eu.kanade.tachiyomi.data.backup.models.BackupSourcePreferences
import eu.kanade.tachiyomi.data.backup.models.BooleanPreferenceValue
import eu.kanade.tachiyomi.data.backup.models.FloatPreferenceValue
import eu.kanade.tachiyomi.data.backup.models.IntPreferenceValue
import eu.kanade.tachiyomi.data.backup.models.LongPreferenceValue
import eu.kanade.tachiyomi.data.backup.models.StringPreferenceValue
import eu.kanade.tachiyomi.data.backup.models.StringSetPreferenceValue
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.preferenceKey
import eu.kanade.tachiyomi.source.sourcePreferences
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.domain.source.service.SourceManager
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class PreferenceBackupCreator(
    private val sourceManager: SourceManager = Injekt.get(),
    private val preferenceStore: PreferenceStore = Injekt.get(),
) {

    // NXS -->
    /**
     * Source preference keys that hold credentials written by extensions. They cannot be
     * moved under [Preference.privateKey], because the extension doing the writing is not
     * part of this repository and would keep writing the original key, silently desyncing
     * our reads from its writes. Filtering at export time instead keeps them out of
     * backups and sync without touching anything the extension reads or writes.
     */
    private val sensitiveCredentialKeys = setOf(
        "APIKEY",
    )
    // NXS <--

    fun createApp(includePrivatePreferences: Boolean): List<BackupPreference> {
        return preferenceStore.getAll().toBackupPreferences()
            .withPrivatePreferences(includePrivatePreferences)
    }

    fun createSource(includePrivatePreferences: Boolean): List<BackupSourcePreferences> {
        return sourceManager.getAll()
            .filterIsInstance<ConfigurableSource>()
            .map {
                BackupSourcePreferences(
                    it.preferenceKey(),
                    it.sourcePreferences().all.toBackupPreferences()
                        .withPrivatePreferences(includePrivatePreferences),
                )
            }
            .filter { it.prefs.isNotEmpty() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun Map<String, *>.toBackupPreferences(): List<BackupPreference> {
        return this
            .filterKeys { !Preference.isAppState(it) }
            .mapNotNull { (key, value) ->
                when (value) {
                    is Int -> BackupPreference(key, IntPreferenceValue(value))
                    is Long -> BackupPreference(key, LongPreferenceValue(value))
                    is Float -> BackupPreference(key, FloatPreferenceValue(value))
                    is String -> BackupPreference(key, StringPreferenceValue(value))
                    is Boolean -> BackupPreference(key, BooleanPreferenceValue(value))
                    is Set<*> -> (value as? Set<String>)?.let {
                        BackupPreference(key, StringSetPreferenceValue(it))
                    }
                    else -> null
                }
            }
    }

    private fun List<BackupPreference>.withPrivatePreferences(include: Boolean) =
        if (include) {
            this
        } else {
            // NXS --> Credential keys are dropped too; they cannot use Preference.privateKey.
            this.filter { !Preference.isPrivate(it.key) && it.key !in sensitiveCredentialKeys }
            // NXS <--
        }
}
