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
        val side=GameProgressStore(rule.activity.applicationContext).activeMission()==PartsMission.SIDE
        if(side) {
            val (x,y)=when(word){
                "FOQUE" -> 450f to 380f
                "VELA MAYOR" -> 260f to 325f
                "CASCO" -> 300f to 510f
                "ORZA" -> 330f to 575f
                "BOTAVARA" -> 210f to 444f
                "MÁSTIL" -> 374f to 175f
                else -> error("Unexpected side term: $word")
            }
            rule.onNodeWithTag("parts_field").performTouchInput {
                click(Offset(x/592f*center.x*2f,y/638f*center.y*2f))
            }
            rule.waitForIdle();return
        }
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
    private fun completeRound(count:Int=7): List<String> {
        val order=mutableListOf<String>()
        repeat(count) { i ->
            val term=word()
            order += term
            rule.mainClock.autoAdvance=false
            tapPart(term)
            rule.mainClock.advanceTimeBy(100)
            when {
                i==count-1 -> rule.onNodeWithTag("reaction_complete").assertExists()
                (i+1)%3==0 -> {
                    rule.onNodeWithTag("reaction_streak").assertExists()
                    rule.onNodeWithText("¡VIENTO EN POPA!").assertExists()
                    rule.onNodeWithContentDescription("Gaviota volando con un pez hacia la derecha").assertExists()
                }
            }
            rule.mainClock.advanceTimeBy(1800)
            rule.mainClock.autoAdvance=true
            rule.waitUntil(5000) {
                val nodes=rule.onAllNodesWithTag("parts_word").fetchSemanticsNodes()
                nodes.isEmpty() || nodes.first().config[androidx.compose.ui.semantics.SemanticsProperties.Text].first().text != term
            }
        }
        rule.onNodeWithText("Practicar otra vez").assertIsDisplayed()
        rule.onNodeWithText("¡MISIÓN CUMPLIDA!").assertIsDisplayed()
        rule.onNodeWithContentDescription("Gaviota volando con un pez hacia la izquierda").assertIsDisplayed()
        return order
    }
    private fun failAttempt() {
        repeat(progress().lives) {
            val wrong=GameProgressStore(rule.activity.applicationContext).activeMission().parts.first{it.word!=word()}.word
            tapPart(wrong)
            rule.mainClock.advanceTimeBy(1800)
            rule.waitForIdle()
        }
        rule.onNodeWithText("¡Inténtalo de nuevo!").assertIsDisplayed()
        rule.onNodeWithText("Estrellas de la misión: 0/${GameProgressStore(rule.activity.applicationContext).activeMission().parts.size}").assertIsDisplayed()
        rule.onNodeWithText("Nuevas estrellas: +0").assertIsDisplayed()
        assertEquals(0,progress().lives);assertEquals(0,progress().stars)
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
        assertEquals(3,progress().lives)
        assertEquals(0,progress().stars)
        val first=word()
        val wrong=listOf("PROA","POPA","BABOR","ESTRIBOR","TIMÓN","FOQUE","VELA MAYOR").first{it!=first}
        tapPart(wrong)
        rule.waitUntil(5000){progress().lives==2}
        rule.mainClock.advanceTimeBy(800)
        rule.mainClock.autoAdvance=false
        tapPart(wrong)
        rule.mainClock.advanceTimeBy(100)
        rule.onNodeWithTag("reaction_error").assertExists()
        rule.onNodeWithText("¡UPS!").assertExists()
        rule.onNodeWithContentDescription("Tres gaviotas sorprendidas").assertExists()
        rule.mainClock.advanceTimeBy(1800)
        rule.mainClock.autoAdvance=true
        rule.waitUntil(5000){progress().lives==1}
        assertEquals(0,progress().stars)
        // Earn a provisional star, then lose the last heart.
        tapPart(word())
        rule.mainClock.advanceTimeBy(800)
        assertEquals(1,progress().stars);assertEquals(0,progress().permanentStars)
        failAttempt()
        assertEquals(0,progress().permanentStars)
        rule.onNodeWithText("Empezar de nuevo").performClick()
        assertEquals(3,progress().lives);assertEquals(0,progress().stars)
        val a=completeRound()
        assertEquals(7,a.toSet().size)
        assertEquals(7,progress().permanentStars)
        rule.onNodeWithText("Nuevas estrellas: +7").assertIsDisplayed()
        rule.onNodeWithText("Practicar otra vez").performClick()
        assertEquals(3,progress().lives);assertEquals(0,progress().stars)
        assertEquals(7,progress().permanentStars)
        val b=completeRound()
        assertNotEquals(a,b)
        assertEquals(7,progress().permanentStars)
        rule.onNodeWithText("Nuevas estrellas: +0").assertIsDisplayed()
        // Regression: previously successful mission, then fail a new replay.
        rule.onNodeWithText("Practicar otra vez").performClick()
        failAttempt()
        assertEquals(7,progress().permanentStars)
        rule.onNodeWithText("← Inicio").performClick()
        rule.onNodeWithText("Estrellas conseguidas: 7").assertExists()
        rule.onNodeWithText("Jugar").performClick()
        rule.onNodeWithText("¡Inténtalo de nuevo!").assertIsDisplayed()
        assertEquals(0,progress().stars)
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()
        rule.onNodeWithText("Jugar").performClick()
        rule.onNodeWithText("¡Inténtalo de nuevo!").assertIsDisplayed()
        assertEquals(0,progress().stars);assertEquals(7,progress().permanentStars)
        rule.onNodeWithText("Empezar de nuevo").performClick()
        assertEquals(3,progress().lives);assertEquals(0,progress().stars)
        assertEquals(7,progress().permanentStars)
        // The original journey above already tested retry, failure, replay and restart.
        completeRound()
        rule.onNodeWithTag("next_mission").assertIsDisplayed().performClick()
        rule.onNodeWithContentDescription("Esquema de un barco visto de lado").assertIsDisplayed()
        assertEquals(PartsMission.SIDE,GameProgressStore(rule.activity.applicationContext).activeMission())
        assertEquals(3,progress().lives);assertEquals(0,progress().stars);assertEquals(7,progress().permanentStars)
        val firstSide=word()
        tapPart(firstSide);rule.mainClock.advanceTimeBy(800)
        assertEquals(1,progress().stars);assertEquals(7,progress().permanentStars)
        rule.onNodeWithText("← Inicio").performClick()
        rule.activityRule.scenario.recreate();rule.waitForIdle()
        rule.onNodeWithText("Jugar").performClick()
        assertEquals(1,progress().stars);assertNotEquals(firstSide,word())
        // Complete the remaining five tasks, preserving the first provisional star.
        repeat(5) { tapPart(word());rule.mainClock.advanceTimeBy(1800);rule.waitForIdle() }
        rule.onNodeWithText("¡MISIÓN CUMPLIDA!").assertIsDisplayed()
        rule.onNodeWithText("Nuevas estrellas: +6").assertIsDisplayed()
        assertEquals(13,progress().permanentStars)
        rule.onNodeWithTag("next_mission").assertDoesNotExist()
        rule.onNodeWithText("Practicar otra vez").performClick()
        completeRound(6)
        assertEquals(13,progress().permanentStars)
        rule.onNodeWithText("Nuevas estrellas: +0").assertIsDisplayed()
        // Fail a side replay after gaining a provisional star; retain all 13 permanent stars.
        rule.onNodeWithText("Practicar otra vez").performClick()
        tapPart(word());rule.mainClock.advanceTimeBy(800)
        assertEquals(1,progress().stars)
        failAttempt()
        assertEquals(13,progress().permanentStars)
        rule.onNodeWithText("← Inicio").performClick()
        rule.onNodeWithText("Estrellas conseguidas: 13").assertExists()
        rule.onNodeWithText("Jugar").performClick()
        rule.onNodeWithText("Estrellas de la misión: 0/6").assertIsDisplayed()
        rule.onNodeWithText("Empezar de nuevo").performClick()
        assertEquals(3,progress().lives);assertEquals(0,progress().stars)
        assertEquals(13,progress().permanentStars)
    }
}
