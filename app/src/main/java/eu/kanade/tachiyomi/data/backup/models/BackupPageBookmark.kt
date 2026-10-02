package eu.kanade.tachiyomi.data.backup.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * A single manual page bookmark (NEXUS's Bookmarks feature), nested under the
 * [BackupChapter] it belongs to.
 *
 * Chapter ids are local-only and get reassigned on restore, so bookmarks travel
 * with their chapter and are re-keyed by chapter url when restoring. `createdAt`
 * is preserved because the Bookmarked pages list is ordered by it.
 */
@Serializable
class BackupPageBookmark(
    @ProtoNumber(1) var pageIndex: Int = 0,
    @ProtoNumber(2) var scrollPosition: Float? = null,
    @ProtoNumber(3) var createdAt: Long = 0,
    @ProtoNumber(4) var note: String? = null,
)
