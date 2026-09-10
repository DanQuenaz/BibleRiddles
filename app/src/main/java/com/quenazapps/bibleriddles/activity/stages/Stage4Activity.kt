package com.quenazapps.bibleriddles.activity.stages

import android.os.Bundle
import android.os.SystemClock
import android.view.OrientationEventListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.quenazapps.bibleriddles.R
import com.quenazapps.bibleriddles.activity.stages.ui.theme.BibleRiddlesTheme
import com.quenazapps.bibleriddles.domain.PlayerInfo
import com.quenazapps.bibleriddles.service.LocalStorage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class Stage4Activity : ComponentActivity() {
    private val stageAnswers by lazy {
        StageAnswers(
            mainAnswer = getString(R.string.stage4_answer),
            alternativeAnswers = resources.getStringArray(R.array.stage4_accepted_answers).toList(),
        )
    }

    private lateinit var localStorage: LocalStorage
    private lateinit var orientationListener: OrientationEventListener
    private var playerInfo by mutableStateOf(PlayerInfo())
    private var answer by mutableStateOf("")
    private var feedbackMessage by mutableStateOf<String?>(null)
    private var answerIsCorrect by mutableStateOf(false)
    private var openingNextStage = false
    private var counter by mutableStateOf(Stage4CounterState())
    private var counterJob: Job? = null
    private var lastCounterTick: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        localStorage = LocalStorage(this)
        playerInfo = localStorage.getPlayerInfo()
        answer = savedInstanceState?.getString(STATE_ANSWER).orEmpty()
        answerIsCorrect = savedInstanceState?.getBoolean(STATE_CORRECT) ?: false
        feedbackMessage = savedInstanceState?.getString(STATE_FEEDBACK)
        counter = Stage4CounterState(
            side = Stage4Side.entries.firstOrNull { it.name == savedInstanceState?.getString(STATE_SIDE) },
            elapsedMillis = savedInstanceState?.getLong(STATE_ELAPSED) ?: 0L,
        )
        orientationListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                updateCounterTime()
                counter = counter.withOrientation(orientation)
            }
        }
        setContent {
            BibleRiddlesTheme(dynamicColor = false) {
                Stage4Screen(
                    playerInfo = playerInfo,
                    counterValue = counter.value,
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
                        if (!answerIsCorrect) {
                            startActivity(StageTipsMenuActivity.createIntent(this, STAGE_NUMBER, STAGE_TIPS))
                        }
                    },
                    onBackClick = ::finish,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        playerInfo = localStorage.getPlayerInfo()
        lastCounterTick = SystemClock.elapsedRealtime()
        if (orientationListener.canDetectOrientation()) orientationListener.enable()
        counterJob = lifecycleScope.launch {
            while (isActive) {
                delay(100L)
                updateCounterTime()
            }
        }
    }

    override fun onPause() {
        updateCounterTime()
        lastCounterTick = null
        counterJob?.cancel()
        counterJob = null
        orientationListener.disable()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        updateCounterTime()
        outState.putString(STATE_ANSWER, answer)
        outState.putBoolean(STATE_CORRECT, answerIsCorrect)
        outState.putString(STATE_FEEDBACK, feedbackMessage)
        outState.putString(STATE_SIDE, counter.side?.name)
        outState.putLong(STATE_ELAPSED, counter.elapsedMillis)
        super.onSaveInstanceState(outState)
    }

    private fun updateCounterTime() {
        val lastTick = lastCounterTick ?: return
        val now = SystemClock.elapsedRealtime()
        counter = counter.advanceBy(now - lastTick)
        lastCounterTick = now
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
        const val STATE_SIDE = "counter_side"
        const val STATE_ELAPSED = "counter_elapsed"
        val STAGE_TIPS = listOf(
            StageTip.TextTip(id = "tip_1", cost = 1, textRes = R.string.stage4_tip_1),
            StageTip.TextTip(id = "tip_2", cost = 1, textRes = R.string.stage4_tip_2),
        )
    }
}

@Composable
private fun Stage4Screen(
    playerInfo: PlayerInfo,
    counterValue: Int?,
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
        stageNumber = 4,
        onBackClick = onBackClick,
        tipPointsRemainingToday = playerInfo.tipPointsRemainingToday,
        tipsUsedForStage = playerInfo.tipsUsedForStage(4),
        onTipClick = onTipClick,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val compact = maxHeight < 400.dp
            val contentHeight = maxHeight.coerceAtLeast(if (compact) 280.dp else 420.dp)
            // Keep the counter and answer reachable in landscape and with the keyboard open.
            Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Column(
                    modifier = Modifier.fillMaxWidth().height(contentHeight),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (playerInfo.scoreForStage(4) > 0 && !answerIsCorrect) {
                        CompletedStageReview(correctAnswer = stringResource(R.string.stage4_answer)) {
                            Stage4CounterText(counterValue)
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Stage4CounterText(counterValue)
                        }
                        StageAnswerForm(
                            answer = answer,
                            feedbackMessage = feedbackMessage,
                            answerIsCorrect = answerIsCorrect,
                            onAnswerChange = onAnswerChange,
                            onSubmitClick = onSubmitClick,
                            onNextStageClick = onNextStageClick,
                            compact = compact,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Stage4CounterText(value: Int?) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (value != null) {
            Text(
                text = value.toString(),
                color = Color(0xFF4A2A12),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 56.sp,
            )
        }
    }
}
