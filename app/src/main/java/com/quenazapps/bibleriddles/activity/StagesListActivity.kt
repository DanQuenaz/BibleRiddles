package com.quenazapps.bibleriddles.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quenazapps.bibleriddles.R
import com.quenazapps.bibleriddles.activity.stages.stageActivityClass
import com.quenazapps.bibleriddles.activity.ui.theme.BibleRiddlesTheme
import com.quenazapps.bibleriddles.domain.PlayerInfo
import com.quenazapps.bibleriddles.domain.PlayerInfo.Companion.TOTAL_STAGES
import com.quenazapps.bibleriddles.service.LocalStorage

class StagesListActivity : ComponentActivity() {
    private lateinit var localStorage: LocalStorage
    private var playerInfo by mutableStateOf(PlayerInfo())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        localStorage = LocalStorage(this)
        playerInfo = localStorage.getPlayerInfo()

        setContent {
            BibleRiddlesTheme(dynamicColor = false) {
                StagesListScreen(
                    playerInfo = playerInfo,
                    onBackClick = ::finish,
                    onStageClick = ::openStage,
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

    private fun openStage(stageNumber: Int) {
        if (!playerInfo.isStageUnlocked(stageNumber)) return

        playerInfo = playerInfo.withStageStarted(stageNumber)
        localStorage.savePlayerInfo(playerInfo)
        startActivity(Intent(this, stageActivityClass(stageNumber)))
    }
}

@Composable
fun StagesListScreen(
    playerInfo: PlayerInfo,
    onBackClick: () -> Unit,
    onStageClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Image(
            painter = painterResource(R.mipmap.stage_background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            StagesTopMenu(onBackClick = onBackClick)

            LazyVerticalGrid(
                columns = GridCells.Fixed(STAGE_COLUMNS),
                state = gridState,
                userScrollEnabled = true,
                contentPadding = PaddingValues(
                    start = 12.dp,
                    top = 8.dp,
                    end = 12.dp,
                    bottom = 20.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                items((1..TOTAL_STAGES).toList(), key = { it }) { stageNumber ->
                    StageShield(
                        stageNumber = stageNumber,
                        score = playerInfo.scoreForStage(stageNumber),
                        isUnlocked = playerInfo.isStageUnlocked(stageNumber),
                        onClick = { onStageClick(stageNumber) },
                    )
                }
            }
        }
    }
}

@Composable
private fun StagesTopMenu(onBackClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(TOP_MENU_HEIGHT),
    ) {
        Image(
            painter = painterResource(R.mipmap.top_menu_stage_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Image(
            painter = painterResource(R.mipmap.back_button),
            contentDescription = stringResource(R.string.back),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp)
                .size(56.dp)
                .clickable(role = Role.Button, onClick = onBackClick),
        )

        Text(
            text = stringResource(R.string.stages_list),
            color = Color(0xFFF2D39A),
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            style = TextStyle(
                shadow = Shadow(color = Color.Black, blurRadius = 4f),
            ),
        )
    }
}

@Composable
private fun StageShield(
    stageNumber: Int,
    score: Int,
    isUnlocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shield = shieldResource(score = score, isUnlocked = isUnlocked)
    val stateDescription = when {
        !isUnlocked -> stringResource(R.string.stage_locked_description, stageNumber)
        score > 0 -> stringResource(R.string.stage_score_description, stageNumber, score)
        else -> stringResource(R.string.stage_current_description, stageNumber)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(SHIELD_ASPECT_RATIO)
            .semantics { contentDescription = stateDescription }
            .clickable(
                enabled = isUnlocked,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        Image(
            painter = painterResource(shield),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )

        Text(
            text = stageNumber.toString(),
            color = if (isUnlocked) Color(0xFFF4D49B) else Color(0xFFC8B99B),
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = if (stageNumber < 10) 25.sp else 21.sp,
            textAlign = TextAlign.Center,
            style = TextStyle(
                shadow = Shadow(color = Color.Black, blurRadius = 4f),
            ),
            modifier = Modifier.offset(y = (-8).dp),
        )
    }
}

@DrawableRes
private fun shieldResource(score: Int, isUnlocked: Boolean): Int = when {
    !isUnlocked -> R.mipmap.locker_shield
    score >= 3 -> R.mipmap.golden_shield
    score == 2 -> R.mipmap.silver_shield
    score == 1 -> R.mipmap.bronze_shield
    else -> R.mipmap.current_shield
}

private const val STAGE_COLUMNS = 5
private const val SHIELD_ASPECT_RATIO = 1163f / 1352f
private val TOP_MENU_HEIGHT = 72.dp

@Preview(showBackground = true, widthDp = 412, heightDp = 800)
@Composable
private fun StagesListPreview() {
    BibleRiddlesTheme(dynamicColor = false) {
        StagesListScreen(
            playerInfo = PlayerInfo(
                highestUnlockedStage = 4,
                stageScores = mapOf(1 to 1, 2 to 2, 3 to 3),
            ),
            onBackClick = {},
            onStageClick = {},
        )
    }
}
