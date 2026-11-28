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

// Canonical original 1229 x 1215 preview coordinates, mapped to the source JPEG
// (1261 x 1247) crop x=180..1140, y=0..1247. No image stretching.
// Mast and boom are exact first; modest tolerance only if no real target was hit.
private val sideZones=ImageHitMap(
    exact=listOf(
        HitZone(BoatPart.MASTIL,rect(767,3,795,949)),
        HitZone(BoatPart.BOTAVARA,rect(203,872,766,895)),
        HitZone(BoatPart.VELA_MAYOR,polygon(211,871,230,820,350,610,450,435,710,125,743,94,757,139,757,871)),
        HitZone(BoatPart.FOQUE,polygon(808,822,814,670,827,545,855,456,875,456,1046,910,1068,938,1018,933,845,890)),
        HitZone(BoatPart.CASCO,polygon(265,951,1090,942,1060,981,1023,1019,970,1054,900,1073,817,1078,265,1078)),
        HitZone(BoatPart.ORZA,polygon(670,1079,750,1079,750,1190,742,1203,710,1210,681,1204,669,1190)),
        // Visible but not assessed: rudder and tiller count as incorrect.
        HitZone(null,polygon(210,952,257,944,257,1122,249,1141,230,1146,212,1134)),
        HitZone(null,polygon(210,949,435,916,440,924,216,965))
    ),
    tolerance=listOf(
        HitZone(BoatPart.MASTIL,rect(755,0,808,950)),
        HitZone(BoatPart.BOTAVARA,rect(198,864,772,906))
    )
)
internal fun sideBoatHit(x:Float,y:Float):BoatHit? {
    if(x !in 0f..1f || y !in 0f..1f) return null
    return sideZones.hit((180f+x*960f)*1229f/1261f,y*1215f)
}
