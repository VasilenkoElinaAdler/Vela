package com.velaelina.cards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
private fun ProgressSymbols(count:Int, maximum:Int, isHeart:Boolean) {
    val label = stringResource(if(isHeart) R.string.progress_lives else R.string.progress_tasks, count, maximum)
    Canvas(Modifier.fillMaxWidth().height(29.dp).clip(RoundedCornerShape(20.dp))
        .background(VelaPalette.Purple).semantics { contentDescription = label }) {
        val r = minOf(size.width / GameRules.INITIAL_HEARTS * .36f, size.height*.40f)
        val step = if(maximum>1) (size.width-2*r)/ (maximum-1) else 0f
        for(i in 0 until maximum) {
            val x = if(maximum==1) size.width*.5f else r+step*i; val y = size.height*.5f
            val color = if(i<count) { if(isHeart) VelaPalette.Pink else VelaPalette.Gold } else VelaPalette.Muted
            val path = Path()
            if(isHeart) {
                path.moveTo(x,y+r*.98f)
                path.cubicTo(x-r*.28f,y+r*.48f,x-r*1.12f,y+r*.04f,x-r,y-r*.38f)
                path.cubicTo(x-r*.92f,y-r*1.02f,x-r*.20f,y-r*1.05f,x,y-r*.48f)
                path.cubicTo(x+r*.20f,y-r*1.05f,x+r*.92f,y-r*1.02f,x+r,y-r*.38f)
                path.cubicTo(x+r*1.12f,y+r*.04f,x+r*.28f,y+r*.48f,x,y+r*.98f)
            } else {
                for(j in 0..9) {
                    val a = -PI/2 + j*PI/5
                    val radius = if(j%2==0)r else r*.46f
                    val px=x+radius*cos(a).toFloat();val py=y+radius*sin(a).toFloat()
                    if(j==0)path.moveTo(px,py) else path.lineTo(px,py)
                }
            }
            path.close();drawPath(path,color)
        }
    }
}



@Composable
internal fun ProgressHeader(progress: GameProgress, required: Set<String>) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        ProgressSymbols(progress.lives,GameRules.INITIAL_HEARTS,true)
        // These slots show the actual lesson catalogue, not a global star cap.
        if (required.size in 2..10) {
            ProgressSymbols(required.count { it in progress.completed },required.size,false)
        }
        Text(stringResource(R.string.progress_stars,progress.stars),color=VelaPalette.Gold,fontSize=15.sp)
    }
}
