package com.ibrahim.to_dolist.util

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat

fun getAppVersion(context: Context): String {
    return try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        "${pInfo.versionName} (${PackageInfoCompat.getLongVersionCode(pInfo)})"
    } catch (e: Exception) {
        "Unknown"
    }
}