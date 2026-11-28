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
import androidx.compose.ui.res.stringResource
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

private val PartsNight = VelaPalette.Night
private val PartsPurple = VelaPalette.Purple
private val PartsPink = VelaPalette.Pink
private val PartsGold = VelaPalette.Gold
private val PartsMint = VelaPalette.Mint
private val PartsMuted = VelaPalette.Muted
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
internal fun PartsLesson(onHome:()->Unit) {
    val context=LocalContext.current
    val assets=remember { PartAssets(context.applicationContext) }
    DisposableEffect(assets) { onDispose { assets.release() } }
    val store=remember { GameProgressStore(context.applicationContext) }
    var state by remember { mutableStateOf(PartsState.resume(store.load())) }
    var celebrated by remember { mutableStateOf(false) }
    LaunchedEffect(state.feedback) {
        if(state.feedback != null) {
            delay(if(state.feedback?.correct==true) 520L else 650L)
            state=state.advance()
        }
    }
    LaunchedEffect(state.won) { if(state.won && !celebrated && state.questions.isNotEmpty()) { celebrated=true; assets.play("finish.ogg") } }
    Column(Modifier.fillMaxSize().safeDrawingPadding().background(PartsNight).padding(8.dp),
        verticalArrangement=Arrangement.spacedBy(7.dp)) {
        OutlinedButton(onClick=onHome,modifier=Modifier.heightIn(min=48.dp),
            border=androidx.compose.foundation.BorderStroke(3.dp,PartsPink),
            colors=ButtonDefaults.outlinedButtonColors(containerColor=PartsPurple,contentColor=Color.White)) {
            Text(stringResource(R.string.home),fontSize=21.sp,fontWeight=FontWeight.Bold)
        }
        Column(Modifier.weight(1f).fillMaxWidth().border(3.dp,PartsPink).padding(horizontal=12.dp,vertical=10.dp),
            horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)) {
            ProgressHeader(state.progress,PARTS_TASK_IDS)
            val bitmap=assets.bitmap
            if(bitmap==null) {
                Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center) {
                    Text(stringResource(R.string.parts_asset_error),textAlign=TextAlign.Center)
                }
            } else if(state.done) {
                Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center) {
                    Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(14.dp)) {
                        Text(stringResource(if(state.won) R.string.parts_completed else R.string.parts_no_lives),fontSize=34.sp,fontWeight=FontWeight.ExtraBold,color=PartsGold,textAlign=TextAlign.Center)
                        Text(stringResource(R.string.progress_stars,state.stars),fontSize=22.sp)
                        Button(onClick={assets.release();celebrated=false;state=if(state.won) PartsState.practice(state.progress) else PartsState.resume(store.restart())},modifier=Modifier.fillMaxWidth().heightIn(min=54.dp)) {Text(stringResource(if(state.won) R.string.parts_practice else R.string.parts_restart),fontSize=22.sp)}
                        OutlinedButton(onClick=onHome,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text(stringResource(R.string.parts_exit),fontSize=20.sp)}
                    }
                }
            } else {
                val word=state.current
                Text(word?.word.orEmpty(),color=PartsGold,fontSize=34.sp,fontWeight=FontWeight.ExtraBold,textAlign=TextAlign.Center)
                Button(onClick={word?.let{assets.play(it.audio)}},enabled=state.feedback==null,
                    modifier=Modifier.heightIn(min=49.dp).border(2.dp,Color.White,RoundedCornerShape(18.dp)),
                    shape=RoundedCornerShape(18.dp),colors=ButtonDefaults.buttonColors(containerColor=PartsMint,contentColor=PartsNight)){
                    Text(stringResource(R.string.parts_listen),fontSize=22.sp,fontWeight=FontWeight.Bold)
                }
                val feedback=state.feedback
                Text(when(feedback?.correct){true->stringResource(R.string.bravo);false->stringResource(R.string.ups);null->stringResource(R.string.parts_instruction)},
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
                                    store.save(next.progress)
                                    state=next
                                    assets.play(if(next.feedback?.correct==true)"correct.ogg" else "ups.ogg")
                                }
                            }
                        }) {
                        Image(bitmap,stringResource(R.string.parts_diagram),Modifier.fillMaxSize(),contentScale=ContentScale.FillBounds)
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
