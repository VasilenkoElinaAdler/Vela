package com.velaelina.cards

/**
 * Canonical training rules. The timed competition will use this model with a
 * separately documented race outcome policy, never a second health balance.
 * These numerical costs are first-lesson test parameters, not release tuning.
 */
internal object GameRules {
    const val INITIAL_HEARTS = 10
    const val ERROR_HEART_COST = 1
    const val STAR_PER_TASK = 1

    fun answer(progress: GameProgress, taskId: String, correct: Boolean): GameProgress {
        require(taskId.isNotBlank())
        if (progress.lives == 0) return progress
        return if (correct) progress.copy(completed = progress.completed + taskId)
        else progress.copy(lives = (progress.lives - ERROR_HEART_COST).coerceAtLeast(0))
    }
}
internal data class GameProgress(
    val lives: Int = GameRules.INITIAL_HEARTS,
    val completed: Set<String> = emptySet()
) {
    val stars: Int get() = completed.size * GameRules.STAR_PER_TASK
    val exhausted: Boolean get() = lives == 0
    fun mastered(required: Set<String>): Boolean = completed.containsAll(required)
}
