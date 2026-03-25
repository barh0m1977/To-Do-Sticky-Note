package com.ibrahim.to_dolist.presentation.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibrahim.to_dolist.data.model.ToDoWithTasks
import com.ibrahim.to_dolist.data.settings.SettingsRepository
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ToDoViewModel
import com.ibrahim.to_dolist.util.DataImporter
import com.ibrahim.to_dolist.util.TaskExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

// ─── Import state machine ─────────────────────────────────────────────────────

sealed interface ImportState {
    data object Idle    : ImportState
    data object Loading : ImportState
    data class  Success(val count: Int) : ImportState
    data class  Error(val message: String) : ImportState
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

    // ── Export ────────────────────────────────────────────────────────────────
    private val _exportStatus = MutableStateFlow<File?>(null)
    val exportStatus: StateFlow<File?> = _exportStatus.asStateFlow()

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

    fun sendEvent(event: SettingsEvent) {
        _events.value = event
    }

    fun consumeEvent() {
        _events.value = null
    }

    // ── Export ────────────────────────────────────────────────────────────────

    fun exportTasks(
        context         : Context,
        todosWithTasks  : List<ToDoWithTasks>,
        format          : ExportFormat,
    ) {
        viewModelScope.launch {
            try {
                val file = when (format) {
                    ExportFormat.CSV  -> TaskExporter.exportToCSV(context, todosWithTasks)
                    ExportFormat.JSON -> TaskExporter.exportToJSON(context, todosWithTasks)
                }
                _exportStatus.value = file
            } catch (e: Exception) {
                e.printStackTrace()
                sendEvent(SettingsEvent.ShowMessage("Export failed: ${e.message}"))
            }
        }
    }

    fun clearExportStatus() {
        _exportStatus.value = null
    }

    // ── Import ────────────────────────────────────────────────────────────────

    /**
     * Full import pipeline:
     * 1. Copy the Uri content into a temp file (required because SAF Uris can't
     *    be passed to File-based readers directly on all API levels).
     * 2. Parse the file using [DataImporter].
     * 3. Persist all records into Room via [DataImporter.importToDatabase].
     * 4. Emit the result through [importState].
     *
     * [createdAt] of existing todos is NEVER touched — new rows get fresh IDs.
     */
    fun importFromUri(
        context         : Context,
        uri             : Uri,
        format          : ImportFormat,
        todoViewModel   : ToDoViewModel,        // needed to trigger UI refresh
    ) {
        viewModelScope.launch {
            _importState.value = ImportState.Loading
            try {
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

                // Step 3 — persist
                DataImporter.importToDatabase(context, parsed)

                // Step 4 — clean up temp file
                withContext(Dispatchers.IO) { tempFile.delete() }

                _importState.value = ImportState.Success(count = parsed.size)
            } catch (e: Exception) {
                e.printStackTrace()
                _importState.value = ImportState.Error(
                    message = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun clearImportState() {
        _importState.value = ImportState.Idle
    }
}