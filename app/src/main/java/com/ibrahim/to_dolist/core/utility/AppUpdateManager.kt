package com.ibrahim.to_dolist.core.utility

import android.app.Activity
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object AppUpdateChecker {

    /**
     * Checks if an update is available and launches the update flow.
     * Uses FLEXIBLE for minor updates, IMMEDIATE for high-priority ones.
     */
    suspend fun checkAndUpdate(activity: Activity) {
        val manager = AppUpdateManagerFactory.create(activity)

        val info = suspendCancellableCoroutine<AppUpdateInfo?> { continuation ->  // ← explicit type
            manager.appUpdateInfo
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener {
                    Log.e("AppUpdateChecker", "Failed to get update info", it)
                    continuation.resume(null)
                }
        } ?: return


        val isAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE

        if (!isAvailable) return

        val updateType = when {
            // Priority 4–5 → force immediate fullscreen update
            info.updatePriority() >= 4 && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                -> AppUpdateType.IMMEDIATE

            // Otherwise → flexible background download
            info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                -> AppUpdateType.FLEXIBLE

            else -> return
        }

        manager.startUpdateFlow(
            info,
            activity,
            AppUpdateOptions.newBuilder(updateType).build(),
        )
    }
}