package com.velaelina.cards

import org.junit.Assert.*
import org.junit.Test

class SideMissionTest {
    private fun hit(x:Float,y:Float)=sideBoatHit((x*1261f/1229f-180f)/960f,y/1215f)?.part
    @Test fun sixExactTargetsAndIndependentIds() {
        val expected=setOf("FOQUE","VELA MAYOR","CASCO","ORZA","BOTAVARA","MÁSTIL")
        assertEquals(expected,PartsMission.SIDE.parts.map{it.word}.toSet())
        assertEquals(6,PartsMission.SIDE.required.size)
        assertTrue(PartsMission.TOP.required.intersect(PartsMission.SIDE.required).isEmpty())
        assertEquals(BoatPart.FOQUE,hit(930f,760f))
        assertEquals(BoatPart.VELA_MAYOR,hit(500f,650f))
        assertEquals(BoatPart.CASCO,hit(650f,1020f))
        assertEquals(BoatPart.ORZA,hit(710f,1150f))
        assertEquals(BoatPart.BOTAVARA,hit(420f,883f))
        assertEquals(BoatPart.MASTIL,hit(780f,350f))
        assertNull(hit(400f,1200f))
        assertNull(hit(234f,1080f))
        // Enlarged mast/boom zones must not steal valid neighbouring sail hits.
        assertEquals(BoatPart.VELA_MAYOR,hit(740f,500f))
        assertEquals(BoatPart.FOQUE,hit(820f,790f))
        assertEquals(BoatPart.VELA_MAYOR,hit(420f,864f))
        assertEquals(BoatPart.BOTAVARA,hit(420f,901f))
    }
    @Test fun randomizedSixDistinctTerms() {
        val orders=(0..999).map{partsQuestions(kotlin.random.Random(it),PartsMission.SIDE.parts)}
        assertEquals(PartsMission.SIDE.parts.toSet(),orders.map{it.first()}.toSet())
        assertTrue(orders.all{it.size==6 && it.toSet()==PartsMission.SIDE.parts.toSet()})
        assertTrue(orders.toSet().size>400)
    }
    private fun finish(start:PartsState):PartsState {
        var s=start
        while(!s.done) s=s.answer(BoatHit(s.current),.5f,.5f).advance()
        return s
    }
    @Test fun twoMissionsEarnThirteenExactlyOnce() {
        val top=finish(PartsState.resume(GameProgress()))
        assertEquals(7,top.progress.permanentStars)
        val side=PartsState.practice(top.progress,mission=PartsMission.SIDE)
        assertEquals(3,side.lives);assertEquals(0,side.stars);assertEquals(7,side.progress.permanentStars)
        val first=side.answer(BoatHit(side.current),.5f,.5f).advance()
        assertEquals(1,first.stars);assertEquals(7,first.progress.permanentStars)
        val resumed=PartsState.resume(first.progress,mission=PartsMission.SIDE)
        assertEquals(5,resumed.questions.size)
        val done=finish(resumed)
        assertTrue(done.won);assertEquals(6,done.progress.newStars);assertEquals(13,done.progress.permanentStars)
        val replay=finish(PartsState.practice(done.progress,mission=PartsMission.SIDE))
        assertEquals(0,replay.progress.newStars);assertEquals(13,replay.progress.permanentStars)
        var lost=PartsState.practice(replay.progress,mission=PartsMission.SIDE)
        repeat(3){lost=lost.answer(BoatHit(null),.5f,.5f).advance()}
        assertEquals(0,lost.stars);assertEquals(13,lost.progress.permanentStars);assertFalse(lost.won)
    }
}
