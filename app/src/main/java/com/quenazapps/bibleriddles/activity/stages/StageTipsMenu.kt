package com.quenazapps.bibleriddles.activity.stages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quenazapps.bibleriddles.R
import com.quenazapps.bibleriddles.service.LocalStorage
import kotlinx.coroutines.delay

/** Opening this menu is free. Only purchasing a previously unowned tip spends points. */
@Composable
internal fun StageTipsMenu(
    stageNumber: Int,
    tips: List<StageTip>,
    localStorage: LocalStorage,
    onOpenTip: (Int, StageTip) -> Unit,
    onClose: () -> Unit,
) {
    val sortedTips = remember(tips) { orderedTips(tips) }
    var player by remember(localStorage) { mutableStateOf(localStorage.getPlayerInfo()) }
    // Also refresh while the menu remains open across local midnight.
    LaunchedEffect(localStorage) {
        while (true) {
            player = localStorage.getPlayerInfo()
            delay(1_000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF24190F)),
    ) {
        StageBackground()
        Column(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            StageTopMenu(
                title = stringResource(R.string.tips_menu_title),
                onBackClick = onClose,
                navigationIconRes = R.mipmap.close_button,
                navigationDescriptionRes = R.string.close,
            )
            Text(
                text = stringResource(R.string.stage_title, stageNumber),
                fontFamily = FontFamily.Serif,
                color = Color(0xFF4A2A12),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Text(
                text = stringResource(R.string.tip_points_balance, player.tipPointsRemainingToday),
                color = Color(0xFF4A2A12),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            Text(
                text = stringResource(R.string.tip_points_reset),
                color = Color(0xFF4A2A12),
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(sortedTips, key = { _, tip -> tip.id }) { index, tip ->
                    val owned = player.ownsTip(stageNumber, tip.id)
                    val affordable = tip.cost <= player.tipPointsRemainingToday
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(Color(0xCCFDF0CB)).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.tip_number, index + 1),
                            color = Color(0xFF4A2A12),
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 23.sp,
                        )
                        Text(
                            text = stringResource(R.string.tip_price, tip.cost),
                            color = Color(0xFF4A2A12),
                        )
                        Button(
                            enabled = owned || affordable,
                            onClick = {
                                // Re-check the balance at purchase time, including day changes.
                                val updated = localStorage.purchaseTip(stageNumber, tip.id, tip.cost)
                                player = updated ?: localStorage.getPlayerInfo()
                                if (updated != null) onOpenTip(index + 1, tip)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF593513),
                                contentColor = Color(0xFFF2D39A),
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(
                                when {
                                    owned -> R.string.tip_open_free
                                    affordable -> R.string.tip_buy
                                    else -> R.string.tip_insufficient_points
                                },
                            ))
                        }
                    }
                }
            }
        }
    }
}
