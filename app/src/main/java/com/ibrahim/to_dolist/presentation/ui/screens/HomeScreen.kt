package com.ibrahim.to_dolist.presentation.ui.screens

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.ibrahim.to_dolist.data.model.ToDoState
import com.ibrahim.to_dolist.data.model.ToDoStickyColors
import com.ibrahim.to_dolist.presentation.ui.component.TaskSheet
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ToDoListScreen
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ToDoViewModel
import com.ibrahim.to_dolist.presentation.util.SortDirection
import com.ibrahim.to_dolist.presentation.util.SortOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 4.dp,
            shadowElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
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

                    // Sort Direction (only for date options)
                    if (selectedSortOption == SortOption.CREATED_DATE ||
                        selectedSortOption == SortOption.MODIFIED_DATE
                    ) {
                        IconButton(
                            onClick = {
                                val newDirection =
                                    if (selectedSortDirection == SortDirection.DESCENDING)
                                        SortDirection.ASCENDING
                                    else SortDirection.DESCENDING

                                onSortDirectionChanged(newDirection)
                            }
                        ) {
                            Icon(
                                imageVector =
                                    if (selectedSortDirection == SortDirection.ASCENDING)
                                        Icons.Default.ArrowDropUp
                                    else Icons.Default.ArrowDropDown,
                                contentDescription = "Sort Direction"
                            )
                        }
                    }

                    // Sort Menu
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort"
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.name.replace("_", " ")) },
                                    onClick = {
                                        onSortOptionChanged(option)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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


    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()
    var showSheet by rememberSaveable { mutableStateOf(false) }

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
            FloatingActionButton(onClick = { showSheet = true }) {
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

        //new task sheet
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState
            ) {

                TaskSheet(
                    onTaskAction = { todo ->

                        viewModel.addToDo(todo)

                        scope.launch {
                            sheetState.hide()
                            showSheet = false
                        }
                    }
                )
            }
        }

        // New Task Dialog
//        if (showDialog) {

//            AlertDialog(
//                onDismissRequest = { showDialog = false },
//                title = { Text(stringResource(R.string.new_task)) },
//                text = {
//                    Column {
//                        Text(stringResource(R.string.enter_task_title))
//                        Spacer(Modifier.height(8.dp))
//                        TextField(
//                            value = text,
//                            onValueChange = { text = it },
//                            placeholder = { AnimatedPlaceholder(textFieldValue = text) }
//                        )
//
//                        Spacer(Modifier.height(8.dp))
//                        Text(stringResource(R.string.select_color))
//                        Spacer(Modifier.height(8.dp))
//                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                            items(ToDoStickyColors.entries.size) { index ->
//                                val color = ToDoStickyColors.entries[index]
//                                ColorCircle(
//                                    color = color.listColor[0],
//                                    isSelected = color == selectedColor
//                                ) {
//                                    selectedColor = color
//                                    colorVal = color
//                                }
//                            }
//                        }
//
//                        Spacer(Modifier.height(8.dp))
//                        Text(stringResource(R.string.select_state))
//                        Spacer(Modifier.height(8.dp))
//                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                            items(ToDoState.entries.size) { index ->
//                                val state = ToDoState.entries[index]
//                                ToDoStateLabel(
//                                    state = state,
//                                    isSelected = state == selectedState
//                                ) {
//                                    selectedState = it
//                                }
//                            }
//                        }
//
//                        Spacer(Modifier.height(12.dp))
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            modifier = Modifier.fillMaxWidth()
//                        ) {
//                            Checkbox(
//                                checked = isChecked,
//                                onCheckedChange = { checked ->
//                                    val biometricManager = BiometricManager.from(context)
//                                    when (biometricManager.canAuthenticate(
//                                        BiometricManager.Authenticators.BIOMETRIC_STRONG
//                                    )) {
//                                        BiometricManager.BIOMETRIC_SUCCESS -> {
//                                            isLocked = checked
//                                            isChecked = checked
//                                        }
//
//                                        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
//                                            isLocked = false
//                                            isChecked = false
//                                            Toast.makeText(
//                                                context,
//                                                context.getString(R.string.no_fingerprints_enrolled),
//                                                Toast.LENGTH_LONG
//                                            ).show()
//                                        }
//
//                                        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
//                                            isLocked = false
//                                            isChecked = false
//                                            Toast.makeText(
//                                                context,
//                                                context.getString(R.string.no_fingerprint_hardware),
//                                                Toast.LENGTH_LONG
//                                            ).show()
//                                        }
//
//                                        else -> {
//                                            isLocked = false
//                                            isChecked = false
//                                            Toast.makeText(
//                                                context,
//                                                context.getString(R.string.fingerprint_unavailable),
//                                                Toast.LENGTH_LONG
//                                            ).show()
//                                        }
//                                    }
//                                }
//                            )
//                            Spacer(Modifier.width(8.dp))
//                            Text(stringResource(R.string.lock_this_task_with_fingerprint))
//                        }
//                    }
//                },
//                confirmButton = {
//                    Button(onClick = {
//                        if (isLeesThan(text)) {
//                            viewModel.addToDo(
//                                ToDo(
//                                    title = text,
//                                    cardColor = colorVal,
//                                    state = selectedState,
//                                    locked = isLocked
//                                )
//                            )
//                            showDialog = false
//                            text = ""
//                            isChecked = false
//                            isLocked = false
//                        } else {
//                            Toast.makeText(
//                                context,
//                                context.getString(R.string.should_title_be_short_less_than_13_characters),
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//                    }) {
//                        Text(stringResource(R.string.add))
//                    }
//                },
//                dismissButton = {
//                    TextButton(onClick = {
//                        showDialog = false
//                        text = ""
//                        isLocked = false
//                        isChecked = false
//                    }) {
//                        Text(stringResource(R.string.cancel))
//                    }
//                }
//            )
        }
    }
//}

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
