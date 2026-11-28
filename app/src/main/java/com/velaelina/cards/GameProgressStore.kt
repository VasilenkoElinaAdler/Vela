package com.velaelina.cards

import android.content.Context

/** One atomic preference update stores attempt and permanent achievements separately. */
internal class GameProgressStore(context: Context) {
    private val prefs = context.getSharedPreferences("vela_progress", Context.MODE_PRIVATE)
    fun load(): GameProgress {
        if (!prefs.getBoolean("training_migrated_v2",false)) {
            val old = prefs.getStringSet("training_tasks_v1",emptySet())?.toSet().orEmpty()
            return GameRules.migrateLegacy(old,PARTS_TASK_IDS).also { save(it) }
        }
        val lives = prefs.getInt("training_lives_v2",GameRules.INITIAL_HEARTS)
            .coerceIn(0,GameRules.INITIAL_HEARTS)
        return GameProgress(
            lives=lives,
            completed=if(lives==0) emptySet() else prefs.getStringSet("training_attempt_tasks_v2",emptySet())?.toSet().orEmpty(),
            earned=prefs.getStringSet("training_earned_tasks_v2",emptySet())?.toSet().orEmpty(),
            awarded=if(lives==0) emptySet() else prefs.getStringSet("training_awarded_tasks_v2",emptySet())?.toSet().orEmpty()
        )
    }
    fun save(progress: GameProgress) {
        prefs.edit()
            .putBoolean("training_migrated_v2",true)
            .putInt("training_lives_v2",progress.lives)
            .putStringSet("training_attempt_tasks_v2",progress.completed.toSet())
            .putStringSet("training_earned_tasks_v2",progress.earned.toSet())
            .putStringSet("training_awarded_tasks_v2",progress.awarded.toSet())
            .apply()
    }
    fun previousPartsOrder(): List<BoatPart> =
        prefs.getString("parts_order_v1", "").orEmpty().split(",").mapNotNull { name ->
            BoatPart.entries.firstOrNull { it.name == name }
        }
    fun savePartsOrder(order: List<BoatPart>) {
        if(order.isNotEmpty()) prefs.edit().putString("parts_order_v1",order.joinToString(","){it.name}).apply()
    }
    fun restart(): GameProgress = GameRules.newAttempt(load()).also { save(it) }
}
