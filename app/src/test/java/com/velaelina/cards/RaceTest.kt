package com.velaelina.cards
import org.junit.Assert.*
import org.junit.Test
class RaceTest {
    @Test fun fiveCorrectWin() {
        var r=Race(); repeat(5) { r=r.answer(1,1).next() }
        assertEquals(RaceOutcome.WIN,r.outcome); assertEquals(5,r.advances); assertEquals(3,r.lives)
    }
    @Test fun wrongStopsPlayerNotRivalAndCannotDoubleTap() {
        val r=Race().answer(0,1); assertEquals(0,r.advances); assertEquals(2,r.lives)
        assertTrue(r.rival>0f); assertEquals(r,r.answer(1,1))
    }
    @Test fun tieAndSecond() {
        for(errors in 1..2) {
            var r=Race(); repeat(errors) { r=r.answer(0,1).next() }; repeat(5) { r=r.answer(1,1).next() }
            assertEquals(if(errors==1) RaceOutcome.TIE else RaceOutcome.SECOND,r.outcome)
        }
    }
    @Test fun threeWrongEndRace() {
        var r=Race(); repeat(3) { r=r.answer(0,1).next() }
        assertEquals(RaceOutcome.LOST,r.outcome); assertEquals(0,r.lives); assertEquals(r,r.answer(1,1))
    }
    @Test fun discoveredThenCollected() {
        val k=Knowledge().correct("VELA-001"); assertTrue("VELA-001" in k.discovered); assertTrue(k.collected.isEmpty())
        val k2=k.correct("VELA-001"); assertEquals(setOf("VELA-001"),k2.collected); assertEquals(k2,k2.correct("VELA-001"))
    }
}
