package com.ibrahim.to_dolist

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import com.ibrahim.to_dolist.navigation.AppNavGraph
import com.ibrahim.to_dolist.presentation.viewmodel.ToDoViewModel
import com.ibrahim.to_dolist.ui.theme.ToDoListTheme


class MainActivity : FragmentActivity () {
    private val viewModel: ToDoViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            enableEdgeToEdge()
        }
        setContent {
            ToDoListTheme {
                AppNavGraph(viewModel)
            }
        }
    }
}



