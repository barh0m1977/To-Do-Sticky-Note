package com.ibrahim.to_dolist.presentation.ui.screens.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ibrahim.to_dolist.R
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ToDoViewModel
import com.ibrahim.to_dolist.util.getAppVersion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel,
    todoViewModel: ToDoViewModel
) {
    val language by viewModel.language.collectAsState()
    val theme by viewModel.theme.collectAsState()
    val exportStatus by viewModel.exportStatus.collectAsState()
    val context = LocalContext.current
    val version = getAppVersion(context)
    val todosWithTasks by todoViewModel.todosWithTasks.collectAsState()

    var selectedExportFormat by remember { mutableStateOf(ExportFormat.CSV) }
    var selectedImportFormat by remember { mutableStateOf(ImportFormat.CSV) }
    val scope = rememberCoroutineScope()

    // File picker launcher (works unconditionally)
//    val importFileLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri ->
//        uri?.let {
//            scope.launch {
//                try {
//                    val tempFile = File(context.cacheDir, "import_temp")
//                    context.contentResolver.openInputStream(it)?.use { input ->
//                        tempFile.outputStream().use { output ->
//                            input.copyTo(output)
//                        }
//                    }
//
//                    val importedTasks =
//                        viewModel.importData(context, tempFile, selectedImportFormat)
//
//                    DataImporter.importToDatabase(context, importedTasks)
//
//                    Toast.makeText(context, "Import completed!", Toast.LENGTH_SHORT).show()
//                } catch (e: Exception) {
//                    Toast.makeText(
//                        context,
//                        "Import failed: ${e.message}",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            }
//        }
//    }

    // Show export completion message
    LaunchedEffect(exportStatus) {
        exportStatus?.let { file ->
            viewModel.sendEvent(SettingsEvent.ShowMessage("Exported to: ${file.name}"))
            viewModel.clearExportStatus()
        }
    }

    // Listen to events from ViewModel
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            event?.let {
                SettingsActions.handelEvent(context, it)
                viewModel.consumeEvent()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Language
            item {
                DropdownSettingCard(
                    title = stringResource(R.string.language),
                    currentSelection = language.name,
                    icon = Icons.Default.Language,
                    options = AppLanguage.values().map { it.name }
                ) { selected -> viewModel.selectLanguage(AppLanguage.valueOf(selected)) }
            }

            // Theme
            item {
                DropdownSettingCard(
                    title = stringResource(R.string.theme),
                    currentSelection = theme.name,
                    icon = Icons.Default.Brightness4,
                    options = AppTheme.values().map { it.name }
                ) { selected -> viewModel.selectTheme(AppTheme.valueOf(selected)) }
            }

            // Feedback
            item {
                SettingCard(title = stringResource(R.string.send_feedback), icon = Icons.Default.Email) {
                    viewModel.sendEvent(SettingsEvent.SendFeedback)
                }
            }

            // Privacy Policy
            item {
                SettingCard(title = stringResource(R.string.privacy_policy), icon = Icons.Default.Gavel) {
                    viewModel.sendEvent(SettingsEvent.OpenPrivacyPolicy)
                }
            }

            // App Version
            item {
                SettingCard(
                    title = stringResource(R.string.version),
                    subtitle = version,
                    icon = Icons.Default.Code
                ) {}
            }

            // Export
            item {
                DropdownSettingCard(
                    title = stringResource(R.string.export),
                    icon = Icons.Default.IosShare,
                    currentSelection = selectedExportFormat.name,
                    options = ExportFormat.values().map { it.name }
                ) { selected ->
                    selectedExportFormat = ExportFormat.valueOf(selected)
                    viewModel.exportTasks(context, todosWithTasks, selectedExportFormat)
                }
            }

            // Import
            item {
                DropdownSettingCard(
                    title = stringResource(R.string.import_from),
                    icon = Icons.Default.GetApp,
                    currentSelection = selectedImportFormat.name,
                    options = ImportFormat.values().map { it.name }
                ) { selected ->
                    selectedImportFormat = ImportFormat.valueOf(selected)

                    // MIME type based on format
                    val mimeType = when (selectedImportFormat) {
                        ImportFormat.CSV -> "text/*"
                        ImportFormat.JSON -> "application/json"
                    }

//                    importFileLauncher.launch(mimeType)
                }
            }
        }
    }
}

@Composable
fun DropdownSettingCard(
    title: String,
    currentSelection: String,
    icon: ImageVector,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.bodyLarge)
                    Text(currentSelection, style = MaterialTheme.typography.bodyMedium)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                subtitle?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.animateContentSize())
                }
            }
        }
    }
}
