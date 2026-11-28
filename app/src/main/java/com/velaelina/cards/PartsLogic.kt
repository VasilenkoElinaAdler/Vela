package com.velaelina.cards

internal enum class BoatPart(val word: String, val audio: String) {
    PROA("PROA","proa.ogg"), POPA("POPA","popa.ogg"),
    BABOR("BABOR","babor.ogg"), ESTRIBOR("ESTRIBOR","estribor.ogg"),
    TIMON("TIMÓN","timon.ogg"), FOQUE("FOQUE","foque.ogg"),
    VELA_MAYOR("VELA MAYOR","vela_mayor.ogg"),
    CASCO("CASCO","casco.ogg"), ORZA("ORZA","orza.ogg"),
    BOTAVARA("BOTAVARA","botavara.ogg"), MASTIL("MÁSTIL","mastil.ogg");
    val taskId: String get() = "parts." + name.lowercase(java.util.Locale.ROOT)
}
internal data class BoatHit(val part: BoatPart?)
internal data class PartsFeedback(val correct: Boolean, val x: Float, val y: Float, val reaction: GameReaction? = null)

// Distance to a visible rounded segment of the supplied illustration.
private fun nearSegment(px:Float,py:Float,ax:Float,ay:Float,bx:Float,by:Float,r:Float):Boolean {
    val dx=bx-ax; val dy=by-ay
    val t=(((px-ax)*dx+(py-ay)*dy)/(dx*dx+dy*dy)).coerceIn(0f,1f)
    val x=px-ax-t*dx; val y=py-ay-t*dy
    return x*x+y*y <= r*r
}

// Normalized coordinates on the original artwork field crop.
// Original 1010x1536; crop [66,589] to [945,1488], never stretched.
internal fun boatHit(x: Float, y: Float): BoatHit? {
    if (x !in 0f..1f || y !in 0f..1f) return null
    val px = 66f + x * 879f
    val py = 589f + y * 899f
    // Owner-approved visual vocabulary: upper red FOQUE, lower red VELA MAYOR,
    // cyan stern control TIMON (not separately CAÑA). Use visible shapes first.
    if (nearSegment(px,py,505f,1204f,505f,1393f,19f)) return BoatHit(BoatPart.TIMON)
    if (nearSegment(px,py,506f,803f,590f,889f,19f)) return BoatHit(BoatPart.FOQUE)
    if (nearSegment(px,py,506f,962f,429f,1105f,19f)) return BoatHit(BoatPart.VELA_MAYOR)
    // Bow, stern and side rails.
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

/** The same lesson engine runs a new mission by changing only this catalogue. */
internal enum class PartsMission(val storageKey:String, val art:String, val aspectRatio:Float, val parts:List<BoatPart>) {
    TOP("top","boat.webp",879f/899f,listOf(BoatPart.PROA,BoatPart.POPA,BoatPart.BABOR,BoatPart.ESTRIBOR,BoatPart.TIMON,BoatPart.FOQUE,BoatPart.VELA_MAYOR)),
    SIDE("side","boat_side.webp",1208f/1302f,listOf(BoatPart.FOQUE,BoatPart.VELA_MAYOR,BoatPart.CASCO,BoatPart.ORZA,BoatPart.BOTAVARA,BoatPart.MASTIL));

    fun taskId(part:BoatPart):String = if(this==TOP) part.taskId else "parts.side."+part.name.lowercase(java.util.Locale.ROOT)
    val required:Set<String> get() = parts.map{taskId(it)}.toSet()
    fun hit(x:Float,y:Float):BoatHit? = if(this==TOP) boatHit(x,y) else sideBoatHit(x,y)
}
internal val PARTS_TASK_IDS get() = PartsMission.TOP.required
internal fun partsQuestions(
    random: kotlin.random.Random = kotlin.random.Random.Default,
    candidates: List<BoatPart> = PartsMission.TOP.parts,
    previous: List<BoatPart> = emptyList()
): List<BoatPart> {
    var shuffled = candidates.shuffled(random)
    // Every term can appear first; sample uniformly among distinct permutations.
    if (shuffled.size > 1 && shuffled == previous) {
        repeat(32) {
            shuffled = candidates.shuffled(random)
            if (shuffled != previous) return shuffled
        }
        // Defensive fallback for a pathological random generator.
        return shuffled.drop(1) + shuffled.first()
    }
    return shuffled
}

internal data class PartsState(
    val questions: List<BoatPart> = partsQuestions(),
    val progress: GameProgress = GameProgress(),
    val index: Int = 0,
    val feedback: PartsFeedback? = null,
    val series: AnswerSeries = AnswerSeries(),
    val mission:PartsMission = PartsMission.TOP
) {
    val current get() = questions.getOrNull(index)
    val lives get() = progress.lives
    val stars get() = progress.stars
    val done get() = feedback == null && (progress.exhausted || current == null)
    val won get() = done && !progress.exhausted && current == null && progress.mastered(mission.required)
    fun answer(hit: BoatHit?, x: Float, y: Float): PartsState {
        val task = current ?: return this
        if (hit == null || done || feedback != null) return this
        val correct = hit.part == task
        val nextProgress = GameRules.answer(progress,mission.taskId(task),correct,mission.required)
        val completed = correct && index == questions.lastIndex && nextProgress.mastered(mission.required)
        val (nextSeries,reaction) = series.accept(correct,completed)
        return copy(progress = nextProgress,series=nextSeries,
            feedback = PartsFeedback(correct,x,y,reaction))
    }
    fun advance(): PartsState {
        val f = feedback ?: return this
        return copy(index = index + if(f.correct) 1 else 0, feedback = null)
    }
    companion object {
        fun resume(progress:GameProgress,previous:List<BoatPart> = emptyList(),mission:PartsMission=PartsMission.TOP):PartsState =
            PartsState(questions=if(progress.exhausted) emptyList() else partsQuestions(candidates=mission.parts.filterNot { mission.taskId(it) in progress.completed },previous=previous),progress=progress,mission=mission)
        fun practice(progress:GameProgress,previous:List<BoatPart> = emptyList(),mission:PartsMission=PartsMission.TOP):PartsState =
            PartsState(questions=partsQuestions(candidates=mission.parts,previous=previous),progress=GameRules.newAttempt(progress),mission=mission)
    }
}
