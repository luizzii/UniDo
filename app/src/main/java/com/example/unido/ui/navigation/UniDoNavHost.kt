package com.example.unido.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.unido.ui.details.TaskDetailsScreen
import com.example.unido.ui.details.TaskFormScreen
import com.example.unido.ui.list.TaskListScreen
import com.example.unido.viewmodel.TaskViewModel

private object Routes {
    const val LIST = "list"
    const val ARG_ID = "id"
    const val DETAILS = "details/{$ARG_ID}"
    const val FORM = "form/{$ARG_ID}"
    const val NEW_TASK_ID = -1

    fun details(id: Int) = "details/$id"
    fun form(id: Int) = "form/$id"
}

@Composable
fun UniDoNavHost(viewModel: TaskViewModel) {
    val navController = rememberNavController()
    val idArgument = listOf(navArgument(Routes.ARG_ID) { type = NavType.IntType })

    NavHost(
        navController = navController,
        startDestination = Routes.LIST
    ) {
        composable(Routes.LIST) {
            TaskListScreen(
                viewModel = viewModel,
                onNewTask = { navController.navigate(Routes.form(Routes.NEW_TASK_ID)) },
                onTaskClick = { id -> navController.navigate(Routes.details(id)) }
            )
        }

        composable(route = Routes.DETAILS, arguments = idArgument) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt(Routes.ARG_ID) ?: Routes.NEW_TASK_ID
            TaskDetailsScreen(
                taskId = id,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.form(id)) }
            )
        }

        // Creates a task when the id is NEW_TASK_ID and edits the existing one otherwise.
        composable(route = Routes.FORM, arguments = idArgument) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt(Routes.ARG_ID) ?: Routes.NEW_TASK_ID
            TaskFormScreen(
                taskId = id,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
