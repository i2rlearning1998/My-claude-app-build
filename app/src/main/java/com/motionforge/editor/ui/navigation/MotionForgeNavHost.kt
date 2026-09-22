package com.motionforge.editor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.motionforge.editor.MotionForgeApp
import com.motionforge.editor.editing.MediaProbe
import com.motionforge.editor.editing.VideoExporter
import com.motionforge.editor.ui.editor.EditorScreen
import com.motionforge.editor.ui.editor.EditorViewModel
import com.motionforge.editor.ui.export.ExportScreen
import com.motionforge.editor.ui.export.ExportViewModel
import com.motionforge.editor.ui.home.HomeScreen
import com.motionforge.editor.ui.home.HomeViewModel

private object Routes {
    const val HOME = "home"
    const val EDITOR = "editor/{projectId}"
    const val EXPORT = "export/{projectId}"

    fun editor(projectId: String) = "editor/$projectId"
    fun export(projectId: String) = "export/$projectId"
}

@Composable
fun MotionForgeNavHost(navController: NavHostController = rememberNavController()) {
    val app = LocalContext.current.applicationContext as MotionForgeApp

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            val viewModel: HomeViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { HomeViewModel(app.repository) }
                }
            )
            HomeScreen(
                viewModel = viewModel,
                onOpenProject = { project -> navController.navigate(Routes.editor(project.id)) }
            )
        }

        composable(
            route = Routes.EDITOR,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            val viewModel: EditorViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        EditorViewModel(app.repository, projectId, MediaProbe(app))
                    }
                }
            )
            EditorScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onExport = { navController.navigate(Routes.export(projectId)) }
            )
        }

        composable(
            route = Routes.EXPORT,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            val viewModel: ExportViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        ExportViewModel(app.repository, projectId, VideoExporter(app))
                    }
                }
            )
            ExportScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
