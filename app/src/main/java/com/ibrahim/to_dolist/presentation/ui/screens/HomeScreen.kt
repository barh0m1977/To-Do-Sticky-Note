package com.ibrahim.to_dolist.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ibrahim.to_dolist.MainActivity
import com.ibrahim.to_dolist.R
import com.ibrahim.to_dolist.presentation.ui.component.TaskSheet
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ToDoListScreen
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ToDoViewModel
import com.ibrahim.to_dolist.presentation.util.SortDirection
import com.ibrahim.to_dolist.presentation.util.SortOption
import kotlinx.coroutines.launch


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

            ToDoListScreen(
                viewModel,
                modifier = Modifier,
                mainActivity,
                navController
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

    }

}
