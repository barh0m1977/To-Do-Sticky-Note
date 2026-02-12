package com.ibrahim.to_dolist

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.ads.MobileAds
import com.ibrahim.to_dolist.core.utility.LocaleHelper
import com.ibrahim.to_dolist.data.settings.SettingsManager
import com.ibrahim.to_dolist.data.settings.SettingsRepository
import com.ibrahim.to_dolist.navigation.AppNavGraph
import com.ibrahim.to_dolist.presentation.ui.screens.settings.AppLanguage
import com.ibrahim.to_dolist.presentation.ui.screens.settings.SettingsViewModel
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ActionType
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ToDoAction
import com.ibrahim.to_dolist.presentation.ui.screens.todolist.ToDoViewModel
import com.ibrahim.to_dolist.ui.theme.ToDoListTheme
import com.ibrahim.to_dolist.util.BiometricHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : FragmentActivity()  {

    private val viewModel: ToDoViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(SettingsManager(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) enableEdgeToEdge()

        CoroutineScope(Dispatchers.IO).launch { MobileAds.initialize(this@MainActivity) {} }

        setContent {
            val language by settingsViewModel.language.collectAsState()
            val theme by settingsViewModel.theme.collectAsState()

            // Compose context with selected locale
            val localizedContext = LocaleHelper.setLocale(LocalContext.current, language.name.lowercase())

            LaunchedEffect(Unit) {
                viewModel.action.collect { action ->
                    when(action) {

                        is ToDoAction.RequestBiometric -> {
                            BiometricHelper(
                                context = this@MainActivity,
                                onSuccess = {
                                    when(action.afterSuccess) {
                                        ActionType.OPEN -> viewModel.openAfterBiometric(action.todo)
                                        ActionType.DELETE -> viewModel.deleteAfterBiometric(action.todo)
                                        ActionType.EDIT -> viewModel.editeAfterBiometric(action.todo)
                                    }
                                },
                                onError = { Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show() }
                            ).authenticate()
                        }

                        is ToDoAction.OpenTodo -> viewModel.openAfterBiometric(action.todo)
                        is ToDoAction.DeleteTodo -> viewModel.deleteAfterBiometric(action.todo)
                        is ToDoAction.EditTodo -> viewModel.editeAfterBiometric(action.todo)
                        is ToDoAction.ShowMessage -> Toast.makeText(this@MainActivity, action.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }




            CompositionLocalProvider(
                LocalLayoutDirection provides if (language == AppLanguage.AR) LayoutDirection.Rtl else LayoutDirection.Ltr,
                LocalContext provides localizedContext
            ) {
                ToDoListTheme(theme.name) {
                    AppNavGraph(viewModel, settingsViewModel)
                }
            }
        }
    }
}

class SettingsViewModelFactory(
    private val repository: SettingsRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}




