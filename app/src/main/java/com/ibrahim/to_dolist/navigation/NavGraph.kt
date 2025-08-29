package com.ibrahim.to_dolist.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ibrahim.to_dolist.presentation.ui.screens.HomeScreen
import com.ibrahim.to_dolist.presentation.ui.screens.SettingsScreen
import com.ibrahim.to_dolist.presentation.viewmodel.SettingsViewModel
import com.ibrahim.to_dolist.presentation.viewmodel.ToDoViewModel
import org.koin.androidx.compose.koinViewModel

object Routes {
    const val HOME = "home"
    const val SETTINGS = "setting"
}

@Composable
fun AppNavGraph(viewModel: ToDoViewModel = koinViewModel(), settingViewModel: SettingsViewModel =koinViewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable(
            Routes.HOME,
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500))
            },
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right, tween(500)
                )
            }) {
            HomeScreen(
                viewModel = viewModel,
                navController = navController,
                settingsViewModel = settingViewModel
            )
        }
        composable(
            Routes.SETTINGS,
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(500)
                )
            },
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left, tween(500)
                )
            }) {
            SettingsScreen(navController = navController, viewModel = settingViewModel)
        }


    }
}

