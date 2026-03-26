package com.ibrahim.to_dolist.core.utility

import android.content.Context

object ReviewPreferences {

    private const val PREFS_NAME     = "review_prefs"
    private const val KEY_TASK_COUNT = "completed_task_count"
    private const val KEY_REVIEW_SHOWN = "review_shown"
    private const val THRESHOLD      = 5

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Call every time a task is completed.
     * Returns true when the review sheet should be shown.
     */
    fun onTaskCompleted(context: Context): Boolean {
        val p = prefs(context)

        // Never ask twice
        if (p.getBoolean(KEY_REVIEW_SHOWN, false)) return false

        val newCount = p.getInt(KEY_TASK_COUNT, 0) + 1
        p.edit().putInt(KEY_TASK_COUNT, newCount).apply()

        return newCount == THRESHOLD
    }

    fun markReviewShown(context: Context) {
        prefs(context).edit().putBoolean(KEY_REVIEW_SHOWN, true).apply()
    }
}