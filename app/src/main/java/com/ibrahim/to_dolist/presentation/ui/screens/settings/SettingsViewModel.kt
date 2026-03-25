package com.ibrahim.to_dolist.presentation.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibrahim.to_dolist.data.model.ToDoWithTasks
import com.ibrahim.to_dolist.data.settings.SettingsRepository
import com.ibrahim.to_dolist.util.DataImporter
import com.ibrahim.to_dolist.util.ImportResult
import com.ibrahim.to_dolist.util.TaskExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

// ─── State machines ───────────────────────────────────────────────────────────

sealed interface ImportState {
    data object Idle    : ImportState
    data object Loading : ImportState
    // Now carries the full ImportResult so the UI can show a detailed summary
    data class  Success(val result: ImportResult) : ImportState
    data class  Error(val message: String) : ImportState
}

sealed interface ExportState {
    data object Idle    : ExportState
    data object Loading : ExportState
    data class  Success(val file: File) : ExportState
    data class  Error(val message: String) : ExportState
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    // ── Events ────────────────────────────────────────────────────────────────
    private val _events = MutableStateFlow<SettingsEvent?>(null)
    val events = _events.asStateFlow()

    // ── Preferences ───────────────────────────────────────────────────────────
    private val _language = MutableStateFlow(repository.getLanguage())
    val language: StateFlow<AppLanguage> = _language

    private val _theme = MutableStateFlow(repository.getTheme())
    val theme: StateFlow<AppTheme> = _theme

    // ── Export — now a proper state machine instead of a raw File? ────────────
    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    // ── Import ────────────────────────────────────────────────────────────────
    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    // ─────────────────────────────────────────────────────────────────────────

    fun selectLanguage(lang: AppLanguage) {
        repository.setLanguage(lang)
        _language.value = lang
    }

    fun selectTheme(theme: AppTheme) {
        repository.setTheme(theme)
        _theme.value = theme
    }

    fun sendEvent(event: SettingsEvent) { _events.value = event }
    fun consumeEvent() { _events.value = null }

    // ── Export ────────────────────────────────────────────────────────────────

    fun exportTasks(
        context        : Context,
        todosWithTasks : List<ToDoWithTasks>,
        format         : ExportFormat,
    ) {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading
            _exportState.value = try {
                val file = when (format) {
                    ExportFormat.CSV  -> TaskExporter.exportToCSV(context, todosWithTasks)
                    ExportFormat.JSON -> TaskExporter.exportToJSON(context, todosWithTasks)
                }
                ExportState.Success(file)
            } catch (e: Exception) {
                ExportState.Error(e.message ?: "Export failed")
            }
        }
    }

    fun clearExportState() { _exportState.value = ExportState.Idle }

    // ── Import ────────────────────────────────────────────────────────────────

    /**
     * Full import pipeline:
     * 1. Copy the Uri content into a temp file.
     * 2. Parse with [DataImporter].
     * 3. Persist via [DataImporter.importToDatabase], getting back a rich [ImportResult].
     * 4. Emit [ImportState.Success] with the full result for detailed UI feedback.
     *
     * @param updateExisting When true, existing to-dos whose title+color match are
     *                       overwritten. Defaults to false (safe / additive mode).
     */
    fun importFromUri(
        context        : Context,
        uri            : Uri,
        format         : ImportFormat,
        updateExisting : Boolean = false,
    ) {
        viewModelScope.launch {
            _importState.value = ImportState.Loading
            _importState.value = try {
                // Step 1 — materialise the SAF Uri into a temp File
                val tempFile = withContext(Dispatchers.IO) {
                    val ext  = if (format == ImportFormat.CSV) "csv" else "json"
                    val temp = File(context.cacheDir, "import_temp.$ext")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(temp).use { output -> input.copyTo(output) }
                    }
                    temp
                }

                // Step 2 — parse
                val parsed = when (format) {
                    ImportFormat.JSON -> DataImporter.importFromJSON(context, tempFile)
                    ImportFormat.CSV  -> DataImporter.importFromCSV(tempFile)
                }

                // Step 3 — persist and capture the full result
                val result = DataImporter.importToDatabase(
                    context        = context,
                    todosWithTasks = parsed,
                    updateExisting = updateExisting,
                )

                // Step 4 — clean up temp file
                withContext(Dispatchers.IO) { tempFile.delete() }

                ImportState.Success(result)          // ✅ rich result, not just a count
            } catch (e: Exception) {
                ImportState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearImportState() { _importState.value = ImportState.Idle }
}