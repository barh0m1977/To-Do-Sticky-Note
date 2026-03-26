package com.ibrahim.to_dolist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.lifecycle.lifecycleScope
import com.ibrahim.to_dolist.core.utility.AppUpdateChecker
import com.ibrahim.to_dolist.core.utility.LocaleHelper
import com.ibrahim.to_dolist.data.onboarding.OnboardingManager
import com.ibrahim.to_dolist.data.settings.SettingsManager
import com.ibrahim.to_dolist.data.settings.SettingsRepository
import com.ibrahim.to_dolist.navigation.AppNavGraph
import com.ibrahim.to_dolist.onboarding.OnboardingViewModel
import com.ibrahim.to_dolist.onboarding.OnboardingViewModelFactory
import com.ibrahim.to_dolist.presentation.ui.screens.settings.AppLanguage
import com.ibrahim.to_dolist.presentation.ui.screens.settings.ExportState
import com.ibrahim.to_dolist.presentation.ui.screens.settings.ImportFormat
import com.ibrahim.to_dolist.presentation.ui.screens.settings.ImportState
import com.ibrahim.to_dolist.presentation.ui.screens.settings.SettingsViewModel
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ToDoViewModel
import com.ibrahim.to_dolist.ui.theme.ToDoListTheme
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private val viewModel: ToDoViewModel by viewModels()

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(SettingsManager(this))
    }

    // ── Onboarding ────────────────────────────────────────────────────────────
    private val onboardingViewModel: OnboardingViewModel by viewModels {
        OnboardingViewModelFactory(OnboardingManager(this))
    }

    private val IMPORT_REQUEST_CODE = 1234
    private var currentImportFormat: ImportFormat = ImportFormat.CSV

    // ── Import launcher ───────────────────────────────────────────────────────

    fun launchImport(format: ImportFormat) {
        currentImportFormat = format
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf(if (format == ImportFormat.CSV) "text/*" else "application/json")
            )
        }
        startActivityForResult(intent, IMPORT_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMPORT_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                settingsViewModel.importFromUri(
                    context = this,
                    uri     = uri,
                    format  = currentImportFormat,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            AppUpdateChecker.checkAndUpdate(this@MainActivity)
        }
    }

    // ── Locale ────────────────────────────────────────────────────────────────

    override fun attachBaseContext(newBase: Context) {
        val langCode = SettingsManager(newBase).getLanguage().name.lowercase()
        super.attachBaseContext(LocaleHelper.wrap(newBase, langCode))
    }

    // ── onCreate ──────────────────────────────────────────────────────────────

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
            val language    by settingsViewModel.language.collectAsState()
            val theme       by settingsViewModel.theme.collectAsState()
            val importState by settingsViewModel.importState.collectAsState()
            val exportState by settingsViewModel.exportState.collectAsState()

            // ── Language change → recreate ────────────────────────────────────
            val currentLocale = resources.configuration.locales[0].language
            LaunchedEffect(language) {
                if (language.name.lowercase() != currentLocale) recreate()
            }

            // ── Import feedback ───────────────────────────────────────────────
            LaunchedEffect(importState) {
                when (val state = importState) {
                    is ImportState.Success -> {
                        Toast.makeText(this@MainActivity, state.result.toMessage(), Toast.LENGTH_LONG).show()
                        settingsViewModel.clearImportState()
                    }
                    is ImportState.Error -> {
                        Toast.makeText(this@MainActivity, "Import failed: ${state.message}", Toast.LENGTH_LONG).show()
                        settingsViewModel.clearImportState()
                    }
                    is ImportState.Loading, ImportState.Idle -> Unit
                }
            }

            // ── Export feedback ───────────────────────────────────────────────
            LaunchedEffect(exportState) {
                when (val state = exportState) {
                    is ExportState.Success -> {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/octet-stream"
                            putExtra(Intent.EXTRA_STREAM, android.net.Uri.fromFile(state.file))
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        startActivity(Intent.createChooser(shareIntent, "Share export file"))
                        settingsViewModel.clearExportState()
                    }
                    is ExportState.Error -> {
                        Toast.makeText(this@MainActivity, "Export failed: ${state.message}", Toast.LENGTH_LONG).show()
                        settingsViewModel.clearExportState()
                    }
                    is ExportState.Loading, ExportState.Idle -> Unit
                }
            }

            CompositionLocalProvider(
                LocalLayoutDirection provides if (language == AppLanguage.AR)
                    LayoutDirection.Rtl else LayoutDirection.Ltr,
            ) {
                ToDoListTheme(theme.name) {
                    AppNavGraph(
                        viewModel           = viewModel,
                        settingsViewModel   = settingsViewModel,
                        onboardingViewModel = onboardingViewModel,
                        mainActivity        = this@MainActivity,
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