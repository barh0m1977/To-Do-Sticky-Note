package com.ibrahim.to_dolist.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.ibrahim.to_dolist.presentation.viewmodel.ToDoViewModel


@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun AppNavGraph(viewModel: ToDoViewModel) {
    val navController = rememberNavController()

//    NavHost(navController = navController, startDestination = "home") {
//        composable("home") {
//            HomeScreen(viewModel = viewModel, navController = navController)
//        }
//
//
//        composable(
//            route = "task_list/{todoId}",
//            arguments = listOf(navArgument("todoId") { type = NavType.IntType })
//        ) { backStackEntry ->
//            val todoId = backStackEntry.arguments?.getInt("todoId") ?: return@composable
//            val todo = viewModel.getToDoById(todoId)
//            TaskList(
//                todo = todo,
//                viewModel = viewModel,
//                onBackClick = { navController.popBackStack() }
//            )
//        }
//    }
}

