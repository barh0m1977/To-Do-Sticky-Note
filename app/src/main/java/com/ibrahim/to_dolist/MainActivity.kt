package com.ibrahim.to_dolist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import com.ibrahim.to_dolist.core.utility.LocaleHelper
import com.ibrahim.to_dolist.data.settings.SettingsManager
import com.ibrahim.to_dolist.data.settings.SettingsRepository
import com.ibrahim.to_dolist.navigation.AppNavGraph
import com.ibrahim.to_dolist.presentation.ui.screens.settings.AppLanguage
import com.ibrahim.to_dolist.presentation.ui.screens.settings.ImportFormat
import com.ibrahim.to_dolist.presentation.ui.screens.settings.SettingsViewModel
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ToDoViewModel
import com.ibrahim.to_dolist.ui.theme.ToDoListTheme

class MainActivity : FragmentActivity() {

    private val viewModel: ToDoViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(SettingsManager(this))
    }
    // We use a manual, small constant to stay within 16-bit limits (0-65535)
    private val IMPORT_REQUEST_CODE = 1234
    private var currentImportFormat: ImportFormat = ImportFormat.CSV

    // 2. Manual Intent trigger
    fun launchImport(format: ImportFormat) {
        currentImportFormat = format
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                if (format == ImportFormat.CSV) "text/*" else "application/json"
            ))
        }
        // Using the direct Activity method with our small ID bypasses the 16-bit crash
        startActivityForResult(intent, IMPORT_REQUEST_CODE)
    }

    // 3. Manual Result Handler
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMPORT_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                settingsViewModel.importFromUri(
                    context = this,
                    uri = uri,
                    format = currentImportFormat,
                    todoViewModel = viewModel
                )
            }
        }
    }

    // ── Apply locale BEFORE resources are ever touched ────────────────────────
    // This is the only safe place to wrap the context with a locale.
    // Calling applyOverrideConfiguration() after resources are accessed (e.g.
    // inside setContent) throws IllegalStateException — which was the crash.
    override fun attachBaseContext(newBase: Context) {
        val langCode = SettingsManager(newBase).getLanguage().name.lowercase()
        super.attachBaseContext(LocaleHelper.wrap(newBase, langCode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            enableEdgeToEdge()
        }

     

        setContent {
            val language by settingsViewModel.language.collectAsState()
            val theme    by settingsViewModel.theme.collectAsState()

            // When the user picks a new language, recreate the Activity so that
            // attachBaseContext applies the new locale from scratch — this is the
            // standard Android pattern and avoids any resource-access ordering issues.
            val currentLocale = resources.configuration.locales[0].language
            LaunchedEffect(language) {
                if (language.name.lowercase() != currentLocale) {
                    recreate()
                }
            }

            CompositionLocalProvider(
                // Layout direction is safe to override here — it does not affect
                // Activity-backed composition locals.
                LocalLayoutDirection provides if (language == AppLanguage.AR)
                    LayoutDirection.Rtl else LayoutDirection.Ltr,
                // NOTE: We intentionally do NOT override LocalContext here.
                // Doing so would strip Activity-backed locals such as
                // LocalActivityResultRegistryOwner, LocalLifecycleOwner, and
                // LocalSavedStateRegistryOwner, causing crashes in
                // rememberLauncherForActivityResult and BiometricPrompt.
                // The locale is already applied via attachBaseContext above.
            ) {
                ToDoListTheme(theme.name) {
                    AppNavGraph(
                        viewModel         = viewModel,
                        settingsViewModel = settingsViewModel,
                        mainActivity      = this@MainActivity,
                    )
                }
            }
        }
    }
}

// ─── ViewModel factory ────────────────────────────────────────────────────────

class SettingsViewModelFactory(
    private val repository: SettingsRepository,
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}