package com.quenazapps.bibleriddles.activity.stages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.font.FontWeight
import androidx.test.platform.app.InstrumentationRegistry
import com.quenazapps.bibleriddles.R
import com.quenazapps.bibleriddles.activity.stages.ui.theme.BibleRiddlesTheme
import com.quenazapps.bibleriddles.domain.PlayerInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StageProgressUiTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun clueWordsAreBoldOnlyWhileTouchingTheRiddle() {
        val restoration = StateRestorationTester(compose)
        var submitClicks = 0
        restoration.setContent {
            Stage2Screen(
                playerInfo = PlayerInfo(), answer = "", feedbackMessage = null,
                answerIsCorrect = false, onAnswerChange = {},
                onSubmitClick = { submitClicks++ }, onNextStageClick = {},
                onTipClick = {}, onBackClick = {},
            )
        }
        val riddle = compose.onNodeWithText(context.getString(R.string.stage2_riddle))
        fun boldWords(): List<String> {
            val text = riddle.fetchSemanticsNode().config[SemanticsProperties.Text].single()
            return text.spanStyles.filter { it.item.fontWeight == FontWeight.Bold }
                .map { text.text.substring(it.start, it.end) }
        }
        assertTrue(boldWords().isEmpty())
        // Controls outside the riddle keep working without revealing the clue.
        val submit = compose.onNodeWithText(context.getString(R.string.submit_answer))
        submit.performTouchInput { down(center) }
        assertTrue(boldWords().isEmpty())
        submit.performTouchInput { up() }
        compose.runOnIdle { assertEquals(1, submitClicks) }
        assertTrue(boldWords().isEmpty())

        riddle.performTouchInput { down(center) }
        assertEquals(listOf("comida", "doçura"), boldWords())
        riddle.performTouchInput { moveTo(Offset(center.x, height + 50f)) }
        assertTrue(boldWords().isEmpty())
        riddle.performTouchInput { moveTo(center) }
        assertEquals(listOf("comida", "doçura"), boldWords())
        riddle.performTouchInput { cancel() }
        assertTrue(boldWords().isEmpty())

        // Keep the emphasis until the last finger lifts.
        riddle.performTouchInput {
            down(0, center)
            down(1, center + Offset(5f, 5f))
        }
        riddle.performTouchInput { up(0) }
        assertEquals(listOf("comida", "doçura"), boldWords())
        riddle.performTouchInput { up(1) }
        assertTrue(boldWords().isEmpty())
        restoration.emulateSavedInstanceStateRestore()
        assertTrue(boldWords().isEmpty())
    }

    @Test fun answerFieldBlocksTypedAndPastedWhitespace() {
        var answer by mutableStateOf("")
        compose.setContent {
            Stage2Screen(
                playerInfo = PlayerInfo(), answer = answer, feedbackMessage = null,
                answerIsCorrect = false, onAnswerChange = { answer = it },
                onSubmitClick = {}, onNextStageClick = {}, onTipClick = {}, onBackClick = {},
            )
        }
        val field = compose.onNode(hasSetTextAction())
        field.performTextReplacement(" M\u00a0e\tL\n! ")
        field.assertTextEquals("MeL!")
        field.performTextInput(" ")
        field.assertTextEquals("MeL!")
        field.performTextReplacement("4 2")
        field.assertTextEquals("42")
    }

    @Test fun correctAnswerReplacesSubmitAndWaitsForNextClick() {
        var correct by mutableStateOf(false)
        var nextClicks = 0
        compose.setContent {
            BibleRiddlesTheme(dynamicColor = false) {
                Stage2Screen(
                    playerInfo = if (correct) PlayerInfo().withStageScore(2, 3) else PlayerInfo(),
                    answer = "answer",
                    feedbackMessage = null,
                    answerIsCorrect = correct,
                    onAnswerChange = {},
                    onSubmitClick = { correct = true },
                    onNextStageClick = { nextClicks++ },
                    onTipClick = {},
                    onBackClick = {},
                )
            }
        }
        compose.onNodeWithText(context.getString(R.string.next_stage)).assertDoesNotExist()
        compose.onNodeWithText(context.getString(R.string.submit_answer)).performClick()
        compose.onNodeWithText(context.getString(R.string.submit_answer)).assertDoesNotExist()
        compose.onNodeWithText(context.getString(R.string.next_stage)).assertIsDisplayed()
        compose.mainClock.advanceTimeBy(5_000L)
        compose.runOnIdle { assertEquals(0, nextClicks) }
        compose.onNodeWithText(context.getString(R.string.next_stage)).performClick()
        compose.runOnIdle { assertEquals(1, nextClicks) }
    }

    @Test fun completedStageReviewHasNoSubmitOrNextButton() {
        compose.setContent {
            BibleRiddlesTheme(dynamicColor = false) {
                Stage2Screen(
                    playerInfo = PlayerInfo().withStageScore(2, 3),
                    answer = "",
                    feedbackMessage = null,
                    answerIsCorrect = false,
                    onAnswerChange = {},
                    onSubmitClick = {},
                    onNextStageClick = {},
                    onTipClick = {},
                    onBackClick = {},
                )
            }
        }
        compose.onNodeWithText(context.getString(R.string.next_stage)).assertDoesNotExist()
        compose.onNodeWithText(context.getString(R.string.submit_answer)).assertDoesNotExist()
        compose.onNodeWithText(context.getString(R.string.stage2_answer)).assertIsDisplayed()
    }
}
