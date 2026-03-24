package com.ibrahim.to_dolist.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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

// ─── Durations ────────────────────────────────────────────────────────────────

private const val PUSH_DURATION = 380
private const val POP_DURATION  = 300

// ─── Easing curves (Material 3 "Emphasized") ─────────────────────────────────

private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

// ─── Transition presets ───────────────────────────────────────────────────────

private val PushEnter: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
    slideIntoContainer(
        towards       = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(PUSH_DURATION, easing = EmphasizedDecelerate),
        initialOffset = { (it * 0.30f).toInt() },
    ) + fadeIn(tween(PUSH_DURATION / 2, easing = EmphasizedDecelerate))
}

private val PushExit: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
    slideOutOfContainer(
        towards       = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(PUSH_DURATION, easing = EmphasizedAccelerate),
        targetOffset  = { (it * 0.12f).toInt() },
    ) + fadeOut(tween(PUSH_DURATION / 3, easing = EmphasizedAccelerate))
}

private val PopEnter: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
    slideIntoContainer(
        towards       = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(POP_DURATION, easing = EmphasizedDecelerate),
        initialOffset = { (it * 0.12f).toInt() },
    ) + fadeIn(tween(POP_DURATION / 2, easing = EmphasizedDecelerate))
}

private val PopExit: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
    slideOutOfContainer(
        towards       = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(POP_DURATION, easing = EmphasizedAccelerate),
        targetOffset  = { (it * 0.30f).toInt() },
    ) + fadeOut(tween(POP_DURATION / 3, easing = EmphasizedAccelerate))
}

private val SettingsEnter: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
    slideIntoContainer(
        towards       = AnimatedContentTransitionScope.SlideDirection.Up,
        animationSpec = tween(PUSH_DURATION, easing = EmphasizedDecelerate),
        initialOffset = { (it * 0.25f).toInt() },
    ) + fadeIn(tween(PUSH_DURATION / 2, easing = EmphasizedDecelerate))
}

private val SettingsExit: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
    slideOutOfContainer(
        towards       = AnimatedContentTransitionScope.SlideDirection.Down,
        animationSpec = tween(POP_DURATION, easing = EmphasizedAccelerate),
        targetOffset  = { (it * 0.25f).toInt() },
    ) + fadeOut(tween(POP_DURATION / 2, easing = EmphasizedAccelerate))
}

// ─── Nav Graph ────────────────────────────────────────────────────────────────

@Composable
fun AppNavGraph(
    viewModel        : ToDoViewModel,
    settingsViewModel: SettingsViewModel,
    mainActivity     : MainActivity,
) {
    val navController = rememberNavController()
    val scope         = rememberCoroutineScope()

    // ── File picker launcher ──────────────────────────────────────────────────
    // Hoisted here (outside NavHost) so it is registered against the Activity's
    // composition — not a NavBackStackEntry's composition. This guarantees the
    // ActivityResultRegistryOwner is always the Activity itself, which avoids
    // two bugs:
    //
    //   1. "Can only use lower 16 bits for requestCode" — FragmentActivity
    //      validates request codes when startActivityForResult is called from a
    //      nested registry. Registering at Activity level bypasses that path.
    //
    //   2. Launcher becoming invalid after back-stack changes, because the
    //      NavBackStackEntry that owned it was destroyed.
    //
    // The key= contract: rememberLauncherForActivityResult must NOT be called
    // conditionally and its registration must survive recomposition — both are
    // guaranteed here because AppNavGraph is composed exactly once per Activity.

//    var pendingImportFormat by remember { mutableStateOf(ImportFormat.CSV) }
//
//
//    val importFileLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.OpenDocument()
//    ) { uri ->
//        if (uri != null) {
//            scope.launch {
//                // Use mainActivity context here
//                settingsViewModel.importFromUri(
//                    context = mainActivity,
//                    uri = uri,
//                    format = pendingImportFormat,
//                    todoViewModel = viewModel
//                )
//            }
//        }
//    }

    NavHost(
        navController      = navController,
        startDestination   = "home",
        enterTransition    = PushEnter,
        exitTransition     = PushExit,
        popEnterTransition = PopEnter,
        popExitTransition  = PopExit,
    ) {
        // ── Home ──────────────────────────────────────────────────────────────
        composable("home") {
            HomeScreen(
                viewModel     = viewModel,
                navController = navController,
                mainActivity  = mainActivity,
            )
        }

        // ── Task detail ───────────────────────────────────────────────────────
        composable(
            route     = "tasks/{todoId}/{todoColor}/{todoTitle}",
            arguments = listOf(
                navArgument("todoId")    { type = NavType.IntType },
                navArgument("todoColor") { type = NavType.StringType },
                navArgument("todoTitle") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            TaskListScreen(
                viewModel      = viewModel,
                todoId         = backStackEntry.arguments?.getInt("todoId") ?: 0,
                cardColor      = backStackEntry.arguments?.getString("todoColor").orEmpty(),
                todoTitle      = backStackEntry.arguments?.getString("todoTitle").orEmpty(),
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Settings ──────────────────────────────────────────────────────────
        composable(
            route              = "setting",
            enterTransition    = SettingsEnter,
            exitTransition     = SettingsExit,
            popEnterTransition = { fadeIn(tween(POP_DURATION, easing = EmphasizedDecelerate)) },
            popExitTransition  = SettingsExit,
        ) {
            SettingsScreen(
                navController = navController,
                viewModel = settingsViewModel,
                todoViewModel = viewModel,
                onRequestImport = { format ->
                    mainActivity.launchImport(format)  // <-- call Activity function
                }
            )
        }
    }
}