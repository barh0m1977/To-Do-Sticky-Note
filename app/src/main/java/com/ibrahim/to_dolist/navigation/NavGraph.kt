package com.ibrahim.to_dolist.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ibrahim.to_dolist.presentation.ui.screens.HomeScreen
import com.ibrahim.to_dolist.presentation.ui.screens.SettingsScreen
import com.ibrahim.to_dolist.presentation.viewmodel.ToDoViewModel


@Composable
fun AppNavGraph(viewModel: ToDoViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(viewModel = viewModel, navController = navController)
        }
        composable("setting"){
            SettingsScreen()
        }


    }
}

