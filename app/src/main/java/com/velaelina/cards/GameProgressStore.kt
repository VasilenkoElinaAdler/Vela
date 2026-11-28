package com.velaelina.cards

import android.content.Context

/** Independent attempt state per mission; one shared, monotonic achievement ledger.
 * First mission retains its v2 keys so an update preserves the existing attempt.
 */
internal class GameProgressStore(context: Context) {
    private val prefs = context.getSharedPreferences("vela_progress", Context.MODE_PRIVATE)
    private fun key(base:String,mission:PartsMission)=if(mission==PartsMission.TOP) base else base+"_"+mission.storageKey
    private fun earned()=prefs.getStringSet("training_earned_tasks_v2",emptySet())?.toSet().orEmpty()
    private fun ensureMigration() {
        if (!prefs.getBoolean("training_migrated_v2",false)) {
            val old=prefs.getStringSet("training_tasks_v1",emptySet())?.toSet().orEmpty()
            save(GameRules.migrateLegacy(old,PARTS_TASK_IDS),PartsMission.TOP)
        }
    }
    fun isUnlocked(mission:PartsMission):Boolean {
        ensureMigration()
        return mission==PartsMission.TOP || earned().containsAll(PartsMission.TOP.required)
    }
    fun activeMission():PartsMission {
        ensureMigration()
        val selected=PartsMission.entries.firstOrNull{it.storageKey==prefs.getString("training_active_mission_v3","top")} ?: PartsMission.TOP
        return if(isUnlocked(selected)) selected else PartsMission.TOP
    }
    fun select(mission:PartsMission):Boolean {
        if(!isUnlocked(mission))return false
        prefs.edit().putString("training_active_mission_v3",mission.storageKey).apply()
        return true
    }
    fun load(mission:PartsMission=activeMission()):GameProgress {
        ensureMigration()
        val lives=prefs.getInt(key("training_lives_v2",mission),GameRules.INITIAL_HEARTS).coerceIn(0,GameRules.INITIAL_HEARTS)
        return GameProgress(
            lives=lives,
            completed=if(lives==0)emptySet() else prefs.getStringSet(key("training_attempt_tasks_v2",mission),emptySet())?.toSet().orEmpty().intersect(mission.required),
            earned=earned(),
            awarded=if(lives==0)emptySet() else prefs.getStringSet(key("training_awarded_tasks_v2",mission),emptySet())?.toSet().orEmpty().intersect(mission.required)
        )
    }
    fun save(progress:GameProgress,mission:PartsMission=activeMission()) {
        prefs.edit()
            .putBoolean("training_migrated_v2",true)
            .putInt(key("training_lives_v2",mission),progress.lives)
            .putStringSet(key("training_attempt_tasks_v2",mission),progress.completed.intersect(mission.required))
            .putStringSet("training_earned_tasks_v2",earned()+progress.earned)
            .putStringSet(key("training_awarded_tasks_v2",mission),progress.awarded.intersect(mission.required))
            .apply()
    }
    fun previousPartsOrder(mission:PartsMission=activeMission()):List<BoatPart> =
        prefs.getString(key("parts_order_v1",mission),"").orEmpty().split(",").mapNotNull{ name -> mission.parts.firstOrNull{it.name==name} }
    fun savePartsOrder(order:List<BoatPart>,mission:PartsMission=activeMission()) {
        if(order.isNotEmpty()) prefs.edit().putString(key("parts_order_v1",mission),order.joinToString(","){it.name}).apply()
    }
    fun restart(mission:PartsMission=activeMission()):GameProgress=GameRules.newAttempt(load(mission)).also{save(it,mission)}
}
