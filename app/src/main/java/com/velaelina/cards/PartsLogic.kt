package com.velaelina.cards

internal const val PARTS_LIVES = 10
internal const val PARTS_ROUNDS = 10
internal enum class BoatPart(val word: String, val audio: String) {
    PROA("PROA","proa.ogg"), POPA("POPA","popa.ogg"),
    BABOR("BABOR","babor.ogg"), ESTRIBOR("ESTRIBOR","estribor.ogg")
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

internal fun partsQuestions(random: kotlin.random.Random = kotlin.random.Random.Default): List<BoatPart> {
    val all = BoatPart.entries.toList()
    val result = mutableListOf(BoatPart.PROA)
    result += all.filter { it != BoatPart.PROA }.shuffled(random)
    val second = all.shuffled(random).toMutableList()
    if (second.first() == result.last()) {
        val other = second.indexOfFirst { it != result.last() }
        val tmp = second[0]; second[0] = second[other]; second[other] = tmp
    }
    result += second
    repeat(2) { result += all.filter { it != result.last() }.random(random) }
    return result
}
internal data class PartsState(
    val questions: List<BoatPart> = partsQuestions(),
    val index: Int = 0, val lives: Int = PARTS_LIVES,
    val stars: Int = 0, val feedback: PartsFeedback? = null
) {
    val current get() = questions.getOrNull(index)
    val done get() = feedback == null && (lives == 0 || current == null)
    val won get() = done && stars == questions.size
    fun answer(hit: BoatHit?, x: Float, y: Float): PartsState {
        if (hit == null || done || feedback != null || current == null) return this
        val correct = hit.part == current
        return copy(lives = lives - if (correct) 0 else 1,
            stars = stars + if (correct) 1 else 0,
            feedback = PartsFeedback(correct, x, y))
    }
    fun advance(): PartsState {
        val f = feedback ?: return this
        return copy(index = index + if(f.correct) 1 else 0, feedback = null)
    }
}
