package com.quenazapps.bibleriddles

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quenazapps.bibleriddles.activity.StagesListActivity
import com.quenazapps.bibleriddles.activity.stages.stageActivityClass
import com.quenazapps.bibleriddles.activity.stages.StageTransitionActivity
import com.quenazapps.bibleriddles.domain.PlayerInfo
import com.quenazapps.bibleriddles.service.LocalStorage
import com.quenazapps.bibleriddles.ui.theme.BibleRiddlesTheme

class MainMenuActivity : ComponentActivity() {
    private lateinit var localStorage: LocalStorage
    private var playerInfo by mutableStateOf(PlayerInfo())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        localStorage = LocalStorage(this)
        playerInfo = localStorage.getPlayerInfo()

        setContent {
            BibleRiddlesTheme(dynamicColor = false) {
                MainMenuScreen(
                    hasPlayedAnyStage = playerInfo.hasPlayedAnyStage,
                    onStartClick = ::openCurrentStage,
                    onStagesListClick = {
                        startActivity(Intent(this, StagesListActivity::class.java))
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::localStorage.isInitialized) {
            playerInfo = localStorage.getPlayerInfo()
        }
    }

    private fun openCurrentStage() {
        playerInfo = localStorage.getPlayerInfo()
        val stageNumber = playerInfo.stageToContinue
        playerInfo = playerInfo.withStageStarted(stageNumber)
        localStorage.savePlayerInfo(playerInfo)
        startActivity(
            if (playerInfo.scoreForStage(stageNumber) > 0) {
                Intent(this, stageActivityClass(stageNumber))
            } else {
                StageTransitionActivity.createIntent(this, stageNumber)
            },
        )
    }
}

@Composable
fun MainMenuScreen(
    hasPlayedAnyStage: Boolean,
    onStartClick: () -> Unit,
    onStagesListClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        val isPortrait = maxHeight >= maxWidth
        val logoSize = minOf(
            maxWidth * if (isPortrait) 0.90f else 0.36f,
            maxHeight * if (isPortrait) 0.43f else 0.30f,
        )
        val buttonWidth = minOf(
            maxWidth * 0.86f,
            maxHeight * if (isPortrait) 0.72f else 0.66f,
        )
        val bottomSpacing = if (isPortrait) maxHeight * 0.10f else 0.dp

        Image(
            painter = painterResource(R.mipmap.main_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp),
        ) {
            Image(
                painter = painterResource(R.mipmap.logo),
                contentDescription = stringResource(R.string.game_logo_description),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(logoSize),
            )

            Spacer(modifier = Modifier.weight(1f))

            MenuImageButton(
                text = stringResource(
                    if (hasPlayedAnyStage) R.string.continue_game else R.string.start_game,
                ),
                width = buttonWidth,
                onClick = onStartClick,
            )

            Spacer(modifier = Modifier.height(if (isPortrait) 16.dp else 8.dp))

            MenuImageButton(
                text = stringResource(R.string.stages_list),
                width = buttonWidth,
                onClick = onStagesListClick,
            )

            Spacer(
                modifier = Modifier.height(bottomSpacing),
            )
        }
    }
}

@Composable
private fun MenuImageButton(
    text: String,
    width: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(width = width, height = width / BUTTON_ASPECT_RATIO)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Image(
            painter = painterResource(R.mipmap.button),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )

        Text(
            text = text,
            color = Color(0xFFF2D39A),
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 21.sp,
            letterSpacing = 0.8.sp,
            textAlign = TextAlign.Center,
            style = androidx.compose.ui.text.TextStyle(
                shadow = Shadow(
                    color = Color.Black,
                    blurRadius = 4f,
                ),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
        )
    }
}

private const val BUTTON_ASPECT_RATIO = 2170f / 725f

@Preview(showBackground = true, widthDp = 412, heightDp = 800)
@Composable
private fun MainMenuPreview() {
    BibleRiddlesTheme(dynamicColor = false) {
        MainMenuScreen(
            hasPlayedAnyStage = false,
            onStartClick = {},
            onStagesListClick = {},
        )
    }
}
