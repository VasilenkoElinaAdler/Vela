package com.velaelina.cards

/** Single training policy. Temporary attempt progress never qualifies for racing. */
internal object GameRules {
    const val INITIAL_HEARTS = 3
    const val ERROR_HEART_COST = 1
    const val STAR_PER_TASK = 1

    fun answer(progress: GameProgress, taskId: String, correct: Boolean, required: Set<String>): GameProgress {
        require(required.isNotEmpty() && taskId in required)
        if (progress.exhausted || progress.mastered(required)) return progress
        if (!correct) {
            val lives = (progress.lives - ERROR_HEART_COST).coerceAtLeast(0)
            return if (lives == 0) progress.copy(lives=0,completed=emptySet(),awarded=emptySet())
            else progress.copy(lives=lives)
        }
        val completed = progress.completed + taskId
        if (!completed.containsAll(required)) return progress.copy(completed=completed)
        // Atomically finalize only a successfully completed mission, once per stable task ID.
        val awarded = required - progress.earned
        return progress.copy(completed=completed,earned=progress.earned + required,awarded=awarded)
    }

    fun newAttempt(progress: GameProgress): GameProgress = GameProgress(earned=progress.earned)

    /**
     * The old preview did not distinguish attempt/achievement. A complete seven-task
     * set proves all seven were answered, even if a subsequent replay exhausted lives.
     * Partial old sets do not prove mission completion; keep them in untouched legacy
     * preferences, but never promote them to qualification stars.
     */
    fun migrateLegacy(oldCompleted:Set<String>, required:Set<String>):GameProgress =
        if (required.isNotEmpty() && oldCompleted.containsAll(required))
            GameProgress(completed=required,earned=required)
        else GameProgress()
}
internal data class GameProgress(
    val lives: Int = GameRules.INITIAL_HEARTS,
    val completed: Set<String> = emptySet(),
    val earned: Set<String> = emptySet(),
    val awarded: Set<String> = emptySet()
) {
    val stars: Int get() = completed.size * GameRules.STAR_PER_TASK
    val permanentStars: Int get() = earned.size * GameRules.STAR_PER_TASK
    val newStars: Int get() = awarded.size * GameRules.STAR_PER_TASK
    val exhausted: Boolean get() = lives == 0
    fun mastered(required: Set<String>): Boolean = !exhausted && completed.containsAll(required)
}
