package mihon.feature.common.utils

import dev.icerock.moko.resources.StringResource
import mihon.domain.migration.models.MigrationFlag
import tachiyomi.i18n.MR
import tachiyomi.i18n.sy.SYMR

fun MigrationFlag.getLabel(): StringResource {
    return when (this) {
        MigrationFlag.CHAPTER -> MR.strings.chapters
        MigrationFlag.CATEGORY -> MR.strings.categories
         -->
        MigrationFlag.TRACK -> MR.strings.track
         <--
        MigrationFlag.CUSTOM_COVER -> MR.strings.custom_cover
        MigrationFlag.NOTES -> MR.strings.action_notes
        MigrationFlag.REMOVE_DOWNLOAD -> MR.strings.delete_downloaded
         -->
        MigrationFlag.EXTRA -> SYMR.strings.log_extra
         <--
    }
}
