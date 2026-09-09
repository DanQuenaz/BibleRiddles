package com.quenazapps.bibleriddles.activity.stages

import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import com.quenazapps.bibleriddles.R
import com.quenazapps.bibleriddles.activity.stages.ui.theme.BibleRiddlesTheme
import com.quenazapps.bibleriddles.domain.PlayerInfo
import com.quenazapps.bibleriddles.service.LocalStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Stage2Activity : ComponentActivity() {
    private lateinit var localStorage: LocalStorage
    private var playerInfo by mutableStateOf(PlayerInfo())
    private var answer by mutableStateOf("")
    private var feedbackMessage by mutableStateOf<String?>(null)
    private var answerIsCorrect by mutableStateOf(false)
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        localStorage = LocalStorage(this)
        playerInfo = localStorage.getPlayerInfo()

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
                    onPlaySoundClick = ::playPsalm,
                    onSubmitClick = ::submitAnswer,
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

    private fun submitAnswer() {
        playerInfo = localStorage.getPlayerInfo()
        if (answerIsCorrect || playerInfo.scoreForStage(STAGE_NUMBER) > 0) return
        if (normalizeAnswer(answer) != normalizeAnswer(getString(R.string.stage2_answer))) {
            feedbackMessage = getString(R.string.incorrect_answer)
            return
        }

        val score = (3 - playerInfo.tipsUsedForStage(STAGE_NUMBER)).coerceIn(1, 3)
        playerInfo = playerInfo.withStageScore(STAGE_NUMBER, score)
        localStorage.savePlayerInfo(playerInfo)
        releasePsalm()
        answerIsCorrect = true
        feedbackMessage = getString(R.string.correct_answer_with_stars, score)

        lifecycleScope.launch {
            delay(NEXT_STAGE_DELAY_MILLIS)
            startActivity(Intent(this@Stage2Activity, Stage3Activity::class.java))
            finish()
        }
    }

    private fun releasePsalm() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private companion object {
        const val STAGE_NUMBER = 2
        const val NEXT_STAGE_DELAY_MILLIS = 1_500L
        val STAGE_TIPS = listOf(
            StageTip.TextTip(id = "tip_1", cost = 1, textRes = R.string.stage2_tip_1),
            StageTip.TextTip(id = "tip_2", cost = 2, textRes = R.string.stage2_tip_2),
        )
    }
}

@Composable
private fun Stage2Screen(
    playerInfo: PlayerInfo,
    answer: String,
    feedbackMessage: String?,
    answerIsCorrect: Boolean,
    onAnswerChange: (String) -> Unit,
    onPlaySoundClick: () -> Unit,
    onSubmitClick: () -> Unit,
    onTipClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    if (playerInfo.scoreForStage(2) > 0) {
        StageLayout(
            stageNumber = 2,
            onBackClick = onBackClick,
            tipPointsRemainingToday = playerInfo.tipPointsRemainingToday,
            tipsUsedForStage = playerInfo.tipsUsedForStage(2),
            onTipClick = onTipClick,
        ) {
            CompletedStageReview(correctAnswer = stringResource(R.string.stage2_answer)) {
                Stage2SoundButton(
                    onClick = onPlaySoundClick,
                    modifier = Modifier.align(Alignment.Center),
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
            Stage2SoundButton(onClick = onPlaySoundClick)
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

@Composable
private fun Stage2SoundButton(
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
