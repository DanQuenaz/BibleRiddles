package com.quenazapps.bibleriddles.activity.stages

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.quenazapps.bibleriddles.R
import com.quenazapps.bibleriddles.activity.stages.ui.theme.BibleRiddlesTheme
import com.quenazapps.bibleriddles.domain.PlayerInfo
import com.quenazapps.bibleriddles.service.LocalStorage

class Stage2Activity : ComponentActivity() {
    private val stageAnswers by lazy {
        StageAnswers(
            mainAnswer = getString(R.string.stage2_answer),
            alternativeAnswers = resources.getStringArray(R.array.stage2_accepted_answers).toList(),
        )
    }

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
                Stage2Screen(
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
                        startActivity(StageTipsMenuActivity.createIntent(this, STAGE_NUMBER, STAGE_TIPS))
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
        const val STAGE_NUMBER = 2
        const val STATE_ANSWER = "answer"
        const val STATE_CORRECT = "correct"
        const val STATE_FEEDBACK = "feedback"
        val STAGE_TIPS = listOf(
            StageTip.TextTip(id = "tip_1", cost = 1, textRes = R.string.stage2_tip_1),
            StageTip.TextTip(id = "tip_2", cost = 2, textRes = R.string.stage2_tip_2),
        )
    }
}

@Composable
internal fun Stage2Screen(
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
    val riddleText = androidx.compose.ui.res.stringResource(R.string.stage2_riddle)
    var cluesRevealed by remember { mutableStateOf(false) }
    val riddle = buildAnnotatedString {
        append(riddleText)
        if (cluesRevealed) {
            Regex("\\b(comida|doçura)\\b", RegexOption.IGNORE_CASE).findAll(riddleText).forEach { match ->
                addStyle(SpanStyle(fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
            }
        }
    }
    val revealOnTouch = Modifier.pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            try {
                cluesRevealed = true
                do {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    cluesRevealed = event.changes.any {
                        it.pressed && it.position.x in 0f..size.width.toFloat() &&
                            it.position.y in 0f..size.height.toFloat()
                    }
                } while (event.changes.any { it.pressed })
            } finally {
                cluesRevealed = false
            }
        }
    }

    if (playerInfo.scoreForStage(2) > 0 && !answerIsCorrect) {
        StageLayout(
            stageNumber = 2,
            onBackClick = onBackClick,
            tipPointsRemainingToday = playerInfo.tipPointsRemainingToday,
            tipsUsedForStage = playerInfo.tipsUsedForStage(2),
            onTipClick = onTipClick,
        ) {
            CompletedStageReview(
                correctAnswer = androidx.compose.ui.res.stringResource(R.string.stage2_answer),
            ) {
                StageTextRiddle(
                    text = riddle,
                    modifier = Modifier.align(Alignment.Center).then(revealOnTouch),
                )
            }
        }
        return
    }

    StageLayout(
        stageNumber = 2,
        onBackClick = onBackClick,
        tipPointsRemainingToday = playerInfo.tipPointsRemainingToday,
        tipsUsedForStage = playerInfo.tipsUsedForStage(2),
        onTipClick = onTipClick,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            StageTextRiddle(text = riddle, modifier = revealOnTouch)
        }
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
