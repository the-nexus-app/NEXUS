package eu.kanade.presentation.browse

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.components.AppBarTitle
import eu.kanade.presentation.components.TabContent
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun BrowseTabWrapper(tab: TabContent, onBackPressed: (() -> Unit)? = null) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = { scrollBehavior ->
            AppBar(
                // NXS -->
                titleContent = {
                    AppBarTitle(
                        title = stringResource(tab.titleRes),
                        titleStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        titleColor = MaterialTheme.colorScheme.primary,
                    )
                },
                // NXS <--
                actions = {
                    AppBarActions(tab.actions)
                },
                navigateUp = onBackPressed,
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        tab.content(paddingValues, snackbarHostState)
    }
}
