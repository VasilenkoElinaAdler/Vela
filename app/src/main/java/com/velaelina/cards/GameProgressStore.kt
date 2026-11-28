package com.velaelina.cards

import android.content.Context

/** Versioned shared training progress, independent of legacy collection data. */
internal class GameProgressStore(context: Context) {
    private val prefs = context.getSharedPreferences("vela_progress", Context.MODE_PRIVATE)
    fun load(): GameProgress = GameProgress(
        lives = prefs.getInt("training_lives_v1", GameRules.INITIAL_HEARTS)
            .coerceIn(0, GameRules.INITIAL_HEARTS),
        completed = (prefs.getStringSet("training_tasks_v1", emptySet()) ?: emptySet()).toSet()
    )
    fun save(progress: GameProgress) {
        prefs.edit().putInt("training_lives_v1", progress.lives)
            .putStringSet("training_tasks_v1", progress.completed.toSet()).apply()
    }
    // Store round ordering independently of earned stars; a new practice round
    // must not accidentally reproduce the previous full order.
    fun previousPartsOrder(): List<BoatPart> =
        prefs.getString("parts_order_v1", "").orEmpty().split(",").mapNotNull { name ->
            BoatPart.entries.firstOrNull { it.name == name }
        }
    fun savePartsOrder(order: List<BoatPart>) {
        if(order.isNotEmpty()) prefs.edit().putString("parts_order_v1",order.joinToString(","){it.name}).apply()
    }
    fun restart(): GameProgress = GameProgress().also { save(it) }
}
