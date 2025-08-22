package com.ibrahim.to_dolist.presentation.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import com.ibrahim.to_dolist.R
import com.ibrahim.to_dolist.presentation.viewmodel.SettingsViewModel

enum class AppTheme { LIGHT, DARK, SYSTEM }
enum class DialogStyle { NORMAL, CUSTOM }
enum class AppLanguage { ENGLISH, ARABIC }

@Composable
fun SettingsScreen(viewModel : SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val context = LocalContext.current

    // Collect values from ViewModel (StateFlow → Compose state)
    val language by viewModel.language.collectAsState()
    val theme by viewModel.theme.collectAsState()
    val dialogStyle by viewModel.dialogStyle.collectAsState()

    val version = getAppVersion(context)

    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.settings),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Language
            item {
                SettingItem(title = "Language", subtitle = language) {
                    val newLang = if (language == "en") "ar" else "en"
                    viewModel.updateLanguage(newLang)
                    val activity = context as? Activity
                    activity?.recreate()
                }
            }

            // Theme
            item {
                SettingItem(title = "Theme", subtitle = theme) {
                    val newTheme = when (theme) {
                        "light" -> "dark"
                        "dark" -> "system"
                        else -> "light"
                    }
                    viewModel.updateTheme(newTheme)
                    val activity = context as? Activity
                    activity?.recreate()
                }
            }

            // Dialog style
            item {
                SettingItem(title = "Dialog Style", subtitle = dialogStyle) {
                    val newStyle = if (dialogStyle == "normal") "custom" else "normal"
                    viewModel.updateDialogStyle(newStyle)
                }
            }

            // Feedback
            item {
                SettingItem(title = "Send Feedback") {
                    sendFeedbackEmail(context)
                }
            }

            // Privacy Policy
            item {
                SettingItem(title = "Privacy Policy") {
                    openPrivacyPolicy(context)
                }
            }

            // App Version
            item {
                SettingItem(title = "App Version", subtitle = version)
            }
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 12.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        subtitle?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    Divider()
}

fun sendFeedbackEmail(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:lubbadibrahim0@gmail.com".toUri()
        putExtra(Intent.EXTRA_SUBJECT, "App Feedback")
    }
    context.startActivity(Intent.createChooser(intent, "Send Feedback"))
}

fun openPrivacyPolicy(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, "https://barh0m1977.github.io/MindList_indexer-privacy-policy/".toUri())
    context.startActivity(intent)
}

fun getAppVersion(context: Context): String {
    return try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        "${pInfo.versionName} (${PackageInfoCompat.getLongVersionCode(pInfo)})"
    } catch (e: Exception) {
        "Unknown"
    }
}
