package com.velaelina.cards

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class GameJourneyTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun progress() = GameProgressStore(rule.activity.applicationContext).load()
    private fun word(): String =
        rule.onNodeWithTag("parts_word").fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.Text].first().text

    private fun tapPart(word:String) {
        val (px,py)=when(word){
            "PROA" -> 505f to 700f
            "POPA" -> 435f to 1306f
            "BABOR" -> 363f to 1090f
            "ESTRIBOR" -> 648f to 1090f
            "TIMÓN" -> 505f to 1380f
            "FOQUE" -> 553f to 850f
            "VELA MAYOR" -> 465f to 1040f
            else -> error("Unexpected term: $word")
        }
        rule.onNodeWithTag("parts_field").performTouchInput {
            click(Offset((px-66f)/879f*center.x*2f,(py-589f)/899f*center.y*2f))
        }
        rule.waitForIdle()
    }
    private fun completeRound(): List<String> {
        val order=mutableListOf<String>()
        repeat(7) { i ->
            val term=word()
            order += term
            rule.mainClock.autoAdvance=false
            tapPart(term)
            rule.mainClock.advanceTimeBy(100)
            when(i){
                2,5 -> rule.onNodeWithTag("reaction_streak").assertExists()
                6 -> rule.onNodeWithTag("reaction_complete").assertExists()
            }
            rule.mainClock.advanceTimeBy(1800)
            rule.mainClock.autoAdvance=true
            rule.waitUntil(5000) {
                val nodes=rule.onAllNodesWithTag("parts_word").fetchSemanticsNodes()
                nodes.isEmpty() || nodes.first().config[androidx.compose.ui.semantics.SemanticsProperties.Text].first().text != term
            }
        }
        rule.onNodeWithText("Practicar otra vez").assertIsDisplayed()
        return order
    }
    @Test fun mainJourney() {
        rule.runOnIdle {
            rule.activity.getSharedPreferences("vela_progress",Context.MODE_PRIVATE).edit().clear().commit()
        }
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()
        val title=rule.onNodeWithTag("home_title").assertIsDisplayed().getUnclippedBoundsInRoot()
        val art=rule.onNodeWithTag("home_art").assertIsDisplayed().getUnclippedBoundsInRoot()
        val play=rule.onNodeWithTag("home_play").assertIsDisplayed().assertHasClickAction().getUnclippedBoundsInRoot()
        val regata=rule.onNodeWithTag("home_regata").assertIsNotEnabled().getUnclippedBoundsInRoot()
        assertTrue(title.bottom <= art.top)
        assertTrue(art.bottom <= play.top)
        assertTrue(play.bottom <= regata.top)
        assertTrue(kotlin.math.abs((play.left+play.right).value/2-(art.left+art.right).value/2)<2f)
        rule.onNodeWithText("Jugar").performClick()
        assertEquals(10,progress().lives)
        assertEquals(0,progress().stars)
        val first=word()
        val wrong=listOf("PROA","POPA","BABOR","ESTRIBOR","TIMÓN","FOQUE","VELA MAYOR").first{it!=first}
        tapPart(wrong)
        rule.waitUntil(5000){progress().lives==9}
        rule.mainClock.advanceTimeBy(800)
        rule.mainClock.autoAdvance=false
        tapPart(wrong)
        rule.mainClock.advanceTimeBy(100)
        rule.onNodeWithTag("reaction_error").assertExists()
        rule.mainClock.advanceTimeBy(1800)
        rule.mainClock.autoAdvance=true
        rule.waitUntil(5000){progress().lives==8}
        assertEquals(0,progress().stars)
        val a=completeRound()
        assertEquals(7,a.toSet().size)
        assertEquals(7,progress().stars)
        rule.onNodeWithText("Practicar otra vez").performClick()
        val b=completeRound()
        assertNotEquals(a,b)
        assertEquals(7,progress().stars)
        assertEquals(8,progress().lives)
        rule.onNodeWithText("← Inicio").performClick()
        rule.onNodeWithText("Jugar").performClick()
        rule.onNodeWithText("Practicar otra vez").assertIsDisplayed()
        assertEquals(7,progress().stars)
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()
        rule.onNodeWithText("Jugar").performClick()
        rule.onNodeWithText("Practicar otra vez").assertIsDisplayed()
        assertEquals(7,progress().stars)
        assertEquals(8,progress().lives)
    }
}
