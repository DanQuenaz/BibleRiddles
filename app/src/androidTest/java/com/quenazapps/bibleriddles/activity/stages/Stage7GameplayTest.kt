package com.quenazapps.bibleriddles.activity.stages

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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

class Stage7GameplayTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun trianglePuzzleAcceptsEzequiasAndOpensStageEight() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val storage = LocalStorage(context)
        val originalPlayer = storage.getPlayerInfo()
        val nextStage = instrumentation.addMonitor(Stage8Activity::class.java.name, null, false)
        try {
            storage.savePlayerInfo(PlayerInfo(highestUnlockedStage = 7))
            ActivityScenario.launch(Stage7Activity::class.java).use {
                compose.onNodeWithContentDescription(context.getString(R.string.stage7_image_description))
                    .assertIsDisplayed()
                assertEquals("EZEQUIAS", context.getString(R.string.stage7_answer))
                assertEquals(listOf(
                    "Vamos lá, trigonometria básica ...",
                    "Ok ... A soma dos ângulos internos de um triângulo é sempre 180 graus.",
                ), STAGE7_TIPS.map { context.getString(it.textRes) })
                val field = compose.onNode(hasSetTextAction())
                field.performTextReplacement("10")
                compose.onNodeWithText(context.getString(R.string.submit_answer)).performClick()
                compose.onNodeWithText(context.getString(R.string.incorrect_answer)).assertIsDisplayed()
                assertEquals(0, storage.getPlayerInfo().scoreForStage(7))
                field.performTextReplacement("eZe.QuIaS!")
                compose.onNodeWithText(context.getString(R.string.submit_answer)).performClick()
                compose.onNodeWithText(context.getString(R.string.next_stage)).assertIsDisplayed()
                assertEquals(3, storage.getPlayerInfo().scoreForStage(7))
                assertEquals(8, storage.getPlayerInfo().highestUnlockedStage)
                compose.onNodeWithText(context.getString(R.string.next_stage)).performClick()
                compose.waitUntil(timeoutMillis = 10_000) { nextStage.lastActivity != null }
            }
        } finally {
            nextStage.lastActivity?.let { activity -> instrumentation.runOnMainSync { activity.finish() } }
            instrumentation.removeMonitor(nextStage)
            storage.savePlayerInfo(originalPlayer)
        }
    }
}
