package com.ibrahim.to_dolist.presentation.ui.screens

import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.ibrahim.to_dolist.MainActivity
import com.ibrahim.to_dolist.R
import com.ibrahim.to_dolist.data.model.ToDo
import com.ibrahim.to_dolist.data.model.ToDoState
import com.ibrahim.to_dolist.data.model.ToDoStickyColors
import com.ibrahim.to_dolist.presentation.ui.component.ColorCircle
import com.ibrahim.to_dolist.presentation.ui.component.ToDoStateLabel
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ToDoListScreen
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ToDoViewModel
import com.ibrahim.to_dolist.presentation.util.SortDirection
import com.ibrahim.to_dolist.presentation.util.SortOption
import kotlinx.coroutines.delay

@Composable
fun AnimatedPlaceholder(textFieldValue: String) {
    val fullText = stringResource(R.string.task_text_here)
    var visibleText by remember { mutableStateOf("") }

    LaunchedEffect(key1 = textFieldValue.isEmpty()) {
        while (textFieldValue.isEmpty()) {
            for (i in 1..fullText.length) {
                visibleText = fullText.take(i)
                delay(100)
            }
            delay(500)
            visibleText = ""
            delay(300)
        }
    }

    if (textFieldValue.isEmpty()) {
        Text(visibleText, color = Color.Gray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoTopBar(
    navController: NavController,
    selectedSortOption: SortOption,
    selectedSortDirection: SortDirection,
    onSortOptionChanged: (SortOption) -> Unit,
    onSortDirectionChanged: (SortDirection) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    // Helper to find activity safely
    fun Context.findActivity(): FragmentActivity? {
        var ctx = this
        while (ctx is ContextWrapper) {
            if (ctx is FragmentActivity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    val activity = rememberUpdatedState(context.findActivity())

//    var interstitialAd = rememberInterstitialAd(activity as Activity, "ca-app-pub-8333272977511600/1167511275")

    TopAppBar(
        title = {
            TextButton(
                onClick = {
//                    if (interstitialAd != null) {
//                        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
//                            override fun onAdDismissedFullScreenContent() {
//                                navController.navigate("setting")
//                            }
//                            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
//                                navController.navigate("setting")
//                            }
//                            override fun onAdShowedFullScreenContent() {
//                                interstitialAd = null
//                            }
//                        }
//                        interstitialAd?.show(activity)
//                    } else {
                        navController.navigate("setting")
//                    }
                },
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        actions = {
            // Sort Direction Button
            if (selectedSortOption == SortOption.CREATED_DATE || selectedSortOption == SortOption.MODIFIED_DATE) {
                TextButton(onClick = {
                    val newDirection = if (selectedSortDirection == SortDirection.DESCENDING)
                        SortDirection.ASCENDING
                    else
                        SortDirection.DESCENDING
                    onSortDirectionChanged(newDirection)
                }) {
                    Icon(
                        if (selectedSortDirection == SortDirection.ASCENDING) Icons.Default.ArrowDropUp
                        else Icons.Default.ArrowDropDown,
                        contentDescription = "Sort Icon"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (selectedSortDirection == SortDirection.ASCENDING)
                            stringResource(R.string.ascending)
                        else stringResource(R.string.descending)
                    )
                }
            }

            // Sort Option Dropdown
            Box {
                TextButton(onClick = { expanded = true }) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort Icon")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        when (selectedSortOption) {
                            SortOption.CREATED_DATE -> stringResource(R.string.created)
                            SortOption.MODIFIED_DATE -> stringResource(R.string.modified)
                            SortOption.ONLY_DONE -> stringResource(R.string.only_done)
                            SortOption.ONLY_PENDING -> stringResource(R.string.only_pending)
                            SortOption.ONLY_IN_PROGRESS -> stringResource(R.string.only_in_progress)
                            SortOption.OPENED -> stringResource(R.string.opened)
                            SortOption.LOCKED -> stringResource(R.string.locked)
                        }
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sort_by_created_date)) },
                        onClick = {
                            onSortOptionChanged(SortOption.CREATED_DATE)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sort_by_modified_date)) },
                        onClick = {
                            onSortOptionChanged(SortOption.MODIFIED_DATE)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.show_only_done)) },
                        onClick = {
                            onSortOptionChanged(SortOption.ONLY_DONE)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.show_only_pending)) },
                        onClick = {
                            onSortOptionChanged(SortOption.ONLY_PENDING)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.show_only_in_progress)) },
                        onClick = {
                            onSortOptionChanged(SortOption.ONLY_IN_PROGRESS)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.show_opened)) },
                        onClick = {
                            onSortOptionChanged(SortOption.OPENED)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.show_locked)) },
                        onClick = {
                            onSortOptionChanged(SortOption.LOCKED)
                            expanded = false
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun HomeScreen(viewModel: ToDoViewModel, navController: NavController, mainActivity: MainActivity) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var text by rememberSaveable { mutableStateOf("") }
    var colorVal by rememberSaveable { mutableStateOf(ToDoStickyColors.SUNRISE) }
    var selectedColor by rememberSaveable { mutableStateOf(ToDoStickyColors.SUNRISE) }
    var selectedState by rememberSaveable { mutableStateOf(ToDoState.PENDING) }
    var isLocked by rememberSaveable { mutableStateOf(false) }
    var isChecked by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    var showAd by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            ToDoTopBar(
                navController = navController,
                selectedSortOption = viewModel.sortOption,
                selectedSortDirection = viewModel.sortDirection,
                onSortOptionChanged = viewModel::onSortOptionChanged,
                onSortDirectionChanged = viewModel::onSortDirectionChanged
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Banner Ad
            BannerAd(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
            ToDoListScreen(
                viewModel,
                modifier = Modifier,
                mainActivity
            )

        }

        // New Task Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(stringResource(R.string.new_task)) },
                text = {
                    Column {
                        Text(stringResource(R.string.enter_task_title))
                        Spacer(Modifier.height(8.dp))
                        TextField(
                            value = text,
                            onValueChange = { text = it },
                            placeholder = { AnimatedPlaceholder(textFieldValue = text) }
                        )

                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.select_color))
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ToDoStickyColors.entries.size) { index ->
                                val color = ToDoStickyColors.entries[index]
                                ColorCircle(
                                    color = color.listColor[0],
                                    isSelected = color == selectedColor
                                ) {
                                    selectedColor = color
                                    colorVal = color
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.select_state))
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ToDoState.entries.size) { index ->
                                val state = ToDoState.entries[index]
                                ToDoStateLabel(
                                    state = state,
                                    isSelected = state == selectedState
                                ) {
                                    selectedState = it
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    val biometricManager = BiometricManager.from(context)
                                    when (biometricManager.canAuthenticate(
                                        BiometricManager.Authenticators.BIOMETRIC_STRONG
                                    )) {
                                        BiometricManager.BIOMETRIC_SUCCESS -> {
                                            isLocked = checked
                                            isChecked = checked
                                        }

                                        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                                            isLocked = false
                                            isChecked = false
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.no_fingerprints_enrolled),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }

                                        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                                            isLocked = false
                                            isChecked = false
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.no_fingerprint_hardware),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }

                                        else -> {
                                            isLocked = false
                                            isChecked = false
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.fingerprint_unavailable),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.lock_this_task_with_fingerprint))
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (isLeesThan(text)) {
                            viewModel.addToDo(
                                ToDo(
                                    title = text,
                                    cardColor = colorVal,
                                    state = selectedState,
                                    locked = isLocked
                                )
                            )
                            showDialog = false
                            text = ""
                            isChecked = false
                            isLocked = false
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.should_title_be_short_less_than_13_characters),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }) {
                        Text(stringResource(R.string.add))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        text = ""
                        isLocked = false
                        isChecked = false
                    }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AndroidView(
        modifier = modifier,
        factory = {
            AdView(context).apply {
                adUnitId = "ca-app-pub-8333272977511600/7589163981"
                setAdSize(com.google.android.gms.ads.AdSize.BANNER)
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

fun isLeesThan(text: String): Boolean {
    return text.isNotEmpty() && text.isNotBlank() && text.length <= 13
}
