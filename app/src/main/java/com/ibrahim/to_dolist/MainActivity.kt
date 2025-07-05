package com.ibrahim.to_dolist

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import com.ibrahim.to_dolist.presentation.ui.screens.HomeScreen
import com.ibrahim.to_dolist.presentation.viewmodel.ToDoViewModel
import com.ibrahim.to_dolist.ui.theme.ToDoListTheme


class MainActivity : FragmentActivity () {
    private val viewModel: ToDoViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToDoListTheme {
                HomeScreen(viewModel)
            }
        }
    }
}



