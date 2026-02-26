package com.ibrahim.to_dolist.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ibrahim.to_dolist.MainActivity
import com.ibrahim.to_dolist.presentation.ui.screens.HomeScreen
import com.ibrahim.to_dolist.presentation.ui.screens.TaskListScreen
import com.ibrahim.to_dolist.presentation.ui.screens.settings.SettingsScreen
import com.ibrahim.to_dolist.presentation.ui.screens.settings.SettingsViewModel
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ToDoViewModel

// Standard Material 3 nav transition duration
private const val NAV_DURATION = 350

@Composable
fun AppNavGraph(
    viewModel: ToDoViewModel,
    settingsViewModel: SettingsViewModel,
    mainActivity: MainActivity,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        // Default transitions applied to ALL routes unless overridden
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(NAV_DURATION)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(NAV_DURATION)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(NAV_DURATION)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(NAV_DURATION)
            )
        },
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                navController = navController,
                mainActivity = mainActivity,
            )
        }

        composable(
            route = "tasks/{todoId}/{todoColor}/{todoTitle}",
            arguments = listOf(
                navArgument("todoId") { type = NavType.IntType },
                navArgument("todoColor") { type = NavType.StringType },
                navArgument("todoTitle") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val todoId = backStackEntry.arguments?.getInt("todoId") ?: 0
            val todoColor = backStackEntry.arguments?.getString("todoColor").orEmpty()
            val todoTitle = backStackEntry.arguments?.getString("todoTitle").orEmpty()

            TaskListScreen(
                viewModel = viewModel,
                todoId = todoId,
                cardColor = todoColor,
                todoTitle = todoTitle,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable("setting") {
            SettingsScreen(
                navController = navController,
                viewModel = settingsViewModel,
                todoViewModel = viewModel,
            )
        }
    }
}