package com.quenazapps.bibleriddles.activity.stages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.quenazapps.bibleriddles.R
import com.quenazapps.bibleriddles.activity.stages.ui.theme.BibleRiddlesTheme
import com.quenazapps.bibleriddles.domain.PlayerInfo
import com.quenazapps.bibleriddles.service.LocalStorage
import kotlinx.coroutines.delay

/** Used only by START/CONTINUE and NEXT STAGE, never by the stage-list route. */
class StageTransitionActivity : ComponentActivity() {
    private val titleOpacity = Animatable(0f)
    private var destinationOpened = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val stageNumber = intent.getIntExtra(EXTRA_STAGE_NUMBER, 0)
        val localStorage = LocalStorage(this)
        if (stageNumber !in 1..PlayerInfo.TOTAL_STAGES ||
            !localStorage.getPlayerInfo().isStageUnlocked(stageNumber)
        ) {
            finish()
            return
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.BLACK),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.BLACK),
        )
        setContent {
            // Compose supplies the MonotonicFrameClock required by Animatable.animateTo.
            LaunchedEffect(stageNumber) {
                // Do not advance while this screen is in the background.
                // Restart the short title animation if interrupted or recreated.
                lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    if (!destinationOpened) {
                        titleOpacity.snapTo(0f)
                        titleOpacity.animateTo(1f, tween(600))
                        delay(400L)
                        titleOpacity.animateTo(0f, tween(600))
                        destinationOpened = true
                        localStorage.savePlayerInfo(localStorage.getPlayerInfo().withStageStarted(stageNumber))
                        startActivity(Intent(this@StageTransitionActivity, stageActivityClass(stageNumber)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        })
                        finish()
                    }
                }
            }
            BibleRiddlesTheme(dynamicColor = false) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize().background(Color.Black)
                        .windowInsetsPadding(WindowInsets.safeDrawing).padding(32.dp),
                ) {
                    Text(
                        text = stringResource(R.string.stage_title, stageNumber),
                        color = Color.White,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(titleOpacity.value),
                    )
                }
            }
        }
    }

    companion object {
        private const val EXTRA_STAGE_NUMBER = "stage_number"

        fun createIntent(context: Context, stageNumber: Int): Intent {
            require(stageNumber in 1..PlayerInfo.TOTAL_STAGES)
            return Intent(context, StageTransitionActivity::class.java).apply {
                putExtra(EXTRA_STAGE_NUMBER, stageNumber)
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            }
        }
    }
}
