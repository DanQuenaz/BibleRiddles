package com.quenazapps.bibleriddles.activity.stages

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.quenazapps.bibleriddles.R
import com.quenazapps.bibleriddles.activity.stages.ui.theme.BibleRiddlesTheme
import com.quenazapps.bibleriddles.domain.PlayerInfo
import com.quenazapps.bibleriddles.service.LocalStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Stage1Activity : ComponentActivity() {
    private lateinit var localStorage: LocalStorage
    private var playerInfo by mutableStateOf(PlayerInfo())
    private var answer by mutableStateOf("")
    private var feedbackMessage by mutableStateOf<String?>(null)
    private var answerIsCorrect by mutableStateOf(false)
    private var showTipsMenu by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        localStorage = LocalStorage(this)
        playerInfo = localStorage.getPlayerInfo()

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
                    onTipClick = { showTipsMenu = true },
                    onBackClick = ::finish,
                )
                if (showTipsMenu) {
                    StageTipsMenu(
                        stageNumber = STAGE_NUMBER,
                        tips = STAGE_TIPS,
                        localStorage = localStorage,
                        onPlayerInfoChanged = { playerInfo = it },
                        onOpenTip = { number, tip ->
                            startActivity(TipActivity.createIntent(this, STAGE_NUMBER, number, tip))
                        },
                        onDismiss = { showTipsMenu = false },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::localStorage.isInitialized) playerInfo = localStorage.getPlayerInfo()
    }

    private fun submitAnswer() {
        playerInfo = localStorage.getPlayerInfo()
        if (answerIsCorrect || playerInfo.scoreForStage(STAGE_NUMBER) > 0) return
        if (normalizeAnswer(answer) != normalizeAnswer(getString(R.string.stage1_answer))) {
            feedbackMessage = getString(R.string.incorrect_answer)
            return
        }

        val score = (3 - playerInfo.tipsUsedForStage(STAGE_NUMBER)).coerceIn(1, 3)
        playerInfo = playerInfo.withStageScore(STAGE_NUMBER, score)
        localStorage.savePlayerInfo(playerInfo)
        answerIsCorrect = true
        feedbackMessage = getString(R.string.correct_answer_with_stars, score)

        lifecycleScope.launch {
            delay(NEXT_STAGE_DELAY_MILLIS)
            startActivity(Intent(this@Stage1Activity, Stage2Activity::class.java))
            finish()
        }
    }

    private companion object {
        const val STAGE_NUMBER = 1
        const val NEXT_STAGE_DELAY_MILLIS = 1_500L
        val STAGE_TIPS = listOf(
            StageTip.TextTip(id = "tip_1", cost = 1, textRes = R.string.stage1_tip_1),
            StageTip.TextTip(id = "tip_2", cost = 2, textRes = R.string.stage1_tip_2),
        )
    }
}

@Composable
private fun Stage1Screen(
    playerInfo: PlayerInfo,
    answer: String,
    feedbackMessage: String?,
    answerIsCorrect: Boolean,
    onAnswerChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onTipClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    val riddle = androidx.compose.ui.res.stringResource(R.string.stage1_riddle)

    if (playerInfo.scoreForStage(1) > 0) {
        StageLayout(
            stageNumber = 1,
            onBackClick = onBackClick,
            tipPointsRemainingToday = playerInfo.tipPointsRemainingToday,
            tipsUsedForStage = playerInfo.tipsUsedForStage(1),
            onTipClick = onTipClick,
        ) {
            CompletedStageReview(
                correctAnswer = androidx.compose.ui.res.stringResource(R.string.stage1_answer),
            ) {
                StageTextRiddle(
                    text = riddle,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
        return
    }

    StageLayout(
        stageNumber = 1,
        onBackClick = onBackClick,
        tipPointsRemainingToday = playerInfo.tipPointsRemainingToday,
        tipsUsedForStage = playerInfo.tipsUsedForStage(1),
        onTipClick = onTipClick,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            StageTextRiddle(text = riddle)
        }
        StageAnswerForm(
            answer = answer,
            feedbackMessage = feedbackMessage,
            answerIsCorrect = answerIsCorrect,
            onAnswerChange = onAnswerChange,
            onSubmitClick = onSubmitClick,
        )
    }
}
