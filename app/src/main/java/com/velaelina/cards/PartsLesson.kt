package com.velaelina.cards

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.io.File
import java.util.zip.ZipInputStream
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val PartsNight = Color(0xFF150820)
private val PartsPurple = Color(0xFF3B1268)
private val PartsPink = Color(0xFFFF20A6)
private val PartsGold = Color(0xFFFFFF00)
private val PartsMint = Color(0xFF00E5F5)
private val PartsMuted = Color(0xFF9170AD)
private val PartFiles = setOf("boat.webp","proa.ogg","popa.ogg","babor.ogg","estribor.ogg","correct.ogg","ups.ogg","finish.ogg")
private class PartAssets(context: Context) {
    private val cache = File(context.cacheDir,"vela_parts_v1").apply { mkdirs() }
    private var player: MediaPlayer? = null
    val bitmap = runCatching {
        val data = mutableMapOf<String,ByteArray>()
        ZipInputStream(context.assets.open("vela_parts_assets.zip")).use { zip ->
            while(true) {
                val entry = zip.nextEntry ?: break
                if(entry.name in PartFiles) {
                    val bytes = zip.readBytes()
                    require(bytes.size in 1..1_000_000)
                    data[entry.name] = bytes
                }
                zip.closeEntry()
            }
        }
        require(data.keys == PartFiles)
        data.filterKeys { it.endsWith(".ogg") }.forEach { (name, bytes) -> File(cache,name).writeBytes(bytes) }
        val bytes = data.getValue("boat.webp")
        BitmapFactory.decodeByteArray(bytes,0,bytes.size)?.asImageBitmap()
    }.getOrNull()
    private val ctx = context
    fun play(name:String) {
        release()
        player = runCatching { MediaPlayer.create(ctx, Uri.fromFile(File(cache,name))) }.getOrNull()
        runCatching { player?.start() }
    }
    fun release() { runCatching { player?.release() }; player = null }
}

@Composable
private fun PartProgress(count:Int, maximum:Int, isHeart:Boolean) {
    val label = if(isHeart) "Vidas: $count de $maximum" else "Estrellas: $count de $maximum"
    Canvas(Modifier.fillMaxWidth().height(29.dp).clip(RoundedCornerShape(20.dp))
        .background(PartsPurple).semantics { contentDescription = label }) {
        val step = size.width / maximum
        val r = minOf(step*.36f, size.height*.40f)
        for(i in 0 until maximum) {
            val x = step*(i+.5f); val y = size.height*.5f
            val color = if(i<count) { if(isHeart) PartsPink else PartsGold } else PartsMuted
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
internal fun PartsLesson(onHome:()->Unit) {
    val context=LocalContext.current
    val assets=remember { PartAssets(context.applicationContext) }
    DisposableEffect(assets) { onDispose { assets.release() } }
    var state by remember { mutableStateOf(PartsState()) }
    LaunchedEffect(state.feedback) {
        if(state.feedback != null) {
            delay(if(state.feedback?.correct==true) 520L else 650L)
            state=state.advance()
        }
    }
    LaunchedEffect(state.won) { if(state.won) assets.play("finish.ogg") }
    Column(Modifier.fillMaxSize().safeDrawingPadding().background(PartsNight).padding(8.dp),
        verticalArrangement=Arrangement.spacedBy(7.dp)) {
        OutlinedButton(onClick=onHome,modifier=Modifier.heightIn(min=48.dp),
            border=androidx.compose.foundation.BorderStroke(3.dp,PartsPink),
            colors=ButtonDefaults.outlinedButtonColors(containerColor=PartsPurple,contentColor=Color.White)) {
            Text("← Inicio",fontSize=21.sp,fontWeight=FontWeight.Bold)
        }
        Column(Modifier.weight(1f).fillMaxWidth().border(3.dp,PartsPink).padding(horizontal=12.dp,vertical=10.dp),
            horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)) {
            PartProgress(state.lives,PARTS_LIVES,true)
            PartProgress(state.stars,PARTS_ROUNDS,false)
            val bitmap=assets.bitmap
            if(bitmap==null) {
                Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center) {
                    Text("No se pudieron cargar la imagen y los sonidos del nivel.",textAlign=TextAlign.Center)
                }
            } else if(state.done) {
                Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center) {
                    Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(14.dp)) {
                        Text(if(state.won)"¡BRAVO!" else "¡Sigue practicando!",fontSize=34.sp,fontWeight=FontWeight.ExtraBold,color=PartsGold,textAlign=TextAlign.Center)
                        Text("Estrellas: ${state.stars}/$PARTS_ROUNDS",fontSize=22.sp)
                        Button(onClick={assets.release();state=PartsState()},modifier=Modifier.fillMaxWidth().heightIn(min=54.dp)) {Text("Jugar otra vez",fontSize=22.sp)}
                    }
                }
            } else {
                val word=state.current
                Text(word?.word.orEmpty(),color=PartsGold,fontSize=34.sp,fontWeight=FontWeight.ExtraBold,textAlign=TextAlign.Center)
                Button(onClick={word?.let{assets.play(it.audio)}},enabled=state.feedback==null,
                    modifier=Modifier.heightIn(min=49.dp).border(2.dp,Color.White,RoundedCornerShape(18.dp)),
                    shape=RoundedCornerShape(18.dp),colors=ButtonDefaults.buttonColors(containerColor=PartsMint,contentColor=PartsNight)){
                    Text("▶ Escuchar",fontSize=22.sp,fontWeight=FontWeight.Bold)
                }
                val feedback=state.feedback
                Text(when(feedback?.correct){true->"¡BRAVO!";false->"¡Ups!";null->"Toca la parte correcta del barco."},
                    color=if(feedback?.correct==true)PartsGold else Color.White,fontSize=15.sp,
                    fontWeight=FontWeight.Medium,textAlign=TextAlign.Center)
                BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center) {
                    val ratio=879f/899f
                    val width=minOf(maxWidth,maxHeight*ratio)
                    Box(Modifier.width(width).aspectRatio(ratio).clip(RoundedCornerShape(18.dp))
                        .pointerInput(state.index,state.lives,state.feedback){
                            detectTapGestures{ p ->
                                val x=p.x/size.width; val y=p.y/size.height
                                val next=state.answer(boatHit(x,y),x,y)
                                if(next!=state) {
                                    state=next
                                    assets.play(if(next.feedback?.correct==true)"correct.ogg" else "ups.ogg")
                                }
                            }
                        }) {
                        Image(bitmap,"Esquema de un barco visto desde arriba",Modifier.fillMaxSize(),contentScale=ContentScale.FillBounds)
                        if(feedback!=null) Canvas(Modifier.fillMaxSize()) {
                            val center=Offset(feedback.x*size.width,feedback.y*size.height)
                            val color=if(feedback.correct)PartsGold else PartsPink
                            drawCircle(color.copy(alpha=.28f),radius=22.dp.toPx(),center=center)
                            drawCircle(color,radius=22.dp.toPx(),center=center,style=Stroke(3.dp.toPx()))
                        }
                    }
                }
            }
        }
    }
}
