package eu.kanade.tachiyomi.extension.api

import android.content.Context
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.tachiyomi.extension.ExtensionManager
import eu.kanade.tachiyomi.extension.model.Extension
import eu.kanade.tachiyomi.extension.model.LoadResult
import eu.kanade.tachiyomi.extension.util.ExtensionLoader
import exh.source.BlacklistedSources
import exh.source.ExhPreferences
import mihon.domain.extension.interactor.UpdateExtensionStores
import mihon.domain.extension.repository.ExtensionStoreRepository
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.core.common.util.lang.withIOContext
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import uy.kohesive.injekt.injectLazy
import java.time.Instant
import kotlin.time.Duration.Companion.days

internal class ExtensionApi {

    private val repository: ExtensionStoreRepository by injectLazy()

    private val preferenceStore: PreferenceStore by injectLazy()
    private val updateExtensionStores: UpdateExtensionStores by injectLazy()
    private val extensionManager: ExtensionManager by injectLazy()

     -->
    private val sourcePreferences: SourcePreferences by injectLazy()
     <--

    private val lastExtCheck: Preference<Long> by lazy {
        preferenceStore.getLong(Preference.appStateKey("last_ext_check"), 0)
    }

    suspend fun findExtensions(): List<Extension.Available> {
         -->
        val disabledRepos = sourcePreferences.disabledRepos().get()
         <--
        return withIOContext {
            repository.fetchExtensions(
                 -->
                disabledRepos,
                 <--
            )
        }
    }

    suspend fun checkForUpdates(
        context: Context,
        fromAvailableExtensionList: Boolean = false,
    ): List<Extension.Installed>? {
        // Limit checks to once a day at most
        if (!fromAvailableExtensionList &&
            Instant.now().toEpochMilli() < lastExtCheck.get() + 1.days.inWholeMilliseconds
        ) {
            return null
        }

        updateExtensionStores()

        val extensions = if (fromAvailableExtensionList) {
            extensionManager.availableExtensionsFlow.value
        } else {
            findExtensions().also { lastExtCheck.set(Instant.now().toEpochMilli()) }
        }

         -->
        val blacklistEnabled = sourcePreferences.enableSourceBlacklist().get()
         <--

        val installedExtensions = ExtensionLoader.loadExtensions(context)
            .filterIsInstance<LoadResult.Success>()
            .map { it.extension }
             -->
            .filterNot { it.isBlacklisted(blacklistEnabled) }
         <--

        val extensionsWithUpdate = mutableListOf<Extension.Installed>()
        for (installedExt in installedExtensions) {
            val pkgName = installedExt.pkgName
            val availableExt = extensions.find { it.pkgName == pkgName } ?: continue
            val hasUpdatedVer = availableExt.versionCode > installedExt.versionCode
            val hasUpdatedLib = availableExt.libVersion > installedExt.libVersion
            val hasUpdate = hasUpdatedVer || hasUpdatedLib
            if (hasUpdate) {
                extensionsWithUpdate.add(installedExt)
            }
        }

        if (extensionsWithUpdate.isNotEmpty()) {
            ExtensionUpdateNotifier(context).promptUpdates(extensionsWithUpdate.map { it.name })
        }

        return extensionsWithUpdate
    }

     -->
    private fun Extension.isBlacklisted(
        blacklistEnabled: Boolean = sourcePreferences.enableSourceBlacklist().get(),
         -->
        isHentaiEnabled: Boolean = Injekt.get<ExhPreferences>().isHentaiEnabled().get(),
         <--
    ): Boolean {
        return pkgName in BlacklistedSources.BLACKLISTED_EXTENSIONS &&
            blacklistEnabled &&
             -->
            isHentaiEnabled
         <--
    }
     <--
}
