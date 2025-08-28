package com.ibrahim.to_dolist.navigation

import android.app.Application
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ibrahim.to_dolist.presentation.ui.screens.HomeScreen
import com.ibrahim.to_dolist.presentation.ui.screens.SettingsScreen
import com.ibrahim.to_dolist.presentation.ui.screens.SettingsViewModelFactory
import com.ibrahim.to_dolist.presentation.viewmodel.SettingsViewModel
import com.ibrahim.to_dolist.presentation.viewmodel.ToDoViewModel


@Composable
fun AppNavGraph(viewModel: ToDoViewModel, settingViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(LocalContext.current.applicationContext as Application)
    )
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
            HomeScreen(
                viewModel = viewModel,
                navController = navController,
                settingsViewModel = settingViewModel
            )
        }
        composable(
            "setting",
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(1050)
                )
            },
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left, tween(1050)
                )
            }) {
            SettingsScreen(navController = navController, viewModel = settingViewModel)
        }


    }
}

