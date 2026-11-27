package com.velaelina.cards

import android.os.Bundle
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipInputStream
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray

private val Night = Color(0xFF150820)
private val Purple = Color(0xFF3B1268)
private val Lilac = Color(0xFFE2C9FF)
private val Gold = Color(0xFFFFFF00)
private val Pink = Color(0xFFFF20A6)
private val Mint = Color(0xFF00E5F5)
private val categories = linkedMapOf("equipment" to R.string.category_equipment, "maneuver" to R.string.category_maneuver, "course" to R.string.category_course, "physics" to R.string.category_physics, "practice" to R.string.category_practice, "rules" to R.string.category_rules)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val cards = runCatching { readCards(this) }.getOrNull()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Mint, onPrimary = Night, secondary = Mint, background = Night, surface = Purple, onSurface = Color.White)) {
                Surface(Modifier.fillMaxSize(), color = Night) {
                    if (cards.isNullOrEmpty()) Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.Center) { Text(stringResource(R.string.error)) }
                    else GameScreen(cards)
                }
            }
        }
    }
}
private fun readCards(context: Context): List<CardData> {
    val language = context.resources.configuration.locales[0].language
    val file = if (context.assets.list("cards")?.contains("$language.json") == true) "$language.json" else "es.json"
    val data = JSONArray(context.assets.open("cards/$file").bufferedReader().use { it.readText() })
    return (0 until data.length()).map { i ->
        val row = data.getJSONObject(i); val options = row.getJSONArray("options")
        CardData(row.getString("id"), row.getString("category"), row.getInt("difficulty"), row.getString("question"), (0 until options.length()).map { options.getString(it) }, row.getInt("correctAnswer"), row.getString("explanation"))
    }
}

private data class Session(val deck:List<CardData>,val race:Race=Race())
private fun newSession(cards:List<CardData>,knowledge:Knowledge):Session=Session(cards.shuffled().sortedBy{if(it.id in knowledge.collected) 1 else 0}.map{c->
    val indices=c.options.indices.shuffled(); c.copy(options=indices.map{c.options[it]},correct=indices.indexOf(c.correct))
})
private fun Session.encode():String {
    val r=race
    return JSONObject().put("schema",1).put("index",r.index).put("lives",r.lives).put("advances",r.advances)
        .put("attempts",r.attempts).put("selected",r.selected?:-1).put("wrong",r.lastWrong)
        .put("deck",JSONArray().apply{deck.forEach{c->put(JSONObject().put("id",c.id).put("options",JSONArray(c.options)).put("correct",c.correct))}}).toString()
}
private fun decodeSession(raw:String?,cards:List<CardData>):Session?=runCatching {
    val obj=JSONObject(raw?:return null); require(obj.getInt("schema")==1)
    val map=cards.associateBy{it.id}; val arr=obj.getJSONArray("deck")
    val deck=(0 until arr.length()).map{i->
        val row=arr.getJSONObject(i); val c=map.getValue(row.getString("id")); val opts=row.getJSONArray("options")
        val shuffled=(0 until opts.length()).map{opts.getString(it)}; val correct=row.getInt("correct")
        require(shuffled.sorted()==c.options.sorted()&&correct in shuffled.indices&&shuffled[correct]==c.options[c.correct])
        c.copy(options=shuffled,correct=correct)
    }
    val r=Race(obj.getInt("index"),obj.getInt("lives"),obj.getInt("advances"),obj.getInt("attempts"),obj.getInt("selected").takeIf{it>=0},obj.getBoolean("wrong"))
    require(deck.map{it.id}.distinct().size==deck.size&&deck.size>=7&&r.index in deck.indices&&r.lives in 0..INITIAL_LIVES&&r.advances in 0..FINISH_ADVANCES)
    require(r.attempts==r.advances+INITIAL_LIVES-r.lives&&r.index+(if(r.selected==null) 0 else 1)==r.attempts)
    require(r.selected==null||r.selected in 0..3)
    Session(deck,r)
}.getOrNull()

// Local owner-provided art pack. No download or network permission.
private fun readArt(context:Context):Map<String,ByteArray> = runCatching {
    val result=mutableMapOf<String,ByteArray>()
    ZipInputStream(context.assets.open("vela_regata_assets.zip")).use{zip->
        while(true){
            val entry=zip.nextEntry?:break
            if(entry.name in setOf("regata.webp","ups.webp","ups.ogg")) {
                val bytes=zip.readBytes(); require(bytes.size<=1_000_000); result[entry.name]=bytes
            }; zip.closeEntry()
        }
    }; result.toMap()
}.getOrDefault(emptyMap())

@Composable
private fun Artwork(bytes:ByteArray?,description:String,ratio:Float) {
    val bitmap=remember(bytes){bytes?.let{BitmapFactory.decodeByteArray(it,0,it.size)?.asImageBitmap()}}
    if(bitmap!=null) Image(bitmap,description,modifier=Modifier.fillMaxWidth().aspectRatio(ratio).clip(RoundedCornerShape(18.dp)),contentScale=ContentScale.Fit)
}

@Composable
private fun RaceTrack(race:Race) {
    val player by animateFloatAsState(race.advances.toFloat()/FINISH_ADVANCES,tween(420),label="player")
    val rival by animateFloatAsState(race.rival/FINISH_ADVANCES,tween(420),label="rival")
    val loop=remember{Animatable(0f)}
    LaunchedEffect(race.attempts) {
        loop.snapTo(0f)
        if(race.lastWrong&&race.selected!=null){loop.animateTo(1f,tween(650));loop.snapTo(0f)}
    }
    val red=Color(0xFFFF3748)
    Column(Modifier.fillMaxWidth().background(Purple,RoundedCornerShape(16.dp)).padding(10.dp)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
            Text(stringResource(R.string.you),color=Mint,fontWeight=FontWeight.Bold)
            Text(stringResource(R.string.rival),color=red,fontWeight=FontWeight.Bold)
            Text(stringResource(R.string.goal),color=Gold,fontWeight=FontWeight.Bold)
        }
        Canvas(Modifier.fillMaxWidth().height(80.dp)){
            val pad=17.dp.toPx();val end=size.width-pad;val h=size.height;val w=end-pad
            for(y in listOf(h*.30f,h*.76f)){
                drawLine(Lilac.copy(alpha=.45f),Offset(pad,y),Offset(end,y),strokeWidth=4.dp.toPx(),cap=StrokeCap.Round)
                for(i in 0..FINISH_ADVANCES) drawCircle(Color.White.copy(alpha=.65f),2.dp.toPx(),Offset(pad+w*i/FINISH_ADVANCES,y))
            }
            drawLine(Color.White,Offset(end,0f),Offset(end,h),strokeWidth=2.dp.toPx())
            fun boat(x:Float,y:Float,c:Color){
                val s=12.dp.toPx()
                val sail=Path().apply{moveTo(x,y-s);lineTo(x+s*.80f,y+s*.23f);lineTo(x,y+s*.23f);close()}
                drawPath(sail,c);drawLine(Color.White,Offset(x,y-s),Offset(x,y+s*.45f),strokeWidth=1.5.dp.toPx())
                val hull=Path().apply{moveTo(x-s,y+s*.32f);lineTo(x+s,y+s*.32f);lineTo(x+s*.60f,y+s*.77f);lineTo(x-s*.55f,y+s*.77f);close()}
                drawPath(hull,Color(0xFFFFD09D))
            }
            val angle=loop.value*(2*PI).toFloat()
            boat(pad+w*player+sin(angle)*9.dp.toPx(),h*.30f+(1-cos(angle))*5.dp.toPx(),Mint)
            boat(pad+w*rival,h*.76f,red)
        }
    }
}

@Composable
private fun GameScreen(cards:List<CardData>) {
    val context=LocalContext.current
    val prefs=remember{context.getSharedPreferences("vela_progress",Context.MODE_PRIVATE)}
    var knowledge by remember{mutableStateOf(Knowledge(
        (prefs.getStringSet("discovered_v1",null)?:prefs.getStringSet("mastered",emptySet())?:emptySet()).toSet(),
        (prefs.getStringSet("collected_v1",emptySet())?:emptySet()).toSet()))}
    var wins by remember{mutableIntStateOf(prefs.getInt("wins_v1",0))}
    var totalStars by remember{mutableIntStateOf(prefs.getInt("stars_v1",0))}
    var session by remember{mutableStateOf(decodeSession(prefs.getString("session_v1",null),cards))}
    var screen by remember{mutableStateOf("home")}
    var detail by remember{mutableStateOf<CardData?>(null)}
    val art=remember{readArt(context)}
    val sound=remember(art){runCatching{art["ups.ogg"]?.let{bytes->
        val file=File(context.cacheDir,"vela_ups.ogg");file.writeBytes(bytes);MediaPlayer.create(context,Uri.fromFile(file))
    }}.getOrNull()}
    val fallback=remember{runCatching{ToneGenerator(AudioManager.STREAM_MUSIC,60)}.getOrNull()}
    DisposableEffect(sound,fallback){onDispose{sound?.release();fallback?.release()}}
    fun playUps(){if(sound!=null)runCatching{sound.seekTo(0);sound.start()} else fallback?.startTone(ToneGenerator.TONE_PROP_NACK,240)}
    fun save(s:Session){
        session=s
        prefs.edit().putString("session_v1",s.encode()).putStringSet("discovered_v1",knowledge.discovered.toSet())
            .putStringSet("collected_v1",knowledge.collected.toSet()).putInt("wins_v1",wins).putInt("stars_v1",totalStars).apply()
    }
    fun start(){save(newSession(cards,knowledge));screen="play"}
    val scroll=rememberScrollState()
    LaunchedEffect(screen,session?.race?.index,session?.race?.lastWrong,detail){scroll.scrollTo(0)}
    BackHandler(enabled=screen!="home"){if(detail!=null)detail=null else screen="home"}
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal=14.dp,vertical=8.dp)){
        if(screen!="home"){
            Button(onClick={detail=null;screen="home"},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),
                colors=ButtonDefaults.buttonColors(containerColor=Purple,contentColor=Color.White),
                border=androidx.compose.foundation.BorderStroke(2.dp,Pink)){
                Text(stringResource(R.string.home),fontSize=19.sp,fontWeight=FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
        }
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(scroll),verticalArrangement=Arrangement.spacedBy(10.dp)){
            when(screen){
                "home"->{
                    Text(stringResource(R.string.title),fontSize=30.sp,fontWeight=FontWeight.ExtraBold,color=Gold)
                    Artwork(art["regata.webp"],stringResource(R.string.regata_art),800f/516f)
                    Text(stringResource(R.string.draft),color=Lilac,fontSize=13.sp)
                    val s=session
                    if(s!=null&&s.race.outcome==null) {
                        Button(onClick={screen="play"},modifier=Modifier.fillMaxWidth().heightIn(min=56.dp)){Text(stringResource(R.string.resume_race),fontSize=19.sp)}
                    }else{
                        Button(onClick={start()},modifier=Modifier.fillMaxWidth().heightIn(min=56.dp)){Text(stringResource(R.string.play_race),fontSize=19.sp)}
                    }
                    if(s?.race?.outcome!=null)OutlinedButton(onClick={screen="result"},modifier=Modifier.fillMaxWidth()){Text(stringResource(R.string.finish))}
                    OutlinedButton(onClick={screen="album"},modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)){Text(stringResource(R.string.collection,knowledge.collected.size,cards.size),fontSize=18.sp)}
                    Text(stringResource(R.string.trophies_stars,wins,totalStars),color=Gold,fontWeight=FontWeight.Bold)
                }
                "play"->session?.let{s->
                    val r=s.race;val c=s.deck[r.index]
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                        Text("♥ "+r.lives+"/"+INITIAL_LIVES,color=Pink,fontSize=24.sp,fontWeight=FontWeight.Bold)
                        Text("★ "+r.advances+"/"+FINISH_ADVANCES,color=Gold,fontSize=24.sp,fontWeight=FontWeight.Bold)
                    }
                    if(!r.lastWrong)Artwork(art["regata.webp"],stringResource(R.string.regata_art),800f/516f)
                    RaceTrack(r)
                    if(r.selected==null){
                        Text(c.question,fontSize=22.sp,lineHeight=27.sp,fontWeight=FontWeight.Bold,color=Color.White)
                        c.options.forEachIndexed{index,option->
                            Button(onClick={
                                val current=session
                                if(current!=null&&current.race.selected==null&&current.race.outcome==null){
                                    val active=current.deck[current.race.index];val nr=current.race.answer(index,active.correct)
                                    if(nr!=current.race){
                                        if(index==active.correct){knowledge=knowledge.correct(active.id);totalStars++}else playUps()
                                        if(nr.outcome==RaceOutcome.WIN)wins++
                                        save(current.copy(race=nr))
                                    }
                                }
                            },modifier=Modifier.fillMaxWidth().heightIn(min=52.dp),shape=RoundedCornerShape(17.dp)){
                                Text(option,modifier=Modifier.fillMaxWidth(),fontSize=17.sp,lineHeight=22.sp,fontWeight=FontWeight.SemiBold)
                            }
                        }
                    }else{
                        if(r.lastWrong){
                            Text(stringResource(R.string.ups),fontSize=38.sp,fontWeight=FontWeight.ExtraBold,color=Gold)
                            Text(stringResource(R.string.incorrect_answer),fontSize=21.sp,fontWeight=FontWeight.Bold)
                            Artwork(art["ups.webp"],stringResource(R.string.ups_art),680f/611f)
                            Text(stringResource(R.string.penalty_hint),color=Lilac)
                            Text(stringResource(R.string.correct,c.options[c.correct]),color=Mint,fontWeight=FontWeight.Bold)
                        }else Text(stringResource(R.string.bravo),fontSize=27.sp,color=Gold,fontWeight=FontWeight.ExtraBold)
                        Text(c.explanation,fontSize=17.sp,lineHeight=23.sp)
                        Button(onClick={if(r.outcome!=null)screen="result" else save(s.copy(race=r.next()))},modifier=Modifier.fillMaxWidth().heightIn(min=54.dp)){
                            Text(stringResource(R.string.continue_game),fontSize=20.sp,fontWeight=FontWeight.Bold)
                        }
                    }
                }
                "result"->session?.let{s->
                    val r=s.race
                    Text(stringResource(when(r.outcome){RaceOutcome.WIN->R.string.race_won;RaceOutcome.TIE->R.string.race_tied;RaceOutcome.SECOND->R.string.race_second;else->R.string.race_lost}),fontSize=30.sp,color=Gold,fontWeight=FontWeight.ExtraBold)
                    Artwork(art["regata.webp"],stringResource(R.string.regata_art),800f/516f)
                    RaceTrack(r)
                    Text(stringResource(R.string.score,r.advances,r.attempts),fontSize=20.sp)
                    Text(stringResource(R.string.collection,knowledge.collected.size,cards.size),color=Mint)
                    Text(stringResource(R.string.trophies_stars,wins,totalStars),color=Gold)
                    Button(onClick={start()},modifier=Modifier.fillMaxWidth().heightIn(min=54.dp)){Text(stringResource(R.string.retry),fontSize=19.sp)}
                    OutlinedButton(onClick={screen="album"},modifier=Modifier.fillMaxWidth()){Text(stringResource(R.string.collection,knowledge.collected.size,cards.size))}
                }
                "album"->{
                    Text(stringResource(R.string.collection,knowledge.collected.size,cards.size),fontSize=25.sp,fontWeight=FontWeight.Bold,color=Gold)
                    val selected=detail
                    if(selected!=null){
                        Text(selected.question,fontSize=21.sp,fontWeight=FontWeight.Bold)
                        Text(selected.options[selected.correct],color=Mint,fontSize=19.sp)
                        Text(selected.explanation,fontSize=17.sp)
                        OutlinedButton(onClick={detail=null}){Text(stringResource(R.string.back_album))}
                    }else categories.forEach{(key,label)->
                        val group=cards.filter{it.category==key}
                        Text(stringResource(label)+" · "+group.count{it.id in knowledge.collected}+"/"+group.size,color=Lilac,fontWeight=FontWeight.Bold)
                        group.forEach{c->
                            val collected=c.id in knowledge.collected;val found=c.id in knowledge.discovered
                            OutlinedButton(onClick={detail=c},enabled=collected,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){
                                Text(if(collected)"★ "+c.options[c.correct] else c.id+" · "+stringResource(if(found)R.string.discovered else R.string.locked),modifier=Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
        }
    }
}
