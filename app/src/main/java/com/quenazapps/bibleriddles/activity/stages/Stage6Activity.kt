package com.quenazapps.bibleriddles.activity.stages

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quenazapps.bibleriddles.R
import com.quenazapps.bibleriddles.activity.stages.ui.theme.BibleRiddlesTheme
import com.quenazapps.bibleriddles.domain.PlayerInfo
import com.quenazapps.bibleriddles.service.LocalStorage

class Stage6Activity : ComponentActivity() {
    private val stageAnswers by lazy { StageAnswers(getString(R.string.stage6_answer)) }
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
                Stage6Screen(
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
                        startActivity(StageTipsMenuActivity.createIntent(this, STAGE_NUMBER, STAGE6_TIPS))
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
        const val STAGE_NUMBER = 6
        const val STATE_ANSWER = "answer"
        const val STATE_CORRECT = "correct"
        const val STATE_FEEDBACK = "feedback"
    }
}

internal val STAGE6_TIPS = listOf(
    StageTip.TextTip(id = "tip_1", cost = 1, textRes = R.string.stage6_tip_1),
    StageTip.TextTip(id = "tip_2", cost = 2, textRes = R.string.stage6_tip_2),
)

@Composable
internal fun Stage6Screen(
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
        stageNumber = 6,
        onBackClick = onBackClick,
        tipPointsRemainingToday = playerInfo.tipPointsRemainingToday,
        tipsUsedForStage = playerInfo.tipsUsedForStage(6),
        onTipClick = onTipClick,
    ) {
        if (playerInfo.scoreForStage(6) > 0 && !answerIsCorrect) {
            CompletedStageReview(correctAnswer = stringResource(R.string.stage6_answer)) {
                Stage6Riddle()
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) { Stage6Riddle() }
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
private fun Stage6Riddle() {
    val context = LocalContext.current
    val coordinates = stringResource(R.string.stage6_coordinates).lines()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(vertical = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.stage6_year),
            color = Color(0xFF4A2A12),
            fontFamily = FontFamily.Serif,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth().weight(1f)
                .verticalScroll(rememberScrollState()).padding(vertical = 16.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                coordinates.forEach { coordinate ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable {
                            context.getSystemService(ClipboardManager::class.java)
                                .setPrimaryClip(ClipData.newPlainText("Coordinate", coordinate))
                            Toast.makeText(context, R.string.stage6_coordinate_copied, Toast.LENGTH_SHORT).show()
                        },
                    ) {
                        Text(
                            text = coordinate,
                            color = Color(0xFF4A2A12),
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            lineHeight = 34.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
