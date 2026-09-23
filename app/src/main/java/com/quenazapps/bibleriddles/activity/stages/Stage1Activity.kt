package com.quenazapps.bibleriddles.activity.stages

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quenazapps.bibleriddles.R
import com.quenazapps.bibleriddles.activity.stages.ui.theme.BibleRiddlesTheme
import com.quenazapps.bibleriddles.domain.PlayerInfo
import com.quenazapps.bibleriddles.service.LocalStorage

class Stage1Activity : ComponentActivity() {
    private val stageAnswers by lazy { StageAnswers(getString(R.string.stage1_answer)) }
    private lateinit var localStorage: LocalStorage
    private var playerInfo by mutableStateOf(PlayerInfo())
    private var answer by mutableStateOf("")
    private var feedbackMessage by mutableStateOf<String?>(null)
    private var answerIsCorrect by mutableStateOf(false)
    private var openingNextStage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        localStorage = LocalStorage(this)
        playerInfo = localStorage.getPlayerInfo()
        answer = filterAnswerInput(savedInstanceState?.getString(STATE_ANSWER).orEmpty())
        answerIsCorrect = savedInstanceState?.getBoolean(STATE_CORRECT) ?: false
        feedbackMessage = savedInstanceState?.getString(STATE_FEEDBACK)
        setContent {
            BibleRiddlesTheme(dynamicColor = false) {
                Stage1Screen(
                    playerInfo = playerInfo,
                    answer = answer,
                    feedbackMessage = feedbackMessage,
                    answerIsCorrect = answerIsCorrect,
                    onAnswerChange = {
                        if (!answerIsCorrect) {
                            answer = it
                            feedbackMessage = null
                        }
                    },
                    onSubmitClick = ::submitAnswer,
                    onNextStageClick = ::openNextStage,
                    onTipClick = {
                        startActivity(StageTipsMenuActivity.createIntent(this, STAGE_NUMBER, STAGE1_TIPS))
                    },
                    onBackClick = ::finish,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::localStorage.isInitialized) playerInfo = localStorage.getPlayerInfo()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_ANSWER, answer)
        outState.putBoolean(STATE_CORRECT, answerIsCorrect)
        outState.putString(STATE_FEEDBACK, feedbackMessage)
        super.onSaveInstanceState(outState)
    }

    private fun submitAnswer() {
        playerInfo = localStorage.getPlayerInfo()
        if (answerIsCorrect || playerInfo.scoreForStage(STAGE_NUMBER) > 0) return
        if (!stageAnswers.accepts(answer)) {
            feedbackMessage = getString(R.string.incorrect_answer)
            return
        }
        val score = (3 - playerInfo.tipsUsedForStage(STAGE_NUMBER)).coerceIn(1, 3)
        playerInfo = playerInfo.withStageScore(STAGE_NUMBER, score)
        localStorage.savePlayerInfo(playerInfo)
        answerIsCorrect = true
        feedbackMessage = getString(R.string.correct_answer_with_stars, score)
    }

    private fun openNextStage() {
        if (!answerIsCorrect || openingNextStage) return
        openingNextStage = true
        startActivity(StageTransitionActivity.createIntent(this, STAGE_NUMBER + 1))
        finish()
    }

    private companion object {
        const val STAGE_NUMBER = 1
        const val STATE_ANSWER = "answer"
        const val STATE_CORRECT = "correct"
        const val STATE_FEEDBACK = "feedback"
    }
}

internal val STAGE1_TIPS = listOf(
    StageTip.TextTip(id = "tip_1", cost = 1, textRes = R.string.stage1_tip_1),
    StageTip.TextTip(id = "tip_2", cost = 2, textRes = R.string.stage1_tip_2),
)

@Composable
internal fun Stage1Screen(
    playerInfo: PlayerInfo,
    answer: String,
    feedbackMessage: String?,
    answerIsCorrect: Boolean,
    onAnswerChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onNextStageClick: () -> Unit,
    onTipClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    StageLayout(
        stageNumber = 1,
        onBackClick = onBackClick,
        tipPointsRemainingToday = playerInfo.tipPointsRemainingToday,
        tipsUsedForStage = playerInfo.tipsUsedForStage(1),
        onTipClick = onTipClick,
    ) {
        if (playerInfo.scoreForStage(1) > 0 && !answerIsCorrect) {
            CompletedStageReview(correctAnswer = stringResource(R.string.stage1_answer)) {
                Stage1Riddle()
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) { Stage1Riddle() }
            StageAnswerForm(
                answer = answer,
                feedbackMessage = feedbackMessage,
                answerIsCorrect = answerIsCorrect,
                onAnswerChange = onAnswerChange,
                onSubmitClick = onSubmitClick,
                onNextStageClick = onNextStageClick,
            )
        }
    }
}

@Composable
private fun Stage1Riddle() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.stage1_riddle),
                color = Color(0xFF4A2A12),
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                lineHeight = 31.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.stage1_reference),
                color = Color(0xFF4A2A12),
                fontFamily = FontFamily.Serif,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
