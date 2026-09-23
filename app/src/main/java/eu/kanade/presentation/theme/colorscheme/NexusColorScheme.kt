package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * NEXUS brand theme.
 *
 * Derived from the NEXUS logo (navy symbol, white disc, blue gradient ribbon) and the
 * NEXUS UI/UX reference board: near-black navy backgrounds with layered navy card
 * surfaces and a single blue accent used consistently for actions, progress, and
 * emphasis, rather than the grey-black background other dark schemes in this app use.
 */
internal object NexusColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFF3D8BFF),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF123B85),
        onPrimaryContainer = Color(0xFFD7E6FF),
        inversePrimary = Color(0xFF1B63E8),
        secondary = Color(0xFF5B9DFF),
        onSecondary = Color(0xFF0A1428),
        secondaryContainer = Color(0xFF1A2C4D),
        onSecondaryContainer = Color(0xFFC7DBFF),
        tertiary = Color(0xFF7FB8FF),
        onTertiary = Color(0xFF0A1428),
        tertiaryContainer = Color(0xFF1A2C4D),
        onTertiaryContainer = Color(0xFFC7DBFF),
        background = Color(0xFF0A0E17),
        onBackground = Color(0xFFF1F4FA),
        surface = Color(0xFF0A0E17),
        onSurface = Color(0xFFF1F4FA),
        surfaceVariant = Color(0xFF131A2B),
        onSurfaceVariant = Color(0xFFA6B3CC),
        surfaceTint = Color(0xFF3D8BFF),
        surfaceContainerLowest = Color(0xFF060811),
        surfaceContainerLow = Color(0xFF0C1120),
        surfaceContainer = Color(0xFF0F1524),
        surfaceContainerHigh = Color(0xFF161D30),
        surfaceContainerHighest = Color(0xFF1D2538),
        inverseSurface = Color(0xFFF1F4FA),
        inverseOnSurface = Color(0xFF0A0E17),
        outline = Color(0xFF2A344D),
        outlineVariant = Color(0xFF1A2033),
        error = Color(0xFFFF6B6B),
        onError = Color(0xFF1A0000),
        errorContainer = Color(0xFF5C1414),
        onErrorContainer = Color(0xFFFFDAD6),
    )

    override val lightScheme = lightColorScheme(
        primary = Color(0xFF1B63E8),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFDCE8FF),
        onPrimaryContainer = Color(0xFF0A1D4D),
        inversePrimary = Color(0xFF9FC3FF),
        secondary = Color(0xFF3D71C7),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE3ECFF),
        onSecondaryContainer = Color(0xFF102A5C),
        tertiary = Color(0xFF3D71C7),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFE3ECFF),
        onTertiaryContainer = Color(0xFF102A5C),
        background = Color(0xFFF7F9FC),
        onBackground = Color(0xFF0A0E17),
        surface = Color(0xFFF7F9FC),
        onSurface = Color(0xFF0A0E17),
        surfaceVariant = Color(0xFFEAEFF9),
        onSurfaceVariant = Color(0xFF4A5570),
        surfaceTint = Color(0xFF1B63E8),
        inverseSurface = Color(0xFF0A0E17),
        inverseOnSurface = Color(0xFFF7F9FC),
        outline = Color(0xFFC7D1E5),
        outlineVariant = Color(0xFFDDE4F2),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
    )
}
