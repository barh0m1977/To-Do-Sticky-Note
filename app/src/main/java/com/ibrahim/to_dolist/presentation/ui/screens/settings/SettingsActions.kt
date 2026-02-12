package com.ibrahim.to_dolist.presentation.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri

object SettingsActions {

    fun handelEvent(context: Context, event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ShowMessage -> {
                Toast.makeText(
                    context,
                    event.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

            SettingsEvent.SendFeedback -> sendFeedback(context)
            SettingsEvent.OpenPrivacyPolicy -> openPrivacyPolicy(context)


        }
    }

    private fun sendFeedback(context: Context) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:lubbadibrahim0@gmail.com".toUri()
            putExtra(Intent.EXTRA_SUBJECT, "MindList Feedback")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        // check if an app can handle the intent
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No email app found.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPrivacyPolicy(context: Context) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "https://barh0m1977.github.io/MindList_indexer-privacy-policy/".toUri()
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try{
            context.startActivity(intent)
        }catch(e: ActivityNotFoundException){
            Toast.makeText(context, "No browser app found.", Toast.LENGTH_SHORT).show()
        }
    }
}