package com.ibrahim.to_dolist.core.utility

import android.app.Activity
import android.util.Log
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object AppReviewManager {

    /**
     * Requests the Play Store review flow.
     * Google decides internally whether to actually show the dialog —
     * it throttles per user so it won't appear on every call.
     */
    suspend fun launchReviewFlow(activity: Activity) {
        val manager = ReviewManagerFactory.create(activity)

        val reviewInfo = requestReviewInfo(manager) ?: return

        suspendCancellableCoroutine { continuation ->
            manager.launchReviewFlow(activity, reviewInfo)
                .addOnCompleteListener {
                    // Always completes — even if the dialog was suppressed by Google.
                    // Do NOT check for success/failure here; the API intentionally
                    // hides whether the user actually rated or not.
                    if (continuation.isActive) continuation.resume(Unit)
                }
        }
    }

    private suspend fun requestReviewInfo(
        manager: com.google.android.play.core.review.ReviewManager,
    ): ReviewInfo? = suspendCancellableCoroutine { continuation ->
        manager.requestReviewFlow()
            .addOnSuccessListener { info ->
                if (continuation.isActive) continuation.resume(info)
            }
            .addOnFailureListener { e ->
                Log.e("AppReviewManager", "Failed to request review info", e)
                if (continuation.isActive) continuation.resume(null)
            }
    }
}