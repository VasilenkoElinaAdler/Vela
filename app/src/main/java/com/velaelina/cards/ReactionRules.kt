package com.velaelina.cards

/** Shared answer-series reactions, independent of subject and rewards. */
internal enum class GameReaction { ERROR, STREAK, COMPLETE }
internal data class AnswerSeries(val correct: Int = 0, val wrong: Int = 0) {
    fun accept(success: Boolean, completed: Boolean = false): Pair<AnswerSeries, GameReaction?> {
        if (success && completed) return AnswerSeries() to GameReaction.COMPLETE
        if (success) {
            val n = correct + 1
            return if (n >= ReactionRules.CORRECT_THRESHOLD) AnswerSeries() to GameReaction.STREAK
            else AnswerSeries(correct=n) to null
        }
        val n = wrong + 1
        return if (n >= ReactionRules.WRONG_THRESHOLD) AnswerSeries() to GameReaction.ERROR
        else AnswerSeries(wrong=n) to null
    }
}
internal object ReactionRules {
    const val CORRECT_THRESHOLD = 3
    const val WRONG_THRESHOLD = 2
    const val DISPLAY_MS = 1500L
}
