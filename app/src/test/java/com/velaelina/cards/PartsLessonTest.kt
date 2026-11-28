package com.velaelina.cards
import org.junit.Assert.*
import org.junit.Test
class PartsLessonTest {
    private fun hit(px:Float,py:Float)=boatHit((px-66f)/879f,(py-589f)/899f)
    @Test fun zonesMatchSource() {
        assertEquals(BoatPart.PROA,hit(505f,700f)?.part)
        assertEquals(BoatPart.POPA,hit(435f,1306f)?.part)
        assertEquals(BoatPart.BABOR,hit(363f,1090f)?.part)
        assertEquals(BoatPart.ESTRIBOR,hit(648f,1090f)?.part)
        assertNull(hit(100f,700f)); assertNotNull(hit(505f,1390f)); assertNull(hit(505f,1390f)?.part)
    }
    @Test fun firstRoundCoversEachPartOnce() {
        repeat(30) {
            val q=partsQuestions(kotlin.random.Random(it))
            assertEquals(BoatPart.entries.size,q.size)
            assertEquals(BoatPart.entries.toSet(),q.toSet())
        }
    }
    @Test fun allTermsCanStartAndRoundOrdersVary() {
        val orders=(0..999).map { partsQuestions(kotlin.random.Random(it)) }
        assertEquals(BoatPart.entries.toSet(),orders.map{it.first()}.toSet())
        assertEquals(24,orders.toSet().size)
        for(part in BoatPart.entries) assertTrue(orders.count{it.first()==part} in 150..350)
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
            assertEquals(PARTS_TASK_IDS.size,p.stars)
        }
    }
    @Test fun correctIsAwardedOnceAndResumesWithoutDuplicateRewards() {
        val s=PartsState(listOf(BoatPart.PROA))
        val t=s.answer(BoatHit(BoatPart.PROA),.5f,.12f)
        assertEquals(1,t.stars); assertEquals(GameRules.INITIAL_HEARTS,t.lives)
        assertEquals(t,t.answer(BoatHit(BoatPart.PROA),.5f,.12f))
        val resumed=PartsState.resume(t.progress)
        assertFalse(resumed.questions.contains(BoatPart.PROA))
        val practice=PartsState.practice(t.progress).answer(BoatHit(BoatPart.PROA),.5f,.12f)
        assertEquals(1,practice.stars)
    }
    @Test fun wrongCostsHeartAndRetainsQuestion() {
        val s=PartsState(listOf(BoatPart.PROA),progress=GameProgress(lives=2))
        assertEquals(s,s.answer(null,0f,0f))
        val t=s.answer(BoatHit(BoatPart.POPA),.5f,.8f)
        assertEquals(1,t.lives); assertEquals(0,t.stars)
        assertEquals(BoatPart.PROA,t.advance().current)
        val lost=t.advance().answer(BoatHit(null),.5f,.5f).advance()
        assertTrue(lost.done);assertFalse(lost.won)
    }
    @Test fun fullLessonCompletionAndPersistenceModel() {
        var s=PartsState.resume(GameProgress())
        while(!s.done) { s=s.answer(BoatHit(s.current),.5f,.5f).advance() }
        assertTrue(s.won); assertEquals(4,s.stars)
        assertTrue(PartsState.resume(s.progress).won)
        val wrong=GameRules.answer(s.progress,"future.task",false)
        assertEquals(4,wrong.stars);assertEquals(9,wrong.lives)
        val later=GameRules.answer(wrong,"future.task",true)
        assertEquals(5,later.stars)
    }
    @Test fun exhaustedStateCannotAward() {
        val p=GameProgress(lives=0)
        assertEquals(p,GameRules.answer(p,"test.task",true))
        assertEquals(p,GameRules.answer(p,"test.task",false))
    }
}
