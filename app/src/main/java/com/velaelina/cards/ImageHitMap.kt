package com.velaelina.cards

/**
 * Invisible, reusable hit geometry, independent of bitmap pixels and UI size.
 * The order resolves any shared outline. Exact visible areas win over tolerance.
 */
internal data class HitPoint(val x:Float,val y:Float)
internal interface HitShape { fun contains(x:Float,y:Float):Boolean }
internal class HitPolygon(private val vertices:List<HitPoint>):HitShape {
    override fun contains(x:Float,y:Float):Boolean {
        var inside=false
        var prev=vertices.last()
        for(p in vertices) {
            if((p.y>y)!=(prev.y>y) && x < (prev.x-p.x)*(y-p.y)/(prev.y-p.y)+p.x) inside=!inside
            prev=p
        }
        return inside
    }
}
internal class HitRect(private val left:Float,private val top:Float,private val right:Float,private val bottom:Float):HitShape {
    override fun contains(x:Float,y:Float)=x in left..right && y in top..bottom
}
internal data class HitZone(val part:BoatPart?,val shape:HitShape)
internal class ImageHitMap(private val exact:List<HitZone>,private val tolerance:List<HitZone> = emptyList()) {
    fun hit(x:Float,y:Float):BoatHit? =
        (exact.firstOrNull{it.shape.contains(x,y)} ?: tolerance.firstOrNull{it.shape.contains(x,y)})?.let{BoatHit(it.part)}
}
private fun polygon(vararg xy:Int):HitShape = HitPolygon(xy.toList().chunked(2).map{HitPoint(it[0].toFloat(),it[1].toFloat())})
private fun rect(l:Int,t:Int,r:Int,b:Int):HitShape = HitRect(l.toFloat(),t.toFloat(),r.toFloat(),b.toFloat())

// Owner-approved Tropical Bubble raster, full frame 1208 x 1302.
// Reference coordinates are on its proportional 592 x 638 review image.
// No crop, stretching or visible geometry. Exact parts win over tolerance.
private val sideZones=ImageHitMap(
    exact=listOf(
        HitZone(BoatPart.MASTIL,rect(366,8,383,476)),
        HitZone(BoatPart.BOTAVARA,rect(81,437,371,451)),
        HitZone(BoatPart.VELA_MAYOR,polygon(44,436,48,414,76,362,113,299,158,230,205,170,269,105,334,69,348,54,358,55,362,76,362,436)),
        HitZone(BoatPart.FOQUE,polygon(384,417,385,366,389,313,396,270,407,236,415,230,421,235,505,449,520,470,493,470,450,460,410,447)),
        HitZone(BoatPart.CASCO,polygon(109,477,263,477,397,475,529,472,515,490,498,506,479,519,454,530,424,537,393,541,109,542)),
        HitZone(BoatPart.ORZA,polygon(312,543,354,543,354,598,351,605,343,610,333,612,321,609,315,605,312,599)),
        // Visible non-target parts: rudder and tiller remain wrong answers.
        HitZone(null,polygon(83,486,107,481,107,563,105,571,99,577,90,577,84,571)),
        HitZone(null,polygon(83,479,199,459,201,465,84,486))
    ),
    tolerance=listOf(
        HitZone(BoatPart.MASTIL,rect(360,4,389,477)),
        HitZone(BoatPart.BOTAVARA,rect(78,432,373,456))
    )
)
internal fun sideBoatHit(x:Float,y:Float):BoatHit? {
    if(x !in 0f..1f || y !in 0f..1f) return null
    return sideZones.hit(x*592f,y*638f)
}
