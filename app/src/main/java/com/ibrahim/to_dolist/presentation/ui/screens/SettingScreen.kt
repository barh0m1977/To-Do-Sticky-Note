package com.ibrahim.to_dolist.presentation.ui.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import com.ibrahim.to_dolist.R

enum class AppTheme { LIGHT, DARK, SYSTEM }
enum class DialogStyle { NORMAL, CUSTOM }
enum class AppLanguage { ENGLISH, ARABIC }

@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    var language by remember { mutableStateOf(AppLanguage.ENGLISH) }
    var theme by remember { mutableStateOf(AppTheme.SYSTEM) }
    var dialogStyle by remember { mutableStateOf(DialogStyle.NORMAL) }

    val version = getAppVersion(context)

    Scaffold(){
        it ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
        ) {
            item {
                Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Language
            item {
                SettingItem(title = "Language", subtitle = language.name) {
                    language = if (language == AppLanguage.ENGLISH) AppLanguage.ARABIC else AppLanguage.ENGLISH
                }
            }

            // Theme
            item {
                SettingItem(title = "Theme", subtitle = theme.name) {
                    theme = when (theme) {
                        AppTheme.LIGHT -> AppTheme.DARK
                        AppTheme.DARK -> AppTheme.SYSTEM
                        AppTheme.SYSTEM -> AppTheme.LIGHT
                    }
                }
            }

            // Dialog style
            item {
                SettingItem(title = "Dialog Style", subtitle = dialogStyle.name) {
                    dialogStyle = when (dialogStyle) {
                        DialogStyle.NORMAL -> DialogStyle.CUSTOM
                        DialogStyle.CUSTOM -> DialogStyle.NORMAL
                    }
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
