package tachiyomi.domain.release.model

/**
 * Contains information about the latest release.
 */
data class Release(
    val version: String,
    val info: String,
    val releaseLink: String,
    val downloadLink: String,
     -->
    val preRelease: Boolean = false,
    val draft: Boolean = false,
     <--
)
