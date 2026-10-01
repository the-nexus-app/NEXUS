package eu.kanade.presentation.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.ui.dashboard.DashboardScreenModel
import tachiyomi.i18n.kmk.KMR
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource

/**
 * Welcome Back card: heading, tagline, and a compact "This week" Reading Insights row
 * embedded directly inside it (not a separate dashboard section).
 *
 * There's no pre-existing artwork asset for this card, so the "visual artwork" is a
 * subtle NEXUS-blue gradient built from theme tokens rather than a bitmap - it stays
 * theme-aware (dark/light) for free and needs no new image asset.
 */
@Composable
fun WelcomeBackCard(
    insights: DashboardScreenModel.ReadingInsights,
    modifier: Modifier = Modifier,
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.padding.medium),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier
                .background(gradient)
                .padding(MaterialTheme.padding.medium),
        ) {
            Text(
                text = stringResource(KMR.strings.dashboard_welcome_back),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = stringResource(KMR.strings.dashboard_tagline),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = MaterialTheme.padding.small),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
            )

            Text(
                text = stringResource(KMR.strings.dashboard_insights_this_week).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                fontWeight = FontWeight.SemiBold,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MaterialTheme.padding.small),
            ) {
                InsightStat(
                    value = insights.libraryTitles,
                    labelRes = KMR.strings.dashboard_insights_library_titles,
                    modifier = Modifier.weight(1f),
                )
                InsightStat(
                    value = insights.chaptersCompletedThisWeek,
                    labelRes = KMR.strings.dashboard_insights_chapters_completed,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun InsightStat(
    value: Int,
    labelRes: StringResource,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = value.toString(),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
