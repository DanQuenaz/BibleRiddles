package com.quenazapps.bibleriddles.activity.stages

import android.content.ClipboardManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.quenazapps.bibleriddles.R
import com.quenazapps.bibleriddles.domain.PlayerInfo
import com.quenazapps.bibleriddles.service.LocalStorage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class Stage6GameplayTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun coordinatesPuzzleAcceptsNormalizedAnswerAndOpensStageSeven() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val storage = LocalStorage(context)
        val originalPlayer = storage.getPlayerInfo()
        val nextStage = instrumentation.addMonitor(Stage7Activity::class.java.name, null, false)
        try {
            storage.savePlayerInfo(PlayerInfo(highestUnlockedStage = 6))
            ActivityScenario.launch(Stage6Activity::class.java).use {
                compose.onNodeWithText("1844").assertIsDisplayed()
                COORDINATES.lines().forEach { coordinate ->
                    compose.onNodeWithText(coordinate).assertIsDisplayed().performClick()
                    compose.runOnIdle {
                        val clipboard = context.getSystemService(ClipboardManager::class.java)
                        assertEquals(coordinate, clipboard.primaryClip?.getItemAt(0)?.text?.toString())
                    }
                }
                assertEquals("LAODICEIA", context.getString(R.string.stage6_answer))
                assertEquals(listOf(
                    "Isso se parecem coordenadas geograficas ...",
                    "Coordenadas geográficas servem para apontar uma localização precisa ...",
                ), STAGE6_TIPS.map { context.getString(it.textRes) })

                val field = compose.onNode(hasSetTextAction())
                field.performTextReplacement("1844")
                compose.onNodeWithText(context.getString(R.string.submit_answer)).performClick()
                compose.onNodeWithText(context.getString(R.string.incorrect_answer)).assertIsDisplayed()
                assertEquals(0, storage.getPlayerInfo().scoreForStage(6))

                field.performTextReplacement("lAo. diCeIa!")
                field.assertTextEquals("lAo.diCeIa!")
                compose.onNodeWithText(context.getString(R.string.submit_answer)).performClick()
                compose.onNodeWithText(context.getString(R.string.next_stage)).assertIsDisplayed()
                assertEquals(3, storage.getPlayerInfo().scoreForStage(6))
                assertEquals(7, storage.getPlayerInfo().highestUnlockedStage)
                compose.onNodeWithText(context.getString(R.string.next_stage)).performClick()
                compose.waitUntil(timeoutMillis = 10_000) { nextStage.lastActivity != null }
            }
        } finally {
            nextStage.lastActivity?.let { activity -> instrumentation.runOnMainSync { activity.finish() } }
            instrumentation.removeMonitor(nextStage)
            storage.savePlayerInfo(originalPlayer)
        }
    }

    @Test fun purchasedHintReducesRewardAndCompletedPuzzleCanBeReviewed() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val storage = LocalStorage(context)
        val originalPlayer = storage.getPlayerInfo()
        try {
            storage.savePlayerInfo(PlayerInfo(
                highestUnlockedStage = 6,
                purchasedStageTips = mapOf(6 to setOf("tip_1")),
            ))
            ActivityScenario.launch(Stage6Activity::class.java).use {
                compose.onNode(hasSetTextAction()).performTextReplacement("LAODICEIA")
                compose.onNodeWithText(context.getString(R.string.submit_answer)).performClick()
                compose.onNodeWithText(context.getString(R.string.next_stage)).assertIsDisplayed()
                assertEquals(2, storage.getPlayerInfo().scoreForStage(6))
            }
            ActivityScenario.launch(Stage6Activity::class.java).use {
                compose.onNodeWithText("1844").assertIsDisplayed()
                COORDINATES.lines().forEach { coordinate ->
                    compose.onNodeWithText(coordinate).assertIsDisplayed()
                }
                compose.onNodeWithText("LAODICEIA").assertIsDisplayed()
                compose.onNode(hasSetTextAction()).assertDoesNotExist()
                assertEquals(2, storage.getPlayerInfo().scoreForStage(6))
            }
        } finally {
            storage.savePlayerInfo(originalPlayer)
        }
    }

    private companion object {
        const val COORDINATES = "37.9398° N, 27.3411° E\n38.4186° N, 27.1391° E\n" +
            "39.1322° N, 27.1841° E\n38.9228° N, 27.8403° E\n38.4883° N, 28.0403° E\n" +
            "38.3492° N, 28.5175° E\n37.8358° N, 29.1075° E"
    }
}
