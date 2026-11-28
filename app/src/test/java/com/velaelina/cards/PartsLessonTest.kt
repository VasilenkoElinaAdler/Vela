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
    @Test fun repeatedPracticeCannotFarmStars() {
        var p=GameProgress()
        repeat(30) {
            var s=PartsState.practice(p)
            while(!s.done) s=s.answer(BoatHit(s.current),.5f,.5f).advance()
            p=s.progress
            assertEquals(7,p.stars)
        }
    }
    @Test fun correctIsAwardedOnceAndResumesWithoutDuplicateRewards() {
        val s=PartsState(listOf(BoatPart.PROA))
        val t=s.answer(BoatHit(BoatPart.PROA),.5f,.12f)
        assertEquals(1,t.stars); assertEquals(GameRules.INITIAL_HEARTS,t.lives)
        assertEquals(t,t.answer(BoatHit(BoatPart.PROA),.5f,.12f))
        val resumed=PartsState.resume(t.progress)
        assertFalse(resumed.questions.contains(BoatPart.PROA))
        val practice=PartsState(listOf(BoatPart.PROA),progress=t.progress).answer(BoatHit(BoatPart.PROA),.5f,.12f)
        assertEquals(1,practice.stars)
    }
    @Test fun wrongCostsHeartAndRetainsQuestion() {
        val s=PartsState(listOf(BoatPart.PROA),progress=GameProgress(lives=2))
        assertEquals(s,s.answer(null,0f,0f))
        val t=s.answer(BoatHit(BoatPart.POPA),.5f,.8f)
        assertEquals(1,t.lives); assertEquals(0,t.stars)
        assertEquals(null,t.feedback?.reaction)
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
        assertTrue(s.won); assertEquals(7,s.stars)
        assertEquals(listOf(GameReaction.STREAK,GameReaction.STREAK,GameReaction.COMPLETE),reactions)
        assertTrue(PartsState.resume(s.progress).won)
        val wrong=GameRules.answer(s.progress,"future.task",false)
        assertEquals(7,wrong.stars);assertEquals(9,wrong.lives)
        val later=GameRules.answer(wrong,"future.task",true)
        assertEquals(8,later.stars)
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
        assertEquals(p,GameRules.answer(p,"test.task",true))
        assertEquals(p,GameRules.answer(p,"test.task",false))
    }
}
