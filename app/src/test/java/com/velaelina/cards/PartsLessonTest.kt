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
        assertNull(hit(100f,700f))
        assertNotNull(hit(505f,1390f))
        assertNull(hit(505f,1390f)?.part)
    }
    @Test fun firstRoundCoversEachPartTwiceWithoutConsecutiveDuplicates() {
        repeat(30) {
            val q=partsQuestions(kotlin.random.Random(it))
            assertEquals(10,q.size); assertEquals(BoatPart.PROA,q.first())
            for(p in BoatPart.entries)assertTrue(q.count{it==p}>=2)
            assertTrue(q.zipWithNext().all{(a,b)->a!=b})
        }
    }
    @Test fun correctIsAwardedOnceAndAdvancesAfterFeedback() {
        val s=PartsState(listOf(BoatPart.PROA))
        val t=s.answer(BoatHit(BoatPart.PROA),.5f,.12f)
        assertEquals(1,t.stars); assertEquals(10,t.lives)
        assertEquals(t,t.answer(BoatHit(BoatPart.PROA),.5f,.12f))
        assertTrue(t.advance().won)
    }
    @Test fun wrongCostsHeartAndRetainsQuestion() {
        val s=PartsState(listOf(BoatPart.PROA),lives=2)
        assertEquals(s,s.answer(null,0f,0f))
        val t=s.answer(BoatHit(BoatPart.POPA),.5f,.8f)
        assertEquals(1,t.lives); assertEquals(0,t.stars)
        assertEquals(BoatPart.PROA,t.advance().current)
        val lost=t.advance().answer(BoatHit(null),.5f,.5f).advance()
        assertTrue(lost.done);assertFalse(lost.won)
    }
}
