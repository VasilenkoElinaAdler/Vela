package com.velaelina.cards
import org.junit.Assert.*
import org.junit.Test
class PartsLessonTest {
    private val required = setOf("PROA","POPA","BABOR","ESTRIBOR","TIMÓN","FOQUE","VELA MAYOR")
    private fun hit(px:Float,py:Float)=boatHit((px-66f)/879f,(py-589f)/899f)
    @Test fun curriculumAndZonesMatchOwnerList() {
        assertEquals(required,BoatPart.entries.map{it.word}.toSet())
        assertEquals(BoatPart.PROA,hit(505f,700f)?.part)
        assertEquals(BoatPart.POPA,hit(435f,1306f)?.part)
        assertEquals(BoatPart.BABOR,hit(363f,1090f)?.part)
        assertEquals(BoatPart.ESTRIBOR,hit(648f,1090f)?.part)
        assertEquals(BoatPart.TIMON,hit(505f,1380f)?.part)
        assertEquals(BoatPart.FOQUE,hit(553f,850f)?.part)
        assertEquals(BoatPart.VELA_MAYOR,hit(465f,1040f)?.part)
        assertNull(hit(100f,700f))
        assertNull(hit(560f,1100f)?.part)
    }
    @Test fun allTermsCanStartAndRoundOrdersVary() {
        val orders=(0..999).map { partsQuestions(kotlin.random.Random(it)) }
        for(q in orders) {assertEquals(7,q.size);assertEquals(required,q.map{it.word}.toSet())}
        assertEquals(BoatPart.entries.toSet(),orders.map{it.first()}.toSet())
        assertTrue(orders.toSet().size>500)
        for(part in BoatPart.entries) assertTrue(orders.count{it.first()==part} in 80..220)
        for(seed in 0..99) {
            val p=partsQuestions(kotlin.random.Random(seed))
            assertNotEquals(p,partsQuestions(kotlin.random.Random(seed),previous=p))
        }
    }
    private fun finish(start:PartsState):PartsState {
        var s=start
        while(!s.done) s=s.answer(BoatHit(s.current),.5f,.5f).advance()
        return s
    }
    private fun fail(start:PartsState):PartsState {
        var s=start
        repeat(s.lives) { s=s.answer(BoatHit(null),.5f,.5f).advance() }
        return s
    }
    @Test fun threeHeartsAndProvisionalStarsUntilSuccess() {
        var s=PartsState.resume(GameProgress())
        assertEquals(3,s.lives);assertEquals(0,s.stars);assertEquals(0,s.progress.permanentStars)
        repeat(6) {
            s=s.answer(BoatHit(s.current),.5f,.5f).advance()
            assertEquals(it+1,s.stars);assertEquals(0,s.progress.permanentStars)
        }
        val won=finish(s)
        assertTrue(won.won);assertEquals(7,won.stars)
        assertEquals(7,won.progress.permanentStars);assertEquals(7,won.progress.newStars)
    }
    @Test fun failureDiscardsAttemptButNotPreviousAchievements() {
        val fresh=PartsState.resume(GameProgress())
        val partial=fresh.answer(BoatHit(fresh.current),.5f,.5f).advance()
        assertEquals(1,partial.stars)
        val lost=fail(partial)
        assertFalse(lost.won);assertEquals(0,lost.lives);assertEquals(0,lost.stars)
        assertEquals(0,lost.progress.permanentStars);assertEquals(0,lost.progress.newStars)
        val won=finish(PartsState.practice(lost.progress))
        assertEquals(7,won.progress.permanentStars)
        val practice=PartsState.practice(won.progress)
        assertEquals(3,practice.lives);assertEquals(0,practice.stars)
        val replayLost=fail(practice)
        assertEquals(0,replayLost.stars);assertEquals(7,replayLost.progress.permanentStars)
        assertEquals(0,replayLost.progress.newStars);assertFalse(replayLost.won)
        val resumed=PartsState.resume(replayLost.progress)
        assertTrue(resumed.done);assertFalse(resumed.won);assertEquals(0,resumed.stars)
        val retry=PartsState.practice(replayLost.progress)
        assertEquals(3,retry.lives);assertEquals(0,retry.stars)
        val replayWon=finish(retry)
        assertEquals(7,replayWon.stars);assertEquals(7,replayWon.progress.permanentStars)
        assertEquals(0,replayWon.progress.newStars)
    }
    @Test fun repeatedPracticeCannotFarmStars() {
        var p=GameProgress()
        repeat(30) {
            val s=finish(PartsState.practice(p))
            p=s.progress
            assertEquals(7,p.permanentStars);assertEquals(if(it==0)7 else 0,p.newStars)
        }
    }
    @Test fun correctIsAwardedOnceAndResumesWithoutDuplicateRewards() {
        val s=PartsState.resume(GameProgress())
        val task=s.current!!
        val t=s.answer(BoatHit(task),.5f,.12f)
        assertEquals(1,t.stars);assertEquals(3,t.lives);assertEquals(0,t.progress.permanentStars)
        assertEquals(t,t.answer(BoatHit(task),.5f,.12f))
        val resumed=PartsState.resume(t.progress)
        assertFalse(resumed.questions.contains(task))
        assertEquals(1,resumed.stars);assertEquals(0,resumed.progress.permanentStars)
    }
    @Test fun wrongCostsHeartAndRetainsQuestion() {
        val s=PartsState(listOf(BoatPart.PROA),progress=GameProgress(lives=2))
        assertEquals(s,s.answer(null,0f,0f))
        val t=s.answer(BoatHit(BoatPart.POPA),.5f,.8f)
        assertEquals(1,t.lives);assertEquals(0,t.stars)
        assertEquals(BoatPart.PROA,t.advance().current)
        val f=t.advance().answer(BoatHit(null),.5f,.5f)
        assertEquals(GameReaction.ERROR,f.feedback?.reaction)
        val lost=f.advance()
        assertTrue(lost.done);assertFalse(lost.won)
    }
    @Test fun fullLessonCompletionAndPersistenceModel() {
        var s=PartsState.resume(GameProgress())
        val reactions=mutableListOf<GameReaction>()
        while(!s.done) {
            s=s.answer(BoatHit(s.current),.5f,.5f)
            s.feedback?.reaction?.let { reactions += it }
            s=s.advance()
        }
        assertTrue(s.won);assertEquals(7,s.stars)
        assertEquals(listOf(GameReaction.STREAK,GameReaction.STREAK,GameReaction.COMPLETE),reactions)
        assertTrue(PartsState.resume(s.progress).won)
        val future=GameRules.newAttempt(s.progress)
        val later=GameRules.answer(future,"future.task",true,setOf("future.task"))
        assertEquals(8,later.permanentStars);assertEquals(1,later.newStars)
    }
    @Test fun legacyMigrationOnlyCreditsFullLesson() {
        val complete=GameRules.migrateLegacy(PARTS_TASK_IDS,PARTS_TASK_IDS)
        assertEquals(7,complete.permanentStars);assertEquals(0,complete.newStars)
        assertEquals(3,complete.lives)
        val partial=GameRules.migrateLegacy(PARTS_TASK_IDS.take(4).toSet(),PARTS_TASK_IDS)
        assertEquals(0,partial.permanentStars);assertEquals(0,partial.stars)
    }
    @Test fun seriesResetAndCompletionPriority() {
        var series=AnswerSeries()
        fun event(correct:Boolean,complete:Boolean=false):GameReaction? {
            val (next,reaction)=series.accept(correct,complete);series=next;return reaction
        }
        assertNull(event(false));assertNull(event(true));assertNull(event(false))
        assertEquals(GameReaction.ERROR,event(false));assertNull(event(false))
        assertNull(event(true));assertNull(event(true));assertEquals(GameReaction.STREAK,event(true))
        assertNull(event(true));assertNull(event(true));assertEquals(GameReaction.COMPLETE,event(true,true))
        assertEquals(1500L,ReactionRules.DISPLAY_MS)
    }
    @Test fun exhaustedStateCannotAward() {
        val p=GameProgress(lives=0)
        assertEquals(p,GameRules.answer(p,"test.task",true,setOf("test.task")))
        assertEquals(p,GameRules.answer(p,"test.task",false,setOf("test.task")))
    }
}
