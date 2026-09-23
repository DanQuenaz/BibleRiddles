package com.quenazapps.bibleriddles.activity.stages

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.quenazapps.bibleriddles.R
import com.quenazapps.bibleriddles.activity.stages.ui.theme.BibleRiddlesTheme
import com.quenazapps.bibleriddles.domain.PlayerInfo
import com.quenazapps.bibleriddles.service.LocalStorage

class Stage3Activity : ComponentActivity() {
    private val stageAnswers by lazy {
        StageAnswers(
            mainAnswer = getString(R.string.stage3_answer),
            alternativeAnswers = resources.getStringArray(R.array.stage3_accepted_answers).toList(),
        )
    }

    private lateinit var localStorage: LocalStorage
    private var playerInfo by mutableStateOf(PlayerInfo())
    private var answer by mutableStateOf("")
    private var feedbackMessage by mutableStateOf<String?>(null)
    private var answerIsCorrect by mutableStateOf(false)
    private var openingNextStage = false
    private var mediaPlayer: MediaPlayer? = null

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
                Stage3Screen(
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
                    onPlaySoundClick = ::playPsalm,
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

    override fun onStop() {
        releasePsalm()
        super.onStop()
    }

    private fun playPsalm() {
        releasePsalm()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        mediaPlayer = MediaPlayer.create(this, R.raw.psalm_23, audioAttributes, 0)?.apply {
            start()
        }
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
        releasePsalm()
        answerIsCorrect = true
        feedbackMessage = getString(R.string.correct_answer_with_stars, score)
    }

    private fun releasePsalm() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun openNextStage() {
        if (!answerIsCorrect || openingNextStage) return
        openingNextStage = true
        startActivity(StageTransitionActivity.createIntent(this, STAGE_NUMBER + 1))
        finish()
    }

    private companion object {
        const val STAGE_NUMBER = 3
        const val STATE_ANSWER = "answer"
        const val STATE_CORRECT = "correct"
        const val STATE_FEEDBACK = "feedback"
        val STAGE_TIPS = listOf(
            StageTip.TextTip(id = "tip_1", cost = 1, textRes = R.string.stage3_tip_1),
            StageTip.TextTip(id = "tip_2", cost = 2, textRes = R.string.stage3_tip_2),
        )
    }
}

@Composable
private fun Stage3Screen(
    playerInfo: PlayerInfo,
    answer: String,
    feedbackMessage: String?,
    answerIsCorrect: Boolean,
    onAnswerChange: (String) -> Unit,
    onPlaySoundClick: () -> Unit,
    onSubmitClick: () -> Unit,
    onNextStageClick: () -> Unit,
    onTipClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    if (playerInfo.scoreForStage(3) > 0 && !answerIsCorrect) {
        StageLayout(
            stageNumber = 3,
            onBackClick = onBackClick,
            tipPointsRemainingToday = playerInfo.tipPointsRemainingToday,
            tipsUsedForStage = playerInfo.tipsUsedForStage(3),
            onTipClick = onTipClick,
        ) {
            CompletedStageReview(correctAnswer = stringResource(R.string.stage3_answer)) {
                Stage3SoundButton(
                    onClick = onPlaySoundClick,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
        return
    }

    StageLayout(
        stageNumber = 3,
        onBackClick = onBackClick,
        tipPointsRemainingToday = playerInfo.tipPointsRemainingToday,
        tipsUsedForStage = playerInfo.tipsUsedForStage(3),
        onTipClick = onTipClick,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Stage3SoundButton(onClick = onPlaySoundClick)
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

@Composable
private fun Stage3SoundButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(R.mipmap.sound),
        contentDescription = stringResource(R.string.play_riddle_audio),
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(230.dp)
            .clickable(role = Role.Button, onClick = onClick),
    )
}
