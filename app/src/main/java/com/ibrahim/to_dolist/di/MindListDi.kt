package com.ibrahim.to_dolist.di

import com.ibrahim.to_dolist.data.SettingsManager
import com.ibrahim.to_dolist.data.db.ToDoDatabase
import com.ibrahim.to_dolist.presentation.viewmodel.SettingsViewModel
import com.ibrahim.to_dolist.presentation.viewmodel.ToDoViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mindListDi = module {
    single{ ToDoDatabase.getDatabase(get()).toDoDao() } // provide DAO
    viewModel { ToDoViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
    single { SettingsManager(get()) }  // Inject context
}