package com.ibrahim.to_dolist.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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

// ─── Durations ────────────────────────────────────────────────────────────────

// Push is slower — new content arriving deserves more time to settle.
// Pop is snappier — going back should feel instant and responsive.
private const val PUSH_DURATION = 380
private const val POP_DURATION  = 250

// ─── Easing curves (Material 3 "Emphasized") ─────────────────────────────────

// Decelerate: starts fast, eases into rest — used for ENTER transitions.
// Accelerate: starts slow, picks up speed — used for EXIT transitions.
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

// ─── Horizontal push/pop transitions (peer screens) ──────────────────────────

// ✅ Fixed: fade duration now matches slide duration on all transitions.
//    Before: fadeIn/fadeOut used DURATION / 2 or DURATION / 3, causing the
//    content to become fully opaque/transparent before the slide finished.
//    The second half of the animation was a fully-visible card still moving —
//    defeating the purpose of the fade entirely.

private val PushEnter: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
    slideIntoContainer(
        towards       = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(PUSH_DURATION, easing = EmphasizedDecelerate),
        initialOffset = { (it * 0.30f).toInt() },
    ) + fadeIn(tween(PUSH_DURATION, easing = EmphasizedDecelerate))
}

private val PushExit: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
    slideOutOfContainer(
        towards       = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(PUSH_DURATION, easing = EmphasizedAccelerate),
        targetOffset  = { (it * 0.12f).toInt() },
    ) + fadeOut(tween(PUSH_DURATION, easing = EmphasizedAccelerate))
}

private val PopEnter: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
    slideIntoContainer(
        towards       = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(POP_DURATION, easing = EmphasizedDecelerate),
        initialOffset = { (it * 0.12f).toInt() },
    ) + fadeIn(tween(POP_DURATION, easing = EmphasizedDecelerate))
}

private val PopExit: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
    slideOutOfContainer(
        towards       = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(POP_DURATION, easing = EmphasizedAccelerate),
        targetOffset  = { (it * 0.30f).toInt() },
    ) + fadeOut(tween(POP_DURATION, easing = EmphasizedAccelerate))
}

// ─── Vertical settings transitions (overlay layer) ───────────────────────────

// Settings slides up like a sheet — signals a different navigation layer,
// not a peer screen. On dismiss, home re-enters with a subtle downward nudge
// to match the settings sheet sliding away beneath it.

private val SettingsEnter: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
    slideIntoContainer(
        towards       = AnimatedContentTransitionScope.SlideDirection.Up,
        animationSpec = tween(PUSH_DURATION, easing = EmphasizedDecelerate),
        initialOffset = { (it * 0.25f).toInt() },
    ) + fadeIn(tween(PUSH_DURATION, easing = EmphasizedDecelerate))
}

private val SettingsExit: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
    slideOutOfContainer(
        towards       = AnimatedContentTransitionScope.SlideDirection.Down,
        animationSpec = tween(POP_DURATION, easing = EmphasizedAccelerate),
        targetOffset  = { (it * 0.25f).toInt() },
    ) + fadeOut(tween(POP_DURATION, easing = EmphasizedAccelerate))
}

// ✅ Fixed: was a bare fadeIn with no slide.
//    Home screen re-entering while settings slides down looked jarring —
//    home just appeared from nothing. Now it drifts down very slightly (8%)
//    in sync with the settings sheet dismissal, giving the illusion of depth.
private val SettingsPopEnter: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
    slideIntoContainer(
        towards       = AnimatedContentTransitionScope.SlideDirection.Down,
        animationSpec = tween(POP_DURATION, easing = EmphasizedDecelerate),
        initialOffset = { (it * 0.08f).toInt() },
    ) + fadeIn(tween(POP_DURATION, easing = EmphasizedDecelerate))
}

// ─── Nav Graph ────────────────────────────────────────────────────────────────

@Composable
fun AppNavGraph(
    viewModel        : ToDoViewModel,
    settingsViewModel: SettingsViewModel,
    mainActivity     : MainActivity,
) {
    // ✅ Fixed: removed unused rememberCoroutineScope() allocation.
    val navController = rememberNavController()

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
                navArgument("todoId")    { type = NavType.IntType    },
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
            popEnterTransition = SettingsPopEnter,
            popExitTransition  = SettingsExit,
        ) {
            SettingsScreen(
                navController   = navController,
                viewModel       = settingsViewModel,
                todoViewModel   = viewModel,
                onRequestImport = { format -> mainActivity.launchImport(format) },
            )
        }
    }
}