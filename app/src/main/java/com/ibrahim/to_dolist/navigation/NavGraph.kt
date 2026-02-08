package com.ibrahim.to_dolist.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ibrahim.to_dolist.presentation.ui.screens.HomeScreen
import com.ibrahim.to_dolist.presentation.ui.screens.settings.SettingsScreen
import com.ibrahim.to_dolist.presentation.ui.screens.settings.SettingsViewModel
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ToDoViewModel


@Composable
fun AppNavGraph(viewModel: ToDoViewModel,settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable(
            "home",
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(1050))
            },
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right, tween(1050)
                )
            }) {
            HomeScreen(viewModel = viewModel, navController = navController)
        }
        composable(
            "setting",
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(1050))
            },
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left, tween(1050)
                )
            }) {
            SettingsScreen(navController = navController, viewModel = settingsViewModel,todoViewModel = viewModel)
        }


    }
}

