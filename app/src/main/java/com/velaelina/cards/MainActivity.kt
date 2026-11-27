package com.velaelina.cards

import android.os.Bundle
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

private val Night = Color(0xFF191626)
private val Purple = Color(0xFF30204F)
private val Lilac = Color(0xFFE2C9FF)
private val Gold = Color(0xFFFFDF50)
private val Pink = Color(0xFFFF93C6)
private val Mint = Color(0xFFA3E5D8)
private val categories = linkedMapOf("equipment" to R.string.category_equipment, "maneuver" to R.string.category_maneuver, "course" to R.string.category_course, "physics" to R.string.category_physics, "practice" to R.string.category_practice, "rules" to R.string.category_rules)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val cards = runCatching { readCards(this) }.getOrNull()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Lilac, onPrimary = Night, secondary = Mint, background = Night, surface = Purple, onSurface = Color.White)) {
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

@Composable
private fun GameScreen(cards: List<CardData>) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("vela_progress", Context.MODE_PRIVATE) }
    var mastered by remember { mutableStateOf(prefs.getStringSet("mastered", emptySet())?.toSet() ?: emptySet()) }
    var screen by remember { mutableStateOf("home") }
    var group by remember { mutableStateOf("all") }
    var deck by remember { mutableStateOf(emptyList<CardData>()) }
    var round by remember { mutableStateOf(Round()) }
    fun start(category: String) {
        group = category
        deck = cards.filter { category == "all" || it.category == category }.shuffled().map { c ->
            val indices = c.options.indices.shuffled(); c.copy(options = indices.map { c.options[it] }, correct = indices.indexOf(c.correct))
        }
        round = Round(); screen = "play"
    }
    val scroll = rememberScrollState()
    LaunchedEffect(screen, round.index) { scroll.scrollTo(0) }
    BackHandler(enabled = screen != "home") { screen = "home" }
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 16.dp, vertical = 10.dp)) {
        if (screen != "home") {
            Button(onClick = { screen = "home" }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), colors = ButtonDefaults.buttonColors(containerColor = Lilac, contentColor = Night)) { Text(stringResource(R.string.home), fontSize = 19.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(10.dp))
        }
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(scroll), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (screen) {
                "home" -> {
                    Text(stringResource(R.string.title), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Lilac)
                    Text(stringResource(R.string.draft), color = Pink, fontSize = 14.sp)
                    Text(stringResource(R.string.mastered, cards.count { it.id in mastered }, cards.size), color = Gold)
                    Text(stringResource(R.string.choose), fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Button(onClick = { start("all") }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text(stringResource(R.string.all), fontSize = 18.sp) }
                    categories.forEach { (key, label) ->
                        OutlinedButton(onClick = { start(key) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                            Text(stringResource(label), modifier = Modifier.weight(1f), fontSize = 18.sp); Text(cards.count { it.category == key }.toString())
                        }
                    }
                }
                "play" -> if (deck.isNotEmpty()) {
                    val c = deck[round.index]
                    Text(stringResource(R.string.health, round.lives), color = Pink, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) { repeat(3) { Text(if (it < round.lives) "♥" else "♡", fontSize = 31.sp, color = Pink) } }
                    Text(stringResource(R.string.stars, round.stars), color = Gold, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(c.id, color = Lilac); Text(stringResource(R.string.counter, round.index + 1, deck.size), color = Lilac) }
                    LinearProgressIndicator(progress = { (round.index + if (round.selected == null) 0f else 1f) / deck.size }, modifier = Modifier.fillMaxWidth(), color = Gold)
                    Column(Modifier.fillMaxWidth().background(Purple, RoundedCornerShape(22.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(categories.getValue(c.category)), color = Lilac, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.level, c.level), color = Gold)
                        Text(c.question, fontSize = 21.sp, lineHeight = 29.sp, fontWeight = FontWeight.SemiBold)
                    }
                    c.options.forEachIndexed { index, option ->
                        val selected = round.selected
                        val color = when { selected != null && index == c.correct -> Mint; selected == index -> Pink; else -> Purple }
                        Button(onClick = {
                            if (round.selected == null && round.lives > 0) {
                                round = round.answer(index, c.correct)
                                if (index == c.correct) { mastered = mastered + c.id; prefs.edit().putStringSet("mastered", mastered.toSet()).apply() }
                            }
                        }, enabled = selected == null, modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White, disabledContainerColor = color, disabledContentColor = if (color == Purple) Color.White else Night)) {
                            Text(option, modifier = Modifier.fillMaxWidth(), fontSize = 17.sp, lineHeight = 23.sp)
                        }
                    }
                    if (round.selected != null) {
                        val ok = round.selected == c.correct
                        Text(stringResource(if (ok) R.string.bravo else R.string.incorrect), color = if (ok) Gold else Pink, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        if (!ok) Text(stringResource(R.string.correct, c.options[c.correct]), color = Mint, fontWeight = FontWeight.Bold)
                        Text(c.explanation, fontSize = 17.sp)
                        Button(onClick = { if (round.finished(deck.size)) screen = "result" else round = round.next(deck.size) }, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)) { Text(stringResource(if (round.finished(deck.size)) R.string.finish else R.string.next), fontSize = 18.sp) }
                    }
                }
                "result" -> {
                    Text(stringResource(R.string.result), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Lilac)
                    Text("★ ${round.stars}", fontSize = 42.sp, color = Gold)
                    Text(stringResource(R.string.score, round.stars, round.index + 1), fontSize = 20.sp)
                    Text(stringResource(R.string.mastered, cards.count { it.id in mastered }, cards.size))
                    Button(onClick = { start(group) }, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)) { Text(stringResource(R.string.retry), fontSize = 18.sp) }
                }
            }
        }
    }
}
