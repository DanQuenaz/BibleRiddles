package com.quenazapps.bibleriddles.activity.stages

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
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class Stage2GameplayTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun melSolvesStageTwoAndUnlocksStageThree() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val storage = LocalStorage(context)
        val originalPlayer = storage.getPlayerInfo()
        try {
            storage.savePlayerInfo(PlayerInfo(highestUnlockedStage = 2))
            ActivityScenario.launch(Stage2Activity::class.java).use {
                assertEquals("MEL", context.getString(R.string.stage2_answer))
                val field = compose.onNode(hasSetTextAction())
                field.performTextReplacement("leão")
                compose.onNodeWithText(context.getString(R.string.submit_answer)).performClick()
                compose.onNodeWithText(context.getString(R.string.incorrect_answer)).assertIsDisplayed()
                assertEquals(0, storage.getPlayerInfo().scoreForStage(2))

                field.performTextReplacement("M.e-L!")
                compose.onNodeWithText(context.getString(R.string.submit_answer)).performClick()
                compose.onNodeWithText(context.getString(R.string.next_stage)).assertIsDisplayed()
                assertEquals(3, storage.getPlayerInfo().scoreForStage(2))
                assertEquals(3, storage.getPlayerInfo().highestUnlockedStage)
            }
        } finally {
            storage.savePlayerInfo(originalPlayer)
        }
    }

    @Test fun allConfiguredAnswersAreSingleWordsOrNumbers() {
        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        listOf(
            R.string.stage1_answer to R.array.stage1_accepted_answers,
            R.string.stage2_answer to R.array.stage2_accepted_answers,
            R.string.stage3_answer to R.array.stage3_accepted_answers,
            R.string.stage4_answer to R.array.stage4_accepted_answers,
            R.string.stage5_answer to R.array.stage5_accepted_answers,
            R.string.stage6_answer to R.array.stage6_accepted_answers,
            R.string.stage7_answer to R.array.stage7_accepted_answers,
        ).forEach { (main, alternatives) ->
            val answers = StageAnswers(resources.getString(main), resources.getStringArray(alternatives).toList())
            assertFalse(answers.accepts("O rei Davi"))
            assertFalse(answers.accepts("Quarto dia"))
            assertFalse(answers.accepts("Profeta Ezequiel"))
        }
    }
}
