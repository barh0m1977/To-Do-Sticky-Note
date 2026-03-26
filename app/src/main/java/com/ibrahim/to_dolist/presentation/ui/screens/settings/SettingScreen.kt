package com.ibrahim.to_dolist.presentation.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.Style
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ibrahim.to_dolist.R
import com.ibrahim.to_dolist.core.utility.CardStylePreference
import com.ibrahim.to_dolist.data.model.ToDoState
import com.ibrahim.to_dolist.data.model.ToDoStickyColors
import com.ibrahim.to_dolist.presentation.ui.component.CardStickyNote
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ToDoViewModel
import com.ibrahim.to_dolist.presentation.util.CardStyle
import com.ibrahim.to_dolist.util.getAppVersion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


// ─── Palette ──────────────────────────────────────────────────────────────────
private val SuccessGreen = Color(0xFF00C853)
private val ErrorRed = Color(0xFFD50000)

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel,
    todoViewModel: ToDoViewModel,
    onRequestImport: (ImportFormat) -> Unit,
) {
    val language by viewModel.language.collectAsState()
    val theme by viewModel.theme.collectAsState()
    val importState by viewModel.importState.collectAsState()
    val exportState by viewModel.exportState.collectAsState()       // ✅ ExportState, not File?
    val context = LocalContext.current
    val version = getAppVersion(context)
    val todosWithTasks by todoViewModel.todosWithTasks.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    var selectedImportFormat by remember { mutableStateOf(ImportFormat.CSV) }
    var selectedExportFormat by remember { mutableStateOf(ExportFormat.CSV) }

    val savedStyle by CardStylePreference.observe(context)
        .collectAsState(initial = CardStyle.OUTLINED)

    val scope = rememberCoroutineScope()

    // ── Snackbar: export state ────────────────────────────────────────────────
    LaunchedEffect(exportState) {
        when (val s = exportState) {
            is ExportState.Success -> {
                snackbarHost.showSnackbar(
                    "✓ Exported: ${s.file.name}",
                    duration = SnackbarDuration.Short,
                )
                viewModel.clearExportState()
            }

            is ExportState.Error -> {
                snackbarHost.showSnackbar(
                    "Export failed: ${s.message}",
                    duration = SnackbarDuration.Long,
                )
                viewModel.clearExportState()
            }

            is ExportState.Loading, ExportState.Idle -> Unit
        }
    }

    // ── Snackbar: import state ────────────────────────────────────────────────
    LaunchedEffect(importState) {
        when (val s = importState) {
            is ImportState.Success -> {
                // ✅ Use the rich ImportResult message instead of a raw count
                snackbarHost.showSnackbar(
                    "✓ ${s.result.toMessage()}",
                    duration = SnackbarDuration.Long,
                )
                viewModel.clearImportState()
            }

            is ImportState.Error -> {
                snackbarHost.showSnackbar(
                    "Import failed: ${s.message}",
                    duration = SnackbarDuration.Long,
                )
                viewModel.clearImportState()
            }

            is ImportState.Loading, ImportState.Idle -> Unit
        }
    }

    // ── Events (feedback / privacy policy) ───────────────────────────────────
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
            item { SectionLabel(stringResource(R.string.appearance)) }
            item {
                SettingsGroup {
                    DropdownSettingRow(
                        title = stringResource(R.string.language),
                        subtitle = language.name,
                        icon = Icons.Default.Language,
                        options = AppLanguage.entries.map { it.name },
                        isLast = false,
                    ) { viewModel.selectLanguage(AppLanguage.valueOf(it)) }

                    DropdownSettingRow(
                        title = stringResource(R.string.theme),
                        subtitle = theme.name,
                        icon = Icons.Default.Brightness4,
                        options = AppTheme.entries.map { it.name },
                        isLast = true,
                    ) { viewModel.selectTheme(AppTheme.valueOf(it)) }
                    CardStylePickerRow(
                        selected = savedStyle,
                        onStyleSelected = { CardStylePreference.save(context, it) },
                    )
                }
            }

            // ── Data ──────────────────────────────────────────────────────────
            item { SectionLabel(stringResource(R.string.data)) }
            item {
                SettingsGroup {
                    DropdownSettingRow(
                        title = stringResource(R.string.export),
                        // ✅ Reflect ExportState in the subtitle
                        subtitle = when (exportState) {
                            is ExportState.Loading -> "Exporting…"
                            is ExportState.Success -> "Exported: ${(exportState as ExportState.Success).file.name}"
                            is ExportState.Error -> "Failed — tap to retry"
                            ExportState.Idle -> "Save as ${selectedExportFormat.name}"
                        },
                        icon = Icons.Default.IosShare,
                        options = ExportFormat.entries.map { it.name },
                        isLast = false,
                        trailingContent = {
                            // ✅ Show spinner during export, badge otherwise
                            when (exportState) {
                                is ExportState.Loading -> CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp), strokeWidth = 2.dp,
                                )

                                is ExportState.Error -> Icon(
                                    Icons.Default.Error, null,
                                    tint = ErrorRed, modifier = Modifier.size(20.dp),
                                )

                                is ExportState.Success -> Icon(
                                    Icons.Default.CheckCircle, null,
                                    tint = SuccessGreen, modifier = Modifier.size(20.dp),
                                )

                                ExportState.Idle -> FormatBadge(selectedExportFormat.name)
                            }
                        },
                    ) { selected ->
                        selectedExportFormat = ExportFormat.valueOf(selected)
                        viewModel.exportTasks(context, todosWithTasks, selectedExportFormat)
                    }

                    ImportSettingRow(
                        selectedFormat = selectedImportFormat,
                        importState = importState,
                        onFormatPicked = { selectedImportFormat = ImportFormat.valueOf(it) },
                        onLaunchPicker = { onRequestImport(selectedImportFormat) },
                        isLast = true,
                    )
                }
            }

            // ── Support ───────────────────────────────────────────────────────
            item { SectionLabel(stringResource(R.string.support)) }
            item {
                SettingsGroup {
                    PlainSettingRow(
                        title = stringResource(R.string.send_feedback),
                        icon = Icons.Default.Email,
                        isLast = false,
                    ) { viewModel.sendEvent(SettingsEvent.SendFeedback) }

                    PlainSettingRow(
                        title = stringResource(R.string.privacy_policy),
                        icon = Icons.Default.Gavel,
                        isLast = true,
                    ) { viewModel.sendEvent(SettingsEvent.OpenPrivacyPolicy) }
                }
            }

            // ── About ─────────────────────────────────────────────────────────
            item { SectionLabel(stringResource(R.string.about)) }
            item {
                SettingsGroup {
                    PlainSettingRow(
                        title = stringResource(R.string.version),
                        subtitle = version,
                        icon = Icons.Default.Code,
                        isLast = true,
                        onClick = {},
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ─── Import row ───────────────────────────────────────────────────────────────

@Composable
private fun ImportSettingRow(
    selectedFormat: ImportFormat,
    importState: ImportState,
    onFormatPicked: (String) -> Unit,
    onLaunchPicker: () -> Unit,
    isLast: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    val isLoading = importState is ImportState.Loading

    // Auto-collapse once a result arrives so the row feels responsive
    LaunchedEffect(importState) {
        if (importState is ImportState.Success || importState is ImportState.Error) {
            expanded = false
        }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isLoading) {
                    if (expanded) {
                        expanded = false; onLaunchPicker()
                    } else expanded = true
                }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconContainer(Icons.Default.GetApp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.import_from),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = when (val s = importState) {
                        is ImportState.Loading -> "Importing…"
                        // ✅ Show the rich result summary in the subtitle
                        is ImportState.Success -> "✓ ${s.result.toMessage()}"
                        is ImportState.Error -> "Failed — tap to retry"
                        ImportState.Idle -> "Pick ${selectedFormat.name} file"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (importState) {
                        is ImportState.Error -> ErrorRed
                        is ImportState.Success -> SuccessGreen
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            // Trailing icon reflects current state
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
            enter = expandVertically(tween(200)) + fadeIn(tween(200)),
            exit = shrinkVertically(tween(180)) + fadeOut(tween(180)),
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
                // CTA
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
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp),
    )
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) { content() }
}

@Composable
private fun PlainSettingRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    isLast: Boolean,
    onClick: () -> Unit,
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

@Composable
private fun DropdownSettingRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    options: List<String>,
    isLast: Boolean,
    trailingContent: (@Composable () -> Unit)? = null,
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

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(200)) + fadeIn(tween(200)),
            exit = shrinkVertically(tween(180)) + fadeOut(tween(180)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = 4.dp),
            ) {
                options.forEach { option ->
                    Text(
                        text = option,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(option); expanded = false }
                            .padding(horizontal = 54.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        if (!isLast) RowDivider()
    }
}

@Composable
private fun IconContainer(icon: ImageVector) {
    val iconTint = MaterialTheme.colorScheme.secondary
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
private fun CardStylePickerRow(
    selected: CardStyle,
    onStyleSelected: (CardStyle) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconContainer(Icons.Default.Style) // or any icon you prefer
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.card_style), style = MaterialTheme.typography.bodyLarge)
                Text(
                    selected.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FormatBadge(selected.name)
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(200)) + fadeIn(tween(200)),
            exit = shrinkVertically(tween(180)) + fadeOut(tween(180)),
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(CardStyle.entries) { style ->
                    val isSelected = style == selected
                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Transparent,
                        label = "border_${style}",
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(110.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(2.dp, borderColor, RoundedCornerShape(10.dp))
                            .clickable { onStyleSelected(style); expanded = false }
                            .padding(4.dp),
                    ) {
                        // ── Mini card preview ──────────────────────────────
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(modifier = Modifier
                                .scale(0.75f)
                                .fillMaxWidth()) {
                                CardStickyNote(
                                    text = "My task",
                                    colorArray = ToDoStickyColors.SUNRISE,
                                    state = ToDoState.IN_PROGRESS,
                                    isLocked = false,
                                    cardStyle = style,
                                    onDeleteConfirmed = {},
                                    onEditConfirmed = {},
                                    onClick = {
                                        onStyleSelected(style)
                                        scope.launch {
                                            delay(200) // let the border animation play first
                                            expanded = false
                                        }
                                    },
                                )
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        // ── Label + checkmark ──────────────────────────────
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(bottom = 4.dp),
                        ) {
                            Text(
                                text = style.toString()
                                    .replace("_", " ")
                                    .lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (isSelected) {
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.CheckCircle, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        RowDivider()
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 66.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsDataSectionPreview() {
    // Dummy state for preview
    var selectedImportFormat by remember { mutableStateOf(ImportFormat.CSV) }
    var selectedExportFormat by remember { mutableStateOf(ExportFormat.CSV) }
    val importState = ImportState.Idle
    val exportState = ExportState.Idle

    val dummyTodos = emptyList<Any>() // replace with preview data if needed
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SectionLabel("Data") // Section title

        SettingsGroup {
            DropdownSettingRow(
                title = "Export",
                subtitle = when (exportState) {
                    is ExportState.Loading -> "Exporting…"
                    is ExportState.Success -> "Exported: ${(exportState as ExportState.Success).file.name}"
                    is ExportState.Error -> "Failed — tap to retry"
                    ExportState.Idle -> "Save as ${selectedExportFormat.name}"
                    else -> "" // fallback for compiler; won't happen in preview
                },
                icon = Icons.Default.IosShare,
                options = ExportFormat.entries.map { it.name },
                isLast = false,
                trailingContent = {
                    when (exportState) {
                        is ExportState.Loading -> CircularProgressIndicator(
                            modifier = Modifier.size(20.dp), strokeWidth = 2.dp
                        )

                        is ExportState.Error -> Icon(
                            Icons.Default.Error, null,
                            tint = ErrorRed, modifier = Modifier.size(20.dp)
                        )

                        is ExportState.Success -> Icon(
                            Icons.Default.CheckCircle, null,
                            tint = SuccessGreen, modifier = Modifier.size(20.dp)
                        )

                        ExportState.Idle -> FormatBadge(selectedExportFormat.name)
                    }
                }
            ) { selected ->
                selectedExportFormat = ExportFormat.valueOf(selected)
                // preview: do nothing
            }

            ImportSettingRow(
                selectedFormat = selectedImportFormat,
                importState = importState,
                onFormatPicked = { selectedImportFormat = ImportFormat.valueOf(it) },
                onLaunchPicker = { /* preview: do nothing */ },
                isLast = true
            )
        }
    }
}