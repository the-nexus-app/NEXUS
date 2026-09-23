package mihon.core.migration.migrations

import eu.kanade.domain.ui.UiPreferences
import eu.kanade.domain.ui.model.AppTheme
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext
import tachiyomi.core.common.util.lang.withIOContext

/**
 * Switches the app theme over to [AppTheme.NEXUS] for upgrading users who never
 * explicitly chose a theme. Users who deliberately picked a theme (Nord, Tako, a custom
 * color, etc.) are left untouched — this only replaces the previous *default*.
 */
class NexusDefaultThemeMigration : Migration {
    override val version: Float = 6f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean = withIOContext {
        val uiPreferences = migrationContext.get<UiPreferences>() ?: return@withIOContext false
        val appThemePref = uiPreferences.appTheme()
        if (!appThemePref.isSet()) {
            appThemePref.set(AppTheme.NEXUS)
        }
        return@withIOContext true
    }
}
