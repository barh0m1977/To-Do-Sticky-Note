package com.ibrahim.to_dolist

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.fragment.app.FragmentActivity
import com.ibrahim.to_dolist.core.utility.LocaleHelper
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
        // Read language from SharedPreferences
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val language = prefs.getString("language", "en") ?: "en"
        val theme = prefs.getString("theme", "light") ?: "light"
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides if (language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr) {
                ToDoListTheme(theme) {
                    AppNavGraph(viewModel)
                }
            }
        }
    }
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "en") ?: "en"
        val context = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(context)
    }

}



