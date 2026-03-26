package com.ibrahim.to_dolist.core.utility

import android.content.Context
import com.ibrahim.to_dolist.presentation.util.CardStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

object CardStylePreference {
    private const val PREFS_NAME = "appearance_prefs"
    private const val KEY = "card_style"

    // ✅ A shared flow that emits whenever save() is called
    private val _styleFlow = MutableStateFlow<CardStyle?>(null)

    fun observe(context: Context): Flow<CardStyle> {
        // Seed the flow with the current saved value on first read
        if (_styleFlow.value == null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val name = prefs.getString(KEY, CardStyle.OUTLINED.name) ?: CardStyle.OUTLINED.name
            _styleFlow.value = runCatching { CardStyle.valueOf(name) }.getOrDefault(CardStyle.OUTLINED)
        }
        return _styleFlow.filterNotNull()
    }

    fun save(context: Context, style: CardStyle) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, style.name)
            .apply()
        _styleFlow.value = style // ✅ This triggers recomposition
    }
}