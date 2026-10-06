package eu.kanade.tachiyomi.data.backup.create.creators

import eu.kanade.tachiyomi.data.backup.models.StringPreferenceValue
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.domain.source.service.SourceManager

/**
 * Guards the credential-exclusion property of backups.
 *
 * Before the fix, credentials stored under plain keys were exported in every backup and
 * every sync. They now live under [Preference.privateKey], and
 * [PreferenceBackupCreator.withPrivatePreferences] drops them unless the user explicitly
 * asks for private preferences to be included.
 *
 * The NEXUS extension credential `APIKEY` cannot use [Preference.privateKey] because the
 * extension writing it is external to this repository, so it is filtered by name through
 * the same predicate. [createSource] applies that predicate to source preferences; it is
 * exercised here through [createApp] because both paths share the identical filter and
 * only `createApp` can run without an Android [android.content.Context].
 *
 * This is the runtime proof that finding 1-3 of SECURITY_AUDIT.md stay fixed: previously
 * these assertions were verified by code review only.
 */
class PreferenceBackupCreatorTest {

    private val ordinarySettings = mapOf(
        "pref_theme_mode" to "dark",
        "library_update_interval" to "12",
    )

    private val credentials = listOf(
        "connection_webdav_password" to "LEAKCHECK_WEBDAV_1",
        "connection_sync_client_api_key" to "LEAKCHECK_APIKEY_1",
        "discord_accounts" to "LEAKCHECK_DISCORD_1",
        "pref_connections_password_anidex" to "LEAKCHECK_CONN_1",
    )

    private val entries: Map<String, Any> = mutableMapOf<String, Any>().apply {
        ordinarySettings.forEach { (key, value) -> put(key, value) }
        credentials.forEach { (plain, value) -> put(Preference.privateKey(plain), value) }
        put("APIKEY", "extension-credential")
        put(Preference.appStateKey("eh_last_version_code"), 10)
    }

    private val preferenceStore = mockk<PreferenceStore>().also {
        every { it.getAll() } returns entries
    }

    // Both constructor arguments are supplied so the Injekt.get() defaults are never evaluated.
    private val creator = PreferenceBackupCreator(
        sourceManager = mockk<SourceManager>(),
        preferenceStore = preferenceStore,
    )

    @Test
    fun excludesCredentialsWhenPrivateSettingsAreNotRequested() {
        val prefs = creator.createApp(includePrivatePreferences = false).associate { it.key to it.value }

        credentials.forEach { (plain, _) ->
            assertFalse(
                prefs.containsKey(Preference.privateKey(plain)),
                "private credential must not be exported: $plain",
            )
        }
        assertFalse(prefs.containsKey("APIKEY"), "extension credential must not be exported")

        assertEquals(ordinarySettings.keys, prefs.keys, "ordinary settings must still be exported")
        assertEquals("dark", (prefs["pref_theme_mode"] as StringPreferenceValue).value)
    }

    @Test
    fun includesCredentialsOnlyWhenExplicitlyRequested() {
        val prefs = creator.createApp(includePrivatePreferences = true).associate { it.key to it.value }

        credentials.forEach { (plain, value) ->
            val key = Preference.privateKey(plain)
            assertTrue(prefs.containsKey(key), "opting in should export: $key")
            assertEquals(value, (prefs[key] as StringPreferenceValue).value, "value must survive: $key")
        }
        assertTrue(prefs.containsKey("APIKEY"), "opting in should export the extension credential")
    }

    @Test
    fun neverExportsAppStatePreferences() {
        creator.createApp(includePrivatePreferences = false).forEach {
            assertFalse(Preference.isAppState(it.key), "app state must never be exported: ${it.key}")
        }
        creator.createApp(includePrivatePreferences = true).forEach {
            assertFalse(Preference.isAppState(it.key), "app state must never be exported: ${it.key}")
        }
    }
}
