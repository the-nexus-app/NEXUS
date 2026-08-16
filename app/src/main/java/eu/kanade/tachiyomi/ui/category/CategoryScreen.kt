package eu.kanade.tachiyomi.ui.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.util.fastMap
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.category.CategoryScreen
import eu.kanade.presentation.category.components.CategoryCreateDialog
import eu.kanade.presentation.category.components.CategoryDeleteDialog
import eu.kanade.presentation.category.components.CategoryRenameDialog
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import tachiyomi.presentation.core.screens.LoadingScreen
import androidx.compose.runtime.rememberCoroutineScope
import androidx.fragment.app.FragmentActivity
import eu.kanade.tachiyomi.util.system.AuthenticatorUtil.authenticate
import eu.kanade.tachiyomi.util.system.AuthenticatorUtil.isAuthenticationSupported
import kotlinx.coroutines.launch

class CategoryScreen : Screen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { CategoryScreenModel() }

        val state by screenModel.state.collectAsState()

        if (state is CategoryScreenState.Loading) {
            LoadingScreen()
            return
        }

        val successState = state as CategoryScreenState.Success

        CategoryScreen(
            state = successState,
            onClickCreate = { screenModel.showDialog(CategoryDialog.Create) },
            onClickRename = { screenModel.showDialog(CategoryDialog.Rename(it)) },
            onClickDelete = { category ->
                scope.launch {
                    val authSupported = context.isAuthenticationSupported()
                    val authenticated = if (authSupported) {
                        (context as FragmentActivity).authenticate(
                            title = "Unlock to delete category",
                        )
                    } else {
                        true
                    }
                    if (authenticated) {
                        screenModel.showDialog(CategoryDialog.Delete(category))
                    }
                }
            },
            onChangeOrder = screenModel::changeOrder,


             -->
            onClickHide = { category ->
                if (category.hidden) {
                    // Category is currently hidden — require auth before unhiding
                    scope.launch {
                        val authSupported = context.isAuthenticationSupported()
                        val authenticated = if (authSupported) {
                            (context as FragmentActivity).authenticate(
                                title = "Unlock to unhide category",
                            )
                        } else {
                            true
                        }
                        if (authenticated) {
                            screenModel.hideCategory(category)
                        }
                    }
                } else {
                    // Hiding doesn't require auth
                    screenModel.hideCategory(category)
                }
            },
            navigateUp = navigator::pop,
        )

        when (val dialog = successState.dialog) {
            null -> {}
            CategoryDialog.Create -> {
                CategoryCreateDialog(
                    onDismissRequest = screenModel::dismissDialog,
                    onCreate = screenModel::createCategory,
                    categories = successState.categories.fastMap { it.name }.toImmutableList(),
                )
            }
            is CategoryDialog.Rename -> {
                CategoryRenameDialog(
                    onDismissRequest = screenModel::dismissDialog,
                    onRename = { screenModel.renameCategory(dialog.category, it) },
                    categories = successState.categories.fastMap { it.name }.toImmutableList(),
                    category = dialog.category.name,
                )
            }
            is CategoryDialog.Delete -> {
                CategoryDeleteDialog(
                    onDismissRequest = screenModel::dismissDialog,
                    onDelete = { screenModel.deleteCategory(dialog.category.id) },
                    category = dialog.category.name,
                )
            }
        }

        LaunchedEffect(Unit) {
            screenModel.events.collectLatest { event ->
                if (event is CategoryEvent.LocalizedMessage) {
                    context.toast(event.stringRes)
                }
            }
        }
    }
}
