package com.ibrahim.to_dolist.presentation.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibrahim.to_dolist.data.model.ToDoWithTasks
import com.ibrahim.to_dolist.data.settings.SettingsRepository
import com.ibrahim.to_dolist.util.TaskExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    private val _events = MutableStateFlow<SettingsEvent?>(null)
    val events = _events.asStateFlow()
    private val _language = MutableStateFlow(repository.getLanguage())
    val language: StateFlow<AppLanguage> = _language

    private val _theme = MutableStateFlow(repository.getTheme())
    val theme: StateFlow<AppTheme> = _theme
    private val _exportStatus = MutableStateFlow<File?>(null)
    val exportStatus: StateFlow<File?> = _exportStatus.asStateFlow()



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


    //Export data to CSV or JSON
    fun exportTasks(context: Context, todosWithTasks: List<ToDoWithTasks>, format: ExportFormat) {
        viewModelScope.launch {
            try {
                val file = when (format) {
                    ExportFormat.CSV -> TaskExporter.exportToCSV(context, todosWithTasks)
                    ExportFormat.JSON -> TaskExporter.exportToJSON(context, todosWithTasks)
                }
                _exportStatus.value = file // success
            } catch (e: Exception) {
                e.printStackTrace()
                _exportStatus.value = null // failure
                sendEvent(SettingsEvent.ShowMessage("Export failed: ${e.message}"))
            }
        }
    }


    fun clearExportStatus() {
        _exportStatus.value = null
    }
}