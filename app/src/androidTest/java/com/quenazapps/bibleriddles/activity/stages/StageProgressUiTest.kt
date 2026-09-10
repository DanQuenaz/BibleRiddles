package com.quenazapps.bibleriddles.activity.stages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.quenazapps.bibleriddles.R
import com.quenazapps.bibleriddles.activity.stages.ui.theme.BibleRiddlesTheme
import com.quenazapps.bibleriddles.domain.PlayerInfo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StageProgressUiTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun correctAnswerReplacesSubmitAndWaitsForNextClick() {
        var correct by mutableStateOf(false)
        var nextClicks = 0
        compose.setContent {
            BibleRiddlesTheme(dynamicColor = false) {
                Stage1Screen(
                    playerInfo = if (correct) PlayerInfo().withStageScore(1, 3) else PlayerInfo(),
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
                Stage1Screen(
                    playerInfo = PlayerInfo().withStageScore(1, 3),
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
        compose.onNodeWithText(context.getString(R.string.stage1_answer)).assertIsDisplayed()
    }
}
