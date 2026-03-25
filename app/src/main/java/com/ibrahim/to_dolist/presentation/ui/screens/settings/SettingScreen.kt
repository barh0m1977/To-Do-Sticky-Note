package com.ibrahim.to_dolist.presentation.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ibrahim.to_dolist.R
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ToDoViewModel
import com.ibrahim.to_dolist.util.getAppVersion

// ─── Palette ──────────────────────────────────────────────────────────────────
private val SurfaceVariant = Color(0xFFF5F5F5)
private val SuccessGreen   = Color(0xFF00C853)
private val ErrorRed       = Color(0xFFD50000)

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController   : NavController,
    viewModel       : SettingsViewModel,
    todoViewModel   : ToDoViewModel,
    // ← The file launcher lives in AppNavGraph (Activity level).
    //   SettingsScreen just calls this callback when it needs a file.
    onRequestImport : (ImportFormat) -> Unit,
) {
    val language       by viewModel.language.collectAsState()
    val theme          by viewModel.theme.collectAsState()
    val exportStatus   by viewModel.exportStatus.collectAsState()
    val importState    by viewModel.importState.collectAsState()
    val context        = LocalContext.current
    val version        = getAppVersion(context)
    val todosWithTasks by todoViewModel.todosWithTasks.collectAsState()
    val snackbarHost   = remember { SnackbarHostState() }

    var selectedImportFormat by remember { mutableStateOf(ImportFormat.CSV) }
    var selectedExportFormat by remember { mutableStateOf(ExportFormat.CSV) }


    // ── Snackbar: export completion ───────────────────────────────────────────
    LaunchedEffect(exportStatus) {
        exportStatus?.let { file ->
            snackbarHost.showSnackbar("✓ Exported: ${file.name}", duration = SnackbarDuration.Short)
            viewModel.clearExportStatus()
        }
    }

    // ── Snackbar: import result ───────────────────────────────────────────────
    LaunchedEffect(importState) {
        when (val s = importState) {
            is ImportState.Success -> {
                snackbarHost.showSnackbar("✓ Imported ${s.count} todo(s)", duration = SnackbarDuration.Short)
                viewModel.clearImportState()
            }
            is ImportState.Error -> {
                snackbarHost.showSnackbar("Import failed: ${s.message}", duration = SnackbarDuration.Long)
                viewModel.clearImportState()
            }
            else -> Unit
        }
    }

    // ── Events (feedback / privacy policy URLs) ───────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            event?.let {
                SettingsActions.handelEvent(context, it)
                viewModel.consumeEvent()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings),
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // ── Appearance ────────────────────────────────────────────────────
            item { SectionLabel("Appearance") }
            item {
                SettingsGroup {
                    DropdownSettingRow(
                        title    = stringResource(R.string.language),
                        subtitle = language.name,
                        icon     = Icons.Default.Language,
                        options  = AppLanguage.entries.map { it.name },
                        isLast   = false,
                    ) { viewModel.selectLanguage(AppLanguage.valueOf(it)) }

                    DropdownSettingRow(
                        title    = stringResource(R.string.theme),
                        subtitle = theme.name,
                        icon     = Icons.Default.Brightness4,
                        options  = AppTheme.entries.map { it.name },
                        isLast   = true,
                    ) { viewModel.selectTheme(AppTheme.valueOf(it)) }
                }
            }

            // ── Data ──────────────────────────────────────────────────────────
            item { SectionLabel("Data") }
            item {
                SettingsGroup {
                    DropdownSettingRow(
                        title    = stringResource(R.string.export),
                        subtitle = "Save as ${selectedExportFormat.name}",
                        icon     = Icons.Default.IosShare,
                        options  = ExportFormat.entries.map { it.name },
                        isLast   = false,
                        trailingContent = { FormatBadge(selectedExportFormat.name) },
                    ) { selected ->
                        selectedExportFormat = ExportFormat.valueOf(selected)
                        viewModel.exportTasks(context, todosWithTasks, selectedExportFormat)
                    }

                    // Import row — calls onRequestImport which triggers the
                    // launcher that lives safely in AppNavGraph.
                    ImportSettingRow(
                        selectedFormat = selectedImportFormat,
                        importState    = importState,
                        onFormatPicked = { selectedImportFormat = ImportFormat.valueOf(it) },
                        onLaunchPicker = { onRequestImport(selectedImportFormat) },
                        isLast         = true,
                    )
                }
            }

            // ── Support ───────────────────────────────────────────────────────
            item { SectionLabel("Support") }
            item {
                SettingsGroup {
                    PlainSettingRow(
                        title  = stringResource(R.string.send_feedback),
                        icon   = Icons.Default.Email,
                        isLast = false,
                    ) { viewModel.sendEvent(SettingsEvent.SendFeedback) }

                    PlainSettingRow(
                        title  = stringResource(R.string.privacy_policy),
                        icon   = Icons.Default.Gavel,
                        isLast = true,
                    ) { viewModel.sendEvent(SettingsEvent.OpenPrivacyPolicy) }
                }
            }

            // ── About ─────────────────────────────────────────────────────────
            item { SectionLabel("About") }
            item {
                SettingsGroup {
                    PlainSettingRow(
                        title    = stringResource(R.string.version),
                        subtitle = version,
                        icon     = Icons.Default.Code,
                        isLast   = true,
                        onClick  = {},
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ─── Section label ────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text       = text.uppercase(),
        style      = MaterialTheme.typography.labelSmall,
        color      = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier   = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp),
    )
}

// ─── Settings group card ──────────────────────────────────────────────────────

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = Modifier.fillMaxWidth(),
    ) { content() }
}

// ─── Plain row ────────────────────────────────────────────────────────────────

@Composable
private fun PlainSettingRow(
    title   : String,
    subtitle: String? = null,
    icon    : ImageVector,
    isLast  : Boolean,
    onClick : () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconContainer(icon)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                subtitle?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (!isLast) RowDivider()
    }
}

// ─── Dropdown row ─────────────────────────────────────────────────────────────

@Composable
private fun DropdownSettingRow(
    title           : String,
    subtitle        : String,
    icon            : ImageVector,
    options         : List<String>,
    isLast          : Boolean,
    trailingContent : (@Composable () -> Unit)? = null,
    onOptionSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconContainer(icon)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            trailingContent?.invoke()
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }

        // Inline expanding list — avoids DropdownMenu z-index issues in LazyColumn
        AnimatedVisibility(
            visible = expanded,
            enter   = expandVertically(tween(200)) + fadeIn(tween(200)),
            exit    = shrinkVertically(tween(180)) + fadeOut(tween(180)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = 4.dp),
            ) {
                options.forEach { option ->
                    Text(
                        text     = option,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(option); expanded = false }
                            .padding(horizontal = 54.dp, vertical = 12.dp),
                        style    = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        if (!isLast) RowDivider()
    }
}

// ─── Import row ───────────────────────────────────────────────────────────────

@Composable
private fun ImportSettingRow(
    selectedFormat : ImportFormat,
    importState    : ImportState,
    onFormatPicked : (String) -> Unit,
    onLaunchPicker : () -> Unit,
    isLast         : Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    val isLoading = importState is ImportState.Loading

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isLoading) {
                    if (expanded) {
                        expanded = false
                        onLaunchPicker()
                    } else {
                        expanded = true
                    }
                }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconContainer(Icons.Default.GetApp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.import_from), style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = when (importState) {
                        is ImportState.Loading -> "Importing…"
                        is ImportState.Success -> "Last: ${importState.count} item(s) imported"
                        is ImportState.Error   -> "Failed — tap to retry"
                        ImportState.Idle       -> "Pick ${selectedFormat.name} file"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (importState) {
                        is ImportState.Error   -> ErrorRed
                        is ImportState.Success -> SuccessGreen
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            when (importState) {
                is ImportState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.size(20.dp), strokeWidth = 2.dp,
                )
                is ImportState.Success -> Icon(
                    Icons.Default.CheckCircle, null,
                    tint = SuccessGreen, modifier = Modifier.size(20.dp),
                )
                is ImportState.Error -> Icon(
                    Icons.Default.Error, null,
                    tint = ErrorRed, modifier = Modifier.size(20.dp),
                )
                ImportState.Idle -> Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        AnimatedVisibility(
            visible = expanded && !isLoading,
            enter   = expandVertically(tween(200)) + fadeIn(tween(200)),
            exit    = shrinkVertically(tween(180)) + fadeOut(tween(180)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = 4.dp),
            ) {
                ImportFormat.entries.forEach { format ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFormatPicked(format.name) }
                            .padding(horizontal = 54.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            format.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        if (format == selectedFormat) {
                            Icon(
                                Icons.Default.CheckCircle, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
                // CTA button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 54.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .clickable { expanded = false; onLaunchPicker() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Choose ${selectedFormat.name} file →",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        if (!isLast) RowDivider()
    }
}

// ─── Shared small composables ─────────────────────────────────────────────────

@Composable
private fun IconContainer(icon: ImageVector) {
     val iconTint       = MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier
            .size(36.dp)
            .background(iconTint.copy(alpha = 0.10f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun FormatBadge(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(start = 66.dp),
        thickness = 0.5.dp,
        color     = MaterialTheme.colorScheme.outlineVariant,
    )
}