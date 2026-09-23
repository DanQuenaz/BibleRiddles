package com.quenazapps.bibleriddles.activity.stages

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quenazapps.bibleriddles.R
import com.quenazapps.bibleriddles.activity.stages.ui.theme.BibleRiddlesTheme
import com.quenazapps.bibleriddles.domain.PlayerInfo
import com.quenazapps.bibleriddles.service.LocalStorage

class Stage4Activity : ComponentActivity() {
    private val stageAnswers by lazy {
        StageAnswers(
            mainAnswer = getString(R.string.stage4_answer),
            alternativeAnswers = resources.getStringArray(R.array.stage4_accepted_answers).toList(),
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
                Stage4Screen(
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
        const val STAGE_NUMBER = 4
        const val STATE_ANSWER = "answer"
        const val STATE_CORRECT = "correct"
        const val STATE_FEEDBACK = "feedback"
        val STAGE_TIPS = listOf(
            StageTip.TextTip(id = "tip_1", cost = 1, textRes = R.string.stage4_tip_1),
            StageTip.TextTip(id = "tip_2", cost = 2, textRes = R.string.stage4_tip_2),
        )
    }
}

@Composable
private fun Stage4Screen(
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
    val pagerState = rememberPagerState { STAGE4_IMAGES.size }
    StageLayout(
        stageNumber = 4,
        onBackClick = onBackClick,
        tipPointsRemainingToday = playerInfo.tipPointsRemainingToday,
        tipsUsedForStage = playerInfo.tipsUsedForStage(4),
        onTipClick = onTipClick,
    ) {
        if (playerInfo.scoreForStage(4) > 0 && !answerIsCorrect) {
            CompletedStageReview(correctAnswer = stringResource(R.string.stage4_answer)) {
                Stage4Riddle(pagerState = pagerState)
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 12.dp)) {
                Stage4Riddle(pagerState = pagerState)
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
}

// The missing fourth image is intentional: it is the solution to this riddle.
internal val STAGE4_IMAGES = listOf(
    R.mipmap.creation_1,
    R.mipmap.creation_2,
    R.mipmap.creation_3,
    R.mipmap.creation_5,
    R.mipmap.creation_6,
    R.mipmap.creation_7,
)

@Composable
internal fun Stage4Riddle(
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState { STAGE4_IMAGES.size },
) {
    // No arrows, page numbers, dots, or instructions; the gesture is part of the puzzle.
    HorizontalPager(state = pagerState, modifier = modifier.fillMaxSize()) { page ->
        Image(
            painter = painterResource(STAGE4_IMAGES[page]),
            contentDescription = stringResource(R.string.stage4_image_description),
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
