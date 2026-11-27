package com.velaelina.cards
import org.junit.Assert.*
import org.junit.Test
class GameTest {
    @Test fun correctAddsOneStar() { val r = Round().answer(1,1); assertEquals(1,r.stars); assertEquals(3,r.lives); assertEquals(r,r.answer(1,1)) }
    @Test fun wrongCostsOneLife() { val r = Round().answer(0,1); assertEquals(2,r.lives); assertEquals(0,r.stars) }
    @Test fun stopsAtZeroLives() { val r = Round(lives=1).answer(0,1); assertTrue(r.finished(5)); assertEquals(r,r.next(5)) }
    @Test fun lastCardEndsRound() { assertTrue(Round(index=4).answer(1,1).finished(5)) }
    @Test fun nextRequiresAnswer() { assertEquals(Round(),Round().next(5)); assertEquals(1,Round().answer(1,1).next(5).index) }
}
