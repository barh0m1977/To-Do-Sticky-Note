package com.ibrahim.to_dolist.presentation.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.ibrahim.to_dolist.R
import com.ibrahim.to_dolist.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),navController: NavController) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val language by viewModel.language.collectAsState()
    val theme by viewModel.theme.collectAsState()
    val dialogStyle by viewModel.dialogStyle.collectAsState()

    val version = getAppVersion(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { activity?.onBackPressedDispatcher?.onBackPressed() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingCard(
                    title = stringResource(R.string.language),
                    subtitle = if (language == "en") stringResource(R.string.english) else "العربية",
                    icon = Icons.Default.Language
                ) {
                    val newLang = if (language == "en") "ar" else "en"
                    viewModel.updateLanguage(newLang)
                    (context as? Activity)?.recreate()
                }
            }

            item {
                SettingCard(
                    title = stringResource(R.string.theme),
                    subtitle = theme.replaceFirstChar { it.uppercase() },
                    icon = Icons.Default.Brightness4
                ) {
                    val newTheme = when (theme) {
                        context.getString(R.string.light) -> context.getString(R.string.dark)
                        context.getString(R.string.dark) -> context.getString(R.string.system)
                        else -> context.getString(R.string.light)
                    }
                    viewModel.updateTheme(newTheme)
                    (context as? Activity)?.recreate()
                }
            }

            // in new version 2.1.1
//            item {
//                SettingCard(
//                    title = "Dialog Style",
//                    subtitle = dialogStyle.replaceFirstChar { it.uppercase() },
//                    icon = Icons.Default.Style
//                ) {
//                    val newStyle = if (dialogStyle == "normal") "custom" else "normal"
//                    viewModel.updateDialogStyle(newStyle)
//                }
//            }

            item {
                SettingCard(
                    title = stringResource(R.string.send_feedback),
                    icon = Icons.Default.Email
                ) {
                    sendFeedbackEmail(context)
                }
            }

            item {
                SettingCard(
                    title = stringResource(R.string.privacy_policy),
                    icon = Icons.Default.Gavel
                ) {
                    openPrivacyPolicy(context)
                }
            }

            item {
                SettingCard(
                    title = stringResource(R.string.app_version),
                    subtitle = version,
                    icon = Icons.Default.Style,
                    clickable = false
                )
            }
        }
    }
}

@Composable
fun SettingCard(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    clickable: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = clickable && onClick != null) { onClick?.invoke() },
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                subtitle?.let {
                    AnimatedContent(targetState = it) { target ->
                        Text(
                            text = target,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

fun sendFeedbackEmail(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:lubbadibrahim0@gmail.com".toUri()
        putExtra(Intent.EXTRA_SUBJECT, "App Feedback")
    }
    context.startActivity(Intent.createChooser(intent, "Send Feedback"))
}

fun openPrivacyPolicy(context: Context) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        "https://barh0m1977.github.io/MindList_indexer-privacy-policy/".toUri()
    )
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
