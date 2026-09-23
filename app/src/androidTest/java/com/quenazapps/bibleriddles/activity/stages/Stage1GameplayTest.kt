package com.quenazapps.bibleriddles.activity.stages

import android.content.ComponentName
import androidx.compose.ui.test.assertIsDisplayed
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
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class Stage1GameplayTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun graceSolvesNewFirstStageAndNextOpensTheHoneyRiddle() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val storage = LocalStorage(context)
        val originalPlayer = storage.getPlayerInfo()
        val nextStage = instrumentation.addMonitor(Stage2Activity::class.java.name, null, false)
        try {
            storage.savePlayerInfo(PlayerInfo())
            ActivityScenario.launch(Stage1Activity::class.java).use {
                compose.onNodeWithText(context.getString(R.string.stage1_riddle)).assertIsDisplayed()
                compose.onNodeWithText("Provérbios 3:1,2 | NVI").assertIsDisplayed()
                assertEquals("GRAÇA", context.getString(R.string.stage1_answer))
                val field = compose.onNode(hasSetTextAction())
                field.performTextReplacement("MEL")
                compose.onNodeWithText(context.getString(R.string.submit_answer)).performClick()
                compose.onNodeWithText(context.getString(R.string.incorrect_answer)).assertIsDisplayed()
                assertEquals(0, storage.getPlayerInfo().scoreForStage(1))
                field.performTextReplacement("gRaCa!")
                compose.onNodeWithText(context.getString(R.string.submit_answer)).performClick()
                compose.onNodeWithText(context.getString(R.string.next_stage)).assertIsDisplayed()
                assertEquals(3, storage.getPlayerInfo().scoreForStage(1))
                assertEquals(2, storage.getPlayerInfo().highestUnlockedStage)
                compose.onNodeWithText(context.getString(R.string.next_stage)).performClick()
                // Keep advancing Compose's test clock while the transition animates.
                compose.waitUntil(timeoutMillis = 10_000) { nextStage.lastActivity != null }
                val destination = requireNotNull(nextStage.lastActivity)
                compose.onNodeWithText(context.getString(R.string.stage2_riddle)).assertIsDisplayed()
                instrumentation.runOnMainSync { destination.finish() }
            }
        } finally {
            nextStage.lastActivity?.let { activity -> instrumentation.runOnMainSync { activity.finish() } }
            instrumentation.removeMonitor(nextStage)
            storage.savePlayerInfo(originalPlayer)
        }
    }

    @Test fun hintsMatchTheInstructionsPuzzleAndStageRoutesIncludeTheNewLastStage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals(listOf("Instruções são importantes", "Já leu todas as instruções?"),
            STAGE1_TIPS.map { context.getString(it.textRes) })
        assertEquals(Stage1Activity::class.java, stageActivityClass(1))
        assertEquals(Stage2Activity::class.java, stageActivityClass(2))
        assertEquals(Stage51Activity::class.java, stageActivityClass(PlayerInfo.TOTAL_STAGES))
        assertEquals(51, (1..PlayerInfo.TOTAL_STAGES).map(::stageActivityClass).toSet().size)
        for (number in 1..PlayerInfo.TOTAL_STAGES) {
            assertEquals("Stage${number}Activity", stageActivityClass(number).simpleName)
            @Suppress("DEPRECATION")
            val info = context.packageManager.getActivityInfo(ComponentName(context, stageActivityClass(number)), 0)
            assertNotNull(info)
        }
    }
}
