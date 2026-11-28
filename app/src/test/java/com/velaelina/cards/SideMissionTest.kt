package com.velaelina.cards

import org.junit.Assert.*
import org.junit.Test

class SideMissionTest {
    private fun hit(x:Float,y:Float)=sideBoatHit(x/592f,y/638f)?.part
    @Test fun sixExactTargetsAndIndependentIds() {
        val expected=setOf("FOQUE","VELA MAYOR","CASCO","ORZA","BOTAVARA","MÁSTIL")
        assertEquals(expected,PartsMission.SIDE.parts.map{it.word}.toSet())
        assertEquals(6,PartsMission.SIDE.required.size)
        assertTrue(PartsMission.TOP.required.intersect(PartsMission.SIDE.required).isEmpty())
        assertEquals(BoatPart.FOQUE,hit(450f,380f))
        assertEquals(BoatPart.VELA_MAYOR,hit(260f,325f))
        assertEquals(BoatPart.CASCO,hit(300f,510f))
        assertEquals(BoatPart.ORZA,hit(330f,575f))
        assertEquals(BoatPart.BOTAVARA,hit(210f,444f))
        assertEquals(BoatPart.MASTIL,hit(374f,175f))
        assertNull(sideBoatHit(20f/592f,600f/638f))
        assertNotNull(sideBoatHit(98f/592f,540f/638f))
        assertNull(hit(98f,540f))
        // Exact adjacent sails take priority over the expanded narrow-part targets.
        assertEquals(BoatPart.VELA_MAYOR,hit(358f,250f))
        assertEquals(BoatPart.FOQUE,hit(386f,395f))
        assertEquals(BoatPart.VELA_MAYOR,hit(210f,433f))
        assertEquals(BoatPart.BOTAVARA,hit(210f,453f))
        assertEquals(BoatPart.MASTIL,hit(364f,175f))
    }
    @Test fun normalizedCoordinatesScaleWithoutChangingHit() {
        val samples=listOf(Triple(450f,380f,BoatPart.FOQUE),Triple(260f,325f,BoatPart.VELA_MAYOR),
            Triple(300f,510f,BoatPart.CASCO),Triple(330f,575f,BoatPart.ORZA),
            Triple(210f,444f,BoatPart.BOTAVARA),Triple(374f,175f,BoatPart.MASTIL))
        for(width in listOf(220f,320f,412f,720f,1208f)) {
            val height=width/PartsMission.SIDE.aspectRatio
            for((x,y,part) in samples) {
                val px=x/592f*width;val py=y/638f*height
                assertEquals(part,sideBoatHit(px/width,py/height)?.part)
            }
        }
        assertEquals(1208f/1302f,PartsMission.SIDE.aspectRatio,0.00001f)
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
