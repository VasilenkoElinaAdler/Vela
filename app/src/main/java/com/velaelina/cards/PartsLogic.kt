package com.velaelina.cards

internal enum class BoatPart(val word: String, val audio: String) {
    PROA("PROA","proa.ogg"), POPA("POPA","popa.ogg"),
    BABOR("BABOR","babor.ogg"), ESTRIBOR("ESTRIBOR","estribor.ogg");
    val taskId: String get() = "parts." + name.lowercase(java.util.Locale.ROOT)
}
internal data class BoatHit(val part: BoatPart?)
internal data class PartsFeedback(val correct: Boolean, val x: Float, val y: Float)

// Normalized coordinates on the original artwork field crop.
// Original 1010x1536; crop [66,589] to [945,1488], never stretched.
internal fun boatHit(x: Float, y: Float): BoatHit? {
    if (x !in 0f..1f || y !in 0f..1f) return null
    val px = 66f + x * 879f
    val py = 589f + y * 899f
    // Bow, stern and side rails only. Do not label ambiguous controls.
    if(py in 625f..788f && kotlin.math.abs(px-505f) <= (py-615f)*0.63f+13f)
        return BoatHit(BoatPart.PROA)
    if(py in 1260f..1343f && px in 355f..655f && !(px in 480f..530f && py >= 1285f))
        return BoatHit(BoatPart.POPA)
    if(py in 940f..1237f && px in 328f..402f) return BoatHit(BoatPart.BABOR)
    if(py in 940f..1237f && px in 607f..686f) return BoatHit(BoatPart.ESTRIBOR)
    // Taps on other parts of the boat count as a wrong answer;
    // accidental taps well outside the boat are ignored.
    if(py in 770f..1310f && px in 365f..645f) return BoatHit(null)
    if(py in 1180f..1420f && px in 475f..535f) return BoatHit(null)
    return null
}

internal val PARTS_TASK_IDS = BoatPart.entries.map { it.taskId }.toSet()
internal fun partsQuestions(
    random: kotlin.random.Random = kotlin.random.Random.Default,
    candidates: List<BoatPart> = BoatPart.entries.toList(),
    previous: List<BoatPart> = emptyList()
): List<BoatPart> {
    val shuffled = candidates.shuffled(random)
    // Every term can appear first. Avoid repeating an identical sequence.
    // Rotate only in the rare case of an identical previous sequence.
    return if (shuffled.size > 1 && shuffled == previous)
        shuffled.drop(1) + shuffled.first() else shuffled
}

internal data class PartsState(
    val questions: List<BoatPart> = partsQuestions(),
    val progress: GameProgress = GameProgress(),
    val index: Int = 0,
    val feedback: PartsFeedback? = null
) {
    val current get() = questions.getOrNull(index)
    val lives get() = progress.lives
    val stars get() = progress.stars
    val done get() = feedback == null && (progress.exhausted || current == null)
    val won get() = done && !progress.exhausted && current == null && progress.mastered(PARTS_TASK_IDS)
    fun answer(hit: BoatHit?, x: Float, y: Float): PartsState {
        val task = current ?: return this
        if (hit == null || done || feedback != null) return this
        val correct = hit.part == task
        return copy(progress = GameRules.answer(progress,task.taskId,correct),
            feedback = PartsFeedback(correct,x,y))
    }
    fun advance(): PartsState {
        val f = feedback ?: return this
        return copy(index = index + if(f.correct) 1 else 0, feedback = null)
    }
    companion object {
        fun resume(progress: GameProgress, previous: List<BoatPart> = emptyList()): PartsState =
            PartsState(questions = partsQuestions(candidates=BoatPart.entries.filterNot { it.taskId in progress.completed },previous=previous),progress=progress)
        fun practice(progress: GameProgress, previous: List<BoatPart> = emptyList()): PartsState =
            PartsState(questions=partsQuestions(previous=previous),progress=progress)
    }
}
